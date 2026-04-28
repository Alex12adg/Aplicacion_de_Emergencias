package Resources.Emergency;

public class EmergencyData {

    private int id;
    private int userId;
    private String type;
    private String description;

    public EmergencyData(int userId, String type, String description) {
        this.userId = userId;
        this.type = type;
        this.description = description;
    }

    public int getUserId() { return userId; }
    public String getType() { return type; }
    public String getDescription() { return description; }
}
