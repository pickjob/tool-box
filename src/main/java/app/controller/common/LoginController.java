package app.controller.common;

import app.common.Context;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-20
 **/
public class LoginController extends BaseController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    @FXML private TextField host;
    @FXML private TextField port;
    @FXML private TextField index;
    @FXML private TextField account;
    @FXML private TextField password;

    @FXML
    public void mouseHandler(MouseEvent mouseEvent) {
        context.getLoginEnumMap().put(Context.LoginKeyEnum.HOST, host.getText());
        context.getLoginEnumMap().put(Context.LoginKeyEnum.PORT, port.getText());
        context.getLoginEnumMap().put(Context.LoginKeyEnum.INDEX, index.getText());
        context.getLoginEnumMap().put(Context.LoginKeyEnum.ACCOUNT, account.getText());
        context.getLoginEnumMap().put(Context.LoginKeyEnum.PASSWORD, password.getText());
        Node  source = (Node)mouseEvent.getSource();
        Context.getInstance().getNextController().run();
        Stage stage  = (Stage)source.getScene().getWindow();
        stage.close();
    }

    @Override
    public void run() {
        host.setText("");
        port.setText("");
        index.setText("");
        account.setText("");
        password.setText("");
    }
}
