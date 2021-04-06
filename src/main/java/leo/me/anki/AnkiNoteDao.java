package leo.me.anki;

import static java.lang.String.format;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AnkiNoteDao {

    private Connection connection;

    public AnkiNoteDao(String collectionFilePath) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + collectionFilePath);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Cannot open file:" + collectionFilePath, e);
        }
    }

    public List<String> findNotes(String ...words) {
        if (words == null || words.length == 0) {
            throw new IllegalArgumentException("words can not be null or empty.");
        }

        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(
                    format("select flds from notes where lower(sfld) in ('%s')",
                            Arrays.stream(words)
                                    .map(String::trim)
                                    .collect(Collectors.joining("','"))));
            List<String> notes = new LinkedList<>();
            while(resultSet.next()) {
                notes.add(resultSet.getString("flds"));
            }
            return notes;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query Anki database.", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw new IllegalStateException("can not close statement.", e);
                }
            }

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    throw new IllegalStateException("can not close result set.", e);
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection.close();
    }
}
