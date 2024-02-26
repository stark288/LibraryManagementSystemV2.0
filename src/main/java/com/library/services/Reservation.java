package com.library.services;
import com.library.databaseservices.DataBaseutils;
import com.library.exceptions.SqlConnectionException;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.library.services.Borrow;
import com.library.users.Admin;

import static com.library.Layout.printTable;
import static com.library.accounts.AccountManagement.getPatronIdFromLoggedInUser;
import static com.library.accounts.AccountManagement.loggedInPatronUsername;

public class Reservation {
    static Scanner sc = new Scanner(System.in);
    private final Map<Integer, String> notifications; // Store notifications for each member
    private final ExecutorService executor;

    public Reservation() {

        this.notifications = new HashMap<>();
        this.executor = Executors.newCachedThreadPool(); // Create a thread pool
    }

    public void doReservation() throws SQLException, SqlConnectionException {
        Admin.viewBooks();

        Borrow borrow = new Borrow();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the ISBN of the book to reserve:");
        String isbnToReserve = scanner.nextLine();

        // Query to check if the book is available for reservation
        String checkAvailabilityQuery = "SELECT BookID, Availability, Copies FROM finallibrary.Book WHERE ISBN = ?";

        try (Connection conn = DataBaseutils.getConnection();
             PreparedStatement checkAvailabilityStatement = conn.prepareStatement(checkAvailabilityQuery)) {

            checkAvailabilityStatement.setString(1, isbnToReserve);
            ResultSet availabilityResultSet = checkAvailabilityStatement.executeQuery();

            if (availabilityResultSet.next()) {
                int bookID = availabilityResultSet.getInt("BookID");
                String status = availabilityResultSet.getString("Availability");
                int copies = availabilityResultSet.getInt("Copies");

                int patronID = getPatronIdFromLoggedInUser(loggedInPatronUsername);
                if (patronID == -1) {
                    System.out.println("No patron found with the logged-in username.");
                    return; // Exit the method if no patron is found
                }

                // Check if the book is already reserved by this patron
                String checkExistingReservationQuery = "SELECT COUNT(*) AS ReservationCount FROM finallibrary.Reservation WHERE BookID = ? AND PatronID = ? AND Status = 'requested'";
                try (PreparedStatement checkExistingReservationStatement = conn.prepareStatement(checkExistingReservationQuery)) {
                    checkExistingReservationStatement.setInt(1, bookID);
                    checkExistingReservationStatement.setInt(2, patronID);
                    ResultSet existingReservationResultSet = checkExistingReservationStatement.executeQuery();
                    if (existingReservationResultSet.next() && existingReservationResultSet.getInt("ReservationCount") > 0) {
                        System.out.println("You have already reserved this book.");
                        return;
                    }
                }

                // Proceed with the reservation
                String insertReservationQuery = "INSERT INTO finallibrary.Reservation (ReservationID, ReservationDate, BookID, PatronID, Status) VALUES (finallibrary.reservation_seq.NEXTVAL, CURRENT_DATE, ?, ?, 'requested')";
                try (PreparedStatement insertReservationStatement = conn.prepareStatement(insertReservationQuery)) {
                    insertReservationStatement.setInt(1, bookID);
                    insertReservationStatement.setInt(2, patronID);

                    // Execute the reservation insert statement
                    int rowsInserted = insertReservationStatement.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("Reservation requested successfully!");
                        // If the book is available, update the status to 'reserved'
                        if (status.equals("available") && copies > 0) {
                            String updateBookStatusQuery = "UPDATE finallibrary.Book SET Availability = 'reserved' WHERE BookID = ?";
                            try (PreparedStatement updateBookStatusStatement = conn.prepareStatement(updateBookStatusQuery)) {
                                updateBookStatusStatement.setInt(1, bookID);
                                updateBookStatusStatement.executeUpdate();
                            }
                        }
                    } else {
                        System.out.println("Failed to request reservation.");
                    }
                }
            } else {
                System.out.println("No book found with the provided ISBN.");
            }
        } catch (SQLException e) {
            System.out.println("Error processing the reservation.");
            e.printStackTrace();
            throw e;
        }
    }



    public void notifyPatron(int patronID, String message) {
        // Create a new notification task
        NotificationTask notificationTask = new NotificationTask(patronID, message);
        // Submit the task to the executor
        executor.submit(notificationTask);
    }

    // Other methods...

    public static void approveReservation() throws SQLException, SqlConnectionException {
        // Display requested reservations
        displayRequestedReservations();

        // Check if there are any reservations to approve
        if (noRequestedReservations()) {
            System.out.println("No reservations to approve.");
            return;
        }

        // Proceed with approval process
        System.out.println("Enter reservation ID to approve: ");
        String reservationID = sc.nextLine();
        String sql = "UPDATE finallibrary.Reservation SET Status = 'approved' WHERE ReservationID = ?";

        try (Connection conn = DataBaseutils.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, reservationID);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Reservation approved successfully.");
            } else {
                System.out.println("No reservation found with the given ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error approving reservation in the database.");
            e.printStackTrace();
            throw e;
        }
    }

    private static boolean noRequestedReservations() throws SQLException, SqlConnectionException {
        // SQL query to count the total number of requested reservations
        String sql = "SELECT COUNT(*) AS total FROM finallibrary.Reservation WHERE Status = 'requested'";

        try (Connection conn = DataBaseutils.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            // Check if the result set has any data
            if (resultSet.next()) {
                // Get the total count of requested reservations from the result set
                int totalReservations = resultSet.getInt("total");
                // Return true if there are no requested reservations, false otherwise
                return totalReservations == 0;
            } else {

                return true;
            }
        } catch (SQLException e) {
            // Print error message and stack trace
            System.out.println("Error checking requested reservations.");
        }
        return false;
    }


    public static void rejectReservation() throws SQLException, SqlConnectionException {
        // Display requested reservations
        displayRequestedReservations();

        // Check if there are any reservations to approve
        if (noRequestedReservations()) {
            System.out.println("No reservations to reject.");
            return;
        }

        // Proceed with approval process
        System.out.println("Enter reservation ID to Reject: ");
        String reservationID = sc.nextLine();
        String sql = "UPDATE finallibrary.Reservation SET Status = 'Rejected' WHERE ReservationID = ?";

        try (Connection conn = DataBaseutils.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, reservationID);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Reservation Rejected .");
            } else {
                System.out.println("No reservation found with the given ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error approving reservation in the database.");
            e.printStackTrace();
            throw e;
        }
    }
    public static void displayRequestedReservations() throws SQLException, SqlConnectionException {
        String sql = "SELECT * FROM finallibrary.Reservation WHERE Status = 'requested'";

        List<String[]> rows = new ArrayList<>();
        String[] headers = {"Reservation ID", "Book ID", "Patron ID", "Reservation Date"};

        try (Connection conn = DataBaseutils.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (!resultSet.isBeforeFirst()) {
                System.out.println("No requested reservations available.");
                return; // Exit the method early
            }

            while (resultSet.next()) {
                String reservationID = String.valueOf(resultSet.getInt("ReservationID"));
                String bookID = String.valueOf(resultSet.getInt("BookID"));
                String patronID = String.valueOf(resultSet.getInt("PatronID"));
                Date reservationDate = resultSet.getDate("ReservationDate");

                String[] rowData = {
                        reservationID,
                        bookID,
                        patronID,
                        reservationDate.toString()
                };
                rows.add(rowData);
            }
        } catch (SQLException e) {
            System.out.println("Error displaying requested reservations from the database.");
        }

        // Sort reservations by Reservation ID
        rows.sort(Comparator.comparing(row -> Integer.parseInt(row[0])));

        printTable(headers, rows);
    }
    public void viewReservedBooks() throws SQLException, SqlConnectionException {
        // SQL query to retrieve reservation details along with the book name
        String sql = "SELECT r.ReservationID, r.ReservationDate, r.Status, r.BookID, b.Title AS BookName, r.PatronID, p.FirstName AS PatronName " +
                "FROM finallibrary.Reservation r " +
                "JOIN finallibrary.Book b ON r.BookID = b.BookID " +
                "JOIN finallibrary.Patron p ON r.PatronID = p.PatronID";

        List<String[]> rows = new ArrayList<>();
        String[] headers = {"Reservation ID", "Date", "Status", "Book ID", "Book Name", "Patron ID", "Reserved By"};

        try (Connection conn = DataBaseutils.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (!resultSet.isBeforeFirst()) {
                System.out.println("No reservations found in the database.");
                return;
            }

            while (resultSet.next()) {
                String[] rowData = {
                        resultSet.getString("ReservationID"),
                        resultSet.getString("ReservationDate"),
                        resultSet.getString("Status"),
                        resultSet.getString("BookID"),
                        resultSet.getString("BookName"),
                        resultSet.getString("PatronID"),
                        resultSet.getString("PatronName")
                };
                rows.add(rowData);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing reservations from the database.");
            e.printStackTrace();
            return;
        }

        // Sort reservations by Reservation ID
        Collections.sort(rows, Comparator.comparing(row -> Integer.parseInt(row[0])));

        printTable(headers, rows);
    }
    public void notifyMember(int patronID, String message) {
        synchronized (notifications) {
            notifications.put(patronID, message);
        }
        System.out.println("Notification for patron ID " + patronID + ": " + message);
    }

    public void viewNotifications(int patronID) {
        String notification;
        synchronized (notifications) {
            notification = notifications.getOrDefault(patronID, "No notifications");
        }
        System.out.println("Notifications for patron ID " + patronID + ": " + notification);
    }

    public void clearNotifications(int patronID) {
        synchronized (notifications) {
            notifications.remove(patronID);
        }
        System.out.println("Notifications cleared for patron ID " + patronID);
    }

    public void shutdown() {
        executor.shutdown();
    }

    private class NotificationTask implements Runnable {
        private int patronID;
        private String message;

        public NotificationTask(int patronID, String message) {
            this.patronID = patronID;
            this.message = message;
        }

        @Override
        public void run() {
            System.out.println("Notification for patron ID " + patronID + ": " + message);
        }
    }
}
