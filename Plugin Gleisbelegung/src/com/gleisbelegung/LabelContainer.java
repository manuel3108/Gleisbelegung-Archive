package com.gleisbelegung;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Repräsentiert immer eine Tabellen-Zelle in einer Tabelle
 */

import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.FahrplanHalt;
import com.gleisbelegung.lib.data.Zug;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LabelContainer extends Plugin {
    private Label l;                                //Textfeld um Text schreiben zu können
    private int labelIndex;                         //speichert den in der @Fenster-Klsse vergebenen labelIndex
    private ArrayList<Zug> trains;                  //Speichert alle Zuüge die Gerade auf diesem Container einen Halt/Durchfahrt haben
    private long time = -1;                         //Die Zeit die in der jeweiligen Zeile die richtige ist.
    private Bahnsteig bahnsteig;                          //int der mit dem Bahnsteig-Namen aus der @Main-Klasse einen Bahnsteigsnamen darstellt
    private boolean letzterBahnsteig;

    private boolean hervorhebungDurchGleis;

    public LabelContainer(int labelIndex, Bahnsteig bahnsteig){
        this.bahnsteig = bahnsteig;
        this.labelIndex = labelIndex;
        trains = new ArrayList<>();
        hervorhebungDurchGleis = false;

        l = new Label();
        l.setFont(Font.font(Einstellungen.schriftgroesse-5));
        l.setMinWidth(Einstellungen.spaltenbreite);
        l.setMaxWidth(Einstellungen.spaltenbreite);
        l.setAlignment(Pos.CENTER);

        if(bahnsteig != null){
            try {
                updateLabel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getLabelIndex() {
        return labelIndex;
    }
    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }

    public Label getLabel(){
        return l;
    }

    public long getTime() {
        return time;
    }
    public void setTime(long time){
        this.time = time;
    }

    public void addTrain(Zug z){
        try{
            trains.add(z);
            updateLabel();

            l.setOnMouseEntered(e -> { try{ showTrainInformations(); }catch(Exception ex) { ex.printStackTrace(); } });

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void removeTrain(Zug z){
        try{
            int size = trains.size();
            for (int i = 0; i < trains.size(); i++) {
                if(trains.size() > 0 && i < trains.size() && trains.get(i) != null && trains.get(i).getZugId() == z.getZugId()){
                    trains.remove(i);
                }
            }

            while(trains.size() > size - 1){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater( () -> { try{ updateLabel(); }catch(Exception ex) { ex.printStackTrace(); } });

            l.setOnMouseEntered(e -> { try{ showTrainInformations(); }catch(Exception ex) { ex.printStackTrace(); } });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateLabel(){
        if(trains.size() == 0){
            Platform.runLater(() -> {
                l.setText("");
                l.setTooltip(null);
                if(hervorhebungDurchGleis){
                    l.setStyle("-fx-text-fill: #fff; " + prepareBorder() + " -fx-background-color: #181818;");
                } else if (labelIndex % 2 == 0) {
                    l.setStyle("-fx-text-fill: #fff; " + prepareBorder());
                } else {
                    l.setStyle("-fx-background-color: #292929; -fx-text-fill: #fff; " + prepareBorder());
                }

                if(letzterBahnsteig){
                    l.setStyle(l.getStyle() + " -fx-border-width: 0 5 1 0;");
                }
            });
        } else if(trains.size() == 1){
            Zug train = trains.get(0);
            Platform.runLater(() -> {
                    l.setText(train.getZugName() + train.getVerspaetungToString());
                    l.setTooltip(new Tooltip(train.getZugName() + train.getVerspaetungToString()));
                    l.setStyle("-fx-text-fill: #fff; " + prepareBorder() + " -fx-background-color: #" + prepareTrainStyle(train.getZugName()) + ";");

                if(letzterBahnsteig){
                    l.setStyle(l.getStyle() + " -fx-border-width: 0 5 1 0");
                }
            });
        } else{
            Platform.runLater(() -> {
                if(trains != null && trains.size() > 0){
                    String text = "";

                    for (Zug z : trains) {
                        text += z.getZugName() + z.getVerspaetungToString() + ", ";
                    }
                    text = text.substring(0, text.length() - 2);

                    final String temp = text;
                    l.setText(temp);
                    l.setTooltip(new Tooltip(temp));
                    l.setStyle("-fx-text-fill: #fff; " + prepareBorder() + " -fx-background-color: red;");

                    //playColisonSound(bahnsteig);

                    if(letzterBahnsteig){
                        l.setStyle(l.getStyle() + " -fx-border-width: 0 5 1 0;");
                    }
                }
            });
        }
    }
    public void updateLabel(String text, boolean isBahnsteig){
        Platform.runLater(() -> {
            try{
                l.setText(text);

                if(bahnsteig == null && !isBahnsteig){
                    prepareBorderForLabelTime();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }
    public void updateLabel(String text, long time){
        this.time = time;
        Platform.runLater(() -> l.setText(text));
    }

    private String prepareTrainStyle(String zugName){
        try {
            int index = zugName.indexOf('(')-1;
            char[] name = zugName.toCharArray();

            if(index > 0){
                zugName = "";
                for (int i = 0; i < name.length; i++) {
                    if(i < index){
                        zugName += name[i];
                    }
                }
            }

            String out = zugName.replaceAll("[^\\d.]", "");
            char[] temp = out.toCharArray();

            int counter = 6 - temp.length;
            if (counter > 0 && counter != 3) {
                for (int i = 0; i < counter; i++) {
                    out += "9";
                }
            } else if (counter < 0) {
                while (counter < 0) {
                    out = "";
                    for (int i = 0; i < temp.length - 1; i++) {
                        out += temp[i];
                    }
                    counter++;
                }
            }

            return out;
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    private String prepareBorder(){
        try{
            String fullHour = "-fx-border-color: yellow #505050 #05af3b yellow; -fx-border-width: 0 1 1 0;";
            String fiveMin = "-fx-border-color: yellow #505050 #969696 yellow; -fx-border-width: 0 1 1 0;";
            String fiveteenMin = "-fx-border-color: yellow #505050 #95b57b yellow; -fx-border-width: 0 1 1 0;";
            String normal = "-fx-border-color: yellow #505050 #505050 yellow; -fx-border-width: 0 1 1 0;";

            Date dNow = new Date(time);
            SimpleDateFormat ft = new SimpleDateFormat("HH:mm");
            String localTime = ft.format(dNow);

            if(localTime.endsWith("00")){
                return fullHour;
            } else if(localTime.endsWith("15") || localTime.endsWith("30") || localTime.endsWith("45")){
                return fiveteenMin;
            } else if(localTime.endsWith("5") || localTime.endsWith("0")){
                return fiveMin;
            } else{
                return normal;
            }
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    private void prepareBorderForLabelTime() {
        String in = l.getText();

        String fullHour = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #05af3b yellow; -fx-border-width: 0 5 1 0;";
        String fiveMin = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #969696 yellow; -fx-border-width: 0 5 1 0;";
        String fiveteenMin = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #95b57b yellow; -fx-border-width: 0 5 1 0;";
        String normal = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #505050 yellow; -fx-border-width: 0 5 1 0;";

        if(in.endsWith("00")){
            l.setStyle(fullHour);
        } else if(in.endsWith("15") || in.endsWith("30") || in.endsWith("45")){
            l.setStyle(fiveteenMin);
        } else if(in.endsWith("5") || in.endsWith("0")){
            l.setStyle(fiveMin);
        } else{
            l.setStyle(normal);
        }

        if (labelIndex % 2 == 0) {
            l.setStyle(l.getStyle() + " -fx-background-color: #303030;");
        } else {
            l.setStyle(l.getStyle() + " -fx-background-color: #292929;");
        }
    }

    public ArrayList<Zug> getTrains(){
        return trains;
    }

    public Bahnsteig getBahnsteig() {
        return bahnsteig;
    }
    public void setBahnsteig(Bahnsteig bahnsteig) {
        this.bahnsteig = bahnsteig;
    }

    private void showTrainInformations(){
        int heightCounter = 35;

        Fenster.informations.getChildren().clear();

        for(Zug z : trains){
            Label trainName = new Label(z.getZugName() + z.getVerspaetungToString());
            trainName.setStyle("-fx-text-fill: white;");
            trainName.setFont(Font.font(Einstellungen.schriftgroesse-2));
            trainName.setTranslateY(heightCounter);
            trainName.setTranslateX(5);
            if(z.getFahrplan() != null){
                for(FahrplanHalt fh : z.getFahrplan()){
                    if(fh.getFlaggedTrain() != null){
                        trainName.setText(trainName.getText() + " => " + fh.getFlaggedTrain().getZugName() + fh.getFlaggedTrain().getVerspaetungToString());
                        break;
                    }
                }
            }

            Label vonBis = new Label(z.getVon() + " - " + z.getNach());
            vonBis.setStyle("-fx-text-fill: white;");
            vonBis.setFont(Font.font(Einstellungen.schriftgroesse-5));
            vonBis.setTranslateY(heightCounter + 25);
            vonBis.setTranslateX(5);

            Fenster.informations.getChildren().addAll(trainName, vonBis);

            for(int i = 0; i < z.getFahrplan().size(); i++){
                long lAnkunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung()*1000*60;
                long lAbfahrt = z.getFahrplan(i).getAbfahrt() + z.getVerspaetung()*1000*60;
                if(z.getVerspaetung() > 3 && (lAbfahrt-lAnkunft)/1000/60 > 3){
                    lAbfahrt = lAnkunft + 4*1000*60;
                }

                String durchfahrt = "";
                if(z.getFahrplan(i).isCrossing()) durchfahrt = " Df.";

                Date anunft = new Date(lAnkunft);
                Date abfahrt = new Date(lAbfahrt);
                SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

                Label l = new Label("Bahnsteig: " + z.getFahrplan(i).getBahnsteig().getName() + " " + ft.format(anunft) + " - " + ft.format(abfahrt) + durchfahrt);
                l.setFont(Font.font(Einstellungen.schriftgroesse-5));
                l.setTranslateY(heightCounter + 55);
                l.setPrefWidth(Einstellungen.informationenBreite);
                l.setTranslateX(5);

                if(z.getBahnsteig().getName().equals(z.getFahrplan(i).getBahnsteig().getName()) && z.getAmGleis()){
                    l.setStyle("-fx-text-fill: white; -fx-background-color: green;");
                } else if(z.getFahrplan(i).getBahnsteig().getName().equals(bahnsteig.getName())){
                    l.setStyle("-fx-text-fill: white; -fx-background-color: #505050;");
                } else{
                    l.setStyle("-fx-text-fill: white;");
                }

                Fenster.informations.getChildren().add(l);

                heightCounter += 20;
            }

            heightCounter += 75;
        }

        Fenster.informations.setPrefHeight(heightCounter);
    }

    public void highlight() {
        Runnable r = () -> {
            try {
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green;"));
                Thread.sleep(1000);
                updateLabel();
                Thread.sleep(1000);
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green;"));
                Thread.sleep(1000);
                updateLabel();
                Thread.sleep(1000);
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green;"));
                Thread.sleep(1000);
                updateLabel();
                Thread.sleep(1000);
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green;"));
                Thread.sleep(1000);
                updateLabel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }

    public void setHervorhebungDurchGleis(boolean hervorhebungDurchGleis) {
        this.hervorhebungDurchGleis = hervorhebungDurchGleis;
        try {
            updateLabel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLetzterBahnsteig() {
        return letzterBahnsteig;
    }
    public void setLetzterBahnsteig(boolean letzterBahnsteig) {
        this.letzterBahnsteig = letzterBahnsteig;
    }
}