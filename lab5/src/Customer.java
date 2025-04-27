public class Customer {
    private static int nextId = 1;
    private final int id;

    public Customer() {
        this.id = nextId++;
    }

    public int getId() {
        return id;
    }
}
