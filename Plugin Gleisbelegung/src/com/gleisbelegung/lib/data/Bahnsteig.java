package com.gleisbelegung.lib.data;

import com.gleisbelegung.LabelContainer;
import com.gleisbelegung.Plugin;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;

public class Bahnsteig extends Plugin {
    private ArrayList<LabelContainer> spalte;
    private LabelContainer gleisLabel;
    private String name;
    private BooleanProperty sichtbar;
    private boolean hervorgehoben;
    private int orderId;
    private int id;

    public Bahnsteig(String name, int orderId){
        this.name = name;
        this.sichtbar = new SimpleBooleanProperty(true);
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
                ", gleisName='" + name + '\'' +
                ", sichtbar=" + sichtbar +
                ", hervorgehoben=" + hervorgehoben +
                ", orderId=" + orderId +
                ", id=" + id +
                '}';
    }
}
