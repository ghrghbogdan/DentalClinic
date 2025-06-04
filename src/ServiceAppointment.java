import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceAppointment extends DatabaseCRUD<Appointment> {
    private static ServiceAppointment instance;
    private final AuditService auditService;

    private ServiceAppointment() {
        super(); // Call parent constructor to initialize connection
        this.auditService = AuditService.getInstance();
    }

    public static ServiceAppointment getInstance() {
        if (instance == null) {
            instance = new ServiceAppointment();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "appointments";
    }

    @Override
    protected Appointment mapResultSetToEntity(ResultSet rs) throws SQLException {

        String patientId = rs.getString("patientId");
        String doctorId = rs.getString("doctorId");
        int clinicId = rs.getInt("clinicId");
        int serviceId = rs.getInt("serviceId");
        LocalDateTime dateTime = rs.getTimestamp("dateTime").toLocalDateTime();

        // Fetch related entities
        ServicePatient servicePatient = ServicePatient.getInstance();
        ServiceDoctor serviceDoctor = ServiceDoctor.getInstance();
        ServiceClinic serviceClinic = ServiceClinic.getInstance();
        ServiceMedical serviceMedical = ServiceMedical.getInstance();

        Patient patient = servicePatient.read(patientId);
        Doctor doctor = serviceDoctor.read(doctorId);
        Clinic clinic = serviceClinic.read(clinicId);
        Service service = serviceMedical.read(serviceId);

        return new Appointment(patient, doctor, clinic, service, dateTime);
    }
    @Override
    public void create(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO appointments (patientId, doctorId, clinicId, serviceId, dateTime) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(stmt, appointment);
            stmt.executeUpdate();

            auditService.logAction("Created appointment for patient: " + appointment.getPatient().getName());
        }
    }
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Appointment appointment) throws SQLException {
        stmt.setString(1, appointment.getPatient().getPersonalId());
        stmt.setString(2, appointment.getDoctor().getPersonalId());
        stmt.setInt(3, getClinicId(appointment.getClinic()));
        stmt.setInt(4, getServiceId(appointment.getService()));
        stmt.setTimestamp(5, Timestamp.valueOf(appointment.getDateTime()));
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Appointment appointment) throws SQLException {
        setInsertParameters(stmt, appointment);
    }

    @Override
    protected String getInsertPlaceholders() {
        return "?, ?, ?, ?, ?";
    }

    @Override
    protected String getUpdateFields() {
        return "id = ?, patientId = ?, doctorId = ?, clinicId = ?, serviceId = ?, dateTime = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 7;
    }

    // Helper methods to get IDs for related entities
    private int getClinicId(Clinic clinic) throws SQLException {
        // Implementation to get clinic ID from database
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

    private int getServiceId(Service service) throws SQLException {
        // Implementation to get service ID from database
        String sql = "SELECT id FROM services WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, service.getName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    // Find appointments by patient
    public List<Appointment> findByPatient(Patient patient) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE patientId = ?";
        System.out.println(patient.getPersonalId());
        List<Appointment> appointments = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getPersonalId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                appointments.add(mapResultSetToEntity(rs));
            }
        }
        auditService.logAction("Retrieved appointments for patient: " + patient.getName());
        return appointments;
    }

    // Find appointments by doctor
    public List<Appointment> findByDoctor(Doctor doctor) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE doctorId = ?";
        List<Appointment> appointments = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, doctor.getPersonalId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                appointments.add(mapResultSetToEntity(rs));
            }
        }
        auditService.logAction("Retrieved appointments for doctor: " + doctor.getName());
        return appointments;
    }

    @Override
    public List<Appointment> readAll() throws SQLException {
        String sql = "SELECT * FROM appointments";
        List<Appointment> appointments = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                appointments.add(mapResultSetToEntity(rs));
            }
        }

        auditService.logAction("Retrieved all appointments");
        return appointments;
    }

}