package jenkinsci.plugins.telegrambot.users;

import jenkins.model.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.TelegramBotGlobalConfiguration;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.stream.Collectors;

public class Subscribers extends Observable {
    private static Subscribers instance;
    private Set<User> users = new HashSet<>();

    private Subscribers() {
    }

    public synchronized static Subscribers getInstance() {
        if (instance == null) {
            instance = new Subscribers();
        }

        return instance;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<User> getApprovedUsers() {
        if (GlobalConfiguration.all().get(TelegramBotGlobalConfiguration.class).getApprovalType() == UserApprover.ApprovalType.ALL) {
            return users.stream().collect(Collectors.toSet());
        }

        return users.stream()
                .filter(User::isApproved)
                .collect(Collectors.toSet());
    }

    private User getUser(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean isSubscribed(Long id) {
        return getUser(id) != null;
    }

    public boolean isApproved(Long id) {
        User user = getUser(id);
        return user != null ? user.isApproved() : false;
    }

    public void subscribe(String name, Long id) {
        User user = getUser(id);

        if (user == null) {
            users.add(new User(name, id));

            setChanged();
            notifyObservers();
        }
    }

    public void unsubscribe(Long id) {
        User user = getUser(id);

        if (user != null) {
            users.remove(user);

            setChanged();
            notifyObservers();
        }
    }
}
