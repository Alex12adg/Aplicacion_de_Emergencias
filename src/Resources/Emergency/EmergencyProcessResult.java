package Resources.Emergency;

import java.util.Collections;
import java.util.List;

public class EmergencyProcessResult {

    private final boolean success;
    private final String statusMessage;
    private final String locationUsed;
    private final EmergencyEvent event;
    private final List<EmergencyCenter> centers;

    public EmergencyProcessResult(
            boolean success,
            String statusMessage,
            String locationUsed,
            EmergencyEvent event,
            List<EmergencyCenter> centers
    ) {
        this.success = success;
        this.statusMessage = statusMessage;
        this.locationUsed = locationUsed;
        this.event = event;
        this.centers = centers == null ? Collections.emptyList() : List.copyOf(centers);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getLocationUsed() {
        return locationUsed;
    }

    public EmergencyEvent getEvent() {
        return event;
    }

    public List<EmergencyCenter> getCenters() {
        return centers;
    }
}
