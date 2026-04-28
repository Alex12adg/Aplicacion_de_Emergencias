package Resources.Model;

public class MedicalInfo {

    private int userId;
    private String allergies;
    private String conditions;
    private String medications;

    public MedicalInfo(int userId, String allergies, String conditions, String medications) {
        this.userId = userId;
        this.allergies = allergies;
        this.conditions = conditions;
        this.medications = medications;
    }

    public String getAllergies() { return allergies; }
    public String getConditions() { return conditions; }
    public String getMedications() { return medications; }
}