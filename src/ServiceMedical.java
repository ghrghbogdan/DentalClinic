import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMedical extends DatabaseCRUD<Service> {
    private static ServiceMedical instance;
    private final AuditService auditService;

    private ServiceMedical() {
        super();
        this.auditService = AuditService.getInstance();
    }

    public static ServiceMedical getInstance() {
        if (instance == null) {
            instance = new ServiceMedical();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "services";
    }

    @Override
    protected Service mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new Service(
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getInt("durationInMinutes")
        );
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Service service) throws SQLException {
        stmt.setString(1, service.getName());
        stmt.setDouble(2, service.getPrice());
        stmt.setInt(3, (int) service.getDurationInMinutes().toMinutes());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Service service) throws SQLException {
        setInsertParameters(stmt, service);
    }

    @Override
    protected String getInsertPlaceholders() {
        return "?, ?, ?";
    }

    @Override
    protected String getUpdateFields() {
        return "name = ?, price = ?, durationInMinutes = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 3;
    }

    // Insert service and link to clinic
    public void createWithClinic(Service service, String clinicName) throws SQLException {
        // Insert service
        String insertServiceSql = "INSERT INTO services (name, price, durationInMinutes) VALUES (?, ?, ?)";
        int serviceId = -1;
        try (PreparedStatement stmt = connection.prepareStatement(insertServiceSql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(stmt, service);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                serviceId = rs.getInt(1);
            }
        }

        // Get clinic ID
        int clinicId = -1;
        String getClinicIdSql = "SELECT id FROM clinics WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(getClinicIdSql)) {
            stmt.setString(1, clinicName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                clinicId = rs.getInt("id");
            }
        }

        // Insert into clinicservices
        if (serviceId != -1 && clinicId != -1) {
            String insertClinicServiceSql = "INSERT INTO clinicservices (clinicId, serviceId) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertClinicServiceSql)) {
                stmt.setInt(1, clinicId);
                stmt.setInt(2, serviceId);
                stmt.executeUpdate();
            }
        }

        auditService.logAction("Added service: " + service.getName() + " to clinic: " + clinicName);
    }
    public List<Service> getServicesByClinic(String clinicName) throws SQLException {
        String sql = "SELECT s.* FROM services s " +
                "JOIN clinicservices cs ON s.id = cs.serviceId " +
                "JOIN clinics c ON cs.clinicId = c.id " +
                "WHERE c.name = ?";
        List<Service> services = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, clinicName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                services.add(mapResultSetToEntity(rs));
            }
        }
        return services;
    }
    public void updateServicePriceForClinic(String serviceName, String clinicName, double newPrice) throws SQLException {
        String sql = "UPDATE services s " +
                "JOIN clinicServices cs ON s.id = cs.serviceId " +
                "JOIN clinics c ON cs.clinicId = c.id " +
                "SET s.price = ? " +
                "WHERE s.name = ? AND c.name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, newPrice);
            stmt.setString(2, serviceName);
            stmt.setString(3, clinicName);
            stmt.executeUpdate();
        }
    }

    // Delete a service for a specific clinic
    public void deleteServiceForClinic(String serviceName, String clinicName) throws SQLException {
        // First, remove the relation from clinic_services
        String sql = "DELETE cs FROM clinicServices cs " +
                "JOIN services s ON cs.serviceId = s.id " +
                "JOIN clinics c ON cs.clinicId = c.id " +
                "WHERE s.name = ? AND c.name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, serviceName);
            stmt.setString(2, clinicName);
            stmt.executeUpdate();
        }
    }

}