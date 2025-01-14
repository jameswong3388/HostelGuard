package org.example.hvvs.modules.users.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.Table;
import jakarta.persistence.NamedQuery;
import org.example.hvvs.commonClasses.BaseEntity;

import java.sql.Timestamp;

@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
        @NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE u.username = :username"),
        @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
        @NamedQuery(name = "User.findByFullName", query = "SELECT u FROM User u WHERE u.full_name = :fullName"),
        @NamedQuery(name = "User.findByPhoneNumber", query = "SELECT u FROM User u WHERE u.phone_number = :phone_number"),
        @NamedQuery(name = "User.findByIsActive", query = "SELECT u FROM User u WHERE u.is_active = :is_active"),
        @NamedQuery(name = "User.findByRole", query = "SELECT u FROM User u WHERE u.role = :role"),
        @NamedQuery(name = "User.findByCreatedAt", query = "SELECT u FROM User u WHERE u.created_at = :created_at"),
        @NamedQuery(name = "User.findByUpdatedAt", query = "SELECT u FROM User u WHERE u.updated_at = :updated_at"),
})


public class User extends BaseEntity {
    @Column(name = "username")
    private String username;

    @Column(name = "salt")
    private String salt;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "full_name")
    private String full_name;

    @Column(name = "phone_number")
    private String phone_number;

    @Column(name = "is_active")
    private boolean is_active;

    @Column(name = "role")
    private String role;

    @Column(name = "created_at")
    private Timestamp created_at;

    @Column(name = "updated_at")
    private Timestamp updated_at;

    public User() {
        super();
    }

    public User(String username, String salt, String password, String email, String full_name, String phone_number, boolean is_active, String role, Timestamp created_at, Timestamp updated_at) {
        this.username = username;
        this.salt = salt;
        this.password = password;
        this.email = email;
        this.full_name = full_name;
        this.phone_number = phone_number;
        this.is_active = is_active;
        this.role = role;
        this.created_at = created_at;
        this.updated_at = updated_at;
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
        return full_name;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public boolean getIsActive() {
        return is_active;
    }

    public String getRole() {
        return role;
    }

    public Timestamp getCreatedAt() {
        return created_at;
    }

    public Timestamp getUpdatedAt() {
        return updated_at;
    }
}