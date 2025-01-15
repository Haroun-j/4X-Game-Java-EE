package com.projet.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseServlet extends HttpServlet {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/4x_game";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Static block to load the MySQL JDBC Driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }

    /**
     * Provides a connection to the database.
     *
     * @return a new Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (Connection connection = getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT * FROM joueurs")) {

            response.setContentType("text/html");
            response.getWriter().println("<h1>Database Connection Successful!</h1>");
            response.getWriter().println("<h2>Players:</h2>");
            while (resultSet.next()) {
                response.getWriter().println("<p>" + resultSet.getString("username") + "</p>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h1>Database Connection Failed:</h1>");
            response.getWriter().println("<p>" + e.getMessage() + "</p>");
        }
    }
}
