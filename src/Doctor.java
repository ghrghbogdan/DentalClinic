import java.util.List;
import java.util.ArrayList;
public class Doctor extends Person {
    private Clinic clinic;
    private String specialization;
    private int yearsOfExperience;
    private List<Appointment> appointmentList;

    public Doctor(String name, String personalId, String email, String phone,Clinic clinic, String specialization, int yearsOfExperience) {
        super(name, personalId, email, phone);
        this.clinic=clinic;
        this.specialization = specialization;
        this.yearsOfExperience = yearsOfExperience;


    }
    public void setClinic(Clinic clinic){
        this.clinic=clinic;
    }
    public Clinic getClinic(){
        return clinic;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
    public void setAppointmentList(){
        this.appointmentList=clinic.getDoctorAppointment(this);
    }
    public List<Appointment> getAppointmentList(){
        return this.appointmentList;
    }


    @Override
    public String toString() {
        return super.toString() + ", Specialization: " + specialization + ", Experience: " + yearsOfExperience + " years";
    }
}
