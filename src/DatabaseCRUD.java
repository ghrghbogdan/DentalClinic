import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DatabaseCRUD<T> {
    protected Connection connection;
    protected DatabaseCRUD() {
        this.connection = DatabaseConnection.getConnection();
    }

    // Metode abstracte
    protected abstract String getTableName();
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;

    // CREATE
    public void create(T entity) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + " VALUES (" + getInsertPlaceholders() + ")";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(stmt, entity);
            stmt.executeUpdate();
        }
    }

    // READ (by ID)
    public T read(int id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapResultSetToEntity(rs) : null;
        }
    }

    // UPDATE
    public void update(T entity) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " + getUpdateFields() + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setUpdateParameters(stmt, entity);
            stmt.executeUpdate();
        }
    }

    // DELETE
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // READ ALL
    public List<T> readAll() throws SQLException {
        List<T> entities = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        }
        return entities;
    }

    // Metode ajutătoare
    protected abstract String getInsertPlaceholders();
    protected abstract String getUpdateFields();
    protected abstract int getUpdateParametersCount();
}