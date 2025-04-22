public abstract class Person {
    protected String name;
    protected String personalId;
    protected String email;
    protected String phone;

    public Person(String name, String personalId, String email, String phone) {
        this.name = name;
        this.personalId = personalId;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersonalId() {
        return personalId;
    }

    public void setPersonalId(String personalId) {
        this.personalId = personalId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", ID: " + personalId + ", Email: " + email + ", Phone: " + phone;
    }
}
