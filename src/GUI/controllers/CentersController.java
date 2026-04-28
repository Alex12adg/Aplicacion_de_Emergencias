package GUI.controllers;

import Resources.Emergency.EmergencyCenter;
import Resources.Emergency.EmergencyManager;
import Resources.Location.FacilityLocator;
import Resources.Location.GPSModule;
import Resources.Model.ReservationResource;
import Resources.Session.UserSession;
import Resources.User.UserData;
import Services.ReservationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

public class CentersController {

    private final ObservableList<String> centerItems = FXCollections.observableArrayList();
    private final ObservableList<ReservationResource> publicResourceItems = FXCollections.observableArrayList();
    private final ObservableList<ReservationResource> privateResourceItems = FXCollections.observableArrayList();
    private final GPSModule gpsModule = new GPSModule();
    private final ReservationService reservationService = new ReservationService();
    private EmergencyManager emergencyManager;
    private ReservationResource editingPrivateResource;

    @FXML
    private CheckBox automaticLocationCheck;

    @FXML
    private VBox manualLocationBox;

    @FXML
    private TextField latitudeField;

    @FXML
    private TextField longitudeField;

    @FXML
    private TextField radiusField;

    @FXML
    private Label centersStatusLabel;

    @FXML
    private Label searchSummaryLabel;

    @FXML
    private ListView<String> centersList;

    @FXML
    private TableView<ReservationResource> publicResourcesTable;

    @FXML
    private TableView<ReservationResource> privateResourcesTable;

    @FXML
    private TableColumn<ReservationResource, String> publicNameColumn;

    @FXML
    private TableColumn<ReservationResource, String> publicCategoryColumn;

    @FXML
    private TableColumn<ReservationResource, String> publicLocationColumn;

    @FXML
    private TableColumn<ReservationResource, Integer> publicDurationColumn;

    @FXML
    private TableColumn<ReservationResource, String> privateNameColumn;

    @FXML
    private TableColumn<ReservationResource, String> privateCategoryColumn;

    @FXML
    private TableColumn<ReservationResource, String> privateLocationColumn;

    @FXML
    private TableColumn<ReservationResource, Integer> privateDurationColumn;

    @FXML
    private TextField privateResourceNameField;

    @FXML
    private TextField privateResourceCategoryField;

    @FXML
    private TextField privateResourceLocationField;

    @FXML
    private TextField privateResourceDurationField;

    @FXML
    private TextArea privateResourceDescriptionArea;

    @FXML
    private Label privateResourceFormLabel;

    @FXML
    private Button updatePrivateCenterButton;

    public void setEmergencyManager(EmergencyManager emergencyManager) {
        this.emergencyManager = emergencyManager;
        loadAllCenters();
        loadReservationResources();
    }

    @FXML
    public void initialize() {
        centersList.setItems(centerItems);
        publicResourcesTable.setItems(publicResourceItems);
        privateResourcesTable.setItems(privateResourceItems);
        automaticLocationCheck.setSelected(true);
        privateResourceDurationField.setText("30");
        updateLocationMode();
        centersStatusLabel.setText("Consulta centros con ubicacion automatica o manual y gestiona tus centros privados.");
        searchSummaryLabel.setText("Aun no se ha ejecutado ninguna busqueda.");
        configureResourceTables();
        setEditingPrivateResource(null);

        privateResourcesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                populatePrivateResourceForm(newValue);
                setEditingPrivateResource(newValue);
            }
        });

        loadReservationResources();
    }

    @FXML
    private void updateLocationMode() {
        boolean automatic = automaticLocationCheck.isSelected();
        manualLocationBox.setManaged(!automatic);
        manualLocationBox.setVisible(!automatic);
        latitudeField.setDisable(automatic);
        longitudeField.setDisable(automatic);

        if (automatic) {
            latitudeField.clear();
            longitudeField.clear();
        }
    }

    @FXML
    private void handleFindCenters() {
        if (emergencyManager == null) {
            centersStatusLabel.setText("El sistema de centros no esta disponible.");
            return;
        }

        try {
            double radius = Double.parseDouble(radiusField.getText().trim());
            double[] coordinates = resolveCoordinates();
            List<EmergencyCenter> allCenters = emergencyManager.loadCenters();
            FacilityLocator locator = new FacilityLocator(allCenters);
            List<EmergencyCenter> nearby = locator.findNearby(coordinates[0], coordinates[1], radius);

            centerItems.clear();

            for (EmergencyCenter center : nearby) {
                centerItems.add(formatCenter(center, coordinates[0], coordinates[1]));
            }

            if (centerItems.isEmpty()) {
                centerItems.add("No se han encontrado centros dentro del radio indicado.");
            }

            centersStatusLabel.setText("Busqueda completada.");
            searchSummaryLabel.setText(
                    "Ubicacion: " + String.format("%.5f, %.5f", coordinates[0], coordinates[1])
                            + " | Radio: " + radius + " km | Resultados: " + nearby.size()
            );
        } catch (NumberFormatException e) {
            centersStatusLabel.setText("El radio y las coordenadas deben ser numericos.");
        } catch (IllegalArgumentException e) {
            centersStatusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleShowAllCenters() {
        loadAllCenters();
        searchSummaryLabel.setText("Listado completo de centros cargados en la aplicacion.");
        centersStatusLabel.setText("Mostrando todos los centros disponibles.");
    }

    @FXML
    private void handleAddPrivateCenter() {
        try {
            UserData user = requireUser();
            ReservationResource created = reservationService.createPrivateResource(
                    user.getId(),
                    privateResourceNameField.getText(),
                    privateResourceCategoryField.getText(),
                    privateResourceLocationField.getText(),
                    privateResourceDescriptionArea.getText(),
                    privateResourceDurationField.getText()
            );
            loadReservationResources();
            selectPrivateResource(created.getId());
            clearPrivateResourceForm();
            setEditingPrivateResource(null);
            centersStatusLabel.setText("Centro privado anadido correctamente.");
        } catch (Exception e) {
            centersStatusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleUpdatePrivateCenter() {
        try {
            UserData user = requireUser();
            ReservationResource updated = reservationService.updatePrivateResource(
                    user.getId(),
                    editingPrivateResource,
                    privateResourceNameField.getText(),
                    privateResourceCategoryField.getText(),
                    privateResourceLocationField.getText(),
                    privateResourceDescriptionArea.getText(),
                    privateResourceDurationField.getText()
            );
            loadReservationResources();
            selectPrivateResource(updated.getId());
            centersStatusLabel.setText("Informacion del centro modificada correctamente.");
        } catch (Exception e) {
            centersStatusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleDeletePrivateCenter() {
        try {
            UserData user = requireUser();
            ReservationResource selected = privateResourcesTable.getSelectionModel().getSelectedItem();
            reservationService.deletePrivateResource(user.getId(), selected);
            loadReservationResources();
            clearPrivateResourceForm();
            setEditingPrivateResource(null);
            centersStatusLabel.setText("Centro privado borrado correctamente.");
        } catch (Exception e) {
            centersStatusLabel.setText(e.getMessage());
        }
    }

    private void loadAllCenters() {
        if (emergencyManager == null || centersList == null) {
            return;
        }

        centerItems.clear();

        for (EmergencyCenter center : emergencyManager.loadCenters()) {
            centerItems.add(center.getType() + " | " + center.getName() + " | " + center.getLatitude() + ", " + center.getLongitude());
        }

        if (centerItems.isEmpty()) {
            centerItems.add("No hay centros cargados.");
        }
    }

    private double[] resolveCoordinates() {
        if (automaticLocationCheck.isSelected()) {
            return GPSModule.parseLatLon(gpsModule.getAutoLocation());
        }

        String latitude = latitudeField.getText();
        String longitude = longitudeField.getText();

        if (latitude == null || latitude.isBlank() || longitude == null || longitude.isBlank()) {
            throw new IllegalArgumentException("Debes indicar latitud y longitud manuales.");
        }

        return GPSModule.parseLatLon(latitude.trim() + "," + longitude.trim());
    }

    private String formatCenter(EmergencyCenter center, double lat, double lon) {
        double distance = calculateDistance(lat, lon, center.getLatitude(), center.getLongitude());
        return center.getType() + " | " + center.getName() + " | Distancia aprox: " + String.format("%.2f km", distance);
    }

    private void loadReservationResources() {
        try {
            UserData user = UserSession.getUser();
            reservationService.initializeModule();

            publicResourceItems.setAll(reservationService.getPublicResources());
            privateResourceItems.clear();

            if (user != null && user.getId() > 0) {
                privateResourceItems.setAll(reservationService.getPrivateResourcesByUser(user.getId()));
            }
        } catch (Exception e) {
            centersStatusLabel.setText("No se pudieron cargar los centros reservables.");
        }
    }

    private void configureResourceTables() {
        publicNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        publicCategoryColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCategory()));
        publicLocationColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLocation()));
        publicDurationColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getSlotDurationMinutes()));

        privateNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        privateCategoryColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCategory()));
        privateLocationColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLocation()));
        privateDurationColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getSlotDurationMinutes()));
    }

    private void populatePrivateResourceForm(ReservationResource resource) {
        privateResourceNameField.setText(resource.getName());
        privateResourceCategoryField.setText(resource.getCategory());
        privateResourceLocationField.setText(resource.getLocation());
        privateResourceDurationField.setText(String.valueOf(resource.getSlotDurationMinutes()));
        privateResourceDescriptionArea.setText(resource.getDescription() == null ? "" : resource.getDescription());
    }

    private void clearPrivateResourceForm() {
        privateResourceNameField.clear();
        privateResourceCategoryField.clear();
        privateResourceLocationField.clear();
        privateResourceDurationField.setText("30");
        privateResourceDescriptionArea.clear();
    }

    private void setEditingPrivateResource(ReservationResource resource) {
        this.editingPrivateResource = resource;
        privateResourceFormLabel.setText(resource == null ? "Nuevo centro privado" : "Editar centro privado");
        updatePrivateCenterButton.setDisable(resource == null);
    }

    private void selectPrivateResource(int resourceId) {
        for (ReservationResource resource : privateResourceItems) {
            if (resource.getId() == resourceId) {
                privateResourcesTable.getSelectionModel().select(resource);
                privateResourcesTable.scrollTo(resource);
                return;
            }
        }
    }

    private UserData requireUser() throws Exception {
        UserData user = UserSession.getUser();

        if (user == null || user.getId() <= 0) {
            throw new Exception("Debes iniciar sesion para gestionar centros privados.");
        }

        return user;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
