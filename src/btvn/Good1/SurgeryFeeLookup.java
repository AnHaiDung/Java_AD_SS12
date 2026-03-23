package btvn.Good1;

import btvn.Mid1.DBConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Scanner;

public class SurgeryFeeLookup {
    /*
    PHÂN TÍCH VỀ THAM SỐ ĐẦU RA (OUT PARAMETER):

    1. Vai trò của registerOutParameter():
       - JDBC bắt buộc phải gọi phương thức này trước khi execute() để thông báo cho Driver
         biết kiểu dữ liệu trả về từ Stored Procedure.
       - Nếu quên bước này, hệ thống sẽ không thể lấy được giá trị tiền tệ ra (lỗi Index).

    2. Ánh xạ kiểu dữ liệu:
       - Với kiểu DECIMAL trong SQL, ta sử dụng hằng số Types.DECIMAL trong Java.

    3. Quy trình thực hiện:
       - Chuẩn bị lời gọi {call ...} -> Đăng ký tham số OUT -> Truyền tham số IN -> Thực thi -> Lấy kết quả.
    */

    public static void main(String[] args) {
        int surgeryIdInput = 505;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "{call GET_SURGERY_FEE(?, ?)}";
            try (CallableStatement cstmt = conn.prepareCall(sql)) {
                cstmt.registerOutParameter(2, Types.DECIMAL);
                cstmt.setInt(1, surgeryIdInput);
                cstmt.execute();
                double totalCost = cstmt.getDouble(2);
                System.out.println("Mã phẫu thuật: " + surgeryIdInput);
                System.out.println("Tổng chi phí (total_cost): " + totalCost + " USD");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi hệ thống khi gọi Stored Procedure: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
