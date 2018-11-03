package com.gleisbelegung.lib.data;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Speichert alle Daten für einen Zug
 */

import com.gleisbelegung.Einstellungen;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class Zug {
    private int zugId;                  //einmalige Zugidentifikationsnummer
    private String zugName;             //Name des Zuges
    private int verspaetung;            //Verspätung des Zuges
    private Bahnsteig gleis;               //Bahnsteig auf welchem sich der zug befindet/ zuletzt befand
    private Bahnsteig plangleis;           //Auf welchem Bahnsteig der Zug eigentlich halten sollte
    private boolean amGleis;            //Ob sich der Zug gerade am Bahnsteig befindet oder nicht
    private String von;                 //Einfahrt des Zuges in das Stellwerk
    private String nach;                //Ausfahrt des Zuges aus dem Stellwerk
    private boolean sichtbar;           //Ist der Zug aktuell im Stellwerksichbar
    private ArrayList<FahrplanHalt> fahrplan;    //Speichert alle Fahrplanhalt des Zuges aus der @FahrplanHalt-Klasse
    private boolean needUpdate;         //Wenn der Zug ein Update benötigt, dann wird er in @Fenster.drawTrain() neu gezeichnet
    private boolean newTrain;           //Wenn ein Zug gerade neu in die Liste zuege aus @Main aufgenommen wurde ist dieser Wert auf true
    private Label stellwerksUebersichtLabel;

    //Setzten der Daten
    public Zug(int zugId, String zugName){
        this.zugId = zugId;
        this.zugName = zugName;

        this.newTrain = true;
        this.needUpdate = true;

        stellwerksUebersichtLabel = new Label(zugName);
        stellwerksUebersichtLabel.setStyle("-fx-text-fill: #fff;");
        stellwerksUebersichtLabel.setFont(Font.font(Einstellungen.schriftgroesse));
    }

    //get-set Zugname
    public String getZugName() {
        return zugName;
    }
    public void setZugName(String zugName) {
        this.zugName = zugName;
    }

    //get-set ZugId
    public int getZugId() {
        return zugId;
    }
    public void setZugId(int zugId) {
        this.zugId = zugId;
    }

    //get-set Verspätung
    public int getVerspaetung() {
        return verspaetung;
    }
    public void setVerspaetung(int verspaetung) {
        this.verspaetung = verspaetung;
    }

    //get-set Bahnsteig
    public Bahnsteig getBahnsteig() {
        return gleis;
    }
    public void setBahnsteig(Bahnsteig gleis) {
        this.gleis = gleis;
    }

    //get-set Plangleis
    public Bahnsteig getPlangleis() {
        return plangleis;
    }
    public void setPlangleis(Bahnsteig plangleis) {
        this.plangleis = plangleis;
    }

    //get-set am-gleis
    public boolean getAmGleis() {
        return amGleis;
    }
    public void setAmGleis(boolean amGleis) {
        this.amGleis = amGleis;
    }

    //get-set von
    public String getVon() {
        return von;
    }
    public void setVon(String von) {
        this.von = von;
    }

    //get-set nach
    public String getNach() {
        return nach;
    }
    public void setNach(String nach) {
        this.nach = nach;
    }

    //get-set sichtbar
    public boolean getSichtbar() {
        return sichtbar;
    }
    public void setSichtbar(boolean sichtbar) {
        this.sichtbar = sichtbar;
    }

    //get-set FahrplanHalt
    public ArrayList<FahrplanHalt> getFahrplan() {
        return fahrplan;
    }
    public FahrplanHalt getFahrplan(int index) {
        if(fahrplan != null && index < fahrplan.size()){
            return fahrplan.get(index);
        } else {
            return null;
        }
    }
    public void setFahrplan(ArrayList<FahrplanHalt> fahrplan) {
        this.fahrplan = fahrplan;
    }

    //get-set needUpdate
    public boolean isNeedUpdate() {
        return needUpdate;
    }
    public void setNeedUpdate(boolean needsUpdate) {
        this.needUpdate = needsUpdate;
    }

    //get-set newTrain
    public boolean isNewTrain() {
        return newTrain;
    }
    public void setNewTrain(boolean newTrain) {
        this.newTrain = newTrain;
    }

    //Entfernen den Zug aus dem LabelContainer (nur wenn hier kein Wechsel VON einem anderen Zug stattfindet)
    public void removeFromGrid(){
        if(fahrplan != null){
            for(FahrplanHalt fh : fahrplan){
                if (fh != null && fh.getVorgaenger() == null) {
                    fh.removeDrawnTo();
                }
            }
        }
    }

    //Formatiert die Verspätung zu einem String
    public String getVerspaetungToString(){
        String out = "";

        if (verspaetung > 0) {
            out = " (+" + verspaetung + ")";
        } else if (verspaetung < 0) {
            out = " (" + verspaetung + ")";
        }

        return out;
    }

    @Override
    public String toString() {
        return "Zug{" +
                "zugId=" + zugId +
                ", zugName='" + zugName + '\'' +
                ", verspaetung=" + verspaetung +
                ", gleis='" + gleis + '\'' +
                ", plangleis='" + plangleis + '\'' +
                ", amGleis=" + amGleis +
                ", von='" + von + '\'' +
                ", nach='" + nach + '\'' +
                ", sichtbar=" + sichtbar +
                ", needUpdate=" + needUpdate +
                ", newTrain=" + newTrain +
                '}';
    }

    public Label getStellwerksUebersichtLabel() {
        return stellwerksUebersichtLabel;
    }

    public void setStellwerksUebersichtLabel(Label stellwerksUebersichtLabel) {
        this.stellwerksUebersichtLabel = stellwerksUebersichtLabel;
    }
}