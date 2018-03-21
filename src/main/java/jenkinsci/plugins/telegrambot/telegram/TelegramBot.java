package jenkinsci.plugins.telegrambot.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.ProxyConfiguration;
import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.telegram.commands.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiValidationException;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

import static org.telegram.telegrambots.Constants.SOCKET_TIMEOUT;

public class TelegramBot extends TelegramLongPollingCommandBot {
    private final static Logger LOGGER = Logger.getLogger(TelegramBot.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String token;
    private volatile CloseableHttpClient httpclient;
    private volatile RequestConfig requestConfig;

    public TelegramBot(String token, String name) {
        super(name);
        this.token = token;

        try {
            HttpHost proxy = getProxy();
            httpclient = getHttpClient(proxy);
            requestConfig = getRequestConfig(proxy);
            getOptions().setRequestConfig(requestConfig);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "TelegramBot: Failed to set proxy", e);
        }

        Stream.of(
                new StartCommand(),
                new HelpCommand(),
                new SubCommand(),
                new UnsubCommand(),
                new StatusCommand()
        ).forEach(this::register);
    }

    private HttpHost getProxy() throws IOException {
        ProxyConfiguration proxyConfig = ProxyConfiguration.load();
        if (proxyConfig != null) {
            LOGGER.fine(String.format("Proxy settings: %s:%d", proxyConfig.name, proxyConfig.port));
            return new HttpHost(proxyConfig.name, proxyConfig.port);
        } else {
            LOGGER.log(Level.FINE, "No proxy settings in Jenkins");
            return null;
        }
    }

    private CloseableHttpClient getHttpClient(HttpHost proxy) {
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setConnectionTimeToLive(70, TimeUnit.SECONDS)
                .setMaxConnTotal(100);
        if (proxy != null) {
            builder.setProxy(proxy)
                    .setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
        }
        return builder.build();
    }

    private RequestConfig getRequestConfig(HttpHost proxy) {
        RequestConfig botRequestConfig = getOptions().getRequestConfig();
        if (botRequestConfig == null) {
            botRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .setConnectTimeout(SOCKET_TIMEOUT)
                    .setConnectionRequestTimeout(SOCKET_TIMEOUT)
                    .build();
        }
        RequestConfig.Builder builder = RequestConfig.copy(botRequestConfig);
        if (proxy != null) {
            builder.setProxy(proxy);
        }
        return builder.build();
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage sendMessageRequest = new SendMessage();

        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setText(message);
        sendMessageRequest.enableMarkdown(true);

        try {
            execute(sendMessageRequest);
        } catch (TelegramApiException e) {
            LOGGER.log(Level.SEVERE, String.format("TelegramBot: Error while sending message: %s%n%s", chatId, message), e);
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        sendMessage(chatId, GlobalConfiguration.getInstance().getBotStrings().get("message.noncommand"));
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        if (method == null) {
            throw new TelegramApiException("Parameter method can not be null");
        }
        return sendApiMethodWithProxy(method);
    }

    protected final <T extends Serializable, Method extends BotApiMethod<T>> T sendApiMethodWithProxy(Method method) throws TelegramApiException {
        try {
            String responseContent = sendMethodRequest(method);
            return method.deserializeResponse(responseContent);
        } catch (IOException e) {
            throw new TelegramApiException(String.format("Unable to execute %s method", method.getMethod()), e);
        }
    }

    private <T extends Serializable, Method extends BotApiMethod<T>> String sendMethodRequest(Method method) throws TelegramApiValidationException, IOException {
        method.validate();
        String url = getBaseUrl() + method.getMethod();
        HttpPost httpPost = configuredHttpPost(url);
        httpPost.addHeader("charset", StandardCharsets.UTF_8.name());
        httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(method), ContentType.APPLICATION_JSON));
        return sendHttpPostRequest(httpPost);
    }

    private String sendHttpPostRequest(HttpPost httpPost) throws IOException {
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity httpEntity = response.getEntity();
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(httpEntity);
            return EntityUtils.toString(bufferedHttpEntity, StandardCharsets.UTF_8);
        }
    }

    private HttpPost configuredHttpPost(String url) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        return httpPost;
    }

    @Override
    public String toString() {
        return "TelegramBot{" + token + "}";
    }
}
