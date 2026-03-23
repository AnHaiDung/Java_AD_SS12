package btvn.Good2;

import btvn.Mid1.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestResultBulkInsert {
    /*
    PHÂN TÍCH TỐI ƯU HÓA (PERFORMANCE ANALYSIS):

    1. Vấn đề lãng phí (Mã nguồn lỗi):
       - Khởi tạo Statement và nối chuỗi bên trong vòng lặp 1.000 lần khiến Database
         phải Parse (phân tích) và lập kế hoạch thực thi 1.000 lần cho cùng một cấu trúc.

    2. Giải pháp PreparedStatement:
       - Khởi tạo bên ngoài vòng lặp: Database chỉ Parse và biên dịch (Pre-compile) 01 lần duy nhất.
       - Trong vòng lặp chỉ thay thế tham số (?), tiết kiệm cực lớn tài nguyên CPU và RAM của Server.

    3. Kỹ thuật Batch Processing:
       - Gom 1.000 bản ghi vào một "lô" (Batch) để gửi đi một lần, giảm thiểu độ trễ mạng.
    */

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            list.add("Kết quả xét nghiệm máu #" + i + ": Chỉ số an toàn");
        }
        try (Connection conn = DBConnection.getConnection()) {

            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS Results (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    data VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }

            String sql = "INSERT INTO Results(data) VALUES(?)";

            long startTime = System.currentTimeMillis();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (String resultData : list) {
                    pstmt.setString(1, resultData);
                    pstmt.addBatch();
                }

                pstmt.executeBatch();

                conn.commit();
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Đã nạp thành công: " + list.size() + " dòng.");
            System.out.println("Thời gian thực thi: " + (endTime - startTime) + " ms.");

        } catch (SQLException e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
