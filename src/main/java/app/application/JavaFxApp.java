package app.application;

import app.components.LoginDialog;
import app.config.Config;
import app.controller.common.BaseController;
import app.util.Constants;
import app.util.ResourceUtils;
import app.util.StageUtils;
import fr.brouillard.oss.cssfx.CSSFX;
import fr.brouillard.oss.cssfx.api.URIToPathConverter;
import fr.brouillard.oss.cssfx.impl.URIToPathConverters;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pickjob@126.com
 * @date 2019-05-13
 **/
public class JavaFxApp extends Application {
    private static final Logger logger = LogManager.getLogger(JavaFxApp.class);
    private static final Pattern cssFilePattern = Pattern.compile("file:/(.*\\.css)");

    @Override
    public void start(Stage mainStage) throws Exception {
        configCssfx();
        loadFont();
        TabPane tabPane = new TabPane();
        for (Path location : ResourceUtils.loadClasspathResourceAsPaths(Constants.FXML_PATH)) {
            FXMLLoader loader = new FXMLLoader();
            // 先加载, Controller才实例化
            loader.setLocation(location.toUri().toURL());
            Parent content = loader.load();
            BaseController controller = loader.getController();
            Tab tab = new Tab(location.getFileName().toString());
            tab.setContent(content);
            if (controller.isNeedLogin()) {
                tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        Platform.runLater(() -> {
                            Config config = controller.loadDefaultConfig();
                            LoginDialog loginDialog = new LoginDialog(config);
                            loginDialog.showAndWait()
                                    .ifPresent(controller::loadWithLoginEnumMap);
                        });
                    }
                });
            }
            tabPane.getTabs().add(tab);
        }
//        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
//        selectionModel.select(tabPane.getTabs().size() - 1);

        Scene scene = new Scene(tabPane);
        scene.getStylesheets().add(ResourceUtils.loadClasspathResourceAsString(Constants.GLOBAL_CSS_PATH));
        mainStage.setTitle("My Personal Tool Box");
        mainStage.setScene(scene);

        StageUtils.halfScreeStage(mainStage, true);

        mainStage.show();
    }

    private void configCssfx() {
        URIToPathConverter fileSystemConverter = new URIToPathConverter() {
            @Override
            public Path convert(String uri) {
                logger.info(uri);
                Matcher m = cssFilePattern.matcher(uri);
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
    }

    private void loadFont() {
        Font.loadFont(ResourceUtils.loadClasspathResourceAsString(Constants.FONT_PATH), 0);
        Font.getFamilies().forEach(fontFamily -> {
            if (logger.isDebugEnabled()) {
                logger.debug("font family: {}", fontFamily);
            }
        });
    }
}
