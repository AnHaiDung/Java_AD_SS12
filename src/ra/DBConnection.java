package ra;

import java.sql.*;

public class DBConnection {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/jdbc?createDatabaseIfNotExist=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "06072006";
    public static Connection openConnection(){
        // B1 : khai báo Driver
        try {
            Class.forName(DRIVER);
            // Mở kết nối
            return DriverManager.getConnection(URL,USERNAME,PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("Chưa cài đặt Mysql Driver");
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối tới database");
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

        try (
                // B1 : Mở kết nối
                Connection conn = openConnection();
                // B2 : Tạo câu lệnh ( Statement )
                Statement stm = conn.createStatement();
        ) {
            // Tạo bảng
            String sql = """
                    create table if not exists Student(
                        id int auto_increment primary key,
                        name varchar(255) not null,
                        gpa decimal(10,2) check(gpa > 0)
                    )
                    """;
            // B3 : Truyền tham số ( nếu có )
            // B4 : Thực thi câu lệnh
            boolean isSesultSet = stm.execute(sql); // true : có bản ghi trả về (ResultSet-Select) - false : không có bản ghi trả về
            System.out.println(isSesultSet);
            // B5 : Xử lí kết quả trả về ( nếu có )
            ResultSet rs = stm.getResultSet();
            System.out.println(isSesultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // B6 : Đóng kết nối
    }
}
