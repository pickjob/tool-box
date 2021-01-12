package app.components;

import app.config.Config;
import app.enums.LoginKey;
import app.util.Constants;
import app.util.ResourceUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;

/**
 * @author pickjob@126.com
 * @date 2020-12-31
 */
public class LoginDialog extends Dialog<EnumMap<LoginKey, String>> {
    private static final Logger logger = LogManager.getLogger(LoginDialog.class);

    public LoginDialog(Config config) {
        try {
            DialogPane dialogPane = getDialogPane();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ResourceUtils.loadClasspathResourceAsURL(Constants.FXML_LOGIN_PATH));
            Parent content = loader.load();
            LoginController loginController = loader.getController();
            loginController.setConfig(config);

            dialogPane.setContent(content);
            dialogPane.getStyleClass().add("login-dialog");
            dialogPane.getStylesheets().add(ResourceUtils.loadClasspathResourceAsString(Constants.GLOBAL_CSS_PATH));

            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            setTitle("请登录!");
            setResultConverter(buttonType -> {
                return buttonType == ButtonType.OK ? loginController.getResult() : null;
            });
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }
    }
}
