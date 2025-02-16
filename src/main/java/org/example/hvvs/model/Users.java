package org.example.hvvs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.Table;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.sql.Timestamp;

@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.findAll", query = "SELECT u FROM Users u"),
        @NamedQuery(name = "User.findByUsername", query = "SELECT u FROM Users u WHERE u.username = :username"),
        @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM Users u WHERE u.email = :email"),
        @NamedQuery(name = "User.findByFirstName", query = "SELECT u FROM Users u WHERE u.first_name = :firstName"),
        @NamedQuery(name = "User.findByLastName", query = "SELECT u FROM Users u WHERE u.last_name = :lastName"),
        @NamedQuery(name = "User.findByPhoneNumber", query = "SELECT u FROM Users u WHERE u.phone_number = :phone_number"),
        @NamedQuery(name = "User.findByIsActive", query = "SELECT u FROM Users u WHERE u.is_active = :is_active"),
        @NamedQuery(name = "User.findByRole", query = "SELECT u FROM Users u WHERE u.role = :role"),
        @NamedQuery(name = "User.findByCreatedAt", query = "SELECT u FROM Users u WHERE u.created_at = :created_at"),
        @NamedQuery(name = "User.findByUpdatedAt", query = "SELECT u FROM Users u WHERE u.updated_at = :updated_at"),
})

public class Users extends BaseEntity {
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "salt", nullable = false)
    private String salt;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String first_name;

    @Column(name = "last_name")
    private String last_name;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phone_number;

    @Column(name = "identity_number", nullable = false, unique = true)
    private String identity_number;

    @Column(name = "address")
    private String address;

    @Column(name = "gender")
    private String gender;

    @Column(name = "is_active")
    private boolean is_active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "is_mfa_enable")
    private Boolean is_mfa_enable;

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at;

    public Users() {
        super();
    }

    public Users(String username, String salt, String password, String email,
                 String first_name, String last_name, String phone_number,
                 boolean is_active, Role role, Boolean is_mfa_enable, Timestamp created_at, Timestamp updated_at) {
        this.username = username;
        this.salt = salt;
        this.password = password;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
        this.is_active = is_active;
        this.role = role;
        this.is_mfa_enable = is_mfa_enable;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public enum Role {
        RESIDENT,
        SECURITY_STAFF,
        MANAGING_STAFF,
        SUPER_ADMIN
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

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public boolean getIsActive() {
        return is_active;
    }

    public Role getRole() {
        return role;
    }

    public Timestamp getCreatedAt() {
        return created_at;
    }

    public Timestamp getUpdatedAt() {
        return updated_at;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public void setPhoneNumber(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setIsActive(boolean is_active) {
        this.is_active = is_active;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdatedAt(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdentity_number() {
        return identity_number;
    }

    public void setIdentity_number(String identity_number) {
        this.identity_number = identity_number;
    }

    public Boolean getIs_mfa_enable() {
        return is_mfa_enable;
    }

    public void setIs_mfa_enable(Boolean is_mfa_enable) {
        this.is_mfa_enable = is_mfa_enable;
    }
}