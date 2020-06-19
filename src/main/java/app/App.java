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
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author pickjob@126.com
 * @time 2019-05-13
 **/
public class App extends Application {
    private static final Logger logger = LogManager.getLogger(App.class);
    private static final int MAX_DEEPTH = 1;

    @Override
    public void start(final Stage mainStage) throws Exception {
        Font.loadFont(App.class.getResource("/others/FiraCode-Medium.otf").toExternalForm(), 0);
        URI uri = App.class.getResource("/fxml").toURI();
        List<Path> locations = new ArrayList<>();
        Path path = null;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            path = fileSystem.getPath("/fxml");
        } else {
            path = Paths.get(uri);
        }
        Stream<Path> stream = Files.walk(path, MAX_DEEPTH);
        stream.forEach(p -> {
            if (!Files.isDirectory(p)) {
                locations.add(p);
            }
        });
        TabPane tabPane = new TabPane();
        for (Path location : locations) {
            int selectIdx = -1;
            FXMLLoader loader = new FXMLLoader();
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            loader.setLocation(location.toUri().toURL());
            Parent content = loader.load();
            final BaseController controller = loader.getController();
            controller.init(Context.getInstance());
            Tab tab = new Tab(location.getFileName().toString());
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
