package Services;

import Resources.Database.UserDAO;
import Resources.User.UserData;

public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserData login(String input, String password) throws Exception {

        if (input == null || input.isEmpty() || password == null || password.isEmpty()) {
            throw new Exception("Campos obligatorios vacios");
        }

        UserData user = userDAO.login(input, password);

        if (user == null) {
            throw new Exception("Usuario o contrasena incorrectos");
        }

        return user;
    }

    public UserData register(String nombre, String phone, String email, String password) throws Exception {

        if (nombre == null || nombre.isEmpty()) {
            throw new Exception("Nombre obligatorio");
        }

        if (email == null || email.isEmpty()) {
            throw new Exception("Email obligatorio");
        }

        if (password == null || password.length() < 4) {
            throw new Exception("Contrasena demasiado corta");
        }

        UserData user = new UserData(nombre, phone, email, password);
        UserData createdUser = userDAO.registerUser(user);

        if (createdUser == null) {
            throw new Exception("Error al registrar usuario");
        }

        return createdUser;
    }

    public UserData updateAccount(UserData currentUser, String currentPassword, String newName,
                                  String newEmail, String newPassword) throws Exception {

        if (currentUser == null || currentUser.getId() <= 0) {
            throw new Exception("No hay usuario en sesion");
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new Exception("Debes introducir la contrasena actual");
        }

        if (!userDAO.verifyPassword(currentUser.getId(), currentPassword)) {
            throw new Exception("La contrasena actual no es correcta");
        }

        String finalName = normalizeOrFallback(newName, currentUser.getNombre());
        String finalEmail = normalizeOrFallback(newEmail, currentUser.getEmail());
        String finalPassword = normalizeOrFallback(newPassword, currentUser.getPassword());

        if (finalName.isBlank()) {
            throw new Exception("El nombre no puede quedar vacio");
        }

        if (finalEmail.isBlank()) {
            throw new Exception("El correo electronico no puede quedar vacio");
        }

        if (finalPassword.length() < 4) {
            throw new Exception("La nueva contrasena debe tener al menos 4 caracteres");
        }

        boolean sameName = finalName.equals(currentUser.getNombre());
        boolean sameEmail = finalEmail.equals(currentUser.getEmail());
        boolean samePassword = finalPassword.equals(currentUser.getPassword());

        if (sameName && sameEmail && samePassword) {
            throw new Exception("No hay cambios para guardar");
        }

        if (userDAO.emailExistsForOtherUser(currentUser.getId(), finalEmail)) {
            throw new Exception("Ese correo electronico ya esta en uso");
        }

        UserData updatedUser = userDAO.updateAccount(currentUser.getId(), finalName, finalEmail, finalPassword);

        if (updatedUser == null) {
            throw new Exception("No se pudo actualizar la cuenta");
        }

        return updatedUser;
    }

    public void deleteAccount(UserData currentUser, String currentPassword) throws Exception {

        if (currentUser == null || currentUser.getId() <= 0) {
            throw new Exception("No hay usuario en sesion");
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new Exception("Debes introducir la contrasena actual");
        }

        if (!userDAO.verifyPassword(currentUser.getId(), currentPassword)) {
            throw new Exception("La contrasena actual no es correcta");
        }

        boolean deleted = userDAO.deleteUserAccount(currentUser.getId());

        if (!deleted) {
            throw new Exception("No se pudo eliminar la cuenta");
        }
    }

    private String normalizeOrFallback(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback == null ? "" : fallback.trim();
        }

        return value.trim();
    }
}
