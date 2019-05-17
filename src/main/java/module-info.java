module pickjob.tool.box {
    // javafx
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    // log4j
    requires org.apache.logging.log4j;

    opens main.app to javafx.graphics
            , javafx.fxml
            ;
    opens main.controller to javafx.graphics
            , javafx.fxml
            ;
}