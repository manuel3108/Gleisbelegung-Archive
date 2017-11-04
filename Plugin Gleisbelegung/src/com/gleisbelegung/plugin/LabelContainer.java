package com.gleisbelegung.plugin;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Repräsentiert immer eine Tabellen-Zelle in einer Tabelle
 */

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LabelContainer extends Plugin_Gleisbelegung{
    private Label l;                                //Textfeld um Text schreiben zu können
    private int labelIndex;                         //speichert den in der @Fenster-Klsse vergebenen labelIndex
    private ArrayList<Zug> trains;                  //Speichert alle Zuüge die Gerade auf diesem Container einen Halt/Durchfahrt haben
    private long time = -1;                         //Die Zeit die in der jeweiligen Zeile die richtige ist.
    private int bahnsteig;                          //int der mit dem Bahnsteig-Namen aus der @Main-Klasse einen Bahnsteigsnamen darstellt
    private ArrayList<LabelContainer> labelTime;    //Übergabe von labelTime aus der @Fenster-Klasse
    private boolean hervorhebungDurchGleis;

    //Speichert alle übergebenen Daten
    public LabelContainer(int labelIndex, int bahnsteig, ArrayList<LabelContainer> labelTime) throws Exception{
        this.labelTime = labelTime;
        this.bahnsteig = bahnsteig;
        this.labelIndex = labelIndex;
        trains = new ArrayList<>();
        hervorhebungDurchGleis = false;

        l = new Label();
        l.setFont(Font.font(settingsFontSize-5));
        l.setMinWidth(settingsGridWidth);
        l.setMaxWidth(settingsGridWidth);
        l.setAlignment(Pos.CENTER);

        if(bahnsteig > -1){
            updateLabel();
        }
    }

    //get-set labelIndex
    public int getLabelIndex() {
        return labelIndex;
    }
    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }

    //get Label l
    public Label getLabel(){
        return l;
    }

    //get-set long time
    public long getTime() {
        return time;
    }
    public void setTime(long time){
        this.time = time;
    }

    //add-remove Zug von trains-Liste
    public void addTrain(Zug z){
        try{
            trains.add(z);
            updateLabel();

            l.setOnMouseEntered(e -> { try{ showTrainInformations(); }catch(Exception ex) { ex.printStackTrace(); } });

            try {
                Thread.sleep(5);
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
                if(i < trains.size() && trains.get(i) != null && trains.get(i).getZugId() == z.getZugId()){
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

    //verschiedene Methoden um die Container zu aktualisieren
    public void updateLabel() throws Exception{
        if(trains.size() == 0){
            Platform.runLater(() -> {
                l.setText("");
                l.setTooltip(null);
                if(hervorhebungDurchGleis){
                    l.setStyle("-fx-text-fill: #fff; " + prepareBorder() + "; -fx-background-color: #181818");
                } else if (labelIndex % 2 == 0) {
                    l.setStyle("-fx-text-fill: #fff; " + prepareBorder());
                } else {
                    l.setStyle("-fx-background-color: #292929; -fx-text-fill: #fff; " + prepareBorder());
                }
            });
        } else if(trains.size() == 1){
            Platform.runLater(() -> {
                if(trains.size() == 1){
                    l.setText(trains.get(0).getZugName() + trains.get(0).getVerspaetungToString());
                    l.setTooltip(new Tooltip(trains.get(0).getZugName() + trains.get(0).getVerspaetungToString()));
                    l.setStyle("-fx-text-fill: #fff; " + prepareBorder() + "-fx-background-color: #" + prepareTrainStyle(trains.get(0).getZugName()) + ";");
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
                    l.setStyle("-fx-text-fill: #fff; " + prepareBorder() + "-fx-background-color: red;");

                    /*if (bahnsteigeSichtbar[bahnsteig]) {
                        playColisonSound();
                    }*/
                }
            });
        }
    }
    public void updateLabel(String text) throws Exception{
        Platform.runLater(() -> {
            try{
                l.setText(text);

                if(bahnsteig == -1){
                    prepareBorderForLabelTime();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }
    public void updateLabel(String text, long time) throws Exception{
        this.time = time;
        Platform.runLater(() -> l.setText(text));
    }

    //Hintergrundfarbe des Containers anhand der Zugnummer erzeugen
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

    //Bereite die Umrandungen für die Container vor (5 und 60 Minütige Linie)
    private String prepareBorder(){
        try{
            String fullHour = "-fx-border-color: yellow #505050 #05af3b yellow; -fx-border-width: 0 1 1 0; ";
            String fiveMin = "-fx-border-color: yellow #505050 #969696 yellow; -fx-border-width: 0 1 1 0; ";
            String fiveteenMin = "-fx-border-color: yellow #505050 #95b57b yellow; -fx-border-width: 0 1 1 0; ";
            String normal = "-fx-border-color: yellow #505050 #505050 yellow; -fx-border-width: 0 1 1 0; ";

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
    private void prepareBorderForLabelTime() throws Exception{
        String in = l.getText();

        String fullHour = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #05af3b yellow; -fx-border-width: 0 5 1 0; ";
        String fiveMin = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #969696 yellow; -fx-border-width: 0 5 1 0; ";
        String fiveteenMin = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #95b57b yellow; -fx-border-width: 0 5 1 0; ";
        String normal = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #505050 yellow; -fx-border-width: 0 5 1 0; ";

        if(in.endsWith("00")){
            l.setStyle(fullHour);
        } else if(in.endsWith("15") || in.endsWith("30") || in.endsWith("45")){
            l.setStyle(fiveteenMin);
        } else if(in.endsWith("5") || in.endsWith("0")){
            l.setStyle(fiveMin);
        } else{
            l.setStyle(normal);
        }
    }

    //get trains
    public ArrayList<Zug> getTrains(){
        return trains;
    }

    //get-set Bahnsteige
    public int getBahnsteig() {
        return bahnsteig;
    }
    public void setBahnsteig(int bahnsteig) {
        this.bahnsteig = bahnsteig;
    }

    //Zeige die Zuginformationen auf dem informations Panel aus @Main
    public void showTrainInformations() throws Exception{
        int heightCounter = 0;

        informations.getChildren().clear();

        for(Zug z : trains){
            debugMessage("INFORMATION: Maus befindet sich ueber " + z.getZugName() +  " und zeigt die Informationen: " + settingsShowInformations, true);



            Label trainName = new Label(z.getZugName() + z.getVerspaetungToString());
            trainName.setStyle("-fx-text-fill: white");
            trainName.setFont(Font.font(settingsFontSize-2));
            trainName.setTranslateY(heightCounter);
            if(z.getFahrplan() != null){
                for(FahrplanHalt fh : z.getFahrplan()){
                    if(fh.getFlaggedTrain() != null){
                        trainName.setText(trainName.getText() + " => " + fh.getFlaggedTrain().getZugName() + fh.getFlaggedTrain().getVerspaetungToString());
                        break;
                    }
                }
            }

            Label vonBis = new Label(z.getVon() + " - " + z.getNach());
            vonBis.setStyle("-fx-text-fill: white");
            vonBis.setFont(Font.font(settingsFontSize-5));
            vonBis.setTranslateY(heightCounter + 25);

            informations.getChildren().addAll(trainName, vonBis);

            for(int i = 0; i < z.getFahrplan().length; i++){
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

                Label l = new Label("Gleis: " + z.getFahrplan(i).getGleis() + " " + ft.format(anunft) + " - " + ft.format(abfahrt) + durchfahrt);
                l.setFont(Font.font(settingsFontSize-5));
                l.setTranslateY(heightCounter + 55);
                l.setPrefWidth(settingsInformationWith-25);

                if(z.getGleis().equals(z.getFahrplan(i).getGleis()) && z.getAmGleis()){
                    l.setStyle("-fx-text-fill: white; -fx-background-color: green");
                } else if(z.getFahrplan(i).getGleis().equals(bahnsteige[bahnsteig])){
                    l.setStyle("-fx-text-fill: white; -fx-background-color: #505050");
                } else{
                    l.setStyle("-fx-text-fill: white");
                }

                informations.getChildren().add(l);

                heightCounter += 20;
            }

            heightCounter += 75;
        }

        informations.setPrefHeight(heightCounter);
    }

    //Hebe den Container hervor, wenn ein Zug aus dem Container in der Suche identifiziert wird
    public void highlight() throws Exception{
        Runnable r = () -> {
            try {
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green"));
                Thread.sleep(1000);
                updateLabel();
                Thread.sleep(1000);
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green"));
                Thread.sleep(1000);
                updateLabel();
                Thread.sleep(1000);
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green"));
                Thread.sleep(1000);
                updateLabel();
                Thread.sleep(1000);
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green"));
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
}