package Resources.User;

public class UserData {

    private int id;
    private String nombre;
    private String phone;
    private String role;
    private String email;
    private String password;
    private String allergies;
    private String conditions;
    private String medications;


    public UserData(int id, String nombre, String phone, String role, String email, String password,
                    String allergies, String conditions, String medications) {
        this.id = id;
        this.nombre = nombre;
        this.phone = phone;
        this.role = role;
        this.email = email;
        this.password = password;
        this.allergies = allergies;
        this.conditions = conditions;
        this.medications = medications;
    }

    public UserData(String nombre, String phone, String email, String password) {
        this.nombre = nombre;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getAllergies() { return allergies; }
    public String getConditions() { return conditions; }
    public String getMedications() { return medications; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public void setConditions(String conditions) { this.conditions = conditions; }
    public void setMedications(String medications) { this.medications = medications; }
}
