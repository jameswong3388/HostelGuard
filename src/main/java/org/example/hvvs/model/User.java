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
    private DateTime created_at;
    private DateTime updated_at;
}
