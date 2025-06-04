import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceBill extends DatabaseCRUD<Bill> {
    private static ServiceBill instance;
    private final AuditService auditService;

    private ServiceBill() {
        super();
        this.auditService = AuditService.getInstance();
    }

    public static ServiceBill getInstance() {
        if (instance == null) {
            instance = new ServiceBill();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "bills";
    }

    @Override
    protected Bill mapResultSetToEntity(ResultSet rs) throws SQLException {
        int appointmentId = rs.getInt("appointmentId");

        // Get the appointment
        ServiceAppointment appointmentService = ServiceAppointment.getInstance();
        Appointment appointment = appointmentService.read(appointmentId);

        Bill bill = new Bill(appointment);
        bill.setTotalAmount(rs.getDouble("totalAmount"));
        bill.setIssueDate(rs.getDate("issueDate").toLocalDate());
        if (rs.getBoolean("paid")) {
            bill.markAsPaid();
        }

        return bill;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, Bill bill) throws SQLException {
        stmt.setNull(1, Types.INTEGER);
        stmt.setInt(2, getAppointmentId(bill.getAppointment()));
        stmt.setDouble(3, bill.getTotalAmount());
        stmt.setDate(4, Date.valueOf(bill.getIssueDate()));
        stmt.setBoolean(5, bill.isPaid());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Bill bill) throws SQLException {
        setInsertParameters(stmt, bill);
        stmt.setInt(6, getAppointmentId(bill.getAppointment())); // For WHERE clause using appointmentId
    }

    @Override
    protected String getInsertPlaceholders() {
        return "?, ?, ?, ?, ?";
    }

    @Override
    protected String getUpdateFields() {
        return "id = ?, appointmentId = ?, totalAmount = ?, issueDate = ?, paid = ?";
    }

    @Override
    protected int getUpdateParametersCount() {
        return 5;
    }

    // Find unpaid bills
    public List<Bill> findUnpaidBills() throws SQLException {
        String sql = "SELECT * FROM bills WHERE paid = false";
        List<Bill> bills = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bills.add(mapResultSetToEntity(rs));
            }
        }

        auditService.logAction("Retrieved unpaid bills list");
        return bills;
    }

    // Find bills by patient
    public List<Bill> findBillsByPatient(Patient patient) throws SQLException {
        String sql = "SELECT b.* FROM bills b " +
                "JOIN appointments a ON b.appointmentId = a.id " +
                "WHERE a.patientId = ?";

        List<Bill> bills = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patient.getPersonalId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bills.add(mapResultSetToEntity(rs));
            }
        }

        auditService.logAction("Retrieved bills for patient: " + patient.getName());
        return bills;
    }

    private Integer getAppointmentId(Appointment appointment) throws SQLException {
        String sql = "SELECT id FROM appointments WHERE patientId = ? AND doctorId = ? AND clinicId = ? AND dateTime = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, appointment.getPatient().getPersonalId());
            stmt.setString(2, appointment.getDoctor().getPersonalId());
            stmt.setInt(3, getClinicId(appointment.getClinic()));
            stmt.setTimestamp(4, Timestamp.valueOf(appointment.getDateTime()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return null; // Return null if no appointment found
    }
    private Integer getClinicId(Clinic clinic) throws SQLException {
        String sql = "SELECT id FROM clinics WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, clinic.getName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return null; // Return null if no clinic found
    }


}