package org.example.hvvs.dao;

import org.example.hvvs.model.User;

import java.util.List;

public interface UserDao {
    /**
     * Add a user record to the database
     *
     * @return true if successful, false otherwise
     */
    boolean add(User user);

    /**
     * Delete a user record from database by id
     *
     * @return true if successful, false otherwise
     */
    boolean delete(String id);

    /**
     * Update user record information in database
     *
     * @return true if successful, false otherwise
     */
    boolean update(User user);

    /**
     * Find user by primary key id
     *
     * @return null if not found
     */
    User findById(String id);

    /**
     * Find all user records in the database
     *
     * @return empty List if no records found, never null
     */
    List<User> findAll();

    /**
     * Types for searching users: ID, NAME, EMAIL
     */
    enum Type {ID, NAME, EMAIL}

    /**
     * Count records matching search criteria by username, name or email
     *
     * @param value         Search value
     * @param type          Search type (ID, NAME, EMAIL)
     * @param isFuzzySearch Whether to use fuzzy search
     * @return count of matching records
     */
    int findCount(String value, Type type, boolean isFuzzySearch);

    /**
     * Find records matching search criteria within specified range
     *
     * @param value         Search value
     * @param type          Search type (ID, NAME, EMAIL)
     * @param isFuzzySearch Whether to use fuzzy search
     * @param offset        Number of records to skip (must be >= 0)
     * @param limit         Maximum records to return (must be > 0)
     * @return empty List if no records found, never null
     */
    List<User> findRange(String value, Type type, boolean isFuzzySearch, int offset, int limit);

    /**
     * Get total count of user records
     *
     * @return total number of users
     */
    int userCount();

    /**
     * Check if user ID already exists
     *
     * @return true if exists, false otherwise
     */
    boolean idExist(String id);

    /**
     * Check if email already exists
     *
     * @return true if exists, false otherwise
     */
    boolean emailExist(String email);
}
