package Resources.Emergency;

import Resources.User.UserData;

public class EmergencyEvent {
    private String tipoEmergencia;
    private String ubicacion;
    private UserData usuario;
    private int gravedad;

    public EmergencyEvent(String tipoEmergencia, String ubicacion, UserData usuario, int gravedad) {
        this.tipoEmergencia = tipoEmergencia;
        this.ubicacion = ubicacion;
        this.usuario = usuario;
        this.gravedad = gravedad;
    }

    public String getTipoEmergencia() { return tipoEmergencia; }
    public String getUbicacion() { return ubicacion; }
    public UserData getUsuario() { return usuario; }
    public int getGravedad() { return gravedad; }

    @Override
    public String toString() {
        String nombreUsuario = "Desconocido";

        if (usuario != null && usuario.getNombre() != null && !usuario.getNombre().isBlank()) {
            nombreUsuario = usuario.getNombre();
        }

        return "Emergencia [" + tipoEmergencia + "] en " + ubicacion +
                " | Usuario: " + nombreUsuario +
                " | Gravedad: " + gravedad;
    }
}
