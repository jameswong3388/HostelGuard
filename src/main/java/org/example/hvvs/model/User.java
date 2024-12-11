package org.example.hvvs.model;

import org.eclipse.persistence.jpa.jpql.parser.DateTime;

public class User {
    private String user_id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phone_number;
    private boolean is_active;
    private String role;
    private String created_at;
    private String updated_at;

    public User(String user_id, String username, String password, String email, String phone_number, boolean is_active, String role, String created_at, String updated_at) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.email = email;
        this.phone_number = phone_number;
        this.is_active = is_active;
        this.role = role;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}
