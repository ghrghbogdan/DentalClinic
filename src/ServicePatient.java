import java.sql.*;
public class ServicePatient extends DatabaseCRUD<Patient> {
    private static ServicePatient instance;
    private final AuditService auditService;

    private ServicePatient() {
        super(); // Initialize connection from parent class
        this.auditService = AuditService.getInstance();
    }

    public static ServicePatient getInstance() {
        if (instance == null) {
            instance = new ServicePatient();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "patients";
    }

    @Override
    protected Patient mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getString("name"),
                rs.getString("personalId"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("insuranceProvider")
        );
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Patient patient) throws SQLException {
        stmt.setString(1, patient.getName());
        stmt.setString(2, patient.getPersonalId());
        stmt.setString(3, patient.getEmail());
        stmt.setString(4, patient.getPhone());
        stmt.setString(5, patient.getInsuranceProvider());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Patient patient) throws SQLException {
        setInsertParameters(stmt, patient);
    }

    @Override
    protected String getInsertPlaceholders() {
        return "?, ?, ?, ?, ?";
    }

    @Override
    protected String getUpdateFields() {
        return "name = ?, personalId = ?, email = ?, phone = ?, insuranceProvider = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 5;
    }

    protected Patient read(String id) throws SQLException {
        String query = "SELECT * FROM " + getTableName() + " WHERE personalId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            } else {
                return null;
            }
        }
    }

}