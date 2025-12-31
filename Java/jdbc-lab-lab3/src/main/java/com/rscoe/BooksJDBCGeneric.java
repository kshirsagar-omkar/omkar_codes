import java.sql.*;
import java.util.Scanner;

public class BooksJDBCGeneric {

    // Database configuration
    private static final String URL =
            "jdbc:postgresql://localhost:5432/adv_java";
    private static final String USER = "root";
    private static final String PASSWORD = "root@123";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try {
            // Load PostgreSQL Driver
            Class.forName("org.postgresql.Driver");

            // Create connection
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully\n");

            int choice;
            do {
                System.out.println("===== BOOKS MENU =====");
                System.out.println("1. Insert Book");
                System.out.println("2. Update Book");
                System.out.println("3. Delete Book");
                System.out.println("4. View All Books");
                System.out.println("0. Exit");
                System.out.print("Enter choice: ");

                choice = sc.nextInt();
                sc.nextLine(); // consume newline

                switch (choice) {

                    case 1:
                        System.out.print("Enter book name to insert: ");
                        String insertName = sc.nextLine();

                        String insertSQL = "INSERT INTO books (name) VALUES (?)";
                        PreparedStatement psInsert =
                                con.prepareStatement(insertSQL);
                        psInsert.setString(1, insertName);
                        psInsert.executeUpdate();
                        System.out.println("Book inserted successfully\n");
                        psInsert.close();
                        break;

                    case 2:
                        System.out.print("Enter existing book name: ");
                        String oldName = sc.nextLine();

                        System.out.print("Enter new book name: ");
                        String newName = sc.nextLine();

                        String updateSQL =
                                "UPDATE books SET name = ? WHERE name = ?";
                        PreparedStatement psUpdate =
                                con.prepareStatement(updateSQL);
                        psUpdate.setString(1, newName);
                        psUpdate.setString(2, oldName);

                        int updated = psUpdate.executeUpdate();
                        if (updated > 0)
                            System.out.println("Book updated successfully\n");
                        else
                            System.out.println("Book not found\n");

                        psUpdate.close();
                        break;

                    case 3:
                        System.out.print("Enter book name to delete: ");
                        String deleteName = sc.nextLine();

                        String deleteSQL =
                                "DELETE FROM books WHERE name = ?";
                        PreparedStatement psDelete =
                                con.prepareStatement(deleteSQL);
                        psDelete.setString(1, deleteName);

                        int deleted = psDelete.executeUpdate();
                        if (deleted > 0)
                            System.out.println("Book deleted successfully\n");
                        else
                            System.out.println("Book not found\n");

                        psDelete.close();
                        break;

                    case 4:
                        String selectSQL = "SELECT * FROM books";
                        PreparedStatement psSelect =
                                con.prepareStatement(selectSQL);
                        ResultSet rs = psSelect.executeQuery();

                        System.out.println("\nID\tNAME");
                        System.out.println("-----------------");
                        while (rs.next()) {
                            System.out.println(
                                    rs.getInt("id") + "\t" +
                                    rs.getString("name")
                            );
                        }
                        System.out.println();

                        rs.close();
                        psSelect.close();
                        break;

                    case 0:
                        System.out.println("Program terminated");
                        break;

                    default:
                        System.out.println("Invalid choice\n");
                }

            } while (choice != 0);

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }
    }
}
