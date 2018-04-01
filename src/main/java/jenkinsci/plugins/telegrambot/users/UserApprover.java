package jenkinsci.plugins.telegrambot.users;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.telegram.TelegramBotRunner;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserApprover {

    public enum ApprovalType {
        ALL,
        MANUAL
    }

    private Set<User> users;

    public UserApprover(Set<User> users) {
        this.users = users;
    }

    public ApprovalType approve(JSONObject formData) {
        ApprovalType approvalType;

        JSONObject approval = (JSONObject) formData.get("approval");
        if (approval == null) {
            approvalType = ApprovalType.ALL;
        } else {
            String value = approval.getString("value");
            approvalType = ApprovalType.valueOf(value);
        }

        JSONArray loaded;
        if (approval == null) {
            loaded = null;
        } else {
            try {
                // Only one user is subscriber
                JSONObject users = approval.getJSONObject("users");
                loaded = new JSONArray();
                loaded.add(users);
            } catch (JSONException e) {
                // More than one subscribers
                loaded = approval.getJSONArray("users");
            }
        }

        Map<User, Boolean> userBooleanMap = new HashMap<>();
        switch (approvalType) {

            case MANUAL:
                userBooleanMap = collectUsersToApprove(loaded);
                break;

            case ALL:
                userBooleanMap = users.stream().collect(Collectors.toMap(Function.identity(), e -> true));
                break;
        }

        userBooleanMap.forEach(this::updateUserApproval);

        return approvalType;
    }

    @SuppressWarnings("unchecked")
    private Map<User, Boolean> collectUsersToApprove(JSONArray jsonUsers) {
        if (jsonUsers == null) return new HashMap<>();
        return jsonUsers.stream()
                .filter(o -> Objects.nonNull(o) && o instanceof JSONObject)
                .map(o -> (JSONObject) o)
                .map(JSONObject::entrySet)
                .map(o -> (Set<Map.Entry<String, Object>>) o)
                .flatMap(Collection::stream)
                .filter(e -> Long.valueOf(e.getKey()) != null)
                .collect(Collectors.toMap(
                        e -> users.stream()
                                .filter(user -> user.getId().equals(Long.valueOf(e.getKey())))
                                .findFirst()
                                .orElse(null),
                        e -> Boolean.valueOf(e.getValue().toString())));
    }

    private void updateUserApproval(User user, boolean approved) {
        String message;
        boolean oldStatus = user.isApproved();
        if (approved) {
            user.approve();
            message = String.valueOf(GlobalConfiguration.getInstance()
                    .getBotStrings().get("message.approved"));
        } else {
            user.unapprove();
            message = String.valueOf(GlobalConfiguration.getInstance()
                    .getBotStrings().get("message.unapproved"));
        }

        if (oldStatus != user.isApproved()) {
            TelegramBotRunner.getInstance().getBot().sendMessage(user.getId(), message);
        }
    }

    public Set<User> getUsers() {
        return users;
    }
}
