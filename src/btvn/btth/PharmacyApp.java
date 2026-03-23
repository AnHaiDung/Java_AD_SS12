package btvn.btth;

import btvn.Mid1.DBConnection;

import java.sql.*;
import java.util.Scanner;

public class PharmacyApp {
    public static void main(String[] args) {
        initDatabase();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("1. Cập nhật tồn kho ");
            System.out.println("2. Tìm thuốc theo khoảng giá ");
            System.out.println("3. Tính tổng tiền đơn thuốc ");
            System.out.println("4. Thống kê doanh thu ngày ");
            System.out.println("5. Thoát");
            System.out.print("Chọn chức năng: ");
            int choice = sc.nextInt(); sc.nextLine();

            try (Connection conn = DBConnection.getConnection()) {
                switch (choice) {
                    case 1: updateMedicineStock(conn, sc); break;
                    case 2: findMedicinesByPriceRange(conn, sc); break;
                    case 3: calculatePrescriptionTotal(conn, sc); break;
                    case 4: getDailyRevenue(conn, sc); break;
                    case 5: System.exit(0);
                }
            } catch (SQLException e) {
                System.err.println("Lỗi: " + e.getMessage());
            }
        }
    }

    private static void initDatabase() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS medicines (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), price DECIMAL(18,2), stock INT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS prescriptions (id INT AUTO_INCREMENT PRIMARY KEY, medicine_id INT, quantity_sold INT, sale_date DATE, FOREIGN KEY (medicine_id) REFERENCES medicines(id))");

            stmt.execute("DROP PROCEDURE IF EXISTS CalculatePrescriptionTotal");
            stmt.execute("CREATE PROCEDURE CalculatePrescriptionTotal(IN p_id INT, OUT p_total DECIMAL(18,2)) " +
                    "BEGIN " +
                    "  SELECT (p.quantity_sold * m.price) INTO p_total " +
                    "  FROM prescriptions p JOIN medicines m ON p.medicine_id = m.id WHERE p.id = p_id; " +
                    "END");

            stmt.execute("DROP PROCEDURE IF EXISTS GetDailyRevenue");
            stmt.execute("CREATE PROCEDURE GetDailyRevenue(IN p_date DATE, OUT p_revenue DECIMAL(18,2)) " +
                    "BEGIN " +
                    "  SELECT SUM(p.quantity_sold * m.price) INTO p_revenue " +
                    "  FROM prescriptions p JOIN medicines m ON p.medicine_id = m.id WHERE p.sale_date = p_date; " +
                    "END");

            System.out.println("Database Pharmacy đã sẵn sàng.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void updateMedicineStock(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Nhập ID thuốc: "); int id = sc.nextInt();
        System.out.print("Số lượng thêm vào: "); int added = sc.nextInt();

        String sql = "UPDATE medicines SET stock = stock + ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, added);
            pstmt.setInt(2, id);
            if (pstmt.executeUpdate() > 0) System.out.println("Cập nhật kho thành công!");
        }
    }

    private static void findMedicinesByPriceRange(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Giá tối thiểu: "); double min = sc.nextDouble();
        System.out.print("Giá tối đa: "); double max = sc.nextDouble();

        String sql = "SELECT * FROM medicines WHERE price BETWEEN ? AND ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, min);
            pstmt.setDouble(2, max);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("--- DANH SÁCH THUỐC PHÙ HỢP ---");
                while (rs.next()) {
                    System.out.printf("ID: %d | Tên: %s | Giá: %.2f | Kho: %d\n",
                            rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock"));
                }
            }
        }
    }

    private static void calculatePrescriptionTotal(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Nhập ID đơn thuốc: "); int pId = sc.nextInt();
        String sql = "{call CalculatePrescriptionTotal(?, ?)}";
        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, pId);
            cstmt.registerOutParameter(2, Types.DECIMAL);
            cstmt.execute();
            System.out.println("Tổng tiền đơn thuốc: " + cstmt.getBigDecimal(2) + " USD");
        }
    }

    private static void getDailyRevenue(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Nhập ngày (yyyy-MM-dd): "); String dateStr = sc.next();
        String sql = "{call GetDailyRevenue(?, ?)}";
        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setDate(1, Date.valueOf(dateStr));
            cstmt.registerOutParameter(2, Types.DECIMAL);
            cstmt.execute();
            System.out.println("Doanh thu ngày " + dateStr + " là: " + cstmt.getBigDecimal(2) + " USD");
        }
    }
}
