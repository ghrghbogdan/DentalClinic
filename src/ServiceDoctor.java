import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDoctor extends DatabaseCRUD<Doctor> {
    private static ServiceDoctor instance;
    private final AuditService auditService;

    private ServiceDoctor() {
        super();
        this.auditService = AuditService.getInstance();
    }

    public static ServiceDoctor getInstance() {
        if (instance == null) {
            instance = new ServiceDoctor();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "doctors";
    }

    @Override
    protected Doctor mapResultSetToEntity(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String personalId = rs.getString("personalId");
        String email = rs.getString("email");
        String phone = rs.getString("phone");

        // Get the clinic
        ServiceClinic clinicService = ServiceClinic.getInstance();
        Clinic clinic = clinicService.read(rs.getInt("clinicId"));

        String specialization = rs.getString("specialization");
        int yearsOfExperience = rs.getInt("yearsOfExperience");

        Doctor doctor = new Doctor(name, personalId, email, phone, clinic, specialization, yearsOfExperience);
        return doctor;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Doctor doctor) throws SQLException {
        stmt.setString(1, doctor.getName());
        stmt.setString(2, doctor.getPersonalId());
        stmt.setString(3, doctor.getEmail());
        stmt.setString(4, doctor.getPhone());

        // Get clinic ID
        if (doctor.getClinic() != null) {
            int clinicId = getClinicId(doctor.getClinic());
            stmt.setInt(5, clinicId);
        } else {
            stmt.setNull(5, Types.INTEGER);
        }

        stmt.setString(6, doctor.getSpecialization());
        stmt.setInt(7, doctor.getYearsOfExperience());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Doctor doctor) throws SQLException {
        setInsertParameters(stmt, doctor);
        stmt.setString(8, doctor.getPersonalId()); // For WHERE clause
    }

    @Override
    protected String getInsertPlaceholders() {
        return "?, ?, ?, ?, ?, ?, ?";
    }

    @Override
    protected String getUpdateFields() {
        return "name = ?, personalId = ?, email = ?, phone = ?, clinicId = ?, specialization = ?, yearsOfExperience = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 8;
    }

    // Helper method to get clinic ID
    private int getClinicId(Clinic clinic) throws SQLException {
        String sql = "SELECT id FROM clinics WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, clinic.getName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }


    // Find doctors by clinic
    public List<Doctor> findByClinic(Clinic clinic) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE clinicId = ?";
        List<Doctor> doctors = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, getClinicId(clinic));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                doctors.add(mapResultSetToEntity(rs));
            }
        }

        auditService.logAction("Retrieved doctors for clinic: " + clinic.getName());
        return doctors;
    }

    protected Doctor read(String id) throws SQLException {
        String query = "SELECT * FROM " + getTableName() + " WHERE personalId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            } else {
                return null; // or throw an exception if not found
            }
        }
    }
}