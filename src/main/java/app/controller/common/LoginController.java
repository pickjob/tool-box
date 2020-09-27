package app.controller.common;

import app.config.RedisConfig;
import app.config.ZookeeperConfig;
import app.controller.RedisController;
import app.controller.ZookeeperController;
import app.enums.LoginKeyEnum;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-20
 **/
public class LoginController extends BaseController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    private BaseController previousController;
//    @FXML private TextField profile;
    @FXML private TextField host;
    @FXML private TextField port;
    @FXML private TextField index;
    @FXML private TextField account;
    @FXML private TextField password;

    @Override
    public void buildUIComponents() {
        super.buildUIComponents();
        if (env != null && env instanceof BaseController) {
            previousController = (BaseController)env;
            if (previousController instanceof RedisController) {
                EnumMap<LoginKeyEnum, String> loginEnumMap = RedisConfig.loadConfig();
                if (loginEnumMap != null) {
                    host.setText(loginEnumMap.get(LoginKeyEnum.HOST));
                    port.setText(loginEnumMap.get(LoginKeyEnum.PORT));
                    index.setText(loginEnumMap.get(LoginKeyEnum.INDEX));
                    password.setText(loginEnumMap.get(LoginKeyEnum.PASSWORD));
                }
            } else if (previousController instanceof ZookeeperController) {
                EnumMap<LoginKeyEnum, String> loginEnumMap = ZookeeperConfig.loadConfig();
                if (loginEnumMap != null) {
                    host.setText(loginEnumMap.get(LoginKeyEnum.HOST));
                    port.setText(loginEnumMap.get(LoginKeyEnum.PORT));
                }
            }
        }
    }

    @Override
    public void init() {
        super.init();
    }

    @FXML
    public void mouseHandler(MouseEvent mouseEvent) {
        EnumMap<LoginKeyEnum, String> loginEnumMap = new EnumMap<>(LoginKeyEnum.class);
        loginEnumMap.put(LoginKeyEnum.HOST, host.getText());
        loginEnumMap.put(LoginKeyEnum.PORT, port.getText());
        loginEnumMap.put(LoginKeyEnum.INDEX, index.getText());
        loginEnumMap.put(LoginKeyEnum.ACCOUNT, account.getText());
        loginEnumMap.put(LoginKeyEnum.PASSWORD, password.getText());
        if (previousController instanceof RedisController) {
            RedisConfig.saveConfig(loginEnumMap);
        } else if (previousController instanceof ZookeeperController) {
            ZookeeperConfig.saveConfig(loginEnumMap);
        }
        previousController.setEnv(loginEnumMap);
        previousController.init();
        Node  source = (Node)mouseEvent.getSource();
        Stage stage  = (Stage)source.getScene().getWindow();
        stage.close();
    }
}
