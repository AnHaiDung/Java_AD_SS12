package ra;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class CallableStatemanet {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.openConnection();
            CallableStatemanet call = (CallableStatemanet) conn.prepareCall("{call deleteStudentById(?)}");
//            truyen tham so
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
