module pickjob.tool.box {
    // javafx
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    // log4j
    requires org.apache.logging.log4j;
    requires org.apache.commons.lang3;
    // lettuce
    requires lettuce.core;

    opens app to javafx.graphics
            , javafx.fxml
            ;
    opens app.controller to javafx.graphics
            , javafx.fxml
            ;
    opens app.controller.common to javafx.graphics
            , javafx.fxml
            ;
    opens app.data.redis to javafx.base
            ;
}