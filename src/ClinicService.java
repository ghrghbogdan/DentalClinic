import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.sql.SQLException;

public class ClinicService {
    private final ClinicNetwork network;
    private final List<Appointment> appointments;
    private final Map<String, List<Bill>> billsByPatient;
    private final static Scanner scanner = new Scanner(System.in);

    private final ServiceClinic clinicService;
    private final ServicePatient patientService;
    private final ServiceDoctor doctorService;
    private final ServiceAppointment appointmentService;
    private final ServiceBill billService;
    private final ServiceMedical medicalService;
    private final ServiceLog logService;
    private final AuditService auditService;

    public ClinicService(ClinicNetwork network) {
        this.network = network;
        this.appointments = new ArrayList<>();
        this.billsByPatient = new HashMap<>();

        this.clinicService = ServiceClinic.getInstance();
        this.patientService = ServicePatient.getInstance();
        this.doctorService = ServiceDoctor.getInstance();
        this.appointmentService = ServiceAppointment.getInstance();
        this.billService = ServiceBill.getInstance();
        this.medicalService = ServiceMedical.getInstance();
        this.logService = ServiceLog.getInstance();
        this.auditService = AuditService.getInstance();

        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        try {
            // Load clinics into network
            List<Clinic> dbClinics = clinicService.readAll();
            for (Clinic clinic : dbClinics) {
                if (network.findClinicByName(clinic.getName()) == null) {
                    network.addClinic(clinic);
                    System.out.println("Loaded clinic: " + clinic.getName());
                }
            }

            // Load doctors and associate with clinics
            List<Doctor> dbDoctors = doctorService.readAll();
            for (Doctor doctor : dbDoctors) {
                if (doctor.getClinic() != null) {
                    Clinic clinic = network.findClinicByName(doctor.getClinic().getName());
                    if (clinic != null && !clinic.getDoctors().contains(doctor)) {
                        clinic.addDoctor(doctor);
                        System.out.println("Loaded doctor: " + doctor.getName() + " in clinic: " + clinic.getName());
                    }
                }
            }

            // Load patients
            List<Patient> dbPatients = patientService.readAll();
            for (Patient patient : dbPatients) {
                // Associate patients with clinics would require additional logic or table
                // For now, we'll add them to the first clinic as a simplification
                if (!network.getClinics().isEmpty() && !network.getClinics().get(0).getPatients().contains(patient)) {
                    network.getClinics().get(0).addPatient(patient);
                    System.out.println("Loaded patient: " + patient.getName() + " in clinic: " + network.getClinics().get(0).getName());
                }
            }

            // Load existing appointments into memory
            List<Appointment> dbAppointments = appointmentService.readAll().stream()
                    .filter(appointment -> !this.appointments.contains(appointment))
                    .sorted(Comparator.comparing(Appointment::getDateTime))
                    .collect(Collectors.toList());

            for (Appointment appointment : dbAppointments) {
                this.appointments.add(appointment);
                System.out.println("Loaded appointment: " + appointment.getDateTime() + " for patient: " + appointment.getPatient().getName());
            }


            // Load bills by patient
            List<Bill> allBills = billService.readAll();
            for (Bill bill : allBills) {
                String patientName = bill.getAppointment().getPatient().getName();
                billsByPatient.computeIfAbsent(patientName, k -> new ArrayList<>()).add(bill);
                System.out.println("Loaded bill for patient: " + patientName );
            }

            // Load services for each clinic
            for (Clinic clinic : network.getClinics()) {
                try {
                    List<Service> clinicServices = medicalService.getServicesByClinic(clinic.getName());
                    for (Service service : clinicServices) {
                        if (!clinic.getServices().contains(service)) {
                            clinic.addService(service);
                            System.out.println("Loaded service: " + service.getName() + " in clinic: " + clinic.getName());
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error loading services for clinic: " + clinic.getName());
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading data from database: " + e.getMessage());
        }
    }

    public Clinic findClinicByName(String name) {
        return network.findClinicByName(name);
    }

    public void showAllClinics() {
        System.out.println("\n--- All Clinics ---");
        for (Clinic clinic : network.getClinics()) {
            System.out.println(clinic);
        }
    }

    public void addDoctorToClinic(Doctor doctor, String clinicName) {
        Clinic clinic = findClinicByName(clinicName);
        if (clinic == null) {
            System.out.println("Clinic not found.");
            return;
        }

        clinic.addDoctor(doctor);


        try {
            // Update doctor with clinic reference
            doctor.setClinic(clinic);
            doctorService.create(doctor);
            auditService.logAction("Added doctor: " + doctor.getName() + " to clinic: " + clinicName);
        } catch (SQLException e) {
            System.err.println("Error saving doctor to database: " + e.getMessage());
        }

        System.out.println("Doctor added to " + clinicName);
    }

    public void addPatientToClinic(Patient patient, String clinicName) {
        Clinic clinic = findClinicByName(clinicName);
        if (clinic == null) {
            System.out.println("Clinic not found.");
            return;
        }

        clinic.addPatient(patient);


        try {
            patientService.create(patient);
            auditService.logAction("Added patient: " + patient.getName() + " to clinic: " + clinicName);
        } catch (SQLException e) {
            System.err.println("Error saving patient to database: " + e.getMessage());
        }

        System.out.println("Patient added to " + clinicName);
    }

    public void addServiceToClinic(String clinicName) {
        Clinic clinic = findClinicByName(clinicName);
        if (clinic == null) {
            System.out.println("Clinic not found.");
            return;
        }

        System.out.print("Enter service name: ");
        String name = scanner.nextLine();

        System.out.print("Enter service price: ");
        double price = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter duration in minutes: ");
        int duration = Integer.parseInt(scanner.nextLine());

        Service newService = new Service(name, price, duration);
        clinic.addService(newService);

        // Persist to database
        try {
            medicalService.createWithClinic(newService, clinicName);
            auditService.logAction("Added service: " + newService.getName() + " to clinic: " + clinicName);
        } catch (SQLException e) {
            System.err.println("Error saving service to database: " + e.getMessage());
        }

        System.out.println("Service added to clinic " + clinicName);
    }

    public void showAllDoctors() {
        try {
            List<Doctor> doctors = doctorService.readAll();
            if (doctors.isEmpty()) {
                System.out.println("No doctors found.");
                return;
            }

            System.out.println("\n--- All Doctors ---");
            for (Doctor doctor : doctors) {
                System.out.println(doctor);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving doctors: " + e.getMessage());

        }
    }

    public void showAllPatients() {
        try {
            List<Patient> patients = patientService.readAll();
            if (patients.isEmpty()) {
                System.out.println("No patients found.");
                return;
            }

            System.out.println("\n--- All Patients ---");
            for (Patient patient : patients) {
                System.out.println(patient);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving patients: " + e.getMessage());
        }
    }
    public LocalDateTime findNextAvailableSlot(String doctorName, String clinicName, String serviceName, LocalDateTime requestedTime) {
        Clinic clinic = findClinicByName(clinicName);
        if (clinic == null) return null;

        Doctor doctor = clinic.getDoctors().stream()
                .filter(d -> d.getName().equalsIgnoreCase(doctorName))
                .findFirst().orElse(null);
        if (doctor == null) return null;

        Service service = clinic.getServices().stream()
                .filter(s -> s.getName().equalsIgnoreCase(serviceName))
                .findFirst().orElse(null);
        if (service == null) return null;

        long duration = service.getDurationInMinutes().toMinutes();

        // Get all appointments for this doctor on the requested day
        List<Appointment> doctorAppointments = appointments.stream()
                .filter(a -> a.getDoctor().getName().equalsIgnoreCase(doctorName))
                .filter(a -> a.getDateTime().toLocalDate().equals(requestedTime.toLocalDate()))
                .sorted(Comparator.comparing(Appointment::getDateTime))
                .collect(Collectors.toList());

        LocalDateTime startOfDay = requestedTime.toLocalDate().atTime(8, 0);
        LocalDateTime endOfDay = requestedTime.toLocalDate().atTime(20, 0);

        LocalDateTime current = requestedTime.isBefore(startOfDay) ? startOfDay : requestedTime.withSecond(0).withNano(0);

        while (!current.plusMinutes(duration).isAfter(endOfDay)) {
            LocalDateTime slotEnd = current.plusMinutes(duration);
            LocalDateTime finalCurrent = current;
            boolean overlaps = doctorAppointments.stream().anyMatch(a -> {

                LocalDateTime apptStart = a.getDateTime();
                LocalDateTime apptEnd = apptStart.plus(a.getService().getDurationInMinutes());
                return !slotEnd.isBefore(apptStart) && !finalCurrent.isAfter(apptEnd.minusSeconds(1));
            });
            if (!overlaps) return current;
            current = current.plusMinutes(5);
        }
        return null; // No slot available that day
    }

    public void scheduleAppointmentByNames(String patientName, String doctorName, String clinicName, String serviceName, LocalDateTime dateTime) {
        Clinic clinic = findClinicByName(clinicName);
        if (clinic == null) {
            System.out.println("Clinic not found.");
            return;
        }

        // Find Doctor by name
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

        // Find patient by name
        Patient patient = null;
        for (Patient p : clinic.getPatients()) {
            if (p.getName().equalsIgnoreCase(patientName)) {
                patient = p;
                break;
            }
        }

        // Create new patient if not found
        if (patient == null) {
            System.out.println("Patient not found. Creating new patient:");
            System.out.print("Personal ID: ");
            String personalId = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Phone: ");
            String phone = scanner.nextLine();
            System.out.print("Insurance Provider: ");
            String insurance = scanner.nextLine();

            patient = new Patient(patientName, personalId, email, phone, insurance);
            addPatientToClinic(patient, clinicName);
        }

        // Find service by name
        Service service = null;
        for (Service s : clinic.getServices()) {
            if (s.getName().equalsIgnoreCase(serviceName)) {
                service = s;
                break;
            }
        }
        if (service == null) {
            System.out.println("Service not found in this clinic.");
            return;
        }


        LocalDateTime availableSlot = findNextAvailableSlot(doctorName, clinicName, serviceName, dateTime);
        if (availableSlot == null) {
            System.out.println("No available slots for the requested service at this time.");
            return;
        }
        if(!availableSlot.isEqual(dateTime))
        {
            System.out.println("Requested time is not available. Next available slot is: " + availableSlot);
            System.out.println("Do you want to proceed with this slot? (yes/no)");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("yes")) {
                System.out.println("Appointment not scheduled.");
                return;
            }
            else {
                System.out.println("Proceeding with the next available slot: " + availableSlot);
            }
        }

        Appointment appointment = new Appointment(patient, doctor, clinic, service, availableSlot);

        try {
            appointmentService.create(appointment);
            appointments.add(appointment);

            // Create medical log
            Log log = new Log(clinic.getName(), service.getName(), doctor, dateTime.toLocalDate());
            patient.getMedicalHistory().add(log);
            logService.create(log);
            logService.addPatientToLog(log, patient);

            // Create bill
            createBillForAppointment(appointment);

            System.out.println("Appointment scheduled successfully");
        } catch (SQLException e) {
            System.err.println("Error saving appointment to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createBillForAppointment(Appointment appointment) {
        Bill bill = new Bill(appointment);
        String patientName = appointment.getPatient().getName();
        billsByPatient.computeIfAbsent(patientName, k -> new ArrayList<>()).add(bill);

        // Persist bill to database
        try {
            billService.create(bill);
            auditService.logAction("Created bill for patient: " + patientName);
        } catch (SQLException e) {
            System.err.println("Error saving bill to database: " + e.getMessage());
        }

        System.out.println("=== BILL CREATED ===");
        System.out.println(bill);
        System.out.println("===================");
    }

    public void showAppointmentsByDoctor(String doctorName) {
        try {
            // Try to get doctor from database first
            List<Doctor> doctors = doctorService.readAll();
            Doctor foundDoctor = null;
            for (Doctor d : doctors) {
                if (d.getName().equalsIgnoreCase(doctorName)) {
                    foundDoctor = d;
                    break;
                }
            }

            if (foundDoctor != null) {
                List<Appointment> doctorAppointments = appointmentService.findByDoctor(foundDoctor);
                System.out.println("\n--- Schedule for " + doctorName + " ---");
                if (doctorAppointments.isEmpty()) {
                    System.out.println("No appointments scheduled.");
                    return;
                }

                doctorAppointments.sort(Comparator.comparing(Appointment::getDateTime));

                for (Appointment appointment : doctorAppointments) {
                    System.out.println(appointment.getDateTime() + " - " +
                            appointment.getPatient().getName() + " - " +
                            appointment.getService().getName());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving doctor schedule from database: " + e.getMessage());
        }
    }

    public void showAppointmentsByClinic(String clinicName) {
        try {
            // Get all appointments and filter by clinic
            List<Appointment> allAppointments = appointmentService.readAll();
            List<Appointment> clinicAppointments = allAppointments.stream()
                    .filter(a -> a.getClinic().getName().equalsIgnoreCase(clinicName))
                    .sorted(Comparator.comparing(Appointment::getDateTime))
                    .collect(Collectors.toList());

            System.out.println("\n--- Appointments for " + clinicName + " ---");
            if (clinicAppointments.isEmpty()) {
                System.out.println("No appointments found for this clinic.");
                return;
            }

            for (Appointment appointment : clinicAppointments) {
                System.out.println(appointment.getDateTime() + " - " +
                        "Patient: " + appointment.getPatient().getName() + " - " +
                        "Doctor: " + appointment.getDoctor().getName() + " - " +
                        "Service: " + appointment.getService().getName());
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointments by clinic: " + e.getMessage());

        }
    }

    public void showAppointmentsByPatient(String patientName) {
        try {
            // Find patient first
            Patient foundPatient = null;
            List<Patient> patients = patientService.readAll();
            for (Patient p : patients) {
                if (p.getName().equalsIgnoreCase(patientName)) {
                    foundPatient = p;
                    break;
                }
            }

            if (foundPatient != null) {
                List<Appointment> patientAppointments = appointmentService.findByPatient(foundPatient);
                System.out.println("\n--- Appointments for patient " + patientName + " ---");

                if (patientAppointments.isEmpty()) {
                    System.out.println("No appointments found for this patient.");
                    return;
                }

                patientAppointments.sort(Comparator.comparing(Appointment::getDateTime));

                for (Appointment appointment : patientAppointments) {
                    System.out.println(appointment.getDateTime() + " - " +
                            "Clinic: " + appointment.getClinic().getName() + " - " +
                            "Doctor: " + appointment.getDoctor().getName() + " - " +
                            "Service: " + appointment.getService().getName());
                }
                return;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointments by patient: " + e.getMessage());
        }
    }

    public void showMedicalHistory(String patientName) {
        try {
            // Try to get patient from database first
            List<Patient> patients = patientService.readAll();
            Patient foundPatient = null;
            for (Patient p : patients) {
                if (p.getName().equalsIgnoreCase(patientName)) {
                    foundPatient = p;
                    break;
                }
            }

            if (foundPatient != null) {
                List<Log> medicalHistory = logService.findByPatient(foundPatient);
                System.out.println("\n--- Medical History for " + patientName + " ---");
                if (medicalHistory.isEmpty()) {
                    System.out.println("No medical history found.");
                    return;
                }

                medicalHistory.sort(Comparator.comparing(Log::getDate));

                for (Log log : medicalHistory) {
                    System.out.println(log.getDate() + " - " +
                            "Clinic: " + log.getClinicName() + " - " +
                            "Service: " + log.getServiceName() + " - " +
                            "Doctor: " + log.getDoctor().getName());
                }
                return;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving patient medical history from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean showAllDoctorsByClinic(String clinicName) {
        Clinic clinic = findClinicByName(clinicName);
        if (clinic == null) {
            return false;
        }

        try {
            List<Doctor> doctors = doctorService.readAll().stream()
                    .filter(d -> d.getClinic() != null && d.getClinic().getName().equalsIgnoreCase(clinicName))
                    .collect(Collectors.toList());

            System.out.println("\n--- Doctors at " + clinicName + " ---");
            if (doctors.isEmpty()) {
                System.out.println("No doctors found at this clinic.");
            } else {
                for (Doctor doctor : doctors) {
                    System.out.println(doctor);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving doctors by clinic: " + e.getMessage());
        }

        return true;
    }

    public void showServices(String clinicName) {
        Clinic clinic = findClinicByName(clinicName);
        if (clinic == null) {
            System.out.println("Clinic not found.");
            return;
        }
        try {
            List<Service> services = medicalService.getServicesByClinic(clinicName);
            if (services.isEmpty()) {
                System.out.println("No services found for this clinic.");
                return;
            }
            System.out.println("\n--- Services for " + clinicName + " ---");
            for (Service service : services) {
                System.out.println(service);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving services: " + e.getMessage());
        }
    }

    public void printBillsForPatient(String patientName) {
        try {
            // First try to get patient from database
            List<Patient> patients = patientService.readAll();
            Patient foundPatient = null;
            for (Patient p : patients) {
                if (p.getName().equalsIgnoreCase(patientName)) {
                    foundPatient = p;
                    break;
                }
            }

            if (foundPatient != null) {
                List<Bill> bills = billService.findBillsByPatient(foundPatient);
                System.out.println("\n--- Bills for " + patientName + " ---");
                if (bills.isEmpty()) {
                    System.out.println("No bills found.");
                    return;
                }

                for (Bill bill : bills) {
                    System.out.println(bill);
                    System.out.println("-----------------");
                }

            }
        } catch (SQLException e) {
            System.err.println("Error retrieving bills from database: " + e.getMessage());
        }
    }

    public void generateClinicReport(String clinicName, LocalDate startDate, LocalDate endDate) {
        Clinic clinic = findClinicByName(clinicName);
        if (clinic == null) {
            System.out.println("Clinic not found.");
            return;
        }

        try {
            // Get all appointments for this clinic in the date range
            List<Appointment> reportAppointments = appointmentService.readAll().stream()
                    .filter(a -> a.getClinic().getName().equalsIgnoreCase(clinicName))
                    .filter(a -> {
                        LocalDate appDate = a.getDateTime().toLocalDate();
                        return (appDate.isEqual(startDate) || appDate.isAfter(startDate)) &&
                                (appDate.isEqual(endDate) || appDate.isBefore(endDate));
                    })
                    .collect(Collectors.toList());

            // Calculate statistics
            double totalRevenue = 0;
            Map<String, Integer> serviceCount = new HashMap<>();
            Map<String, Integer> doctorAppointments = new HashMap<>();

            for (Appointment appointment : reportAppointments) {
                // Add to revenue
                totalRevenue += appointment.getService().getPrice();

                // Count services
                String serviceName = appointment.getService().getName();
                serviceCount.put(serviceName, serviceCount.getOrDefault(serviceName, 0) + 1);

                // Count doctor appointments
                String doctorName = appointment.getDoctor().getName();
                doctorAppointments.put(doctorName, doctorAppointments.getOrDefault(doctorName, 0) + 1);
            }

            // Print report
            System.out.println("\n=== CLINIC REPORT: " + clinicName + " ===");
            System.out.println("Period: " + startDate + " to " + endDate);
            System.out.println("Total appointments: " + reportAppointments.size());
            System.out.println("Total revenue: $" + totalRevenue);

            System.out.println("\n-- Services Breakdown --");
            for (Map.Entry<String, Integer> entry : serviceCount.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            System.out.println("\n-- Doctor Appointments --");
            for (Map.Entry<String, Integer> entry : doctorAppointments.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            // Calculate total revenue per doctor
            Map<String, Double> doctorRevenue = new HashMap<>();
            for (Appointment appointment : reportAppointments) {
                String doctorName = appointment.getDoctor().getName();
                double price = appointment.getService().getPrice();
                doctorRevenue.put(doctorName, doctorRevenue.getOrDefault(doctorName, 0.0) + price);
            }

            // Find the doctor with the highest revenue
            Map.Entry<String, Double> mostProfitableEntry = doctorRevenue.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (mostProfitableEntry != null) {
                System.out.println("Most Profitable Doctor: " + mostProfitableEntry.getKey() +
                        " (Total Revenue: $" + mostProfitableEntry.getValue() + ")");
            } else {
                System.out.println("Most Profitable Doctor: None");
            }



            auditService.logAction("Generated report for clinic: " + clinicName);
        } catch (SQLException e) {
            System.err.println("Error generating clinic report: " + e.getMessage());
        }
    }

    public void updateService(String clinicName, String serviceName, Double newPrice) {
        try {
            medicalService.updateServicePriceForClinic(serviceName, clinicName, newPrice);
        } catch (SQLException e) {
            System.err.println("Error updating service price: " + e.getMessage());
        }

    }
    public void deleteService(String clinicName, String serviceName) {
        try {
            medicalService.deleteServiceForClinic(serviceName, clinicName);
            auditService.logAction("Deleted service: " + serviceName + " from clinic: " + clinicName);
        } catch (SQLException e) {
            System.err.println("Error deleting service: " + e.getMessage());
        }
    }
}