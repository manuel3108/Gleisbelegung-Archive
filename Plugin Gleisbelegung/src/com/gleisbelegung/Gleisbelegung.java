package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.FahrplanHalt;
import com.gleisbelegung.lib.data.Zug;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Gleisbelegung {
    private GridPane gp;                                //Das ist die Tabelle, die alles enthält.                       (Vereinfacht einige Sachen, könnte/sollte irgendwann entfernt werden
    private ScrollPane spContent;                       //Das ist das Scroll-Feld welches um die Tabelle von oben gewrappt wird.
    private GridPane gpTime;                            //Speichert die Zeiten in einer einzelnen Tabelle
    private GridPane gpBahnhof;
    private ScrollPane spTime;                          //Wrappt die Zeiten-Tabelle in ein Scroll-Feld
    private GridPane gpPlatform;                        //Eine Tabelle für die Bahnsteige
    private ScrollPane spPlatform;                      //Wrappt die Bahnssteige in ein Scroll-Feld
    private ScrollPane spBahnhof;
    private ScrollBar scrollBarWidth;                   //Scroll-Balken um die Tabelle bewegen zu können
    private ScrollBar scrollBarHeight;                  //Scroll-Balken um die Tabelle bewegen zu können
    private Label firstLabel;                           //neues Label oben links mit Bahnhofs-Namen
    private int labelIndexCounter;                      //Zählt die Textfelder auf der y-Achse mit um eine Identifikation zu ermöglichen
    private ArrayList<LabelContainer> labelTime;
    private List<Bahnsteig> sortierteGleise = new ArrayList<>();

    private Pane content;
    private Stellwerk stellwerk;

    public Gleisbelegung(Stellwerk stellwerk){
        this.stellwerk = stellwerk;
        labelTime = new ArrayList<>();

        firstLabel = new Label("Bahnhofsname");
        firstLabel.setFont(Font.font(Einstellungen.schriftgroesse-5));
        firstLabel.setMinWidth(Einstellungen.spaltenbreite);
        firstLabel.setMaxWidth(Einstellungen.spaltenbreite);
        firstLabel.setMinHeight(45);
        firstLabel.setMaxHeight(45);
        firstLabel.setAlignment(Pos.CENTER);
        firstLabel.setWrapText(true);
        firstLabel.setAlignment(Pos.CENTER);
        Platform.runLater(() -> {
            firstLabel.setText(stellwerk.getStellwerksname());
            firstLabel.setStyle("-fx-text-fill: white; -fx-border-color: #fff #505050 #505050 #fff; -fx-border-width: 0 5 5 0;");
        });

        scrollBarWidth = new ScrollBar();
        scrollBarWidth.setOrientation(Orientation.HORIZONTAL);
        scrollBarWidth.setMin(0);
        scrollBarWidth.setMax(1);
        scrollBarWidth.setUnitIncrement(0.03);
        scrollBarWidth.setVisibleAmount(0.3);
        scrollBarWidth.valueProperty().addListener((ov, old_val, new_val) -> {
            spContent.setHvalue(scrollBarWidth.getValue());
            spBahnhof.setHvalue(spContent.getHvalue());
        });

        scrollBarHeight = new ScrollBar();
        scrollBarHeight.setOrientation(Orientation.VERTICAL);
        scrollBarHeight.setPrefHeight(500);
        scrollBarHeight.setMin(0);
        scrollBarHeight.setMax(1);
        scrollBarHeight.setUnitIncrement(0.02);
        scrollBarHeight.setVisibleAmount(0.3);
        scrollBarHeight.valueProperty().addListener((ov, old_val, new_val) -> spContent.setVvalue(scrollBarHeight.getValue()));

        gp = new GridPane();
        gp.setHgap(0);
        gp.setVgap(0);
        gp.setPadding(new Insets(0,20,0,0));
        spContent = new ScrollPane();
        spContent.setContent(gp);
        spContent.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spContent.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spContent.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spContent.vvalueProperty().addListener((ov, old_val, new_val) -> {
            spTime.setVvalue(spContent.getVvalue());
            scrollBarHeight.adjustValue(spContent.getVvalue());
        });
        spContent.hvalueProperty().addListener((ov, old_val, new_val) -> {
            spPlatform.setHvalue(spContent.getHvalue());
            scrollBarWidth.adjustValue(spContent.getHvalue());
        });
        spContent.setTranslateY(50);

        gpTime = new GridPane();
        gpTime.setHgap(0);
        gpTime.setVgap(0);
        spTime = new ScrollPane();
        spTime.setContent(gpTime);
        spTime.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spTime.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spTime.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spTime.setTranslateY(50);

        gpPlatform = new GridPane();
        gpPlatform.setHgap(0);
        gpPlatform.setVgap(0);
        gpPlatform.setPadding(new Insets(0,20,0,0));
        spPlatform = new ScrollPane();
        spPlatform.setContent(gpPlatform);
        spPlatform.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spPlatform.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spPlatform.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spPlatform.setTranslateY(10);

        gpBahnhof = new GridPane();
        gpBahnhof.setHgap(0);
        gpBahnhof.setVgap(0);
        spBahnhof = new ScrollPane();
        spBahnhof.setContent(gpBahnhof);
        spBahnhof.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spBahnhof.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spBahnhof.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spBahnhof.setTranslateX(Einstellungen.spaltenbreite);

        content = new Pane(spBahnhof, spPlatform, spContent, spTime, scrollBarWidth, scrollBarHeight, firstLabel);

        for (int i = 0; i < Einstellungen.vorschau; i++) {
            labelTime.add(i, new LabelContainer(i, null));

            Date dNow = new Date(stellwerk.getSpielzeit() + i*1000*60);
            SimpleDateFormat ft = new SimpleDateFormat("HH:mm");
            labelTime.get(i).updateLabel(ft.format(dNow), false);

            int temp = i;
            Platform.runLater(() -> gpTime.add(labelTime.get(temp).getLabel(), 0, temp));
        }

        for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
            for (int i = 0; i < bahnhof.getAnzahlBahnsteige(); i++) {
                LabelContainer lc = new LabelContainer(i,null);
                lc.updateLabel(bahnhof.getBahnsteig(i).getName(), true);
                lc.getAussehen().textFarbe = "#fff";
                lc.getAussehen().raender.farbeUnten = "#505050";
                lc.getAussehen().raender.farbeRechts = "#505050";
                lc.getAussehen().raender.setze(0, 1, 5, 0);
                lc.getLabel().setStyle(lc.getAussehen().toCSSStyle());

                int temp = bahnhof.getBahnsteig(i).getId();
                Platform.runLater(() -> gpPlatform.add(lc.getLabel(), temp, 0));
                bahnhof.getBahnsteig(i).setGleisLabel(lc);
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for(Bahnhof b : stellwerk.getBahnhoefe()){
            for(Bahnsteig ba : b.getBahnsteige()){
                for(int i = 0; i < Einstellungen.vorschau; i++){
                    LabelContainer lc = new LabelContainer(i, ba);
                    lc.updateLabel("", stellwerk.getSpielzeit() + i*1000*60);
                    ba.getSpalte().add(lc);

                    final int tempI = i;
                    Platform.runLater(() -> {
                        gp.add(lc.getLabel(), ba.getId(), tempI);
                    });
                }
            }
        }
        labelIndexCounter = Einstellungen.vorschau;

        for(Bahnhof b : stellwerk.getBahnhoefe()){
            for(LabelContainer lc : b.getLastBahnsteig().getSpalte()){
                lc.setLetzterBahnsteig(true);

                b.getLastBahnsteig().getGleisLabel().setLetzterBahnsteig(true);
                Label l = b.getLastBahnsteig().getGleisLabel().getLabel();
                l.setStyle(l.getStyle() + " -fx-border-width: 0 5 5 0;");
            }
        }

        sortiereGleise();
    }

    public void scrollPaneTo(double x, double y, FahrplanHalt fh){
        scrollBarWidth.adjustValue(x);
        scrollBarHeight.adjustValue(y);


        for (int i = 0; i < fh.getDrawnTo().size(); i++) {
            fh.getDrawnTo(i).highlight();
        }

        /*LabelContainer lc = fh.getDrawnTo(0);   BBUUGGSS

        double scrollValueX = (x - scrollBarWidth.getValue()) / 100;
        double scrollValueY = (y - scrollBarHeight.getValue()) / 100;

        Runnable r = () -> {
            try{
                for(int i = 0; i < 100; i++){
                    scrollBarWidth.adjustValue(scrollBarWidth.getValue() + scrollValueX);
                    scrollBarHeight.adjustValue(scrollBarHeight.getValue() + scrollValueY);

                    Thread.sleep(10);
                }


                lc.highlight();
                for (int i = 1; i < fh.getDrawnTo().size(); i++) {
                    fh.getDrawnTo(i).highlight();
                }
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("INFORMATION: Fehler beim autom. Scrollen!");
            }
        };

        Thread t = new Thread(r);
        t.start();*/
    }

    private void drawTrain(Zug z){
        z.removeFromGrid();
        try{
            if(z.getFahrplan() != null){
                for (int i = 0; i < z.getFahrplan().size(); i++) {
                    if (z.getFahrplan(i) != null && z.getFahrplan(i).getVorgaenger() == null) {
                        for (Bahnhof b : stellwerk.getBahnhoefe()) {
                            for (int j = 0; j < b.getAnzahlBahnsteige(); j++) {
                                Bahnsteig g = b.getBahnsteig(j);
                                if (g != null && z.getFahrplan(i) != null && z.getFahrplan(i).getBahnsteig().getName().equals(g.getName())) {
                                    if (z.getFahrplan(i).getFlaggedTrain() != null) {
                                        Zug eFlag = z.getFahrplan(i).getFlaggedTrain();

                                        if (z.getFahrplan(i).getVorgaenger() == null) {
                                            long ankunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung() * 1000 * 60;
                                            long abfahrt = eFlag.getFahrplan(0).getAbfahrt() + eFlag.getVerspaetung() * 1000 * 60;
                                            if (eFlag.getVerspaetung() < 0 && !z.getFahrplan(i).isCrossing()) {
                                                abfahrt = eFlag.getFahrplan(0).getAbfahrt();
                                            } else if (z.getVerspaetung() > 3 && (abfahrt - ankunft) / 1000 / 60 > 3) {
                                                abfahrt = ankunft + 4 * 1000 * 60;
                                            }

                                            for (int k = 0; k < Einstellungen.vorschau; k++) {
                                                if (g.getSpalte() != null && k < g.getSpalte().size() && b.getBahnsteig(j).getSpalte().get(k) != null) {
                                                    LabelContainer lc = b.getBahnsteig(j).getSpalte().get(k);
                                                    if (ankunft <= lc.getTime() && abfahrt >= lc.getTime() - 1000 * 60) {

                                                        z.getFahrplan(i).addDrawnTo(lc);

                                                        if (z.getFahrplan(i).isCrossing()) {
                                                            Platform.runLater(() -> lc.getLabel().setText(lc.getLabel().getText() + " D"));
                                                            System.out.println(z.getZugName() + " Durchfahrt");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if (z.getFahrplan(i).getVorgaenger() == null) {
                                            long ankunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung() * 1000 * 60;
                                            long abfahrt = z.getFahrplan(i).getAbfahrt() + z.getVerspaetung() * 1000 * 60;
                                            if (z.getVerspaetung() < 0 && !z.getFahrplan(i).isCrossing()) {
                                                abfahrt = z.getFahrplan(i).getAbfahrt();
                                            } else if (z.getVerspaetung() > 3 && (abfahrt - ankunft) / 1000 / 60 > 3) {
                                                abfahrt = ankunft + 4 * 1000 * 60;
                                            }

                                            for (int k = 0; k < Einstellungen.vorschau; k++) {
                                                if (g.getSpalte() != null && k < g.getSpalte().size() && b.getBahnsteig(j).getSpalte().get(k) != null) {
                                                    LabelContainer lc = b.getBahnsteig(j).getSpalte().get(k);
                                                    if (ankunft <= lc.getTime() && abfahrt >= lc.getTime() - 1000 * 60) {
                                                        z.getFahrplan(i).addDrawnTo(lc);

                                                        if (z.getFahrplan(i).isCrossing()) {
                                                            Platform.runLater(() -> lc.getLabel().setFont(Font.font("", FontPosture.ITALIC, Einstellungen.schriftgroesse - 5)));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void update(){
        for (Zug z : stellwerk.getZuege()) {
            try {
                if (z.isNeedUpdate() && z.getFahrplan(0) != null && z.getFahrplan(0).getVorgaenger() != null) {
                    z.getFahrplan(0).getVorgaenger().getZ().setNeedUpdate(true);
                }
            } catch (Exception e) {
                System.out.println("Fehler koennen passieren :(");
                e.printStackTrace();
            }
        }
        for (Zug z : stellwerk.getZuege()) {
            try {
                if (z.isNeedUpdate()) {
                    drawTrain(z);
                    z.setNeedUpdate(false);
                    z.setNewTrain(false);
                }
            } catch (Exception e) {
                System.out.println("Fehler koennen passieren :(");
                e.printStackTrace();
            }
        }
    }

    public void aktualisiereTabelle(){
        Platform.runLater(() -> {
            gpTime.getChildren().remove(0);
            labelTime.remove(0);

            for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
                for(Bahnsteig bahnsteig : bahnhof.getBahnsteige()){
                    if(gp != null && gp.getChildren() != null && bahnsteig != null && bahnsteig.getSpalte() != null && bahnsteig.getSpalte().get(0) != null && bahnsteig.getSpalte().get(0).getLabel() != null){
                        gp.getChildren().remove(bahnsteig.getSpalte().get(0).getLabel());
                        bahnsteig.getSpalte().remove(0);
                    }
                }
            }
        });


        Date dNow = new Date(stellwerk.getSpielzeit() + Einstellungen.vorschau*1000*60 - 2000);
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

        LabelContainer lc = new LabelContainer(labelIndexCounter,null);
        lc.updateLabel(ft.format(dNow), false);
        Platform.runLater(() -> {
            gpTime.add(lc.getLabel(), 0, labelIndexCounter+1);
            labelTime.add(labelTime.size(), lc);
        });


        ArrayList<Bahnsteig> sortierteGleise = new ArrayList<>();
        for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
            sortierteGleise.addAll(bahnhof.getBahnsteige());
        }
        sortierteGleise.sort(Comparator.comparing(Bahnsteig::getOrderId));

        ArrayList<LabelContainer> labelContainer = new ArrayList<>();
        int counter = 0;
        for(Bahnsteig b : sortierteGleise){
            LabelContainer laco = new LabelContainer(labelIndexCounter, b);

            labelContainer.add(laco);

            laco.updateLabel("", stellwerk.getSpielzeit() + Einstellungen.vorschau*1000*60 - 2000);
            b.getSpalte().add(laco);

            final int tempI = labelIndexCounter+2;
            final int tempCounter = counter;
            Platform.runLater(() -> {
                gp.add(laco.getLabel(), tempCounter, tempI);
            });

            if(b.isSichtbar()){
                laco.getLabel().setPrefWidth(Einstellungen.spaltenbreite);
            } else{
                laco.getLabel().setMaxWidth(0);
                laco.getLabel().setPrefWidth(0);
                laco.getLabel().setMinWidth(0);
            }

            if(b.getHebeHervor()) laco.setHervorhebungDurchGleis(true);

            counter++;
        }
        labelIndexCounter++;

        updateSomeTrains(stellwerk.getSpielzeit() + Einstellungen.vorschau*1000*60 - 2000);
    }

    private void updateSomeTrains(long time){
        for(Zug z : stellwerk.getZuege()){
            try{
                if (z.getFahrplan() != null && z.getFahrplan(0) != null) {
                    for (int i = 0; i < z.getFahrplan().size(); i++) {
                        if (z.getFahrplan(i) != null && z.getFahrplan(i).getFlaggedTrain() != null) {
                            Zug eFlag = z.getFahrplan(i).getFlaggedTrain();

                            if (z.getFahrplan() != null && z.getFahrplan(i) != null && eFlag != null && eFlag.getFahrplan() != null && eFlag.getFahrplan(0) != null) {
                                long ankunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung() * 1000 * 60;
                                long abfahrt = eFlag.getFahrplan(0).getAbfahrt() + eFlag.getVerspaetung() * 1000 * 60;

                                if (ankunft <= time && abfahrt >= time) {
                                    z.setNeedUpdate(true);
                                }
                            }
                        } else if (z.getFahrplan() != null && z.getFahrplan(i) != null) {
                            long ankunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung() * 1000 * 60;
                            long abfahrt = z.getFahrplan(i).getAbfahrt() + z.getVerspaetung() * 1000 * 60;

                            if (ankunft <= time && abfahrt >= time) {
                                z.setNeedUpdate(true);
                            }

                        }
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("ZUG: " + z.getZugName() + ": Darstellungsfehler!");
            }
        }
    }

    public void sortiereGleise(){
        Platform.runLater(() -> {
            SortedSet<Bahnsteig> gleisSet = new TreeSet<>(new Comparator<Bahnsteig>() {

              @Override
              public int compare(Bahnsteig o1, Bahnsteig o2) {
                if (o1 == o2) {
                  return 0;
                }
                int cmp;
                cmp = o1.getOrderId() - o2.getOrderId();
                if (cmp != 0) {
                  return cmp;
                }
                cmp = o1.getId() - o2.getId();
                if (cmp != 0) {
                  return cmp;
                }
                cmp = o1.getBahnhof().getId() - o2.getBahnhof().getId();
                if (cmp != 0) {
                  return cmp;
                }
                // wir haben ein Problem

                return 0;
              }

            });
            for (Bahnhof bahnhof : stellwerk.getBahnhoefe()){
            	bahnhof.getBahnsteigOrderSet(gleisSet);
            }
            this.sortierteGleise.clear();
            this.sortierteGleise.addAll(gleisSet);

            gpPlatform.getChildren().clear();
            gp.getChildren().clear();


            int x = 0;
            int y = 0;
            for(Bahnsteig b : sortierteGleise){
                gpPlatform.addColumn(x,b.getGleisLabel().getLabel());
                b.setOrderId(x);
                for(LabelContainer lc : b.getSpalte()){
                    try {
                        gp.add(lc.getLabel(), x, y);
                    }catch (Exception e){
                        System.out.println(b.getName() + " " + x + " " + lc.getLabelIndex() + " " + y);
                        e.printStackTrace();
                    }
                    y++;
                }
                x++;
                y = 0;
            }

            erzeugeBahnsteigLabel();
        });
    }

    public void erzeugeBahnsteigLabel(){
        int aufeinanderfolgendeBahnsteige = 1;
        Bahnsteig letzterBahnsteig = null;
        ArrayList<Bahnsteig> bahnsteige = new ArrayList<>();
        int counter = 0;

        gpBahnhof.getChildren().clear();
        for(Bahnhof b : stellwerk.getBahnhoefe()){
            b.clearBahnhofTeile();
        }

        for(Bahnsteig bahnsteig : sortierteGleise){
            if(bahnsteig.isSichtbar()){
                if(bahnsteig.getGleisLabel().isLetzterBahnsteig()){
                    bahnsteig.getGleisLabel().setLetzterBahnsteig(false);
                    bahnsteig.getGleisLabel().getAussehen().raender.setze(0, 1, 5, 0);
                    bahnsteig.getGleisLabel().getLabel().setStyle(bahnsteig.getGleisLabel().getAussehen().toCSSStyle());
                }
                for(LabelContainer lc : bahnsteig.getSpalte()){
                    lc.setLetzterBahnsteig(false);
                    lc.updateLabel();
                }

                if(letzterBahnsteig != null && letzterBahnsteig.getBahnhof().getId() == bahnsteig.getBahnhof().getId()){
                    if(bahnsteig.isSichtbar()){
                        aufeinanderfolgendeBahnsteige++;
                        bahnsteige.add(letzterBahnsteig);
                    }
                } else if(letzterBahnsteig != null){
                    LabelContainer lc = new LabelContainer(-1, null);
                    if(!letzterBahnsteig.getBahnhof().getAlternativName().equals("")) {
                        lc.getLabel().setText(letzterBahnsteig.getBahnhof().getAlternativName() + (!letzterBahnsteig.getBahnhof().getName().equals("") ? " (" + letzterBahnsteig.getBahnhof().getName() + ")" : ""));
                    }
                    else if(!letzterBahnsteig.getBahnhof().getName().equals("")) lc.getLabel().setText(letzterBahnsteig.getBahnhof().getName());
                    else lc.getLabel().setText(stellwerk.getStellwerksname());

                    bahnsteige.add(letzterBahnsteig);
                    letzterBahnsteig.getBahnhof().addBahnhofLabel(lc, new ArrayList<>(bahnsteige));
                    bahnsteige.clear();

                    final int tempCounter = aufeinanderfolgendeBahnsteige;
                    final int temp = counter;
                    Platform.runLater(() -> {
                        lc.getAussehen().textFarbe = "#fff";
                        lc.getAussehen().raender.farbeUnten = "#505050";
                        lc.getAussehen().raender.farbeRechts = "#505050";
                        lc.getAussehen().raender.setze(0, 5, 1, 0);
                        lc.getLabel().setMinWidth(Einstellungen.spaltenbreite * tempCounter);
                        lc.getLabel().setMaxWidth(Einstellungen.spaltenbreite * tempCounter);
                        lc.getLabel().setStyle(lc.getAussehen().toCSSStyle());
                        gpBahnhof.add(lc.getLabel(), temp, 0);
                    });

                    letzterBahnsteig.getGleisLabel().setLetzterBahnsteig(true);
                    letzterBahnsteig.getGleisLabel().getAussehen().raender.setze(0, 5, 5, 0);
                    letzterBahnsteig.getGleisLabel().getLabel().setStyle(letzterBahnsteig.getGleisLabel().getAussehen().toCSSStyle());

                    for(LabelContainer labelContainer : letzterBahnsteig.getSpalte()){
                        labelContainer.setLetzterBahnsteig(true);
                        labelContainer.updateLabel();
                    }

                    aufeinanderfolgendeBahnsteige = 1;
                }

                counter++;
                letzterBahnsteig = bahnsteig;
            }
        }

        LabelContainer lc = new LabelContainer(-1, null);
        if(letzterBahnsteig != null && letzterBahnsteig.getBahnhof() != null && letzterBahnsteig.getBahnhof().getAlternativName() != null && !letzterBahnsteig.getBahnhof().getAlternativName().equals("")) {
            lc.getLabel().setText(letzterBahnsteig.getBahnhof().getAlternativName() + (!letzterBahnsteig.getBahnhof().getName().equals("") ? " (" + letzterBahnsteig.getBahnhof().getName() + ")" : ""));
        }
        else if(!letzterBahnsteig.getBahnhof().getName().equals("")) lc.getLabel().setText(letzterBahnsteig.getBahnhof().getName());
        else lc.getLabel().setText(stellwerk.getStellwerksname());

        bahnsteige.add(letzterBahnsteig);
        letzterBahnsteig.getBahnhof().addBahnhofLabel(lc, new ArrayList<>(bahnsteige));
        bahnsteige.clear();

        final int tempCounter = aufeinanderfolgendeBahnsteige;
        final int temp = counter;
        Platform.runLater(() -> {
            lc.getAussehen().textFarbe = "#fff";
            lc.getAussehen().raender.farbeUnten = "#505050";
            lc.getAussehen().raender.farbeRechts = "#505050";
            lc.getAussehen().raender.setze(0, 5, 1, 0);
            lc.getLabel().setStyle(lc.getAussehen().toCSSStyle());

            lc.getLabel().setMinWidth(Einstellungen.spaltenbreite * tempCounter);
            lc.getLabel().setMaxWidth(Einstellungen.spaltenbreite * tempCounter);
            gpBahnhof.add(lc.getLabel(), temp, 0);
        });

        letzterBahnsteig.getGleisLabel().setLetzterBahnsteig(true);
        letzterBahnsteig.getGleisLabel().getAussehen().raender.setze(0, 5, 5, 0);
        letzterBahnsteig.getGleisLabel().getLabel().setStyle(letzterBahnsteig.getGleisLabel().getAussehen().toCSSStyle());
        for(LabelContainer labelContainer : letzterBahnsteig.getSpalte()){
            labelContainer.setLetzterBahnsteig(true);
            labelContainer.updateLabel();
        }
    }

    public void zeigeOrderIds(){
        for(Bahnhof b : stellwerk.getBahnhoefe()){
            for(Bahnsteig ba : b.getBahnsteige()){
                ba.getGleisLabel().getLabel().setText(ba.getName() + " (" + (ba.getOrderId() + 1) + ")");
            }
        }
    }

    public void versteckeOrderIds(){
        for(Bahnhof b : stellwerk.getBahnhoefe()){
            for(Bahnsteig ba : b.getBahnsteige()){
                ba.getGleisLabel().getLabel().setText(ba.getName());
            }
        }
    }

    public void updateUi(double stageWidth, double stageHeight){
        scrollBarWidth.setPrefWidth(stageWidth - 15);
        scrollBarWidth.setTranslateY(stageHeight - 90);

        scrollBarHeight.setPrefHeight(stageHeight - 91);
        scrollBarHeight.setTranslateX(stageWidth - 30);

        spTime.setTranslateY(45);
        spTime.setMinHeight(stageHeight - 120);
        spTime.setMaxHeight(stageHeight - 120);

        spContent.setTranslateY(45);
        spContent.setTranslateX(Einstellungen.spaltenbreite);
        spContent.setMinHeight(stageHeight - 120);
        spContent.setMaxHeight(stageHeight - 120);

        spBahnhof.setTranslateX(Einstellungen.spaltenbreite);
        spBahnhof.setMinWidth(stageWidth - 95);
        spBahnhof.setMaxWidth(stageWidth - 95);

        spPlatform.setTranslateY(20);
        spPlatform.setTranslateX(Einstellungen.spaltenbreite);
        spPlatform.setMinWidth(stageWidth - 95);


        if(Einstellungen.informationenAnzeigen){
            scrollBarWidth.setPrefWidth(stageWidth - Einstellungen.informationenBreite - 15);
            scrollBarWidth.setTranslateY(stageHeight - 90);

            scrollBarHeight.setPrefHeight(stageHeight - 91);
            scrollBarHeight.setTranslateX(stageWidth - Einstellungen.informationenBreite - 30);

            spContent.setMinWidth(stageWidth - Einstellungen.informationenBreite - 115);
            spContent.setMaxWidth(stageWidth - Einstellungen.informationenBreite - 115);

            spBahnhof.setMinWidth(stageWidth - Einstellungen.informationenBreite - 135);
            spBahnhof.setMaxWidth(stageWidth - Einstellungen.informationenBreite - 135);

            spPlatform.setMinWidth(stageWidth - Einstellungen.informationenBreite - 115);
            spPlatform.setMaxWidth(stageWidth - Einstellungen.informationenBreite - 115);
        } else{
            scrollBarWidth.setPrefWidth(stageWidth - 15);
            scrollBarWidth.setTranslateY(stageHeight - 90);

            scrollBarHeight.setPrefHeight(stageHeight - 91);
            scrollBarHeight.setTranslateX(stageWidth - 30);

            spContent.setMinWidth(stageWidth - 95);
            spContent.setMaxWidth(stageWidth - 95);

            spBahnhof.setMinWidth(stageWidth - 95);
            spBahnhof.setMaxWidth(stageWidth - 95);

            spPlatform.setMinWidth(stageWidth - 95);
            spPlatform.setMaxWidth(stageWidth - 95);
        }
    }

    public ArrayList<LabelContainer> getLabelTime(){
        return labelTime;
    }
    public Label getFirstLabel() {
        return firstLabel;
    }
    public Pane getContent() {
        return content;
    }
}
