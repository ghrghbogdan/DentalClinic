import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClinicService {
    private ClinicNetwork network;
    private List<Appointment> appointments;
    private List<Bill> bills;

    public ClinicService(ClinicNetwork network) {
        this.network = network;
        this.appointments = new ArrayList<>();
        this.bills = new ArrayList<>();
    }

    public void addClinic(Clinic clinic) {
        network.addClinic(clinic);
    }

    public Clinic findClinicByName(String name) {
        return network.findClinicByName(name);
    }

    public void scheduleAppointment(Patient patient, Doctor doctor, Clinic clinic, Service service, LocalDateTime dateTime) {
        Appointment appointment = new Appointment(patient, doctor, clinic, service, dateTime);
        appointments.add(appointment);
        clinic.addPatient(patient);

        // Generate Bill
        Bill bill = new Bill(appointment);
        bills.add(bill);

        // Add log to patient history
        Log log = new Log(clinic.getName(), service.getName(), doctor, dateTime.toLocalDate());
        patient.addMedicalHistory(log);

        System.out.println("Appointment scheduled:\n" + appointment);
        System.out.println("Bill generated:\n" + bill);
    }

    public void showAllAppointments() {
        for (Appointment a : appointments) {
            System.out.println(a);
            System.out.println("--------");
        }
    }
    public void showAppointmentsByDoctor(String name){
        for (Appointment a : appointments) {
            if(a.getDoctor().getName().equals(name))
            {
                System.out.println(a);
                System.out.println("--------");
            }
        }

    }
    public void showAppointmentsByClinic(String name){
        for (Appointment a : appointments) {
            if(a.getClinic().getName().equals(name))
            {
                System.out.println(a);
                System.out.println("--------");
            }
        }

    }
    public void showAppointmentsByPatient(String name){
        for (Appointment a : appointments) {
            if(a.getPatient().getName().equals(name))
            {
                System.out.println(a);
                System.out.println("--------");
            }
        }

    }
    public void showAllDoctors(){
        for(Clinic cl : network.getClinics()){
            System.out.println(cl.getName());
            System.out.println("--------");
            System.out.println();
            for(Doctor doc : cl.getDoctors()){
                System.out.println(doc.getName());
            }
        }
    }
    public void showAllPatients(){
        for(Clinic cl : network.getClinics()){
            System.out.println(cl.getName());
            System.out.println("--------");
            System.out.println();
            for(Patient pat : cl.getPatients()){
                System.out.println(pat.getName());
            }
        }
    }
    public void showAllClinics(){
        for(Clinic cl : network.getClinics()){
            System.out.println(cl.getName());
            System.out.println("--------");
        }
    }
    public void showMedicalHistory(String name){
        for(Clinic cl : network.getClinics()){
            for(Patient pat : cl.getPatients()){
                System.out.println(pat.getMedicalHistory());
            }
        }
    }

    public void addPatientToClinic(Patient patient, String clinic){
        for(Clinic cl : network.getClinics()){
            if(cl.getName().equals(clinic)){
                cl.addPatient(patient);
                break;
            }
        }
    }
    public void scheduleAppointmentByNames(String patientName, String doctorName, String clinicName, String serviceName, LocalDateTime dateTime) {
        // Find the clinic
        Clinic clinic = null;
        for (Clinic c : network.getClinics()) {
            if (c.getName().equalsIgnoreCase(clinicName)) {
                clinic = c;
                break;
            }
        }

        if (clinic == null) {
            System.out.println("Clinic not found.");
            return;
        }

        // Find the doctor in the clinic
        Doctor doctor = null;
        for (Doctor d : clinic.getDoctors()) {
            if (d.getName().equalsIgnoreCase(doctorName)) {
                doctor = d;
                break;
            }
        }

        if (doctor == null) {
            System.out.println("Doctor not found in this clinic.");
            return;
        }

        // Find the patient in the clinic
        Patient patient = null;
        for (Patient p : clinic.getPatients()) {
            if (p.getName().equalsIgnoreCase(patientName)) {
                patient = p;
                break;
            }
        }

        if (patient == null) {
            System.out.println("Patient not found in this clinic.");
            return;
        }

        // Find the service in the clinic
        Service service = null;
        for (Service s : clinic.getServices()) {
            if (s.getName().equalsIgnoreCase(serviceName)) {
                service = s;
                break;
            }
        }

        if (service == null) {
            System.out.println("Service not available in this clinic.");
            return;
        }

        // Create the appointment
        Appointment appointment = new Appointment(patient, doctor, clinic, service, dateTime);
        appointments.add(appointment);

        // Add the log entry to patient's medical history
        Log log = new Log(clinic.getName(), service.getName(), doctor, dateTime.toLocalDate());
        patient.getMedicalHistory().add(log);

        System.out.println("Appointment successfully scheduled!");
    }


    public void createBillForAppointment(int targetAppointment) {
        // Check if the appointment exists in the list
        Appointment app =null;
        boolean found = false;
        for (Appointment a : appointments) {
            if (a.getId()==targetAppointment) {
                app = a;
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Appointment not found in the system.");
            return;
        }

        // Create the bill using the simplified constructor
        Bill bill = new Bill(app);
        bills.add(bill);

        System.out.println("✅ Bill created successfully:");
        System.out.println("Amount: " + bill.getTotalAmount() + " RON");
        System.out.println("Patient: " + app.getPatient().getName());
        System.out.println("Service: " + app.getService().getName());
    }


    public void showAllBills(){
        for (Bill b : bills) {
            System.out.println(b);
            System.out.println("--------");
        }
    }
}
