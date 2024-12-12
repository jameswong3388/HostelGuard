package org.example.hvvs.db;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Database utility class providing the following methods:<br/>
 * 1. Get Connection object<br/>
 * 2. Close Connection, Statement, ResultSet objects<br/>
 * 3. Execute a SQL statement with no return value, returns number of affected records (e.g. insert/update/delete)<br/>
 * 4. Execute a SQL statement with return value<br/>
 * <p>
 * Database config file must be /WEB-INF/db-config.properties
 */
public class DBUtil {

    private static String driver;
    private static String url;
    private static String name;
    private static String pass;

    static {
        ResourceBundle config = ResourceBundle.getBundle("org.example.hvvs.db.db-config");
        driver = config.getString("jdbc-driver");
        url = config.getString("jdbc-url");
        name = config.getString("jdbc-name");
        pass = config.getString("jdbc-pass");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading database file or loading database driver");
        }
    }

    /**
     * Get connection object, returns null if failed
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, name, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Close ResultSet, Statement, Connection objects in sequence. Does nothing if parameter is null
     */
    public static void close(Connection conn, Statement state, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            rs = null;
        }

        if (state != null) {
            try {
                state.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            state = null;
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn = null;
        }
    }

    /**
     * Execute a SQL statement with no return value (e.g. insert/update/delete), returns number of affected rows<br/>
     * Does not guarantee mapping null in params to database NULL, use executeUpdateWithNull() if needed
     *
     * @throws SQLException
     */
    public static int executeUpdate(String sql, Object[] params) throws SQLException {
        return executeUpdateWithNull(sql, params, null);
    }

    /**
     * Execute a SQL statement with no return value (e.g. insert/update/delete), returns number of affected rows<br/>
     * Will map null in params to database NULL<br/>
     * <p>
     * If sqlType is null, behaves same as executeUpdate()
     *
     * @param sqlTypes SQL types corresponding to params
     * @throws SQLException
     */
    public static int executeUpdateWithNull(String sql, Object[] params, int[] sqlTypes) throws SQLException {
        Connection conn = null;
        PreparedStatement pstate = null;
        int result = 0;

        try {
            conn = getConnection();
            pstate = conn.prepareStatement(sql);
            if (sqlTypes == null) {
                fillParams(pstate, params);
            } else {
                fillParamsWithNull(pstate, params, sqlTypes);
            }
            result = pstate.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(conn, pstate, null);
        }
        return result;
    }

    /**
     * Execute a SQL statement with return value, guarantees records in returned List match query result order<br/>
     *
     * @return Never null, List length is 0 if no records returned
     * @throws SQLException
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object[] params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstate = null;
        ResultSet rs = null;
        List<Map<String, Object>> table = new ArrayList<>();

        try {
            conn = getConnection();
            pstate = conn.prepareStatement(sql);
            fillParams(pstate, params);
            rs = pstate.executeQuery();

            // Get column info like names and count
            ResultSetMetaData metaData = rs.getMetaData();
            int columns = metaData.getColumnCount();
            String[] columnNames = new String[columns];
            for (int i = 0; i < columns; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            // Package data from ResultSet
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (String name : columnNames) {
                    row.put(name, rs.getObject(name));
                }
                table.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(conn, pstate, rs);
        }

        return table;
    }

    /**
     * Fill parameters into PreparedStatement object, does nothing if params is null<br/>
     *
     * @throws SQLException
     */
    private static void fillParams(PreparedStatement pstate, Object[] params) throws SQLException {
        if (params == null) {
            return;
        }

        for (int i = 0; i < params.length; i++) {
            pstate.setObject(i + 1, params[i]);
        }
    }

    /**
     * Fill parameters into PreparedStatement object based on sqlTypes, maps null in params to database NULL<br/>
     * <p>
     * Does nothing if params is null<br/>
     * Behaves same as fillParams if sqlTypes is null<br/>
     *
     * @param sqlTypes SQL types corresponding to params
     * @throws SQLException
     * @throws RuntimeException when sqlTypes != null && params.length != sqlTypes.length
     */
    private static void fillParamsWithNull(PreparedStatement pstate, Object[] params, int[] sqlTypes) throws SQLException {
        if (params == null) {
            return;
        }
        if (sqlTypes == null) {
            fillParams(pstate, params);
            return;
        }
        if (sqlTypes.length != params.length) {
            throw new RuntimeException("params and sqlTypes parameters do not match");
        }

        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                pstate.setNull(i + 1, sqlTypes[i]);
            } else {
                pstate.setObject(i + 1, params[i], sqlTypes[i]);
            }
        }
    }

}
