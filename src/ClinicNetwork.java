import java.util.ArrayList;
import java.util.List;

public class ClinicNetwork {
    private String networkName;
    private List<Clinic> clinics;

    public ClinicNetwork(String networkName) {
        this.networkName = networkName;
        this.clinics = new ArrayList<>();
    }

    public String getNetworkName() {
        return networkName;
    }

    public List<Clinic> getClinics() {
        return clinics;
    }

    public void addClinic(Clinic clinic) {
        clinics.add(clinic);
    }

    public void removeClinic(Clinic clinic) {
        clinics.remove(clinic);
    }

    public Clinic findClinicByName(String name) {
        for (Clinic clinic : clinics) {
            if (clinic.getName().equalsIgnoreCase(name)) {
                return clinic;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Clinic Network: " + networkName + ", Total Clinics: " + clinics.size();
    }
}
