package com.mycompany.busreservationproject;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class SeatBooking extends JFrame {

    private JPanel seatPanel;
    private JButton btnBook;
    private JLabel lblInfo;
    private ArrayList<JButton> seatButtons = new ArrayList<>();
    
    private String selectedBusNumber;
    private String customerEmail;

    public SeatBooking(String busNumber, String email) {
        this.selectedBusNumber = busNumber;
        this.customerEmail = email;

        setTitle("Seat Booking - Bus " + busNumber);
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        lblInfo = new JLabel("Select seats for bus: " + busNumber, SwingConstants.CENTER);
        lblInfo.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblInfo, BorderLayout.NORTH);

        seatPanel = new JPanel(new GridLayout(5, 4, 10, 10));
        seatPanel.setBorder(new TitledBorder("Seats"));
        createSeatButtons();
        add(seatPanel, BorderLayout.CENTER);

        btnBook = new JButton("Confirm Booking");
        btnBook.setBackground(new java.awt.Color(0, 102, 102));
        btnBook.setForeground(Color.WHITE);
        btnBook.setFont(new Font("Arial", Font.BOLD, 14));
        add(btnBook, BorderLayout.SOUTH);

        loadBookedSeats();

        btnBook.addActionListener(e -> bookSelectedSeats());
    }

    private void createSeatButtons() {
        for (int i = 1; i <= 40; i++) {
            String seatLabel = "S" + i;
            JButton btn = new JButton(seatLabel);
            btn.setBackground(Color.GREEN);
            btn.addActionListener(e -> toggleSeatSelection(btn));
            seatButtons.add(btn);
            seatPanel.add(btn);
        }
    }

    private void toggleSeatSelection(JButton btn) {
        if (btn.getBackground() == Color.GREEN) {
            btn.setBackground(Color.YELLOW); // selected
        } else if (btn.getBackground() == Color.YELLOW) {
            btn.setBackground(Color.GREEN); // deselected
        }
    }

    private void loadBookedSeats() {
        String url = "jdbc:mysql://localhost:3306/busreservationsystem";
        String user = "root";
        String password = "";

        String query = "SELECT seat_no FROM bookings WHERE bus_number = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, selectedBusNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String bookedSeat = rs.getString("seat_no");
                for (JButton btn : seatButtons) {
                    if (btn.getText().equals(bookedSeat)) {
                        btn.setBackground(Color.RED);
                        btn.setEnabled(false);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading seats: " + e.getMessage());
        }
    }

    private void bookSelectedSeats() {
        String url = "jdbc:mysql://localhost:3306/busreservationsystem";
        String user = "root";
        String password = "";

        String insertSQL = "INSERT INTO bookings (bus_number, seat_no, customer_email) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            int bookedCount = 0;
            for (JButton btn : seatButtons) {
                if (btn.getBackground() == Color.YELLOW) {
                    pstmt.setString(1, selectedBusNumber);
                    pstmt.setString(2, btn.getText());
                    pstmt.setString(3, customerEmail);
                    pstmt.addBatch();
                    btn.setBackground(Color.RED);
                    btn.setEnabled(false);
                    bookedCount++;
                }
            }

            if (bookedCount == 0) {
                JOptionPane.showMessageDialog(this, "Please select at least one seat.");
                return;
            }

            pstmt.executeBatch();
            JOptionPane.showMessageDialog(this, "Booking successful for " + bookedCount + " seat(s).");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error booking seats: " + e.getMessage());
        }
    }

    // Example usage (for testing)
    public static void main(String[] args) {
        new SeatBooking("BUS123", "john@example.com").setVisible(true);
    }
}
