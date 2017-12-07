package com.gleisbelegung.plugin;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Gleis extends Plugin_Gleisbelegung {
    private ArrayList<LabelContainer> spalte;
    private LabelContainer gleisLabel;
    private String gleisName;
    private boolean sichtbar;
    private boolean hervorgehoben;
    private int orderId;
    private int id;

    public Gleis(String gleisName, int orderId){
        this.gleisName = gleisName;
        this.sichtbar = true;
        this.orderId = orderId;
        id = orderId;

        hervorgehoben = false;
    }

    public ArrayList<LabelContainer> getSpalte() {
        return spalte;
    }
    public void setSpalte(ArrayList<LabelContainer> spalte) {
        this.spalte = spalte;
    }

    public String getGleisName() {
        return gleisName;
    }
    public void setGleisName(String gleisName) {
        this.gleisName = gleisName;
    }

    public boolean isSichtbar() {
        return sichtbar;
    }
    public void setSichtbar(boolean sichtbar) {
        this.sichtbar = sichtbar;
    }

    public LabelContainer getGleisLabel() {
        return gleisLabel;
    }
    public void setGleisLabel(LabelContainer gleisLabel) {
        gleisLabel.getLabel().setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY){
                hebeHervor();
            } else if(e.getButton() == MouseButton.SECONDARY){
                aendereReihenfolge();
            }
        });

        this.gleisLabel = gleisLabel;
    }

    public void setLabelContainerToWith(int width){
        for(LabelContainer lc : spalte){
            Platform.runLater(() -> {
                lc.getLabel().setMaxWidth(width);
                lc.getLabel().setPrefWidth(width);
                lc.getLabel().setMinWidth(width);
            });
        }

        gleisLabel.getLabel().setMaxWidth(width);
        gleisLabel.getLabel().setPrefWidth(width);
        gleisLabel.getLabel().setMinWidth(width);
    }

    private void hebeHervor(){
        if(hervorgehoben) {
            gleisLabel.getLabel().setStyle(gleisLabel.getLabel().getStyle() + "; -fx-background-color: #303030");
            for(LabelContainer lc : spalte){
                lc.setHervorhebungDurchGleis(false);
            }

            hervorgehoben = false;
        }
        else {
            gleisLabel.getLabel().setStyle(gleisLabel.getLabel().getStyle() + "; -fx-background-color: #181818");

            for(LabelContainer lc : spalte){
                lc.setHervorhebungDurchGleis(true);
            }

            hervorgehoben = true;
        }
    }
    public boolean getHebeHervor(){
        return hervorgehoben;
    }

    public int getOrderId(){
        return orderId;
    }

    private void aendereReihenfolge(){
        Stage stage = new Stage();

        Label l = new Label("Reihenfolge festlegen:");
        l.setStyle("-fx-text-fill: white");
        l.setFont(Font.font(settingsFontSize));
        l.setTranslateY(25);
        l.setTranslateX(25);

        TextField tf = new TextField(String.valueOf(orderId+1));
        tf.setFont(Font.font(settingsFontSize-3));
        tf.setTranslateX(25);
        tf.setTranslateY(60);

        Button b = new Button("Speichern");
        b.setFont(Font.font(settingsFontSize));
        b.setTranslateX(25);
        b.setTranslateY(120);
        b.setOnAction(e -> {
            orderId = Integer.parseInt(tf.getText())-1;
            stage.close();
            Plugin_Gleisbelegung.sortiereGleiseListener();
        });

        Pane p = new Pane(l,tf,b);
        p.setStyle("-fx-background-color: #303030");
        p.setMinSize(500,200);
        p.setMaxSize(500, 200);

        Scene scene = new Scene(p, 300,200);

        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);
    }

    public int getId() {
        return id;
    }
}
