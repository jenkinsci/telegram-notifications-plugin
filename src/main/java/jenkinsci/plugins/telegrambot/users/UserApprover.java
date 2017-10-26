package jenkinsci.plugins.telegrambot.users;

import jenkinsci.plugins.telegrambot.telegram.TelegramBot;
import jenkinsci.plugins.telegrambot.telegram.TelegramBotRunner;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
            return ApprovalType.ALL;
        }

        String value = approval.getString("value");
        approvalType = ApprovalType.valueOf(value);

        switch (approvalType) {

            // Manual approving
            case MANUAL:
                JSONArray loaded;

                try {
                    // Only one user is subscriber
                    JSONObject users = approval.getJSONObject("users");
                    loaded = new JSONArray();
                    loaded.add(users);
                } catch (JSONException e) {
                    // More than one subscribers
                    loaded = approval.getJSONArray("users");
                }

                if (loaded != null) {
                    updateUsersApproval(loaded);
                }

                break;

            // Approve all
            case ALL:
                break;

        }

        return approvalType;
    }

    /**
     * Approve users manual
     *
     * @param loaded the loaded users
     */
    private void updateUsersApproval(JSONArray loaded) {
        loaded.stream()
                .filter(o -> Objects.nonNull(o) && o instanceof JSONObject)
                .forEach(o -> {
                    JSONObject loadedUser = (JSONObject) o;
                    ((Set<Map.Entry<String, Object>>) loadedUser.entrySet()).forEach(e -> {
                        Long id = Long.valueOf(e.getKey());
                        Boolean approved = Boolean.valueOf(e.getValue().toString());

                        users.stream()
                                .filter(user -> user.getId().equals(id))
                                .findFirst()
                                .ifPresent(user -> {
                                    String message;
                                    boolean oldStatus = user.isApproved();
                                    if (approved) {
                                        user.approve();
                                        message = String.valueOf(TelegramBot.getProp().get("message.approved"));
                                    } else {
                                        user.unapprove();
                                        message = String.valueOf(TelegramBot.getProp().get("message.unapproved"));
                                    }

                                    if (oldStatus != user.isApproved()) {
                                        TelegramBotRunner.getInstance().getThread().getBot().sendMessage(
                                                user.getId(), message);
                                    }
                                });
                    });
                });
    }

    public Set<User> getUsers() {
        return users;
    }
}
