package btvn.Excellent;

import btvn.Mid1.DBConnection;

import java.sql.*;
import java.util.Scanner;

public class RMHS {
    /*
    PHÂN TÍCH KỸ THUẬT:
    1. Tự động tạo bảng: Dùng Statement để chuẩn bị môi trường ngay khi chạy App.
    2. Chống SQL Injection: Dùng PreparedStatement cho mọi thao tác nhập liệu, xử lý được tên như D'Arcy.
    3. Stored Procedure: Dùng CallableStatement để gọi hàm tính phí với tham số OUT.
    */

    public static void main(String[] args) {
        initDatabase();
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("1. Hiển thị danh sách bệnh nhân");
            System.out.println("2. Tiếp nhận bệnh nhân mới");
            System.out.println("3. Cập nhật khoa điều trị");
            System.out.println("4. Xuất viện & Tính viện phí ");
            System.out.println("5. Thoát");
            System.out.print("chọn: ");
            int choice = sc.nextInt();
            sc.nextLine();
            try (Connection conn = DBConnection.getConnection()) {
                switch (choice) {
                    case 1:
                        showPatients(conn);
                        break;
                    case 2:
                        addPatient(conn, sc);
                        break;
                    case 3:
                        updatePatient(conn, sc);
                        break;
                    case 4:
                        dischargeAndPay(conn, sc);
                        break;
                    case 5:
                        System.out.println("Thoát");
                        return;
                    default:
                        System.out.println("Lựa chọn không hợp lệ");
                }
            } catch (SQLException e) {
                System.err.println("Lỗi kết nối: " + e.getMessage());
            }
        }
    }


    private static void initDatabase() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = """
                    CREATE TABLE IF NOT EXISTS Patients (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    patient_code VARCHAR(20) UNIQUE NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    age INT,
                    department VARCHAR(100),
                    admission_days INT DEFAULT 0);
                    """;
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showPatients(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Patients";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("DANH SÁCH BỆNH NHÂN NỘI TRÚ");
            while (rs.next()) {
                System.out.printf("ID: %d | Mã: %s | Tên: %s | Tuổi: %d | Khoa: %s | Số ngày: %d\n",
                        rs.getInt("id"), rs.getString("patient_code"), rs.getString("full_name"),
                        rs.getInt("age"), rs.getString("department"), rs.getInt("admission_days"));
            }
        }
    }

    private static void addPatient(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Mã BN: "); String code = sc.nextLine();
        System.out.print("Tên BN (VD: L'Oréal): "); String name = sc.nextLine();
        System.out.print("Tuổi: "); int age = sc.nextInt(); sc.nextLine();
        System.out.print("Khoa: "); String dept = sc.nextLine();
        System.out.print("Số ngày nằm viện: "); int days = sc.nextInt();

        String sql = "INSERT INTO Patients(patient_code, full_name, age, department, admission_days) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, name);
            pstmt.setInt(3, age);
            pstmt.setString(4, dept);
            pstmt.setInt(5, days);
            pstmt.executeUpdate();
            System.out.println(" Đã tiếp nhận bệnh nhân thành công.");
        }
    }

    private static void updatePatient(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Nhập ID bệnh nhân: "); int id = sc.nextInt(); sc.nextLine();
        System.out.print("Khoa điều trị mới: "); String dept = sc.nextLine();

        String sql = "UPDATE Patients SET department = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dept);
            pstmt.setInt(2, id);
            if (pstmt.executeUpdate() > 0) System.out.println("Đã cập nhật bệnh án.");
            else System.out.println("Không tìm thấy ID bệnh nhân.");
        }
    }

    private static void dischargeAndPay(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Nhập ID bệnh nhân xuất viện: "); int id = sc.nextInt();

        String sql = "{call CALCULATE_DISCHARGE_FEE(?, ?)}";
        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, id);
            cstmt.registerOutParameter(2, Types.DECIMAL);
            cstmt.execute();

            double fee = cstmt.getDouble(2);
            System.out.println("Tổng viện phí phải nộp: " + fee + " USD");
        } catch (SQLException e) {
            System.err.println("Lỗi gọi Stored Procedure: " + e.getMessage());
        }
    }
}
