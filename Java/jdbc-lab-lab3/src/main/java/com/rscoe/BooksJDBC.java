package com.rscoe;

import java.sql.*;
import java.util.Scanner;

public class BooksJDBCGeneric {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/adv_java";
    private static final String USER = "root";
    private static final String PASSWORD = "root@123";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try {
            Class.forName("org.postgresql.Driver");

            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

                System.out.println("Database Connected Successfully");

                int choice;
                do {
                    System.out.println("\n----- BOOKS MENU -----");
                    System.out.println("1. Insert Book");
                    System.out.println("2. Update Book");
                    System.out.println("3. Delete Book");
                    System.out.println("4. View All Books");
                    System.out.println("0. Exit");
                    System.out.print("Enter choice: ");

                    choice = sc.nextInt();
                    sc.nextLine(); // consume newline

                    switch (choice) {

                        case 1 -> insertBook(con, sc);
                        case 2 -> updateBook(con, sc);
                        case 3 -> deleteBook(con, sc);
                        case 4 -> viewBooks(con);
                        case 0 -> System.out.println("Exiting program");
                        default -> System.out.println("Invalid choice");
                    }

                } while (choice != 0);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }
    }

    // ---------- INSERT ----------
    private static void insertBook(Connection con, Scanner sc) throws SQLException {
        System.out.print("Enter book name to insert: ");
        String name = sc.nextLine();

        String sql = "INSERT INTO books (name) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
            System.out.println("Book inserted successfully");
        }
    }

    // ---------- UPDATE ----------
    private static void updateBook(Connection con, Scanner sc) throws SQLException {
        System.out.print("Enter existing book name: ");
        String oldName = sc.nextLine();

        System.out.print("Enter new book name: ");
        String newName = sc.nextLine();

        String sql = "UPDATE books SET name = ? WHERE name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Book updated successfully");
            else
                System.out.println("Book not found");
        }
    }

    // ---------- DELETE ----------
    private static void deleteBook(Connection con, Scanner sc) throws SQLException {
        System.out.print("Enter book name to delete: ");
        String name = sc.nextLine();

        String sql = "DELETE FROM books WHERE name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Book deleted successfully");
            else
                System.out.println("Book not found");
        }
    }

    // ---------- SELECT ----------
    private static void viewBooks(Connection con) throws SQLException {
        String sql = "SELECT * FROM books";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\nID\tNAME");
            System.out.println("----------------");

            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + "\t" + rs.getString("name")
                );
            }
        }
    }
}
