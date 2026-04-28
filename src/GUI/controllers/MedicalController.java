package GUI.controllers;

import Resources.Model.MedicalInfo;
import Resources.Session.UserSession;
import Resources.User.UserData;
import Services.MedicalService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class MedicalController {

    @FXML
    private TextArea medicalInfo;

    private final MedicalService medicalService;

    public MedicalController() {
        this.medicalService = new MedicalService();
    }

    @FXML
    public void initialize() {
        try {
            UserData user = UserSession.getUser();

            if (user == null) {
                medicalInfo.setText("No hay usuario en sesion");
                return;
            }

            MedicalInfo info = medicalService.getMedicalInfo(user.getId());

            if (info == null) {
                medicalInfo.setText("No hay informacion medica registrada");
                return;
            }

            medicalInfo.setText(buildMedicalSummary(info));
        } catch (Exception e) {
            medicalInfo.setText("Error al cargar el historial medico");
        }
    }

    private String buildMedicalSummary(MedicalInfo info) {
        return "Alergias: " + nullSafe(info.getAllergies()) + "\n\n"
                + "Condiciones: " + nullSafe(info.getConditions()) + "\n\n"
                + "Medicacion: " + nullSafe(info.getMedications());
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "Sin datos" : value;
    }
}
