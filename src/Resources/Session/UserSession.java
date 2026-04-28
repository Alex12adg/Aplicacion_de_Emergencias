package Resources.Session;

import Resources.User.UserData;

public class UserSession {

    private static UserData currentUser;
    private static boolean isNewUser = false;

    public static void setUser(UserData user) {
        currentUser = user;
    }

    public static UserData getUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
        isNewUser = false;
    }

    public static void setNewUser(boolean value) {
        isNewUser = value;
    }

    public static boolean isNewUser() {
        return isNewUser;
    }
}