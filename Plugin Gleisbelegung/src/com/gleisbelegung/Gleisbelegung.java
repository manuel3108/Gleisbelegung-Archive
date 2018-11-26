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

import java.util.*;
import java.util.stream.Collectors;

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
    private List<Bahnsteig> sortierteGleise = new ArrayList<>();

    private Pane content;
    private Stellwerk stellwerk;
    private TimeTable timeTable;

    public Gleisbelegung(Stellwerk stellwerk){
        this.stellwerk = stellwerk;
        timeTable = new TimeTable(stellwerk);

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
        spTime.vvalueProperty().addListener((ov, old_val, new_val) -> {
            spContent.setVvalue(spTime.getVvalue());
            scrollBarHeight.adjustValue(spTime.getVvalue());
        });

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

        sortiereGleise(null);
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

    private void zeichneTabelle(){
        /*Platform.runLater(() -> {
            content.getChildren().clear();
        });*/

        int rowCounter = 0;

        for(Iterator<TimeTable.TimeTableRow> iterRow = timeTable.rowIterator(); iterRow.hasNext(); ){
        	TimeTable.TimeTableRow ttr = iterRow.next();
            LabelContainer lc = new LabelContainer(rowCounter, null);

            final int temp = rowCounter;
            Platform.runLater(() -> {
                lc.setTimeLabel(true, ttr.time);
                gpTime.add(lc.getLabel(), 0, temp);
            });

            int colCounter = 0;

            for(Iterator<TimeTable.TimeTableData> iterData = ttr.dataIterator(); iterData.hasNext(); ){
            	TimeTable.TimeTableData ttd = iterData.next();
                LabelContainer lcTemp = new LabelContainer(rowCounter, ttd.getCol().getBahnsteig());
                lcTemp.updateLabel("", ttd.getRow().time);

                ttd.setLabelContainer(lcTemp);

                if(ttd.getZuege().size() > 0){
                    for(FahrplanHalt fh : ttd.getZuege()){
                        lcTemp.addTrain(fh.getZug());
                    }

                }

                timeTable.getRefresh().remove(ttd);

                final int tempCol = colCounter;
                Platform.runLater(() -> {
//                    lcTemp.getLabel().setTranslateX(tempCol*100);
//                    lcTemp.getLabel().setTranslateY(temp*20);
//                    gp.getChildren().add(lcTemp.getLabel());
                    gp.add(lcTemp.getLabel(), tempCol, temp);
                });

                colCounter++;
            }

            rowCounter++;
        }
    }

    public void aktualisiereTabelle(){

        Iterator<TimeTable.TimeTableData> iterator = timeTable.getRefresh().iterator();

        while(iterator.hasNext()){
            TimeTable.TimeTableData ttd = iterator.next();

            System.out.println("AKTUALISIERE: " + ttd.getRow().time + " und " + ttd.getCol().getBahnsteig().getName());
            int indexRow = timeTable.getRows().indexOf(ttd.getRow());
            int indexCol = stellwerk.getBahnsteige().indexOf(ttd.getCol().getBahnsteig());

            Platform.runLater(() -> {
                if(ttd.getLabelContainer() != null){
                    ttd.getLabelContainer().setTrains(ttd.getZuege());
                }
            });

            iterator.remove();
        }
        timeTable.getRefresh().clear();
    }

    public void entferneVergangenheit(){
        timeTable.entferneVergangenheit();

        if(gp.getChildren().size() - stellwerk.getAnzahlBahnsteige() > 0){
            Platform.runLater(() -> {
                gpTime.getChildren().remove(0);
                gp.getChildren().remove(0, stellwerk.getAnzahlBahnsteige());
            });
        }
    }

    public void update(){
        boolean wasUpdate = false;
        for (Zug z : stellwerk.getZuege()) {
            if(z.getZugName().equals("LT 84355")){
                System.out.println("zug");
            }

            if(z.isNewTrain()) {
                timeTable.addZug(z);
                wasUpdate = true;
            } else if(z.isSchwerwiegendesUpdate()) {
                timeTable.updateZug(z);
                wasUpdate = true;
            } else {
                for(FahrplanHalt fh : z.getFahrplan()){
                    if(fh.isNeedUpdate()){
                        wasUpdate = true;
                        timeTable.updateFahrplanhalt(fh);
                    }
                }
            }
        }

        if(gp.getChildren().size() == 0) zeichneTabelle();
        else if(wasUpdate) aktualisiereTabelle();
    }

    public void sortiereGleise(Runnable callback){
        Platform.runLater(() -> {
            SortedSet<Bahnsteig> gleisSet = new TreeSet<>();
            for (Bahnhof bahnhof : stellwerk.getBahnhoefe()){
            	bahnhof.getBahnsteigOrderSet(gleisSet);
            }
            if (null != callback) {
              callback.run();
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
                /*for(LabelContainer lc : b.getSpalte()){
                  // TOFIX: ConcurrentModification for lc
                    try {
                        gp.add(lc.getLabel(), x, y);
                    }catch (Exception e){
                        System.out.println(b.getName() + " " + x + " " + lc.getLabelIndex() + " " + y);
                        e.printStackTrace();
                    }
                    y++;
                }*/
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
                /*for(LabelContainer lc : bahnsteig.getSpalte()){
                    lc.setLetzterBahnsteig(false);
                    lc.updateLabel();
                }*/

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

                    /*for(LabelContainer labelContainer : letzterBahnsteig.getSpalte()){
                        labelContainer.setLetzterBahnsteig(true);
                        labelContainer.updateLabel();
                    }*/

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
        /*for(LabelContainer labelContainer : letzterBahnsteig.getSpalte()){
            labelContainer.setLetzterBahnsteig(true);
            labelContainer.updateLabel();
        }*/
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
        //return labelTime;
        return null;
    }
    public Label getFirstLabel() {
        return firstLabel;
    }
    public Pane getContent() {
        return content;
    }
}
