package org.example.hvvs.model;

import org.eclipse.persistence.jpa.jpql.parser.DateTime;
import java.sql.Timestamp;

public class User {
    private String user_id;
    private String username;
    private String salt;
    private String password;
    private String email;
    private String fullName;
    private String phone_number;
    private boolean is_active;
    private String role;
    private Timestamp created_at;
    private Timestamp updated_at;

    public User(String user_id, String username, String salt, String password, String email, String phone_number, boolean is_active, String role, Timestamp created_at, Timestamp updated_at) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.email = email;
        this.phone_number = phone_number;
        this.is_active = is_active;
        this.role = role;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getSalt() {
        return salt;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public String getRole() {
        return role;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }
}
