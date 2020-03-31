package controller.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.LegalPerson;
import model.Phone;
import model.dao.DBConfig;
import model.dao.DBConnection;

public class LegalPersonDAO {

    public static int create(LegalPerson legalPerson) throws SQLException {
        int fkAdress = AdressDAO.create(legalPerson.getAdress());
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("INSERT INTO legal_persons "
                + "(company_name, "
                + "trading_name, "
                + "cnpj, "
                + "fk_adress, "
                + "email, "
                + "date_hour_inclusion, "
                + "fk_user_who_included, "
                + "excluded) "
                + "VALUES(?,?,?,?,?,?,?,?)",
                PreparedStatement.RETURN_GENERATED_KEYS
        );
        stm.setString(1, legalPerson.getCompanyName());
        stm.setString(2, legalPerson.getTradingName());
        stm.setString(3, legalPerson.getCnpj());
        stm.setInt(4, fkAdress);
        stm.setString(5, legalPerson.getEmail());
        stm.setTimestamp(6, DBConfig.now(), DBConfig.tzUTC);
        stm.setInt(7, DBConfig.idUserLogged);
        stm.setBoolean(8, legalPerson.isExcluded());
        stm.execute();
        ResultSet rs = stm.getGeneratedKeys();
        rs.next();
        legalPerson.setId(rs.getInt("pk_legal_person"));
        legalPerson.getPhones().forEach(phone -> {
            try {
                PhoneDAO.create(phone, legalPerson.getId(), 2);
                        } catch (SQLException ex) {
                Logger.getLogger(LegalPersonDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return legalPerson.getId();
    }

    public static LegalPerson retrieveExcluded(int pkLegalPerson, boolean excluded) throws SQLException {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("SELECT * FROM legal_persons WHERE pk_legal_person=? AND excluded=?");
        stm.setInt(1, pkLegalPerson);
        stm.setBoolean(2, excluded);
        stm.execute();
        ResultSet rs = stm.getResultSet();
        if (!rs.next()) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        LegalPerson aux = new LegalPerson(
                rs.getInt("pk_legal_person"),
                rs.getString("company_name"),
                rs.getString("trading_name"),
                rs.getString("cnpj"),
                rs.getString("email")
        );
        aux.setAdress(AdressDAO.retrieveExcluded(pkLegalPerson, excluded));
        ArrayList<Phone> auxPhones = new ArrayList<>();
        auxPhones = PhoneDAO.retrieveAllForEntityPerson(pkLegalPerson, 2);
        auxPhones.forEach(phone -> {
            aux.addPhone(phone);
        });
        return aux;
    }

    public static LegalPerson retrieve(int pkLegalPerson) throws SQLException {
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("SELECT * FROM legal_persons WHERE pk_legal_person=?");
        stm.setInt(1, pkLegalPerson);
        stm.execute();
        ResultSet rs = stm.getResultSet();
        if (!rs.next()) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        LegalPerson aux = new LegalPerson(
                rs.getInt("pk_legal_person"),
                rs.getString("company_name"),
                rs.getString("trading_name"),
                rs.getString("cnpj"),
                rs.getString("email")
        );
        aux.setAdress(AdressDAO.retrieve(pkLegalPerson));
        ArrayList<Phone> auxPhones = new ArrayList<>();
        auxPhones = PhoneDAO.retrieveAllForEntityPerson(pkLegalPerson, 2);
        auxPhones.forEach(phone -> {
            aux.addPhone(phone);
        });
        return aux;
    }

    public static ArrayList<LegalPerson> retrieveAllExcluded(boolean excluded) throws SQLException {
        ArrayList<LegalPerson> aux = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM legal_persons WHERE excluded=" + excluded);
        if (!rs.next()) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        do {
            LegalPerson temp = new LegalPerson(
                    rs.getInt("pk_legal_person"),
                    rs.getString("company_name"),
                    rs.getString("trading_name"),
                    rs.getString("cnpj"),
                    rs.getString("email")
            );
            temp.setAdress(AdressDAO.retrieve(rs.getInt("pk_legal_person")));
            ArrayList<Phone> auxPhones = new ArrayList<>();
            auxPhones = PhoneDAO.retrieveAllForEntityPerson(temp.getId(), 2);
            auxPhones.forEach(phone -> {
                temp.addPhone(phone);
            });
            aux.add(temp);
        } while (rs.next());
        return aux;
    }

    public static ArrayList<LegalPerson> retrieveAll() throws SQLException {
        ArrayList<LegalPerson> aux = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM legal_persons");
        if (!rs.next()) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        do {
            LegalPerson temp = new LegalPerson(
                    rs.getInt("pk_legal_person"),
                    rs.getString("company_name"),
                    rs.getString("trading_name"),
                    rs.getString("cnpj"),
                    rs.getString("email")
            );
            temp.setAdress(AdressDAO.retrieve(rs.getInt("pk_legal_person")));
            ArrayList<Phone> auxPhones = new ArrayList<>();
            auxPhones = PhoneDAO.retrieveAllForEntityPerson(temp.getId(), 2);
            auxPhones.forEach(phone -> {
                temp.addPhone(phone);
            });
            aux.add(temp);
        } while (rs.next());
        return aux;
    }

    public static void update(LegalPerson legalPerson) throws SQLException {
        if (legalPerson.getId() == 0) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        legalPerson.getPhones().forEach(phone -> {
            try {
                PhoneDAO.update(phone, legalPerson.getId(), 2);
            } catch (SQLException ex) {
                Logger.getLogger(LegalPersonDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        AdressDAO.update(legalPerson.getAdress());
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("UPDATE legal_persons SET "
                + "company_name=?, "
                + "trading_name=?, "
                + "cnpj=?, "
                + "fk_adress=?, "
                + "email=?, "
                + "date_hour_alteration=?, "
                + "fk_user_who_altered=? "
                + "WHERE pk_legal_person=?");
        stm.setString(1, legalPerson.getCompanyName());
        stm.setString(2, legalPerson.getTradingName());
        stm.setString(3, legalPerson.getCnpj());
        stm.setInt(4, legalPerson.getAdress().getId());
        stm.setString(5, legalPerson.getEmail());
        stm.setTimestamp(6, DBConfig.now(), DBConfig.tzUTC);
        stm.setInt(7, DBConfig.idUserLogged);
        stm.setInt(8, legalPerson.getId());
        stm.execute();
        stm.close();
    }

    public static void updateExcluded(LegalPerson legalPerson) throws SQLException {
        if (legalPerson.getId() == 0) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        legalPerson.getPhones().forEach(phone -> {
            try {
                PhoneDAO.updateExcluded(phone, 2);
            } catch (SQLException ex) {
                Logger.getLogger(LegalPersonDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        AdressDAO.updateExcluded(legalPerson.getAdress());
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("UPDATE legal_persons SET "
                + "excluded=?, "
                + "date_hour_deletion=?, "
                + "fk_user_who_deleted=?, "
                + "WHERE pk_legal_person=?");
        stm.setBoolean(1, true);
        stm.setTimestamp(2, DBConfig.now(), DBConfig.tzUTC);
        stm.setInt(3, DBConfig.idUserLogged);
        stm.setInt(4, legalPerson.getId());
        stm.execute();
        stm.close();
        legalPerson.setExcluded(true);
    }

    public static void delete(LegalPerson legalPerson) throws SQLException {
        if (legalPerson.getId() == 0) {
            throw new SQLException("Objeto não persistido ainda ou com a chave primária não configurada!");
        }
        legalPerson.getPhones().forEach(phone -> {
            try {
                PhoneDAO.delete(phone, 2);
            } catch (SQLException ex) {
                Logger.getLogger(LegalPersonDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        Connection conn = DBConnection.getConnection();
        PreparedStatement stm = conn.prepareStatement("DELETE FROM legal_persons WHERE pk_legal_person=?");
        stm.setInt(1, legalPerson.getId());
        stm.execute();
        stm.close();
    }

}
