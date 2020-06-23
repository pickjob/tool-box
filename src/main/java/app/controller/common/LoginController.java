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

    @Override
    public void init(Context context) {
        super.init(context);
    }

    @FXML
    public void mouseHandler(MouseEvent mouseEvent) {
        context.setLoginEnum(Context.LoginKeyEnum.HOST, host.getText());
        context.setLoginEnum(Context.LoginKeyEnum.PORT, port.getText());
        context.setLoginEnum(Context.LoginKeyEnum.INDEX, index.getText());
        context.setLoginEnum(Context.LoginKeyEnum.ACCOUNT, account.getText());
        context.setLoginEnum(Context.LoginKeyEnum.PASSWORD, password.getText());
        Node  source = (Node)mouseEvent.getSource();
        Context.getInstance().getNextController().run();
        Stage stage  = (Stage)source.getScene().getWindow();
        stage.close();
    }
}
