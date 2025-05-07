import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ClinicService {
    private ClinicNetwork network;
    private List<Appointment> appointments;
    private Map<String, List<Bill>> billsByPatient;
    private static Scanner scanner = new Scanner(System.in);

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

    public void addServiceToClinic(String clinicName) {
        Clinic clinic = findClinicByName(clinicName);
        if (clinic == null) {
            System.out.println("Clinic not found.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter service name: ");
        String name = scanner.nextLine();

        System.out.print("Enter service price: ");
        double price = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter duration in minutes: ");
        int duration = Integer.parseInt(scanner.nextLine());

        Service newService = new Service(name, price, duration);

        List<Service> services = clinic.getServices();
        int i = 0;
        while (i < services.size() && services.get(i).getName().compareToIgnoreCase(name) < 0) {    //Insertion sort by name
            i++;
        }
        services.add(i, newService);

        System.out.println("Service added and sorted by name.");
    }

    public void scheduleAppointmentByNames(String patientName, String doctorName, String clinicName, String serviceName, LocalDateTime dateTime) {
        Clinic clinic = null;
        //Find clinic by name
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
        //Find Doctor by name
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
        //Find patient by name
        Patient patient = null;
        for (Patient p : clinic.getPatients()) {
            if (p.getName().equalsIgnoreCase(patientName)) {
                patient = p;
                break;
            }
        }
        //If no patient found with this name we crate a new patient
        if (patient == null) {
            System.out.println("Patient not found in this clinic.\n Please provide the information needed for registering a new patient:\n");
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Personal ID: ");
            String personalId = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Phone: ");
            String phone = scanner.nextLine();
            System.out.print("Insurance Provider: ");
            String insurance = scanner.nextLine();
            Patient patientNameForRegister = new Patient(name, personalId, email, phone, insurance);
            addPatientToClinic(patientNameForRegister,clinic.getName());
            patient = patientNameForRegister;

        }
        //Find service by name
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
        //Create appointment
        Appointment appointment = new Appointment(patient, doctor, clinic, service, dateTime);
        appointments.add(appointment);

        //Create a new log in patient medical history
        Log log = new Log(clinic.getName(), service.getName(), doctor, dateTime.toLocalDate());
        patient.getMedicalHistory().add(log);

        //Create the bill
        createBillForAppointment(appointment);

        System.out.println("Appointment successfully scheduled!");
    }

    public void createBillForAppointment(Appointment appointment) {
        Bill bill = new Bill(appointment);
        //Get patient name for composing his bill
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
