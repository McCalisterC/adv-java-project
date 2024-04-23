module com.tapehat.combat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.tapehat.combat to javafx.fxml;
    exports com.tapehat.combat;
}