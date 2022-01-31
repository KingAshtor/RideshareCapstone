package io.capstone.userservice.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    private String name;
    private String hashedPwd;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) && Objects.equals(hashedPwd, user.hashedPwd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hashedPwd);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", hashedPwd='" + hashedPwd + '\'' +
                '}';
    }
}
