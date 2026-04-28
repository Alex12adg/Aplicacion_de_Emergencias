package GUI.controllers;

import GUI.AppLayout;
import Resources.Danger.DangerAlertState;
import Resources.Danger.DangerAlertSystem;
import Resources.Emergency.EmergencyManager;
import Resources.Heart.HeartRateMonitor;
import Resources.Session.UserSession;
import Resources.User.UserData;
import Resources.Voice.VoiceConfig;
import Resources.Voice.VoiceDetector;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;
import java.util.function.Consumer;

public class MainController {

    private static final double MENU_EXPANDED_WIDTH = 172;
    private static final double MENU_COLLAPSED_WIDTH = 0;

    @FXML
    private Label usernameLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private ScrollPane sideMenu;

    @FXML
    private StackPane dangerOverlay;

    @FXML
    private Label dangerModalMessageLabel;

    @FXML
    private Label dangerModalLocationLabel;

    @FXML
    private Button dangerModalStopButton;

    private EmergencyManager emergencyManager;
    private VoiceDetector voiceDetector;
    private VoiceConfig voiceConfig;
    private DangerAlertSystem dangerSystem;
    private HeartRateMonitor heartMonitor;
    private boolean menuVisible = true;
    private Timeline dangerAlarmTimeline;

    public MainController() {
        emergencyManager = new EmergencyManager();
        voiceConfig = new VoiceConfig();
        voiceDetector = new VoiceDetector(voiceConfig);
        dangerSystem = new DangerAlertSystem();
        heartMonitor = new HeartRateMonitor();
    }

    @FXML
    public void initialize() {
        UserData user = UserSession.getUser();
        usernameLabel.setText(user != null ? user.getNombre() : "Usuario Demo");
        loadView("Home-view.fxml");
        applyMenuWidth(MENU_COLLAPSED_WIDTH);
        menuVisible = false;
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/Views/" + fxml)
            );

            Parent view = loader.load();
            Object controller = loader.getController();

            if (controller instanceof EmergencyController) {
                ((EmergencyController) controller).setEmergencyManager(emergencyManager);
            }

            if (controller instanceof HomeController) {
                UserData currentUser = UserSession.getUser();
                ((HomeController) controller).setWelcomeName(currentUser != null ? currentUser.getNombre() : "Usuario");
                ((HomeController) controller).setNavigationActions(
                        () -> loadProtectedView("Emergency-view.fxml"),
                        () -> loadProtectedView("health-view.fxml"),
                        () -> loadProtectedView("voice-view.fxml"),
                        () -> loadView("Danger-view.fxml"),
                        () -> loadProtectedView("Reservations-view.fxml"),
                        () -> loadProtectedView("Centers-view.fxml"),
                        () -> loadProtectedView("Medical-form-view.fxml"),
                        () -> loadProtectedView("Settings-view.fxml")
                );
            }

            if (controller instanceof SettingsController) {
                Consumer<String> profileNameUpdateHandler = updatedName -> usernameLabel.setText(
                        updatedName == null || updatedName.isBlank() ? "Usuario" : updatedName
                );
                ((SettingsController) controller).setProfileNameUpdateHandler(profileNameUpdateHandler);
            }

            if (controller instanceof VoiceController) {
                ((VoiceController) controller).setVoiceDetector(voiceDetector, emergencyManager);
            }

            if (controller instanceof DangerController) {
                ((DangerController) controller).setDangerSystem(dangerSystem, emergencyManager);
                ((DangerController) controller).setModalHandler(this::showDangerOverlay);
            }

            if (controller instanceof HealthController) {
                ((HealthController) controller).setHeartMonitor(heartMonitor);
            }

            if (controller instanceof CentersController) {
                ((CentersController) controller).setEmergencyManager(emergencyManager);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            System.out.println("Error cargando vista: " + fxml);
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleSideMenu() {
        if (dangerSystem.isAlertActive()) {
            return;
        }

        double targetWidth = menuVisible ? MENU_COLLAPSED_WIDTH : MENU_EXPANDED_WIDTH;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(sideMenu.prefWidthProperty(), targetWidth),
                        new KeyValue(sideMenu.minWidthProperty(), targetWidth),
                        new KeyValue(sideMenu.maxWidthProperty(), targetWidth),
                        new KeyValue(sideMenu.opacityProperty(), menuVisible ? 0 : 1)
                )
        );
        timeline.play();
        menuVisible = !menuVisible;
    }

    private void applyMenuWidth(double width) {
        sideMenu.setPrefWidth(width);
        sideMenu.setMinWidth(width);
        sideMenu.setMaxWidth(width);
        sideMenu.setOpacity(width == 0 ? 0 : 1);
    }

    @FXML
    private void loadHome() {
        loadProtectedView("Home-view.fxml");
    }

    @FXML
    private void loadEmergency() {
        loadProtectedView("Emergency-view.fxml");
    }

    @FXML
    private void loadHealth() {
        loadProtectedView("health-view.fxml");
    }

    @FXML
    private void loadVoice() {
        loadProtectedView("voice-view.fxml");
    }

    @FXML
    private void loadDanger() {
        loadView("Danger-view.fxml");
    }

    @FXML
    private void loadCenters() {
        loadProtectedView("Centers-view.fxml");
    }

    @FXML
    private void loadReservations() {
        loadProtectedView("Reservations-view.fxml");
    }

    @FXML
    private void loadMedical() {
        loadProtectedView("Medical-form-view.fxml");
    }

    @FXML
    private void loadSettings() {
        loadProtectedView("Settings-view.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Alert confirmationAlert = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "¿Quieres cerrar sesion?",
                    ButtonType.YES,
                    ButtonType.NO
            );
            confirmationAlert.setTitle("Confirmar cierre de sesion");
            confirmationAlert.setHeaderText("Cerrar sesion");
            confirmationAlert.initOwner(usernameLabel.getScene().getWindow());

            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isEmpty() || result.get() != ButtonType.YES) {
                return;
            }

            UserSession.clear();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/Views/Login-view.fxml")
            );

            Parent root = loader.load();
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, AppLayout.MOBILE_WIDTH, AppLayout.MOBILE_HEIGHT));
            stage.show();
        } catch (Exception e) {
            System.out.println("Error al cerrar sesion");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEmergency() {
        Thread thread = new Thread(() -> emergencyManager.startSystemInteractive());
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleVoiceDetection() {
        Thread thread = new Thread(() -> voiceDetector.startListening(emergencyManager));
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleDangerAlert() {
        Thread thread = new Thread(() -> dangerSystem.activateAlert(emergencyManager));
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleHeartMonitor() {
        Thread thread = new Thread(() -> heartMonitor.startMonitoring());
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleDismissDangerAlarm() {
        if (!dangerSystem.isAlertActive()) {
            hideDangerOverlay();
            return;
        }

        DangerAlertState state = dangerSystem.confirmSafe();
        applyDangerOverlayState(state);
        hideDangerOverlay();
    }

    private void loadProtectedView(String fxml) {
        if (dangerSystem.isAlertActive()) {
            return;
        }

        loadView(fxml);
    }

    private void showDangerOverlay(DangerAlertState state) {
        applyDangerOverlayState(state);
        dangerOverlay.setManaged(true);
        dangerOverlay.setVisible(true);
        startDangerCountdown();
    }

    private void hideDangerOverlay() {
        if (dangerAlarmTimeline != null) {
            dangerAlarmTimeline.stop();
        }

        dangerOverlay.setVisible(false);
        dangerOverlay.setManaged(false);
    }

    private void startDangerCountdown() {
        if (dangerAlarmTimeline != null) {
            dangerAlarmTimeline.stop();
        }

        dangerAlarmTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> handleDangerTimeout())
        );
        dangerAlarmTimeline.setCycleCount(Animation.INDEFINITE);
        dangerAlarmTimeline.play();
    }

    private void handleDangerTimeout() {
        DangerAlertState state = dangerSystem.registerNoConfirmation(emergencyManager);
        applyDangerOverlayState(state);

        if (!state.isActive()) {
            hideDangerOverlay();
        }
    }

    private void applyDangerOverlayState(DangerAlertState state) {
        dangerModalMessageLabel.setText(state.getStatusMessage());
        dangerModalLocationLabel.setText(
                state.getLocation() == null || state.getLocation().isBlank()
                        ? "Ubicacion no disponible"
                        : "Ubicacion detectada: " + state.getLocation()
        );
    }
}
