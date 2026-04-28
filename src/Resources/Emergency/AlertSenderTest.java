package Resources.Emergency;

import Resources.User.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlertSenderTest {

    @Test
    public void testAlertaTexto() {
        UserData user = new UserData(
                1,
                "Test",
                "600000000",
                "user",
                "test@example.com",
                "secret",
                null,
                null,
                null
        );

        EmergencyEvent event = new EmergencyEvent("Prueba", "Ubicacion X", user, 8);

        assertTrue(event.toString().contains("Prueba"));
        assertTrue(event.toString().contains("Test"));
    }
}
