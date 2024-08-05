module com.ljfriedman.sotnmapvisualization {
    requires javafx.graphics;

    exports com.ljfriedman.sotnmapvisualization;
    opens com.ljfriedman.sotnmapvisualization to javafx.graphics;
}
