package app;

import app.util.Constants;
import app.util.StageUtils;
import fr.brouillard.oss.cssfx.CSSFX;
import fr.brouillard.oss.cssfx.api.URIToPathConverter;
import fr.brouillard.oss.cssfx.impl.URIToPathConverters;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import app.controller.common.BaseController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        Font.loadFont(getClass().getResource("/others/FiraCode-Medium.otf").toExternalForm(), 0);
        TabPane tabPane = new TabPane();
        for (Path location : retriveFxmlPaths()) {
            FXMLLoader loader = new FXMLLoader();
            // 先加载，Controller才实例化
            loader.setLocation(location.toUri().toURL());
            Parent content = loader.load();
            final BaseController controller = loader.getController();
            controller.buildUIComponents();
            Tab tab = new Tab(location.getFileName().toString());
            tab.setContent(content);
            if (controller.isNeedLogin()) {
                tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        Platform.runLater(() -> {
                            createLoginStageAndShow(mainStage, controller);
                        });
                    }
                });
            }
            tabPane.getTabs().add(tab);
        }
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(tabPane.getTabs().size() - 1);

        Scene scene = new Scene(tabPane);
        scene.getStylesheets().addAll(Constants.loadStyleSheets());
        mainStage.setTitle("My Personal Tool Box");
        mainStage.setScene(scene);

        StageUtils.halfScreeStage(mainStage, true);

        mainStage.show();
    }

    private void createLoginStageAndShow(Stage mainStage, BaseController parentController) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/common/login.fxml"));
            Parent content = loader.load();
            BaseController controller = loader.getController();
            controller.setEnv(parentController);
            controller.buildUIComponents();
            controller.init();
            Scene loginScene = new Scene(content);
            loginScene.getStylesheets().addAll(Constants.loadStyleSheets());
            Stage loginStage = new Stage();
            StageUtils.quarterScreeStage(loginStage, false);
            loginStage.setScene(loginScene);
            loginStage.setTitle("Login");
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(mainStage );
            loginStage.showAndWait();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private List<Path> retriveFxmlPaths() {
        List<Path> locations = new ArrayList<>();
        try {
            URI uri = getClass().getResource("/fxml").toURI();
            Path path = null;
            if ("jar".equals(uri.getScheme())) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
                path = fileSystem.getPath("/fxml");
            } else {
                path = Paths.get(uri);
            }
            Stream<Path> stream = Files.walk(path, MAX_DEEPTH);
            stream.sorted((p1, p2) -> {
                return p1.getFileName().compareTo(p2.getFileName());
            }).forEach(p -> {
                if (!Files.isDirectory(p)) {
                    locations.add(p);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return locations;
    }

    public static void main(String... args){
        URIToPathConverter fileSystemConverter = new URIToPathConverter() {
            @Override
            public Path convert(String uri) {
                System.out.println(uri);
                Matcher m = Pattern.compile("file:/(.*\\.css)").matcher(uri);
                if (m.matches()) {
                    String path = m.group(1);
                    return Paths.get(path);
                }
                return null;
            }
        };
        CSSFX.CSSFXConfig config = CSSFX.addConverter(fileSystemConverter);
        for (URIToPathConverter converter : URIToPathConverters.DEFAULT_CONVERTERS) {
            config.addConverter(converter);
        }
        config.start();
        launch(args);
    }
}
