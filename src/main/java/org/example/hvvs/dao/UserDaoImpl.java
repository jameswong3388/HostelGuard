package org.example.hvvs.dao;

import org.example.hvvs.db.DBUtil;
import org.example.hvvs.model.User;
import java.sql.Timestamp;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UserDaoImpl implements UserDao {
    @Override
    public boolean add(User user) {
        String sql = "INSERT INTO users (user_id, username, salt, password, email, phone_number, is_active, role, created_at, updated_at) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] params = new Object[]{
                user.getUser_id(), user.getUsername(),user.getSalt(), user.getPassword(), user.getEmail(),
                user.getPhone_number(), user.isIs_active(), user.getRole(), user.getCreated_at(), user.getUpdated_at()
        };
        int[] types = new int[]{
                Types.CHAR, Types.CHAR, Types.VARCHAR, Types.CHAR, Types.VARCHAR,
                Types.VARCHAR, Types.BIT, Types.VARCHAR, Types.TIMESTAMP, Types.TIMESTAMP
        };
        int result = 0;
        try {
            result = DBUtil.executeUpdateWithNull(sql, params, types);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result == 1;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM users WHERE id=?";
        int result = 0;
        try {
            result = DBUtil.executeUpdate(sql, new Object[]{id});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result == 1;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET username=?, password=?, email=?, phone_number=?, is_active=?, role=?, updated_at=? WHERE user_id=?";
        Object[] params = new Object[]{
                user.getUsername(), user.getPassword(), user.getEmail(),
                user.getPhone_number(), user.isIs_active(), user.getRole(), user.getUpdated_at(),
                user.getUser_id()
        };
        int[] types = new int[]{
                Types.VARCHAR, Types.CHAR, Types.VARCHAR,
                Types.VARCHAR, Types.BIT, Types.VARCHAR, Types.TIMESTAMP,
                Types.CHAR
        };
        int result = 0;
        try {
            result = DBUtil.executeUpdateWithNull(sql, params, types);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result == 1;
    }

    @Override
    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE user_id=?";
        List<User> result = search(sql, new Object[]{id});
        if (result.size() == 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return search(sql, new Object[]{});
    }

    @Override
    public int findCount(String value, Type type, boolean isFuzzySearch) {
        String sql = "SELECT COUNT(*) num FROM users WHERE";
        if (type == Type.ID) {
            sql += " id like ?";
        } else if (type == Type.NAME) {
            sql += " name like ?";
        } else {
            sql += " email like ?";
        }
        if (isFuzzySearch) {
            value = addFuzzy(value);
        }

        List<Map<String, Object>> result = null;
        try {
            result = DBUtil.executeQuery(sql, new Object[]{value});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result == null || result.size() != 1) {
            return 0;
        } else {
            return (int) (long) result.get(0).get("num");
        }
    }

    @Override
    public List<User> findRange(String value, Type type, boolean isFuzzySearch, int offset, int limit) {
        String sql = "SELECT * FROM users WHERE";
        if (type == Type.ID) {
            sql += " id like ?";
        } else if (type == Type.NAME) {
            sql += " name like ?";
        } else {
            sql += " email like ?";
        }
        if (offset >= 0 && limit > 0) {
            sql += (" limit " + limit + " offset " + offset);
        }

        if (isFuzzySearch) {
            value = addFuzzy(value);
        }

        return search(sql, new Object[]{value});
    }

    @Override
    public int userCount() {
        String sql = "SELECT COUNT(*) num FROM users";
        List<Map<String, Object>> result = null;
        try {
            result = DBUtil.executeQuery(sql, new Object[]{});
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (result == null || result.size() != 1) {
            return 0;
        }

        return (int) (long) result.get(0).get("num");
    }


    @Override
    public boolean idExist(String id) {
        String sql = "SELECT COUNT(*) AS num FROM users WHERE id = ?";
        List<Map<String, Object>> list = null;
        try {
            list = DBUtil.executeQuery(sql, new Object[]{id});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (list == null) {
            return true;
        }

        long num = (long) (list.get(0).get("num"));
        return num > 0;
    }

    @Override
    public boolean emailExist(String email) {
        String sql = "SELECT COUNT(*) AS num FROM users WHERE email = ?";
        List<Map<String, Object>> list = null;
        try {
            list = DBUtil.executeQuery(sql, new Object[]{email});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (list == null) {
            return true;
        }

        long num = (long) (list.get(0).get("num"));
        return num > 0;
    }

    /**
     * Add % to the beginning and end of a string. If value is null or empty string, return %
     * Example: abc becomes %abc%
     */
    private String addFuzzy(String value) {
        if ("".equals(value)) {
            return "%";
        } else {
            return "%" + value + "%";
        }
    }

    /**
     * Wrapper for DBUtil.executeQuery() and toUsers()
     *
     * @return not null, returns empty List when no data found
     */
    private List<User> search(String sql, Object[] params) {
        List<Map<String, Object>> table = null;
        try {
            table = DBUtil.executeQuery(sql, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toUsers(table);
    }

    /**
     * Convert record to User object.
     */
    private User toUser(Map<String, Object> record) {
        String user_id = toStringAndTrime(record.get("user_id"));
        String username = toStringAndTrime(record.get("username"));
        String salt = toStringAndTrime(record.get("salt")); 
        String password = toStringAndTrime(record.get("password"));
        String email = toStringAndTrime(record.get("email"));
        String phone_number = toStringAndTrime(record.get("phone_number"));
        boolean is_active = (boolean) record.get("is_active");
        String role = toStringAndTrime(record.get("role"));
        Timestamp created_at = (Timestamp) record.get("created_at");
        Timestamp updated_at = (Timestamp) record.get("updated_at");

        return new User(user_id, username, salt, password, email, phone_number, is_active, role, created_at, updated_at);
    }

    /**
     * Convert List<Map<String, Object>> object to List<User> object
     *
     * @return not null, returns empty List if table == null || table.size == 0
     */
    private List<User> toUsers(List<Map<String, Object>> table) {
        List<User> result = new ArrayList<>();
        if (table == null) {
            return result;
        }

        Iterator<Map<String, Object>> iter = table.iterator();
        while (iter.hasNext()) {
            result.add(toUser(iter.next()));
        }
        return result;
    }

    /**
     * Cast o to String and call String.trim() method. Returns null when o is null.
     */
    public String toStringAndTrime(Object o) {
        if (o == null) {
            return null;
        }
        return ((String) o).trim();
    }
}
