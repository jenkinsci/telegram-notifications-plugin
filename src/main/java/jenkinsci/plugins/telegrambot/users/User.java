package jenkinsci.plugins.telegrambot.users;

import java.util.Objects;

public class User {
    private String name;
    private Long id;
    private Boolean isApproved;

    public User(String name, Long id) {
        this(name, id, false);
    }

    public User(String name, Long id, Boolean isApproved) {
        this.name = name;
        this.id = id;
        this.isApproved = isApproved;
    }

    public void approve() {
        isApproved = true;
    }

    public void unapprove() {
        isApproved = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isApproved() {
        return isApproved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) &&
                Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }

    @Override
    public String toString() {
        return "User{" +
                name + '\'' +
                ", id=" + id +
                ", approved=" + isApproved +
                '}';
    }
}
