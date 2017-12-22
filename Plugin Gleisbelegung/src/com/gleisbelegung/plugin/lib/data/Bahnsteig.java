package com.gleisbelegung.plugin.lib.data;

import com.gleisbelegung.plugin.LabelContainer;
import com.gleisbelegung.plugin.Plugin;
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

public class Bahnsteig extends Plugin {
    private ArrayList<LabelContainer> spalte;
    private LabelContainer gleisLabel;
    private String gleisName;
    private boolean sichtbar;
    private boolean hervorgehoben;
    private int orderId;
    private int id;

    public Bahnsteig(String gleisName, int orderId){
        this.gleisName = gleisName;
        this.sichtbar = true;
        this.orderId = orderId;
        id = orderId;

        spalte = new ArrayList<>();
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

    public void hebeHervor(){
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
    public void setOrderId(int orderId){
        this.orderId = orderId;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Bahnsteig{" +
                "spalte=" + spalte +
                ", gleisLabel=" + gleisLabel +
                ", gleisName='" + gleisName + '\'' +
                ", sichtbar=" + sichtbar +
                ", hervorgehoben=" + hervorgehoben +
                ", orderId=" + orderId +
                ", id=" + id +
                '}';
    }
}
