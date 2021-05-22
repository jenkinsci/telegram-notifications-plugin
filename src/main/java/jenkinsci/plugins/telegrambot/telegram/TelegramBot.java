package jenkinsci.plugins.telegrambot.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.TelegramBotGlobalConfiguration;
import jenkinsci.plugins.telegrambot.telegram.commands.*;
import jenkinsci.plugins.telegrambot.users.Subscribers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URLEncoder;

import static org.telegram.telegrambots.Constants.SOCKET_TIMEOUT;

public class TelegramBot extends TelegramLongPollingCommandBot {
    private static final Logger LOG = Logger.getLogger(TelegramBot.class.getName());

    private static final TelegramBotGlobalConfiguration CONFIG = GlobalConfiguration.all().get(TelegramBotGlobalConfiguration.class);
    private static final Subscribers SUBSCRIBERS = Subscribers.getInstance();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String token;
    private volatile CloseableHttpClient httpclient;
    private volatile RequestConfig requestConfig;


    public TelegramBot(String token, String name) {
        super(name);
        this.token = token;

        initializeProxy();

        Arrays.asList(
                new StartCommand(),
                new HelpCommand(),
                new SubCommand(),
                new UnsubCommand(),
                new StatusCommand()
        ).forEach(this::register);
    }

    public void sendMessage(Long chatId, String message) {
        final SendMessage sendMessageRequest = new SendMessage();

        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setText(message);
        sendMessageRequest.enableMarkdown(true);

        try {
            execute(sendMessageRequest);
        } catch (TelegramApiException e) {
            LOG.log(Level.SEVERE, String.format(
                    "TelegramBot: Error while sending message: %s%n%s", chatId, message), e);
        }
    }

    private static String expandMessage(String message, Run<?, ?> run, FilePath filePath, TaskListener taskListener)
            throws IOException, InterruptedException {

        try {
            return TokenMacro.expandAll(run, filePath, taskListener, message);
        } catch (MacroEvaluationException e) {
            LOG.log(Level.SEVERE, "Error while expanding the message", e);
        }

        return message;
    }

    public void sendMessage(
            Long chatId, String message, Run<?, ?> run, FilePath filePath, TaskListener taskListener)
            throws IOException, InterruptedException {

        final String expandedMessage = expandMessage(message, run, filePath, taskListener);

        try {
            if (chatId == null) {
                SUBSCRIBERS.getApprovedUsers()
                        .forEach(user -> this.sendMessage(user.getId(), expandedMessage));
            } else {
                sendMessage(chatId, expandedMessage);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while sending the message", e);
        }

        if (CONFIG.isShouldLogToConsole()) taskListener.getLogger().println(expandedMessage);
    }

    public void sendMessage(
            String message, Run<?, ?> run, FilePath filePath, TaskListener taskListener)
            throws IOException, InterruptedException {

        sendMessage(null, message, run, filePath, taskListener);
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update == null) {
            LOG.log(Level.WARNING, "Update is null");
            return;
        }

        final String nonCommandMessage = CONFIG.getBotStrings()
                .get("message.noncommand");

        final Message message = update.getMessage();
        final Chat chat = message.getChat();

        if (chat.isUserChat()) {
            sendMessage(chat.getId(), nonCommandMessage);
            return;
        }

        final String text = message.getText();

        try {
            // Skip not direct messages in chats
            if (text.length() < 1 || text.charAt(0) != '@') return;
            final String[] tmp = text.split(" ");
            if (tmp.length < 2 || !CONFIG.getBotName().equals(tmp[0].substring(1, tmp[0].length()))) return;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Something bad happened while message processing", e);
            return;
        }

        sendMessage(chat.getId(), nonCommandMessage);
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method)
            throws TelegramApiException {
        if (method == null) throw new TelegramApiException("Parameter method can not be null");
        return sendApiMethodWithProxy(method);
    }

    private HttpPost configuredHttpPost(String url) {
        HttpPost httpPost = new HttpPost(URLEncoder.encode(url, StandardCharsets.UTF_8));
        httpPost.setConfig(requestConfig);
        return httpPost;
    }

    @Override
    public String toString() {
        return "TelegramBot{" + token + "}";
    }

    private void initializeProxy() {
        try {
            HttpHost proxy = getProxy();
            httpclient = getHttpClient(proxy);
            requestConfig = getRequestConfig(proxy);
            getOptions().setRequestConfig(requestConfig);
            LOG.log(Level.INFO, "Proxy successfully initialized");
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "TelegramBot: Failed to set proxy", e);
        }
    }

    private HttpHost getProxy() throws IOException {
        ProxyConfiguration proxyConfig = ProxyConfiguration.load();
        if (proxyConfig != null) {
            LOG.log(Level.FINE, String.format("Proxy settings: %s:%d", proxyConfig.name, proxyConfig.port));
            return new HttpHost(proxyConfig.name, proxyConfig.port);
        } else {
            LOG.log(Level.FINE, "No proxy settings in Jenkins");
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

    private <T extends Serializable, Method extends BotApiMethod<T>> T sendApiMethodWithProxy(
            Method method) throws TelegramApiException {
        try {
            String responseContent = sendMethodRequest(method);
            return method.deserializeResponse(responseContent);
        } catch (IOException e) {
            throw new TelegramApiException(
                    String.format("Unable to execute %s method", method.getMethod()), e);
        }
    }

    private <T extends Serializable, Method extends BotApiMethod<T>> String sendMethodRequest(
            Method method) throws TelegramApiValidationException, IOException {
        method.validate();
        String url = getBaseUrl() + method.getMethod();
        HttpPost httpPost = configuredHttpPost(url);
        httpPost.addHeader("charset", StandardCharsets.UTF_8.name());
        httpPost.setEntity(new StringEntity(
                objectMapper.writeValueAsString(method), ContentType.APPLICATION_JSON));
        return sendHttpPostRequest(httpPost);
    }

    private String sendHttpPostRequest(HttpPost httpPost) throws IOException {
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity httpEntity = response.getEntity();
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(httpEntity);
            return EntityUtils.toString(bufferedHttpEntity, StandardCharsets.UTF_8);
        }
    }
}
