package btvn.Mid2;

import btvn.Mid1.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class VitalSignUpdater {
    /*
    PHÂN TÍCH VỀ XỬ LÝ KIỂU DỮ LIỆU (TYPE HANDLING):

    1. Vấn đề của Statement nối chuỗi:
       - Khi nối chuỗi: "SET temperature = " + temp, giá trị double sẽ bị chuyển sang String
         dựa trên cài đặt vùng miền (Locale) của máy tính.
       - Nếu máy tính dùng Locale Pháp/Việt, số 37.5 sẽ biến thành "37,5" (dấu phẩy).
       - Câu lệnh SQL lỗi: UPDATE Vitals SET temperature = 37,5 ... (sai cú pháp SQL).

    2. Giải pháp từ PreparedStatement (setDouble, setInt):
       - Các phương thức này truyền dữ liệu ở dạng nguyên bản (binary/literal) trực tiếp
         đến database, không thông qua bước chuyển đổi String của hệ điều hành.
       - Giúp lập trình viên KHÔNG cần lo lắng về định dạng dấu chấm hay dấu phẩy.

    3. Lợi ích:
       - Đảm bảo tính chính xác tuyệt đối của dữ liệu y tế (nhiệt độ, nhịp tim).
       - Tránh các lỗi cú pháp SQL phát sinh do định dạng vùng miền khác nhau.
    */

    public static void main(String[] args) {
        int patientId = 1;
        double tempInput = 37.5;
        int heartRateInput = 80;

        try (Connection conn = DBConnection.getConnection()) {

            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS Vitals (
                    p_id INT PRIMARY KEY,
                    temperature DOUBLE,
                    heart_rate INT
                    );
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);

                String insertSample = "INSERT IGNORE INTO Vitals (p_id, temperature, heart_rate) VALUES (1, 36.5, 75);";
                stmt.execute(insertSample);
            }

            // CẬP NHẬT CHỈ SỐ SINH TỒN AN TOÀN
            String sql = "UPDATE Vitals SET temperature = ?, heart_rate = ? WHERE p_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, tempInput);
                pstmt.setInt(2, heartRateInput);
                pstmt.setInt(3, patientId);

                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("Cập nhật thành công");
                    System.out.println("ID: " + patientId + " | Temp: " + tempInput + " | Heart: " + heartRateInput);
                } else {
                    System.out.println("Không tìm thấy bệnh nhân có ID: " + patientId);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi hệ thống khi cập nhật: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
