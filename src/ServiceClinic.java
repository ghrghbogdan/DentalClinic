import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceClinic extends DatabaseCRUD<Clinic> {
    private static ServiceClinic instance;
    private final AuditService auditService;

    private ServiceClinic() {
        super();
        this.auditService = AuditService.getInstance();
    }

    public static ServiceClinic getInstance() {
        if (instance == null) {
            instance = new ServiceClinic();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "clinics";
    }

    @Override
    protected Clinic mapResultSetToEntity(ResultSet rs) throws SQLException {
        Clinic clinic = new Clinic(rs.getString("name"), rs.getString("address"));
        return clinic;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Clinic clinic) throws SQLException {
        stmt.setString(1, null);
        stmt.setString(2, clinic.getName());
        stmt.setString(3, clinic.getAddress());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Clinic clinic) throws SQLException {
        stmt.setString(1, null);
        stmt.setString(2, clinic.getName());
        stmt.setString(3, clinic.getAddress());
    }

    @Override
    protected String getInsertPlaceholders() {
        return "?, ?, ?";
    }

    @Override
    protected String getUpdateFields() {
        return "id= ?, name = ?, address = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 3;
    }

    public List<Clinic> getClinicsByDoctor(int doctorId) throws SQLException {
        String sql = "SELECT c.* FROM clinics c JOIN doctors d ON c.id = d.clinicId WHERE d.id = ?";
        List<Clinic> clinics = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                clinics.add(mapResultSetToEntity(rs));
            }
        }
        return clinics;
    }
}