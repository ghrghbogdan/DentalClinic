public class Service {
    private String name;
    private double price;
    private int durationInMinutes;

    public Service(String name, double price, int durationInMinutes) {
        this.name = name;
        this.price = price;
        this.durationInMinutes = durationInMinutes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    @Override
    public String toString() {
        return name + " (Price: $" + price + ", Duration: " + durationInMinutes + " mins)";
    }
}
