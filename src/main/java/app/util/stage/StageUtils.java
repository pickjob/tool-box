package app.util.stage;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author: pickjob@126.com
 * @time: 2020-06-22
 **/
public class StageUtils {

    public static void halfScreeStage(Stage stage) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setX(bounds.getWidth() / 4);
        stage.setY(bounds.getHeight() / 4);
        stage.setWidth(bounds.getWidth() / 2);
        stage.setHeight(bounds.getHeight() / 2);
    }

    public static void quarterScreeStage(Stage stage) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setX(bounds.getWidth() / 8 * 3);
        stage.setY(bounds.getHeight() / 8 * 3);
        stage.setWidth(bounds.getWidth() / 4);
        stage.setHeight(bounds.getHeight() / 4);
    }
}
