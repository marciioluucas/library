
package controller.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Book;
import model.Loan;
import model.dao.DBConfig;
import model.dao.DBConnection;

public class BorrowedBooksDAO {
    
    public static int create(Loan loan, Book book) throws SQLException {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("INSERT INTO borrowed_books "
                + "(fk_loan, "
                + "fk_book, "
                + "date_hour_inclusion, "
                + "fk_user_who_included, "
                + "excluded) "
                + "VALUES(?,?,?,?,?)",
                PreparedStatement.RETURN_GENERATED_KEYS
        );
        stm.setInt(1, loan.getId());
        stm.setInt(2, book.getId());
        stm.setTimestamp(3, DBConfig.now(), DBConfig.tzUTC);
        stm.setInt(4, DBConfig.idUserLogged);
        stm.setBoolean(5, loan.isExcluded());
        stm.execute();
        ResultSet rs = stm.getGeneratedKeys();
        rs.next();
        return rs.getInt("pk_borrowed_book");
    }
}
