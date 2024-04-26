module com.tapehat.combat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;


    opens com.tapehat.combat to javafx.fxml;
    exports com.tapehat.combat;
}