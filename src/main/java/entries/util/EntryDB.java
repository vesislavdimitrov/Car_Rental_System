package entries.util;

import entries.Entry;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/* DAO CLASS */
public class EntryDB {

    private String SQL_USERNAME;
    private String SQL_PASSWORD;
    private String SQL_URL;
    Connection connection;

    EntryDB() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("db.properties"));
            SQL_USERNAME = props.getProperty("SQL_USERNAME");
            SQL_PASSWORD = props.getProperty("SQL_PASSWORD");
            SQL_URL = props.getProperty("SQL_URL");

            connection = DriverManager.getConnection(SQL_URL, SQL_USERNAME, SQL_PASSWORD);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    //C
    public void createEntry(Entry entry) {
        String sql = "INSERT INTO entries(first_name, last_name, birth_date, email_address) VALUES(?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entry.getFirstName());
            statement.setString(2, entry.getLastName());
            statement.setDate(3, java.sql.Date.valueOf(entry.getBirthDate()));
            statement.setString(4, entry.getEMail());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //R
    public List<Entry> readEntries() {
        String sql = "SELECT * FROM entries";
        List<Entry> entries = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                Entry entry = new Entry.Builder()
                .setFirstName(resultSet.getString("first_name"))
                .setLastName(resultSet.getString("last_name"))
                .setBirthDate(resultSet.getDate("birth_date").toLocalDate())
                .setEMail(resultSet.getString("email_address")).build();
                entries.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    //U
    public void updateEntry(String entryEmail, int propertyIndex, String updatedValue) {
        String sql = "UPDATE entries SET ";
        switch (propertyIndex) {
            case 1 -> sql += "first_name=?";
            case 2 -> sql += "last_name=?";
            case 3 -> sql += "birth_date=?";
            case 4 -> sql += "email_address=?";
            default -> {
                System.err.println("Invalid choice!");
                return;
            }
        }
        sql += " WHERE email_address=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            switch (propertyIndex) {
                case 1, 2, 4 -> statement.setString(1, updatedValue);
                case 3 -> statement.setDate(1, Date.valueOf(LocalDate.parse(updatedValue)));
            }
            statement.setString(2, entryEmail);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //D
    public void deleteEntry(String email) {
        String sql = "DELETE FROM entries WHERE email_address=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
