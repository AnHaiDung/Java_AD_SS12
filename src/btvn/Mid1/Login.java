package btvn.Mid1;

import java.sql.*;
import java.util.Scanner;

public class Login {
    /*
    PHÂN TÍCH BẢO MẬT:

    1. PreparedStatement là "tấm khiên" vì nó tách biệt Câu lệnh (Logic) và Tham số (Data).

    2. Cơ chế biên dịch trước (Pre-compiled):
       - Database nhận khung SQL: "SELECT * FROM doctors WHERE doctor_code = ? AND password = ?"
       - Các dấu (?) là Placeholder, database sẽ hiểu đây CHỈ là dữ liệu thuần túy.

    3. Chống SQL Injection:
       - Nếu kẻ tấn công nhập password là ' OR '1'='1, hệ thống sẽ tìm kiếm chuỗi
         ký tự khớp y hệt như vậy thay vì thực thi lệnh OR.
       - Do đó, logic truy vấn không bị thay đổi, đảm bảo an toàn tuyệt đối.
    */
    public static void main(String[] args) {
        String doctorCodeInput = "hello123";
        String passwordInput = "123456789";

        try (Connection conn = DBConnection.getConnection()) {

            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS doctors 
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    doctor_code VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100) NOT NULL
                    );
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);

                String insertSample = "INSERT IGNORE INTO doctors (doctor_code, password, full_name) "
                        + "VALUES ('hello123', '123456789', 'Bác sĩ An Hải Dũng');";
                stmt.execute(insertSample);
            }

            // THỰC HIỆN ĐĂNG NHẬP AN TOÀN
            String sql = "SELECT * FROM doctors WHERE doctor_code = ? AND password = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, doctorCodeInput);
                pstmt.setString(2, passwordInput);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Chúc mừng! Đăng nhập thành công.");
                        System.out.println("Bác sĩ: " + rs.getString("full_name"));
                    } else {
                        System.out.println("Lỗi: Sai mã bác sĩ hoặc mật khẩu!");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
