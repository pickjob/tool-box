package app.components;

import app.config.Config;
import app.controller.common.BaseController;
import app.enums.LoginKey;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.EnumMap;
import java.util.ResourceBundle;

/**
 * @author: pickjob@126.com
 * @date: 2020-04-20
 **/
public class LoginController extends BaseController implements Initializable {
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    private Config config;
    @FXML private ComboBox<String> profile;
    @FXML private TextField host;
    @FXML private TextField port;
    @FXML private TextField index;
    @FXML private TextField account;
    @FXML private TextField password;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Override
    public boolean isNeedLogin() {
        return false;
    }

    public EnumMap<LoginKey, String> getResult() {
        EnumMap<LoginKey, String> loginEnumMap = new EnumMap<>(LoginKey.class);
        loginEnumMap.put(LoginKey.HOST, host.getText());
        loginEnumMap.put(LoginKey.PORT, port.getText());
        loginEnumMap.put(LoginKey.INDEX, index.getText());
        loginEnumMap.put(LoginKey.ACCOUNT, account.getText());
        loginEnumMap.put(LoginKey.PASSWORD, password.getText());
        return loginEnumMap;
    }

    public void setConfig(Config config) {
        this.config = config;
        logger.debug("config: {}", this.config);
        if (this.config != null) {
            profile.setValue(config.getConfigProfile());
            profile.setDisable(true);
            EnumMap<LoginKey, String> enumMap = config.loadLoginEnumMapFromFile();
            host.setText(enumMap.get(LoginKey.HOST));
            port.setText(enumMap.get(LoginKey.PORT));
            index.setText(enumMap.get(LoginKey.INDEX));
            account.setText(enumMap.get(LoginKey.ACCOUNT));
            password.setText(enumMap.get(LoginKey.PASSWORD));
        }
    }
}
