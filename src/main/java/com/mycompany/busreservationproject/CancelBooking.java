package com.mycompany.busreservationproject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CancelBooking extends JFrame {

    private JTable bookingTable;
    private DefaultTableModel model;
    private JButton cancelButton;
    private String customerEmail;

    public CancelBooking(String customerEmail) {
        this.customerEmail = customerEmail;

        setTitle("Cancel Your Bookings");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top label
        JLabel titleLabel = new JLabel("Your Current Bookings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new String[]{"Booking ID", "Bus Number", "Seat Number"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // disable editing
            }
        };
        bookingTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        add(scrollPane, BorderLayout.CENTER);

        // Cancel Button
        cancelButton = new JButton("Cancel Selected Booking");
        cancelButton.setBackground(new java.awt.Color(0, 102, 102));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        add(cancelButton, BorderLayout.SOUTH);

        // Load bookings
        loadBookings();

        // Button action
        cancelButton.addActionListener(e -> cancelSelectedBooking());

        setVisible(true);
    }

    private void loadBookings() {
        model.setRowCount(0); // clear table first

        String url = "jdbc:mysql://localhost:3306/busreservationsystem";
        String user = "root";
        String password = "";

        String query = "SELECT id, bus_number, seat_no FROM bookings WHERE customer_email = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, customerEmail);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int bookingId = rs.getInt("id");
                String busNumber = rs.getString("bus_number");
                String seatNo = rs.getString("seat_no");

                model.addRow(new Object[]{bookingId, busNumber, seatNo});
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No bookings found.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage());
        }
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.");
            return;
        }

        int bookingId = (int) model.getValueAt(selectedRow, 0);
        String seatNo = (String) model.getValueAt(selectedRow, 2);
        String busNo = (String) model.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel seat " + seatNo + " on bus " + busNo + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String url = "jdbc:mysql://localhost:3306/busreservationsystem";
        String user = "root";
        String password = "";

        String deleteQuery = "DELETE FROM bookings WHERE id = ? AND customer_email = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

            pstmt.setInt(1, bookingId);
            pstmt.setString(2, customerEmail);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Booking canceled successfully.");
                model.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error canceling booking: " + e.getMessage());
        }
    }

    // For testing independently
    public static void main(String[] args) {
        UserSession.setLoggedInEmail("john@example.com"); // Test email
        new CancelBooking(UserSession.getLoggedInEmail());
    }
}
