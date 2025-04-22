import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    private Patient patient;
    private Doctor doctor;
    private Clinic clinic;
    private Service service;
    private LocalDateTime dateTime;

    public Appointment(Patient patient, Doctor doctor, Clinic clinic, Service service, LocalDateTime dateTime) {
        this.patient = patient;
        this.doctor = doctor;
        this.clinic = clinic;
        this.service = service;
        this.dateTime = dateTime;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public Service getService() {
        return service;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "Appointment at " + clinic.getName() +
                "\nPatient: " + patient.getName() +
                "\nDoctor: " + doctor.getName() +
                "\nService: " + service.getName() +
                "\nDate: " + dateTime;
    }
}
