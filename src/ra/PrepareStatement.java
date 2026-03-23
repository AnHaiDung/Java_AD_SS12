package ra;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrepareStatement {
    // ke thua statement
    // cho phep truyen tham so

    public static void main(String[] args) {
//        mo ket noi
        try(
                Connection conn = DBConnection.openConnection();
//        chuan bi cau lenh
                PreparedStatement pre = conn.prepareStatement("select * from student where id = ?");
        ) {
//            truyen tham so neu co
            pre.setInt(1, 2); // tìm theo ma student id =2
//          thuc thi cau lenh:
            ResultSet rs = pre.executeQuery();

            if ( rs.next() ) {

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}