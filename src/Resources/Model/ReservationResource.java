package Resources.Model;

public class ReservationResource {

    private final int id;
    private final String name;
    private final String category;
    private final String location;
    private final String description;
    private final int slotDurationMinutes;
    private final boolean active;
    private final boolean publicResource;
    private final Integer ownerUserId;

    public ReservationResource(int id, String name, String category, String location,
                               String description, int slotDurationMinutes, boolean active,
                               boolean publicResource, Integer ownerUserId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.location = location;
        this.description = description;
        this.slotDurationMinutes = slotDurationMinutes;
        this.active = active;
        this.publicResource = publicResource;
        this.ownerUserId = ownerUserId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public int getSlotDurationMinutes() { return slotDurationMinutes; }
    public boolean isActive() { return active; }
    public boolean isPublicResource() { return publicResource; }
    public Integer getOwnerUserId() { return ownerUserId; }

    @Override
    public String toString() {
        return (publicResource ? "Publico" : "Privado")
                + " | " + category
                + " | " + name
                + " | " + location;
    }
}
