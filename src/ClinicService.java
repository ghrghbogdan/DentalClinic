import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClinicService {
    private ClinicNetwork network;
    private List<Appointment> appointments;
    private Map<String, List<Bill>> billsByPatient; // noua structură

    public ClinicService(ClinicNetwork network) {
        this.network = network;
        this.appointments = new ArrayList<>();
        this.billsByPatient = new HashMap<>();
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

        // Generate and store bill
        createBillForAppointment(appointment);

        // Add to medical history
        Log log = new Log(clinic.getName(), service.getName(), doctor, dateTime.toLocalDate());
        patient.addMedicalHistory(log);

        System.out.println("Appointment scheduled:\n" + appointment);
    }

    public void scheduleAppointmentByNames(String patientName, String doctorName, String clinicName, String serviceName, LocalDateTime dateTime) {
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

        Appointment appointment = new Appointment(patient, doctor, clinic, service, dateTime);
        appointments.add(appointment);

        Log log = new Log(clinic.getName(), service.getName(), doctor, dateTime.toLocalDate());
        patient.getMedicalHistory().add(log);

        createBillForAppointment(appointment);

        System.out.println("Appointment successfully scheduled!");
    }

    public void createBillForAppointment(Appointment appointment) {
        Bill bill = new Bill(appointment);

        String patientName = appointment.getPatient().getName();
        billsByPatient.computeIfAbsent(patientName, k -> new ArrayList<>()).add(bill);

        System.out.println("=== BILL CREATED ===");
        System.out.println("Patient: " + patientName);
        System.out.println("Service: " + appointment.getService().getName());
        System.out.println("Clinic: " + appointment.getClinic().getName());
        System.out.println("Doctor: " + appointment.getDoctor().getName());
        System.out.println("Date: " + appointment.getDateTime());
        System.out.println("Amount: " + bill.getTotalAmount());
        System.out.println("Paid: " + (bill.isPaid() ? "Yes" : "No"));
        System.out.println("=====================");
    }

    public void printBillsForPatient(String patientName) {
        List<Bill> billList = billsByPatient.get(patientName);
        if (billList == null || billList.isEmpty()) {
            System.out.println("No bills found for patient: " + patientName);
            return;
        }

        for (Bill bill : billList) {
            System.out.println("Service: " + bill.getAppointment().getService().getName());
            System.out.println("Amount: " + bill.getTotalAmount());
            System.out.println("Date: " + bill.getIssueDate());
            System.out.println("Paid: " + (bill.isPaid() ? "Yes" : "No"));
            System.out.println("-----------------------------");
        }
    }

    // === Other unchanged methods ===
    public void showAllAppointments() {
        for (Appointment a : appointments) {
            System.out.println(a);
            System.out.println("--------");
        }
    }

    public void showAppointmentsByDoctor(String name) {
        for (Appointment a : appointments) {
            if (a.getDoctor().getName().equalsIgnoreCase(name)) {
                System.out.println(a);
                System.out.println("--------");
            }
        }
    }

    public void showAppointmentsByClinic(String name) {
        for (Appointment a : appointments) {
            if (a.getClinic().getName().equalsIgnoreCase(name)) {
                System.out.println(a);
                System.out.println("--------");
            }
        }
    }

    public void showAppointmentsByPatient(String name) {
        for (Appointment a : appointments) {
            if (a.getPatient().getName().equalsIgnoreCase(name)) {
                System.out.println(a);
                System.out.println("--------");
            }
        }
    }

    public void showAllDoctors() {
        for (Clinic cl : network.getClinics()) {
            System.out.println(cl.getName());
            System.out.println("--------");
            for (Doctor doc : cl.getDoctors()) {
                System.out.println(doc.getName());
            }
        }
    }

    public void showAllPatients() {
        for (Clinic cl : network.getClinics()) {
            System.out.println(cl.getName());
            System.out.println("--------");
            for (Patient pat : cl.getPatients()) {
                System.out.println(pat.getName());
            }
        }
    }

    public void showAllClinics() {
        for (Clinic cl : network.getClinics()) {
            System.out.println(cl.getName());
            System.out.println("--------");
        }
    }

    public void showMedicalHistory(String name) {
        for (Clinic cl : network.getClinics()) {
            for (Patient pat : cl.getPatients()) {
                if (pat.getName().equalsIgnoreCase(name)) {
                    System.out.println("Medical history for " + name + ":");
                    for (Log log : pat.getMedicalHistory()) {
                        System.out.println(log);
                    }
                    return;
                }
            }
        }
        System.out.println("Patient not found: " + name);
    }

    public void addPatientToClinic(Patient patient, String clinic) {
        for (Clinic cl : network.getClinics()) {
            if (cl.getName().equalsIgnoreCase(clinic)) {
                cl.addPatient(patient);
                break;
            }
        }
    }
}
