package com.gleisbelegung.plugin;

import javafx.application.Platform;
import java.util.ArrayList;

public class Gleis extends Plugin_Gleisbelegung {
    private ArrayList<LabelContainer> spalte;
    private LabelContainer gleisLabel;
    private String gleisName;
    private boolean sichtbar;
    private boolean hervorgehoben;

    public Gleis(String gleisName){
        this.gleisName = gleisName;
        this.sichtbar = true;

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
        gleisLabel.getLabel().setOnMouseClicked(e -> hebeHervor());

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
}
