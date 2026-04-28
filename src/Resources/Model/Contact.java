package Resources.Model;

public class Contact {

    private int id;
    private String name;
    private String phone;
    private String relation;

    public Contact(int id, String name, String phone, String relation) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.relation = relation;
    }

    public Contact(String name, String phone, String relation) {
        this(0, name, phone, relation);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRelation() { return relation; }

    @Override
    public String toString() {
        String safeRelation = relation == null || relation.isBlank() ? "Sin relacion indicada" : relation;
        return name + " | " + phone + " | " + safeRelation;
    }
}
