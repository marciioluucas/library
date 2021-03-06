package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.Book;
import jdbc.DBConfig;
import jdbc.DBConnection;
import model.enums.Genre;
import model.enums.Language;

public class BookDAO {

    public static int create(Book book) throws SQLException {
        java.sql.Date dateSql = new java.sql.Date(book.getDatePublication().getTime());
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("INSERT INTO books "
                + "(name, "
                + "fk_author, "
                + "fk_publisher, "
                + "language, "
                + "isbn10, "
                + "isbn13, "
                + "date_publication, "
                + "genre, "
                + "available_quantity, "
                + "date_hour_inclusion, "
                + "fk_user_who_included, "
                + "excluded)"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                PreparedStatement.RETURN_GENERATED_KEYS
        );
        stm.setString(1, book.getName());
        stm.setInt(2, AuthorDAO.create(book.getAuthor()));
        stm.setInt(3, PublisherDAO.create(book.getPublisher()));
        stm.setInt(4, book.getLanguage().getId());
        stm.setString(5, book.getIsbn10());
        stm.setString(6, book.getIsbn13());
        stm.setDate(7, dateSql);
        stm.setInt(8, book.getGenre().getId());
        stm.setInt(9, book.getAvailableQuantity());
        stm.setTimestamp(10, DBConfig.now(), DBConfig.TZ_UTC);
        stm.setInt(11, DBConfig.userLogged.getId());
        stm.setBoolean(12, book.isExcluded());
        stm.execute();
        ResultSet rs = stm.getGeneratedKeys();
        rs.next();
        book.setId(rs.getInt("pk_book"));
        return book.getId();
    }
    
    public static int createOnlyBook(Book book) throws SQLException {
        java.sql.Date dateSql = new java.sql.Date(book.getDatePublication().getTime());
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("INSERT INTO books "
                + "(name, "
                + "fk_author, "
                + "fk_publisher, "
                + "language, "
                + "isbn10, "
                + "isbn13, "
                + "date_publication, "
                + "genre, "
                + "available_quantity, "
                + "date_hour_inclusion, "
                + "fk_user_who_included, "
                + "excluded)"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                PreparedStatement.RETURN_GENERATED_KEYS
        );
        stm.setString(1, book.getName());
        stm.setInt(2, book.getAuthor().getId());
        stm.setInt(3, book.getPublisher().getId());
        stm.setInt(4, book.getLanguage().getId());
        stm.setString(5, book.getIsbn10());
        stm.setString(6, book.getIsbn13());
        stm.setDate(7, dateSql);
        stm.setInt(8, book.getGenre().getId());
        stm.setInt(9, book.getAvailableQuantity());
        stm.setTimestamp(10, DBConfig.now(), DBConfig.TZ_UTC);
        stm.setInt(11, DBConfig.userLogged.getId());
        stm.setBoolean(12, book.isExcluded());
        stm.execute();
        ResultSet rs = stm.getGeneratedKeys();
        rs.next();
        book.setId(rs.getInt("pk_book"));
        return book.getId();
    }

    public static Book retrieveExcluded(int pkBook, boolean excluded) throws SQLException {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("SELECT * FROM books WHERE pk_book=? AND excluded=?");
        stm.setInt(1, pkBook);
        stm.setBoolean(2, excluded);
        stm.execute();
        ResultSet rs = stm.getResultSet();
        if (!rs.next()) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        return new Book(
                rs.getInt("pk_book"),
                rs.getString("name"),
                AuthorDAO.retrieve(rs.getInt("fk_author")),
                PublisherDAO.retrieve(rs.getInt("fk_publisher")),
                Language.getById(rs.getInt("language")),
                rs.getString("isbn10"),
                rs.getString("isbn13"),
                rs.getDate("date_publication"),
                Genre.getById(rs.getInt("genre")),
                rs.getInt("available_quantity")
        );
    }

    public static Book retrieve(int pkBook) throws SQLException {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("SELECT * FROM books WHERE pk_book=?");
        stm.setInt(1, pkBook);
        stm.execute();
        ResultSet rs = stm.getResultSet();
        if (!rs.next()) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        return new Book(
                rs.getInt("pk_book"),
                rs.getString("name"),
                AuthorDAO.retrieve(rs.getInt("fk_author")),
                PublisherDAO.retrieve(rs.getInt("fk_publisher")),
                Language.getById(rs.getInt("language")),
                rs.getString("isbn10"),
                rs.getString("isbn13"),
                rs.getDate("date_publication"),
                Genre.getById(rs.getInt("genre")),
                rs.getInt("available_quantity")
        );
    }

    public static ArrayList<Book> retrieveAllExcluded(boolean excluded) throws SQLException {
        ArrayList<Book> aux = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM books WHERE excluded=" + excluded);
        if (!rs.next()) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        do {
            aux.add(new Book(
                    rs.getInt("pk_book"),
                    rs.getString("name"),
                    AuthorDAO.retrieve(rs.getInt("fk_author")),
                    PublisherDAO.retrieve(rs.getInt("fk_publisher")),
                    Language.getById(rs.getInt("language")),
                    rs.getString("isbn10"),
                    rs.getString("isbn13"),
                    rs.getDate("date_publication"),
                    Genre.getById(rs.getInt("genre")),
                    rs.getInt("available_quantity")
            ));
        } while (rs.next());
        return aux;
    }
    
    public static ArrayList<Book> retrieveAllExcludedAvailable(boolean excluded) throws SQLException {
        ArrayList<Book> aux = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM books WHERE available_quantity >0 AND excluded=" + excluded);
        if (!rs.next()) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        do {
            aux.add(new Book(
                    rs.getInt("pk_book"),
                    rs.getString("name"),
                    AuthorDAO.retrieve(rs.getInt("fk_author")),
                    PublisherDAO.retrieve(rs.getInt("fk_publisher")),
                    Language.getById(rs.getInt("language")),
                    rs.getString("isbn10"),
                    rs.getString("isbn13"),
                    rs.getDate("date_publication"),
                    Genre.getById(rs.getInt("genre")),
                    rs.getInt("available_quantity")
            ));
        } while (rs.next());
        return aux;
    }

    public static ArrayList<Book> retrieveAll() throws SQLException {
        ArrayList<Book> aux = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM books");
        if (!rs.next()) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        do {
            aux.add(new Book(
                    rs.getInt("pk_book"),
                    rs.getString("name"),
                    AuthorDAO.retrieve(rs.getInt("fk_author")),
                    PublisherDAO.retrieve(rs.getInt("fk_publisher")),
                    Language.getById(rs.getInt("language")),
                    rs.getString("isbn10"),
                    rs.getString("isbn13"),
                    rs.getDate("date_publication"),
                    Genre.getById(rs.getInt("genre")),
                    rs.getInt("available_quantity")
            ));
        } while (rs.next());
        return aux;
    }

    public static void update(Book book) throws SQLException {
        if (book.getId() == 0) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("UPDATE books SET "
                + "name=?, "
                + "fk_author=?, "
                + "fk_publisher=?, "
                + "language=?, "
                + "isbn10=?, "
                + "isbn13=?, "
                + "date_publication=?, "
                + "genre=?, "
                + "available_quantity=?, "
                + "date_hour_alteration=?, "
                + "fk_user_who_altered=? "
                + "WHERE pk_book=?");
        stm.setString(1, book.getName());
        stm.setInt(2, book.getAuthor().getId());
        stm.setInt(3, book.getPublisher().getId());
        stm.setInt(4, book.getLanguage().getId());
        stm.setString(5, book.getIsbn10());
        stm.setString(6, book.getIsbn13());
        stm.setDate(7, (java.sql.Date) book.getDatePublication());
        stm.setInt(8, book.getGenre().getId());
        stm.setInt(9, book.getAvailableQuantity());
        stm.setTimestamp(10, DBConfig.now(), DBConfig.TZ_UTC);
        stm.setInt(11, DBConfig.userLogged.getId());
        stm.setInt(12, book.getId());
        stm.execute();
        stm.close();
    }

    public static void updateExcluded(Book book) throws SQLException {
        if (book.getId() == 0) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("UPDATE books SET "
                + "excluded=?, "
                + "date_hour_deletion=?, "
                + "fk_user_who_deleted=? "
                + "WHERE pk_book=?");
        stm.setBoolean(1, true);
        stm.setTimestamp(2, DBConfig.now(), DBConfig.TZ_UTC);
        stm.setInt(3, DBConfig.userLogged.getId());
        stm.setInt(4, book.getId());
        stm.execute();
        stm.close();
        book.setExcluded(true);
    }
    
    public static void delete(Book book) throws SQLException {
        if (book.getId() == 0) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("DELETE FROM books WHERE pk_book=?");
        stm.setInt(1, book.getId());
        stm.execute();
        stm.close();
    }
    
}
