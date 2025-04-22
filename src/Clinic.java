import java.util.ArrayList;
import java.util.List;

public class Clinic {
    private String name;
    private String address;
    private List<Doctor> doctors;
    private List<Service> services;
    private List<Patient> patients;
    private List<Appointment> appointments;

    public Clinic(String name, String address) {
        this.name = name;
        this.address = address;
        this.doctors = new ArrayList<>();
        this.services = new ArrayList<>();
        this.patients = new ArrayList<>();
        this.appointments= new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
    }

    public List<Service> getServices() {
        return services;
    }

    public void addService(Service service) {
        services.add(service);
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void addPatient(Patient patient) {
        if (!patients.contains(patient)) {
            patients.add(patient);
        }
    }
    public List<Appointment> getAppointments(){
        return appointments;
    }
    public void addAppointment(Appointment appointment)
    {
        appointments.add(appointment);
    }
    public List<Appointment> getDoctorAppointment(Doctor doctor){
        ArrayList<Appointment> appointments = null;
        for(int i=0;i<this.appointments.size();i++)
        {
            if(this.appointments.get(i).getDoctor() == doctor)
                appointments.add(this.appointments.get(i));
        }
        return appointments;
    }


    @Override
    public String toString() {
        return "Clinic: " + name + ", Address: " + address +
                ", Doctors: " + doctors.size() +
                ", Services: " + services.size() +
                ", Patients: " + patients.size();
    }
}
