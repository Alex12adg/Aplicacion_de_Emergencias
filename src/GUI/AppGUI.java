package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppGUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/Views/Login-view.fxml")
        );

        Scene scene = new Scene(loader.load(), AppLayout.MOBILE_WIDTH, AppLayout.MOBILE_HEIGHT);

        stage.setTitle("Sistema de Emergencias");
        stage.setScene(scene);
        stage.show();
    }
}
