package com.gleisbelegung.lib.data;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Speichert alle Daten für einen Train
 */

import com.gleisbelegung.Settings;
import de.heidelbach_net.util.XML;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;


public class Train {

    private int trainId;                  //einmalige Zugidentifikationsnummer
    private String trainName;             //Name des Zuges
    private int delay;            //Verspätung des Zuges
    private Platform platform;
    //Platform auf welchem sich der zug befindet/ zuletzt befand
    private Platform scheduledPlatform;
    //Auf welchem Platform der Train eigentlich halten sollte
    private boolean atPlatform;
    //Ob sich der Train gerade am Platform befindet oder nicht
    private String from;                 //Einfahrt des Zuges in das SignalBox
    private String to;                //Ausfahrt des Zuges aus dem SignalBox
    private boolean visible;
    //Ist der Train aktuell im Stellwerksichbar
    private List<TrainStop> schedule;
    //Speichert alle Fahrplanhalt des Zuges aus der @TrainStop-Klasse
    private boolean newTrain;
    //Wenn ein Train gerade neu in die Liste zuege aus @Main aufgenommen wurde ist dieser Wert auf true
    private Label stellwerksUebersichtLabel;
    private boolean schwerwiegendesUpdate;

    /**
     * Ziel from E/F
     */
    private Train predecessor;

    /**
     * Ausgangspunkt from E/K
     */
    private Train successor;

    //Setzten der Daten
    public Train(int trainId, String trainName) {
        this.trainId = trainId;
        this.trainName = trainName;

        this.newTrain = true;

        schedule = new ArrayList<TrainStop>();

        stellwerksUebersichtLabel = new Label(trainName);
        stellwerksUebersichtLabel.setStyle("-fx-text-fill: #fff;");
        stellwerksUebersichtLabel
                .setFont(Font.font(Settings.fontSize));
        schwerwiegendesUpdate = false;
    }

    public static Train parseXml(final XML xmlZug) {
        final Integer id = Integer.valueOf(xmlZug.get("zid"));
        final String name = xmlZug.get("name");
        assert id != null;
        assert name != null;
        return new Train(id, name);
    }

    //get-set Zugname
    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    //get-set ZugId
    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    //get-set Verspätung
    public int getVerspaetungInMinuten() {
        return delay;
    }

    public long getVerspaetungInMiliSekunden() {
        return delay * 1000 * 60;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    //get-set Platform
    public Platform getBahnsteig() {
        return platform;
    }

    public void setBahnsteig(Platform gleis) {
        this.platform = gleis;
    }

    //get-set Plangleis
    public Platform getScheduledPlatform() {
        return scheduledPlatform;
    }

    public void setScheduledPlatform(Platform scheduledPlatform) {
        this.scheduledPlatform = scheduledPlatform;
    }

    //get-set am-platform
    public boolean getAtPlatform() {
        return atPlatform;
    }

    public void setAtPlatform(boolean atPlatform) {
        this.atPlatform = atPlatform;
    }

    //get-set from
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    //get-set to
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    //get-set visible
    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    //get-set TrainStop
    public List<TrainStop> getSchedule() {
        return schedule;
    }

    public void setSchedule(ArrayList<TrainStop> schedule) {
        this.schedule = schedule;
    }

    public TrainStop getFahrplan(int index) {
        if (schedule != null && index < schedule.size()) {
            return schedule.get(index);
        } else {
            return null;
        }
    }

    //get-set newTrain
    public boolean isNewTrain() {
        return newTrain;
    }

    public void setNewTrain(boolean newTrain) {
        this.newTrain = newTrain;
    }

    //Entfernen den Train aus dem LabelContainer (nur wenn hier kein Wechsel VON einem anderen Train stattfindet)
    public void removeFromGrid() {
        if (schedule != null) {
            for (TrainStop fh : schedule) {
                if (fh != null && fh.getVorgaenger() == null) {
                    fh.removeDrawnTo();
                }
            }
        }
    }

    //Formatiert die Verspätung zu einem String
    public String getVerspaetungToString() {
        String out = "";

        if (delay > 0) {
            out = " (+" + delay + ")";
        } else if (delay < 0) {
            out = " (" + delay + ")";
        }

        return out;
    }

    @Override public String toString() {
        return "Train{" + "trainId=" + trainId + ", trainName='" + trainName + '\''
                + ", delay=" + delay + ", platform='" + platform + '\''
                + ", scheduledPlatform='" + scheduledPlatform + '\'' + ", atPlatform=" + atPlatform
                + ", from='" + from + '\'' + ", to='" + to + '\''
                + ", visible=" + visible + ", newTrain=" + newTrain + '}';
    }

    public boolean equals(Train o) {
        return this.trainId == o.trainId;
    }

    public Label getStellwerksUebersichtLabel() {
        return stellwerksUebersichtLabel;
    }

    public void setStellwerksUebersichtLabel(Label stellwerksUebersichtLabel) {
        this.stellwerksUebersichtLabel = stellwerksUebersichtLabel;
    }

    public void setNeedUpdate(boolean value) {
        schwerwiegendesUpdate = true;
        for (TrainStop fh : schedule) {
            fh.setNeedUpdate(value);
        }
    }

    public boolean isSchwerwiegendesUpdate() {
        return schwerwiegendesUpdate;
    }

    public void setSchwerwiegendesUpdate(boolean schwerwiegendesUpdate) {
        this.schwerwiegendesUpdate = schwerwiegendesUpdate;
    }

    /**
     * @return Ein Train mit E/K-Flag
     */
    public Train getVorgaenger() {
        return this.predecessor;
    }

    /**
     * @param train Ein Train mit E/K-Flag
     */
    public void setVorgaenger(Train train) {
        this.predecessor = train;
    }

    public Train getNachfolger() {
        return this.successor;
    }

    /**
     * @param train Ein Train der aus E/F-Flag folgt
     */
    public void setNachfolger(Train train) {
        this.successor = train;
    }

    public TrainStop getFahrplanHalt(TrainStop trainStop) {
        if (trainStop.getArrivalTime() == 0) {
            for (java.util.ListIterator<TrainStop> fhIter =
                 this.schedule.listIterator(this.schedule.size()); fhIter
                         .hasPrevious(); ) {
                TrainStop fh = fhIter.previous();
                if (fh.getArrivalTime() < trainStop.getDepartureTime()) {
                    return fh;
                }
            }
        } else {
            for (TrainStop fh : this.schedule) {
                if (fh.getDepartureTime() > trainStop.getArrivalTime()) {
                    return fh;
                }
            }
        }
        // TODO Auto-generated method stub
        return null;
    }
}
