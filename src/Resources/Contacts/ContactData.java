package Resources.Contacts;

public class ContactData {

    private String nombre;
    private String telefono;
    private String relacion;

    public ContactData(String nombre, String telefono, String relacion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.relacion = relacion;
    }

    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getRelacion() { return relacion; }
}