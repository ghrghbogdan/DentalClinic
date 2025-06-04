import java.time.LocalDate;

public class Bill {
    private Appointment appointment;
    private double totalAmount;
    private LocalDate issueDate;
    private boolean paid;

    public Bill(Appointment appointment) {
        this.appointment = appointment;
        this.totalAmount = appointment.getService().getPrice();
        this.issueDate = LocalDate.now();
        this.paid = false;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public boolean isPaid() {
        return paid;
    }

    public void markAsPaid() {
        this.paid = true;
    }

    @Override
    public String toString() {
        return "Bill for " + appointment.getPatient().getName() +
                "\nService: " + appointment.getService().getName() +
                "\nAmount: $" + totalAmount +
                "\nDate: " + issueDate ;
    }
}
