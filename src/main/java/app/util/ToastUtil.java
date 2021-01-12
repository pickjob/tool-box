package app.util;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author pickjob@126.com
 * @date 2020-12-30
 */
public class ToastUtil {
    private static final Logger logger = LogManager.getLogger(ToastUtil.class);
    private static final Double TIME_TRANSLATE_AND_FADE = 2.0;
    private static final Double TIME_PAUSE = 3.0;

    public static void makeText(Node node, String text) {
        Scene scene = node.getScene();
        Parent oldParent = scene.getRoot();

        Label label = new Label(text);
        label.getStyleClass().add("toast");
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(label);
        BorderPane borderPane = new BorderPane();
        borderPane.setBottom(container);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(node.getScene().getRoot());
        stackPane.getChildren().add(borderPane);
        stackPane.setOnMouseClicked(event -> {
            stackPane.getChildren().remove(oldParent);
            scene.setRoot(oldParent);
        });

        scene.setRoot(stackPane);

        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(TIME_TRANSLATE_AND_FADE));
        translateTransition.setFromY(20);
        translateTransition.setToY(0);

        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(TIME_PAUSE));

        SequentialTransition sequentialTransition = new SequentialTransition(translateTransition, pauseTransition);

        FadeTransition fade = new FadeTransition(Duration.seconds(TIME_TRANSLATE_AND_FADE));
        fade.setFromValue(0.3);
        fade.setToValue(1);

        ParallelTransition parallelTransition = new ParallelTransition(label, sequentialTransition, fade);
        parallelTransition.setOnFinished(actionEvent -> {
            stackPane.getChildren().remove(oldParent);
            scene.setRoot(oldParent);
        });

        parallelTransition.play();
    }
}
