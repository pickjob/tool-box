package app;

import app.common.Context;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import app.controller.common.BaseController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author pickjob@126.com
 * @time 2019-05-13
 **/
public class App extends Application {
    private static final Logger logger = LogManager.getLogger(App.class);

    @Override
    public void start(final Stage mainStage) throws Exception {
        Font.loadFont(App.class.getResource("/others/FiraCode-Medium.otf").toExternalForm(), 0);
        File fxmlDirectory = new File(App.class.getResource("/fxml").getPath());
        TabPane tabPane = new TabPane();
        if (fxmlDirectory.isDirectory()) {
            File[] files = fxmlDirectory.listFiles();
            Arrays.sort(files, (f1, f2) -> {
                return f1.getName().compareTo(f2.getName());
            });
            int selectIdx = -1;
            for (File f : files) {
                if (f.isDirectory()) {
                    continue;
                }
                FXMLLoader loader = new FXMLLoader();
                loader.setBuilderFactory(new JavaFXBuilderFactory());
                loader.setLocation(f.toURI().toURL());
                Parent content = loader.load();
                final BaseController controller = loader.getController();
                controller.init(Context.getInstance());
                Tab tab = new Tab(f.getName());
                tab.setContent(content);
                if (controller.isNeedLogin()) {
                    tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            Stage loginStage = createLoginStage(mainStage);
                            Platform.runLater(() -> {
                                Context.getInstance().setNextController(controller);
                                loginStage.show();
                            });
                        }
                    });
                }
                tabPane.getTabs().add(tab);
                selectIdx++;
            }
            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            selectionModel.select(selectIdx);
        }
        Scene scene = new Scene(tabPane);
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        scene.getStylesheets().add(App.class.getResource("/css/global.css").toExternalForm());
        mainStage.setTitle("My Personal Tool Box");
        mainStage.setScene(scene);
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        mainStage.setX(bounds.getWidth() / 4);
        mainStage.setY(bounds.getHeight() / 4);
        mainStage.setWidth(bounds.getWidth() / 2);
        mainStage.setHeight(bounds.getHeight() / 2);

        mainStage.show();
    }

    private Stage createLoginStage(Stage mainStage) {
        Stage loginStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(App.class.getResource("/fxml/common/login.fxml"));
        Parent content = null;
        try {
            content = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final BaseController controller = loader.getController();
        controller.init(Context.getInstance());
        Scene loginScene = new Scene(content);
        loginScene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        loginScene.getStylesheets().add(App.class.getResource("/css/global.css").toExternalForm());
        loginStage.setScene(loginScene);
        loginStage.setTitle("Login");
        loginStage.initModality(Modality.WINDOW_MODAL);
        loginStage.initOwner(mainStage );
        return loginStage;
    }

    public static void main(String... args){
        launch(args);
    }
}
