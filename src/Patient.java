import java.util.ArrayList;
import java.util.List;

public class Patient extends Person {
    private String insuranceProvider;
    private List<Log> medicalHistory;

    public Patient(String name, String personalId, String email, String phone, String insuranceProvider) {
        super(name, personalId, email, phone);
        this.insuranceProvider = insuranceProvider;
        this.medicalHistory = new ArrayList<>();

    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public List<Log> getMedicalHistory() {
        return medicalHistory;
    }

    public void addLog(Log logEntry) {
        medicalHistory.add(logEntry);
    }

    @Override
    public String toString() {
        return super.toString() + ", Insurance: " + insuranceProvider + ", History: " + medicalHistory;
    }
}
