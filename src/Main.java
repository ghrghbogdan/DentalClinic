import java.time.LocalDateTime;
import java.util.*;
import java.time.LocalDate;
public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static ClinicService clinicService = new ClinicService(new ClinicNetwork("DentalCare Network"));

    public static void main(String[] args) {
        DatabaseConnection.getConnection();
        while (true) {
            printMenu();
            int choice = getChoice();
            try{
            switch (choice) {
                case 1 -> showAppointmentsByCriteria();
                case 2 -> showAllEntities();
                case 3 -> showPatientHistory();
                case 4 -> addEntity();
                case 5 -> addService();
                case 6 -> createAppointment();
                case 7 -> showBillForPatient();
                case 8 -> showClinicReport();
                case 9 -> {
                    updatedeleteService();
                }
                case 0 -> {
                    System.out.println("Exiting system.");
                    DatabaseConnection.closeConnection();
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }}
            catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void printMenu() {
        //CLI
        System.out.println("""
                
                ======= DentalCare Network Menu =======
                1. Show appointments by Doctor / Clinic / Patient
                2. Show all Doctors / Clinics / Patients
                3. Show patient medical history
                4. Add new Patient / Doctor
                5. Add new Service
                6. Create a new Appointment
                7. Show bills for a patient
                8. Show clinic report
                9. Update / Delete Service
                0. Exit
                ========================================
                Choose an option (0-9):
                """);
    }
    // Function for getting CLI input
    private static int getChoice() {
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next();
        }
        return scanner.nextInt();
    }


    private static void showAppointmentsByCriteria() {
        System.out.println("Filter appointments by:");
        System.out.println("1. Doctor name\n2. Clinic name\n3. Patient name");
        int option = getChoice();
        scanner.nextLine();

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
        scanner.nextLine();
        System.out.print("Enter patient's name: ");
        String name = scanner.nextLine();
        clinicService.showMedicalHistory(name);
    }

    private static void addEntity() {
        System.out.println("What do you want to add?");
        System.out.println("1. Patient\n2. Doctor");
        int option = getChoice();
        scanner.nextLine();

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
            clinicService.addDoctorToClinic(doctor,clinicName);
        } else {
            System.out.println("Invalid option.");
        }
    }
    private static void addService(){
        scanner.nextLine();
        System.out.print("Clinic name : ");
        String clinicName = scanner.nextLine();
        clinicService.addServiceToClinic(clinicName);
    }


    private static void createAppointment() {
        scanner.nextLine();
        System.out.print("Patient name: ");
        String patientName = scanner.nextLine();
        System.out.print("Clinic name: ");
        String clinicName = scanner.nextLine();
        boolean valid=clinicService.showAllDoctorsByClinic(clinicName);
        if(!valid){
            System.out.println("Clinic unknown");
            return ;
        }
        System.out.print("Doctor name: ");
        String doctorName = scanner.nextLine();
        clinicService.showServices(clinicName);
        System.out.print("Service name: ");
        String serviceName = scanner.nextLine();
        System.out.print("Date and time (yyyy-MM-ddTHH:mm): ");
        String dateTimeString = scanner.nextLine();
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);
        clinicService.scheduleAppointmentByNames(patientName, doctorName, clinicName, serviceName, dateTime);
    }

    private static void showBillForPatient() {
        scanner.nextLine();
        System.out.print("Enter patient name: ");
        String billPatientName = scanner.nextLine();
        System.out.println("Print bills");
        clinicService.printBillsForPatient(billPatientName);

    }

    private static void showClinicReport() {
        scanner.nextLine();
        System.out.print("Clinic name: ");
        String clinicName = scanner.nextLine();
        System.out.println("Start date (yyyy-MM-dd): ");
        String startDateString = scanner.nextLine();
        LocalDate startDate = LocalDate.parse(startDateString);
        System.out.println("End date (yyyy-MM-dd): ");
        String endDateString = scanner.nextLine();
        LocalDate endDate = LocalDate.parse(endDateString);
        clinicService.generateClinicReport(clinicName,startDate,endDate);
    }
    private static void updatedeleteService() {
        scanner.nextLine();
        System.out.print("Clinic name: ");
        String clinicName = scanner.nextLine();
        System.out.print("Service name: ");
        String serviceName = scanner.nextLine();
        System.out.println("1. Update service\n2. Delete service");
        int option = getChoice();
        if (option == 1) {
            System.out.print("New Price: ");
            double newPrice = scanner.nextDouble();


            clinicService.updateService(clinicName, serviceName, newPrice);
        } else if (option == 2) {
            clinicService.deleteService(clinicName, serviceName);
        } else {
            System.out.println("Invalid option.");
        }
    }
}
