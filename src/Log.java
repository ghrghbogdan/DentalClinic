import java.time.LocalDate;

public class Log {
    private String clinicName;           // or Cabinet name
    private String serviceName;          // type of treatment
    private Doctor doctor;               // doctor who performed the treatment
    private LocalDate date;              // when it was done

    public Log(String clinicName, String serviceName, Doctor doctor, LocalDate date) {
        this.clinicName = clinicName;
        this.serviceName = serviceName;
        this.doctor = doctor;
        this.date = date;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Clinic: " + clinicName + ", Service: " + serviceName + ", Doctor: " + doctor.getName() + ", Date: " + date;
    }
}
