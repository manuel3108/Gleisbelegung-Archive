package com.gleisbelegung.lib.data;

import com.gleisbelegung.Einstellungen;
import com.gleisbelegung.LabelContainer;
import com.gleisbelegung.Plugin;
import javafx.application.Platform;
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
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

public class Bahnsteig extends Plugin implements Comparable<Bahnsteig> {
    private LabelContainer gleisLabel;
    private String name;
    private BooleanProperty sichtbar;
    private boolean hervorgehoben;
    private int orderId;
    private int id;
    private Bahnhof bahnhof;
    private boolean sorting = false;
    private List<Bahnsteig> nachbarn;

    public Bahnsteig(Bahnhof b, String name, int orderId){
        this.bahnhof = b;
        this.name = name;
        this.sichtbar = new SimpleBooleanProperty(true);
        this.orderId = orderId;
        id = orderId;

        hervorgehoben = false;

        nachbarn = new ArrayList<Bahnsteig>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public BooleanProperty getSichtbarProperty() {
        return sichtbar;
    }

    public boolean isSichtbar() {
        return sichtbar.get();
    }
    public void setSichtbar(boolean sichtbar) {
        this.sichtbar.set(sichtbar);
    }

    public LabelContainer getGleisLabel() {
        return gleisLabel;
    }
    public void setGleisLabel(LabelContainer gleisLabel) {
        this.gleisLabel = gleisLabel;

        this.gleisLabel.getLabel().setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY){
                hebeHervor();
            } else if(e.getButton() == MouseButton.SECONDARY){
                aendereReihenfolge();
            }
        });
    }

    public void setLabelContainerToWith(int width){
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

    public void hebeHervor(){
        /*if(hervorgehoben) {
            gleisLabel.getLabel().setStyle(gleisLabel.getLabel().getStyle() + "; -fx-background-color: #303030");
            for(LabelContainer lc : spalte){
                lc.setHervorhebungDurchGleis(false);
            }

            hervorgehoben = false;
        } else {
            gleisLabel.getLabel().setStyle(gleisLabel.getLabel().getStyle() + "; -fx-background-color: #181818");

            for(LabelContainer lc : spalte){
                lc.setHervorhebungDurchGleis(true);
            }

            hervorgehoben = true;
        }*/
    }
    public boolean getHebeHervor(){
        return hervorgehoben;
    }

    public int getOrderId(){
        return orderId;
    }
    public void setOrderId(int orderId){
        this.orderId = orderId;
    }

    public int getId() {
        return id;
    }

    public Bahnhof getBahnhof() {
        return bahnhof;
    }

    // Sorting active on this bahnsteig
    void setSortActive() {
      this.sorting = true;
    }

    void resetSortActive() {
      this.sorting = false;
    }

    @Override
    public int compareTo(final Bahnsteig o) {
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


    private void aendereReihenfolge(){
        Einstellungen.fenster.gleisbelegung.zeigeOrderIds();

        Stage stage = new Stage();

        Label l = new Label("Reihenfolge festlegen:");
        l.setStyle("-fx-text-fill: white;");
        l.setFont(Font.font(Einstellungen.schriftgroesse));
        l.setTranslateY(25);
        l.setTranslateX(25);

        TextField tf = new TextField(String.valueOf(orderId+1));
        tf.setFont(Font.font(Einstellungen.schriftgroesse-3));
        tf.setTranslateX(25);
        tf.setTranslateY(60);

        Button b = new Button("Speichern");
        b.setFont(Font.font(Einstellungen.schriftgroesse));
        b.setTranslateX(25);
        b.setTranslateY(120);
        b.setOnAction(e -> {
            Bahnsteig.this.setOrderId(Integer.parseInt(tf.getText()) - 1);
            Bahnsteig.this.setSortActive();
            stage.close();
            Einstellungen.fenster.gleisbelegung.versteckeOrderIds();
            Einstellungen.fenster.gleisbelegung.sortiereGleise(new Runnable() {
              public void run() {
                Bahnsteig.this.resetSortActive();
              }
            });
        });

        Pane p = new Pane(l,tf,b);
        p.setStyle("-fx-background-color: #303030;");
        p.setMinSize(500,200);
        p.setMaxSize(500, 200);

        Scene scene = new Scene(p, 300,200);

        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);

        stage.setOnCloseRequest(e -> {
            Einstellungen.fenster.gleisbelegung.versteckeOrderIds();
        });
    }

    @Override
    public String toString() {
        return "Bahnsteig{" +
                ", gleisLabel=" + gleisLabel +
                ", gleisName='" + name + '\'' +
                ", sichtbar=" + sichtbar +
                ", hervorgehoben=" + hervorgehoben +
                ", orderId=" + orderId +
                ", id=" + id +
                '}';
    }

    public boolean getSortingActive() {
    return this.sorting;
  }

    public static void applySorting(SortedSet<Bahnsteig> bahnsteige) {
      for(Bahnsteig bahnsteig : bahnsteige) {
        bahnsteig.setSortActive();
        bahnsteig.aendereReihenfolge();
      }
  }

    public List<Bahnsteig> getNachbarn() {
        return nachbarn;
    }

    public void setNachbarn(List<Bahnsteig> nachbarn) {
        this.nachbarn = nachbarn;
    }
}
