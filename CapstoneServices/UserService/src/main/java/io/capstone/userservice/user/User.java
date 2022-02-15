package io.capstone.userservice.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
@Getter
public class User {
    @Setter
    private Integer usrID;
    private String email;
    @JsonProperty("fName")
    private String fName;
    @JsonProperty("lName")
    private String lName;
    private String hashedPwd, salt;

    public User(String email, String salt) {
        this.usrID = null;
        setEmail(email);
        this.fName = null;
        this.lName = null;
        this.hashedPwd = null;
        setSalt(salt);
    }

    public User(Integer usrID, String email, String fName, String lName, String hashedPwd, String salt) {
        this.usrID = usrID;
        setEmail(email);
        setFName(fName);
        setLName(lName);
        setHashedPwd(hashedPwd);
        setSalt(salt);
    }

    @JsonProperty("fName")
    public String getFName() {
        return fName;
    }

    @JsonProperty("lName")
    public String getLName() {
        return lName;
    }

    public void setFName(String fName) {
        if (fName != null && fName.length() > 16) this.fName = fName.substring(0, 16);
        else this.fName = fName;
    }

    public void setLName(String lName) {
        if (lName != null && lName.length() > 16)
            this.lName = lName.substring(0, 16);
        else this.lName = lName;
    }

    public void setEmail(String email) {
        if (email != null && email.length() > 320)
            this.email = email.substring(0, 320);
        else this.email = email;
    }

    public void setHashedPwd(String hashedPwd) {
        if (hashedPwd != null && hashedPwd.length() != 64) this.hashedPwd = null;
        else this.hashedPwd = hashedPwd;
    }

    public void setSalt(String salt) {
        if (salt != null && salt.length() != 36) this.salt = null;
        else this.salt = salt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(usrID, user.usrID) && Objects.equals(email, user.email) && Objects.equals(fName, user.fName) && Objects.equals(lName, user.lName) && Objects.equals(hashedPwd, user.hashedPwd) && Objects.equals(salt, user.salt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usrID, email, fName, lName, hashedPwd, salt);
    }

    @Override
    public String toString() {
        return "User{" +
                "usrID='" + usrID  + '\'' +
                ", email='" + email + '\'' +
                ", fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", hashedPwd='" + hashedPwd + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}
