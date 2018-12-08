package com.gleisbelegung.lib.data;

import com.gleisbelegung.Settings;
import com.gleisbelegung.LabelContainer;
import com.gleisbelegung.Plugin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;


public class Platform extends Plugin implements Comparable<Platform> {

    private LabelContainer gleisLabel;
    private String name;
    private BooleanProperty visible;
    private boolean highlighted;
    private int orderId;
    private int id;
    private Station station;
    private boolean sorting = false;
    private List<Platform> neighbors;

    public Platform(Station b, String name, int orderId) {
        this.station = b;
        this.name = name;
        this.visible = new SimpleBooleanProperty(true);
        this.orderId = orderId;
        id = orderId;

        highlighted = false;

        neighbors = new ArrayList<Platform>();
    }

    public static void applySorting(SortedSet<Platform> bahnsteige) {
        for (Platform platform : bahnsteige) {
            platform.setSortActive();
            platform.aendereReihenfolge();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BooleanProperty getSichtbarProperty() {
        return visible;
    }

    public boolean getVisible() {
        return visible.get();
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }

    public LabelContainer getGleisLabel() {
        return gleisLabel;
    }

    public void setGleisLabel(LabelContainer gleisLabel) {
        this.gleisLabel = gleisLabel;

        this.gleisLabel.getLabel().setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                hebeHervor();
            } else if (e.getButton() == MouseButton.SECONDARY) {
                aendereReihenfolge();
            }
        });
    }

    public void setLabelContainerToWith(int width) {
        /*for(LabelContainer lc : spalte){
            Platform.runLater(() -> {
                lc.getLabel().setMaxWidth(width);
                lc.getLabel().setPrefWidth(width);
                lc.getLabel().setMinWidth(width);
            });
        }

        Platform.runLater(() -> {
            gleisLabel.getLabel().setMaxWidth(width);
            gleisLabel.getLabel().setPrefWidth(width);
            gleisLabel.getLabel().setMinWidth(width);
        });*/
    }

    public void hebeHervor() {
        /*if(highlighted) {
            gleisLabel.getLabel().setStyle(gleisLabel.getLabel().getStyle() + "; -fx-background-color: #303030");
            for(LabelContainer lc : spalte){
                lc.setHervorhebungDurchGleis(false);
            }

            highlighted = false;
        } else {
            gleisLabel.getLabel().setStyle(gleisLabel.getLabel().getStyle() + "; -fx-background-color: #181818");

            for(LabelContainer lc : spalte){
                lc.setHervorhebungDurchGleis(true);
            }

            highlighted = true;
        }*/
    }

    public boolean getHebeHervor() {
        return highlighted;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getId() {
        return id;
    }

    public Station getStation() {
        return station;
    }

    // Sorting active on this bahnsteig
    void setSortActive() {
        this.sorting = true;
    }

    void resetSortActive() {
        this.sorting = false;
    }

    @Override public int compareTo(final Platform o) {
        if (this == o) {
            return 0;
        }
        int cmp;
        cmp = this.getOrderId() - o.getOrderId();
        if (cmp != 0) {
            return cmp;
        }

        // code should never apply below
        cmp = this.getId() - this.getOrderId();
        if (cmp != 0) {
            return cmp;
        }
        cmp = o.getOrderId() - o.getId();
        return cmp;
    }

    private void aendereReihenfolge() {
        Settings.window.gleisbelegung.zeigeOrderIds();

        Stage stage = new Stage();

        Label l = new Label("Reihenfolge festlegen:");
        l.setStyle("-fx-text-fill: white;");
        l.setFont(Font.font(Settings.fontSize));
        l.setTranslateY(25);
        l.setTranslateX(25);

        TextField tf = new TextField(String.valueOf(orderId + 1));
        tf.setFont(Font.font(Settings.fontSize - 3));
        tf.setTranslateX(25);
        tf.setTranslateY(60);

        Button b = new Button("Speichern");
        b.setFont(Font.font(Settings.fontSize));
        b.setTranslateX(25);
        b.setTranslateY(120);
        b.setOnAction(e -> {
            Platform.this.setOrderId(Integer.parseInt(tf.getText()) - 1);
            Platform.this.setSortActive();
            stage.close();
            Settings.window.gleisbelegung.versteckeOrderIds();
            Settings.window.gleisbelegung.sortiereGleise(new Runnable() {

                public void run() {
                    Platform.this.resetSortActive();
                }
            });
        });

        Pane p = new Pane(l, tf, b);
        p.setStyle("-fx-background-color: #303030;");
        p.setMinSize(500, 200);
        p.setMaxSize(500, 200);

        Scene scene = new Scene(p, 300, 200);

        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);

        stage.setOnCloseRequest(e -> {
            Settings.window.gleisbelegung.versteckeOrderIds();
        });
    }

    @Override public String toString() {
        return "Platform{" + ", gleisLabel=" + gleisLabel + ", gleisName='"
                + name + '\'' + ", visible=" + visible + ", highlighted="
                + highlighted + ", orderId=" + orderId + ", id=" + id + '}';
    }

    public boolean getSortingActive() {
        return this.sorting;
    }

    public List<Platform> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Platform> neighbors) {
        this.neighbors = neighbors;
    }
}
