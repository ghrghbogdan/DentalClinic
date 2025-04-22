import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static ClinicService clinicService = new ClinicService(new ClinicNetwork("DentalCare Network"));

    public static void main(String[] args) {
        initializeSystem();

        while (true) {
            printMenu();
            int choice = getChoice();

            switch (choice) {
                case 1 -> showAppointmentsByCriteria();
                case 2 -> showAllEntities();
                case 3 -> showPatientHistory();
                case 4 -> addEntity();
                case 5 -> createAppointment();
                case 6 -> createBill();
                case 7 -> {
                    System.out.println("Exiting system.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("""
                
                ======= DentalCare Network Menu =======
                1. Show appointments by Doctor / Clinic / Patient
                2. Show all Doctors / Clinics / Patients
                3. Show patient medical history
                4. Add new Patient / Doctor
                5. Create a new Appointment
                6. Create a Bill
                7. Exit
                ========================================
                Choose an option (1-7):
                """);
    }

    private static int getChoice() {
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private static void initializeSystem() {
        // Create clinics, services, doctors, patients for demo purposes
        Clinic clinic1 = new Clinic("Smile Clinic", "Strada Zambetului 5");
        Clinic clinic2 = new Clinic("Healthy Teeth", "Bulevardul Sanatatii 12");
        clinicService.addClinic(clinic1);
        clinicService.addClinic(clinic2);

        Service cleaning = new Service("Cleaning", 50.0, 30);
        Service checkup = new Service("Checkup", 60.0, 25);
        clinic1.addService(cleaning);
        clinic1.addService(checkup);
        clinic2.addService(cleaning);

        Doctor doc1 = new Doctor("Dr. John", "123", "john@mail.com", "0700000001", clinic1, "Dentist", 5);
        Doctor doc2 = new Doctor("Dr. Alice", "456", "alice@mail.com", "0700000002", clinic2, "Orthodontist", 7);
        clinic1.addDoctor(doc1);
        clinic2.addDoctor(doc2);

        Patient pat1 = new Patient("Ana", "111", "ana@mail.com", "0700000003", "CAS");
        Patient pat2 = new Patient("Mihai", "222", "mihai@mail.com", "0700000004", "CAS");
        clinic1.addPatient(pat1);
        clinic2.addPatient(pat2);
    }

    private static void showAppointmentsByCriteria() {
        System.out.println("Filter appointments by:");
        System.out.println("1. Doctor name\n2. Clinic name\n3. Patient name");
        int option = getChoice();
        scanner.nextLine(); // consume newline

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        switch (option) {
            case 1 -> clinicService.showAppointmentsByDoctor(name);
            case 2 -> clinicService.showAppointmentsByClinic(name);
            case 3 -> clinicService.showAppointmentsByPatient(name);
            default -> System.out.println("Invalid option.");
        }
    }

    private static void showAllEntities() {
        System.out.println("Which list do you want to see?");
        System.out.println("1. Doctors\n2. Clinics\n3. Patients");
        int option = getChoice();

        switch (option) {
            case 1 -> clinicService.showAllDoctors();
            case 2 -> clinicService.showAllClinics();
            case 3 -> clinicService.showAllPatients();
            default -> System.out.println("Invalid option.");
        }
    }

    private static void showPatientHistory() {
        scanner.nextLine(); // consume newline
        System.out.print("Enter patient's name: ");
        String name = scanner.nextLine();
        clinicService.showMedicalHistory(name);
    }

    private static void addEntity() {
        System.out.println("What do you want to add?");
        System.out.println("1. Patient\n2. Doctor");
        int option = getChoice();
        scanner.nextLine(); // consume newline

        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Personal ID: ");
        String personalId = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();


        if (option == 1) {
            System.out.print("Insurance Provider: ");
            String insurance = scanner.nextLine();
            System.out.print("Clinic: ");
            String clinic = scanner.nextLine();
            Patient patient = new Patient(name, personalId, email, phone, insurance);
            clinicService.addPatientToClinic(patient,clinic);
        } else if (option == 2) {
            System.out.print("Clinic name: ");
            String clinicName = scanner.nextLine();
            Clinic clinic = clinicService.findClinicByName(clinicName);
            if (clinic == null) {
                System.out.println("Clinic not found.");
                return;
            }
            System.out.print("Specialization: ");
            String spec = scanner.nextLine();
            System.out.print("Years of experience: ");
            int exp = scanner.nextInt();
            Doctor doctor = new Doctor(name, personalId, email, phone, clinic, spec, exp);
            clinic.addDoctor(doctor);
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void createAppointment() {
        scanner.nextLine(); // flush
        System.out.print("Patient name: ");
        String patientName = scanner.nextLine();
        System.out.print("Doctor name: ");
        String doctorName = scanner.nextLine();
        System.out.print("Clinic name: ");
        String clinicName = scanner.nextLine();
        System.out.print("Service name: ");
        String serviceName = scanner.nextLine();
        System.out.print("Date and time (yyyy-MM-ddTHH:mm): ");
        String dateTimeString = scanner.nextLine();

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);
        clinicService.scheduleAppointmentByNames(patientName, doctorName, clinicName, serviceName, dateTime);
    }

    private static void createBill() {
        scanner.nextLine();
        System.out.print("Enter appointment ID (index): ");
        int index = scanner.nextInt();
        clinicService.createBillForAppointment(index);
    }
}
