package main.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import main.controller.MainController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author pickjob@126.com
 * @time 2019-05-13
 **/
public class ToolBox extends Application {
    private static final Logger logger = LogManager.getLogger(ToolBox.class);

    static {
        Font.loadFont("others/FiraCode-Medium.otf", 10);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(ToolBox.class.getResource("/fxml/tool-box.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        Scene scene = new Scene(root);
        primaryStage.setTitle("My Personal Tool Box");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String... args){
        launch(args);
    }
}
