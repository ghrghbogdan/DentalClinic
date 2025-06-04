import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceLog extends DatabaseCRUD<Log> {
    private static ServiceLog instance;
    private final AuditService auditService;

    private ServiceLog() {
        super();
        this.auditService = AuditService.getInstance();
    }

    public static ServiceLog getInstance() {
        if (instance == null) {
            instance = new ServiceLog();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "medicalLogs";
    }

    @Override
    protected Log mapResultSetToEntity(ResultSet rs) throws SQLException {
        String clinicName = rs.getString("clinicName");
        String serviceName = rs.getString("serviceName");

        // Get the doctor
        ServiceDoctor doctorService = ServiceDoctor.getInstance();
        Doctor doctor = doctorService.read(rs.getString("doctorId"));

        LocalDate date = rs.getDate("date").toLocalDate();

        Log log = new Log(clinicName, serviceName, doctor, date);
        return log;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Log log) throws SQLException {
        stmt.setNull(1, Types.INTEGER);
        stmt.setString(2, log.getClinicName());
        stmt.setString(3, log.getServiceName());
        stmt.setString(4, getDoctorId(log.getDoctor()));
        stmt.setDate(5, Date.valueOf(log.getDate()));
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Log log) throws SQLException {
        setInsertParameters(stmt, log);
        // Assuming we have id field as primary key for logs
        stmt.setInt(5, getLogId(log));
    }

    @Override
    protected String getInsertPlaceholders() {
        return "?, ?, ?, ?, ?";
    }

    @Override
    protected String getUpdateFields() {
        return "id = ?, clinicName = ?, serviceName = ?, doctorId = ?, date = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 5;
    }

    protected void addPatientToLog(Log log, Patient patient) throws SQLException {
        String sql = "INSERT INTO patientLogs (logId, patientId) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, getLogId(log));
            stmt.setString(2, patient.getPersonalId());
            stmt.executeUpdate();
        }
        auditService.logAction("Added patient " + patient.getName() + " to log for " + log.getClinicName());
    }
    // Helper methods to get IDs for related entities
    private String getDoctorId(Doctor doctor) throws SQLException {
        return doctor.getPersonalId();
    }

    private int getLogId(Log log) throws SQLException {
        // This method would find the ID of a log entry based on its properties
        String sql = "SELECT id FROM medicalLogs WHERE clinicName = ? AND serviceName = ? AND doctorId = ? AND date = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, log.getClinicName());
            stmt.setString(2, log.getServiceName());
            stmt.setString(3, getDoctorId(log.getDoctor()));
            stmt.setDate(4, Date.valueOf(log.getDate()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }
    // Find logs by patient
    public List<Log> findByPatient(Patient patient) throws SQLException {
        String sql = "SELECT l.* FROM medicalLogs l " +
                "JOIN patientLogs pl ON l.id = pl.logId " +
                "WHERE pl.patientId = ?";

        List<Log> logs = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getPersonalId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToEntity(rs));
            }
        }

        auditService.logAction("Retrieved medical history for patient: " + patient.getName());
        return logs;
    }
}