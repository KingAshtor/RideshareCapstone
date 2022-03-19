package com.example.ridesharecapstone.api;

import java.util.Set;

public class User {
    private Integer usrID;
    private String email;
    private String fName;
    private String lName;
    private String hashedPwd, salt;
    private Address address;
    private Set<String> roles;

    public User(Integer usrID, String email, String fName, String lName, String hashedPwd, String salt, Address address, Set<String> roles) {
        this.usrID = usrID;
        this.email = email;
        this.fName = fName;
        this.lName = lName;
        this.hashedPwd = hashedPwd;
        this.salt = salt;
        this.address = address;
        this.roles = roles;
    }

    public User() {

    }

    public void setUsrID(Integer usrID) {
        this.usrID = usrID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public void setHashedPwd(String hashedPwd) {
        this.hashedPwd = hashedPwd;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Integer getUsrID() {
        return usrID;
    }

    public String getEmail() {
        return email;
    }

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public String getHashedPwd() {
        return hashedPwd;
    }

    public String getSalt() {
        return salt;
    }

    public Address getAddress() {
        return address;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
