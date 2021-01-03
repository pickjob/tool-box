package app.components;

import app.data.TreeData;
import app.util.Constants;
import app.util.ResourceUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author pickjob@126.com
 * @date 2021-01-03
 **/
public class DetailDialog extends Dialog<Void> {
    private static final Logger logger = LogManager.getLogger(DetailDialog.class);

    public DetailDialog(TreeData data) {
        try {
            DialogPane dialogPane = getDialogPane();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ResourceUtils.loadClasspathResourceAsURL(Constants.FXML_DETAIL_PATH));
            Parent content = loader.load();
            DetailController detailController = loader.getController();
            detailController.loadWithData(data);

            dialogPane.setContent(content);
            dialogPane.getStyleClass().add("detail-dialog");
            dialogPane.getStylesheets().add(ResourceUtils.loadClasspathResourceAsString(Constants.COMPONENT_CSS_PATH));

            dialogPane.getButtonTypes().addAll(ButtonType.OK);
            setTitle("详情");
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }
    }
}
