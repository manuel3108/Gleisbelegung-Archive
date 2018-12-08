package com.gleisbelegung;

import com.gleisbelegung.lib.SignalBox;
import com.gleisbelegung.lib.data.Platform;
import com.gleisbelegung.lib.data.Station;
import com.gleisbelegung.lib.data.Train;
import com.gleisbelegung.lib.data.TrainStop;
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


public class Gleisbelegung {

    private GridPane gp;
    //Das ist die Tabelle, die alles enthält.                       (Vereinfacht einige Sachen, könnte/sollte irgendwann entfernt werden
    private ScrollPane spContent;
    //Das ist das Scroll-Feld welches um die Tabelle von oben gewrappt wird.
    private GridPane gpTime;
    //Speichert die Zeiten in einer einzelnen Tabelle
    private GridPane gpBahnhof;
    private ScrollPane spTime;
    //Wrappt die Zeiten-Tabelle in ein Scroll-Feld
    private GridPane gpPlatform;
    //Eine Tabelle für die Bahnsteige
    private ScrollPane spPlatform;
    //Wrappt die Bahnssteige in ein Scroll-Feld
    private ScrollPane spBahnhof;
    private ScrollBar scrollBarWidth;
    //Scroll-Balken um die Tabelle bewegen zu können
    private ScrollBar scrollBarHeight;
    //Scroll-Balken um die Tabelle bewegen zu können
    private Label firstLabel;
    //neues Label oben links mit Bahnhofs-Namen
    private List<Platform> sortierteGleise = new ArrayList<>();

    private Pane content;
    private SignalBox signalBox;
    private TimeTable timeTable;
    private int rowCounter;

    public Gleisbelegung(SignalBox signalBox) {
        this.signalBox = signalBox;
        timeTable = new TimeTable(signalBox);

        rowCounter = 0;

        firstLabel = new Label("Bahnhofsname");
        firstLabel.setFont(Font.font(Settings.fontSize - 5));
        firstLabel.setMinWidth(Settings.columnWidth);
        firstLabel.setMaxWidth(Settings.columnWidth);
        firstLabel.setMinHeight(45);
        firstLabel.setMaxHeight(45);
        firstLabel.setAlignment(Pos.CENTER);
        firstLabel.setWrapText(true);
        firstLabel.setAlignment(Pos.CENTER);
        javafx.application.Platform.runLater(() -> {
            firstLabel.setText(signalBox.getSignalBoxName());
            firstLabel.setStyle(
                    "-fx-text-fill: white; -fx-border-color: #fff #505050 #505050 #fff; -fx-border-width: 0 5 5 0;");
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
        scrollBarHeight.valueProperty().addListener(
                (ov, old_val, new_val) -> spContent
                        .setVvalue(scrollBarHeight.getValue()));

        gp = new GridPane();
        gp.setHgap(0);
        gp.setVgap(0);
        gp.setPadding(new Insets(0, 20, 0, 0));
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
        gpPlatform.setPadding(new Insets(0, 20, 0, 0));
        spPlatform = new ScrollPane();
        spPlatform.setContent(gpPlatform);
        spPlatform.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spPlatform.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spPlatform.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spPlatform.setTranslateY(10);
        spPlatform.vvalueProperty().addListener((ov, old_val, new_val) -> {
            spContent.setHvalue(spPlatform.getHvalue());
            spBahnhof.setHvalue(spPlatform.getHvalue());
        });

        gpBahnhof = new GridPane();
        gpBahnhof.setHgap(0);
        gpBahnhof.setVgap(0);
        spBahnhof = new ScrollPane();
        spBahnhof.setContent(gpBahnhof);
        spBahnhof.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spBahnhof.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spBahnhof.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spBahnhof.setTranslateX(Settings.columnWidth);

        content = new Pane(spBahnhof, spPlatform, spContent, spTime,
                scrollBarWidth, scrollBarHeight, firstLabel);

        for (Station station : signalBox.getStations()) {
            for (int i = 0; i < station.getAnzahlBahnsteige(); i++) {
                LabelContainer lc = new LabelContainer(i, null);
                lc.updateLabel(station.getBahnsteig(i).getName(), true);
                lc.getAussehen().textFarbe = "#fff";
                lc.getAussehen().raender.farbeUnten = "#505050";
                lc.getAussehen().raender.farbeRechts = "#505050";
                lc.getAussehen().raender.setze(0, 1, 5, 0);
                lc.getLabel().setStyle(lc.getAussehen().toCSSStyle());

                int temp = station.getBahnsteig(i).getId();
                javafx.application.Platform
                        .runLater(() -> gpPlatform.add(lc.getLabel(), temp, 0));
                station.getBahnsteig(i).setGleisLabel(lc);
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sortiereGleise(null);
    }

    public void scrollPaneTo(double x, double y, TrainStop fh) {
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

    private void fuegeReiheHinzu(TimeTable.TimeTableRow ttr) {
        ttr.setNewRow(false);

        LabelContainer lc = new LabelContainer(rowCounter, null);
        final int temp = rowCounter;
        javafx.application.Platform.runLater(() -> {
            lc.setTimeLabel(true, ttr.time);
            gpTime.add(lc.getLabel(), 0, temp);
        });

        int colCounter = 0;
        for (Iterator<TimeTable.TimeTableData> iterData =
             ttr.dataIterator(); iterData.hasNext(); ) {
            TimeTable.TimeTableData ttd = iterData.next();
            LabelContainer lcTemp =
                    new LabelContainer(rowCounter, ttd.getCol().getPlatform());
            lcTemp.updateLabel("", ttd.getRow().time);

            ttd.setLabelContainer(lcTemp);

            if (ttd.getZuege().size() > 0) {
                for (TrainStop fh : ttd.getZuege()) {
                    lcTemp.addTrain(fh.getZug());
                }

            }

            timeTable.getRefresh().remove(ttd);

            final int tempCol = colCounter;
            javafx.application.Platform.runLater(() -> {
                gp.add(lcTemp.getLabel(), tempCol, temp);
            });

            colCounter++;
        }

        rowCounter++;
    }

    public void aktualisiereTabelle() {
        for (TimeTable.TimeTableRow ttr : new ArrayList<TimeTable.TimeTableRow>(
                timeTable.getRows())) {
            if (ttr.isNewRow()) {
                fuegeReiheHinzu(ttr);
            }
        }

        Iterator<TimeTable.TimeTableData> iterator =
                new ArrayList<TimeTable.TimeTableData>(timeTable.getRefresh())
                        .iterator();
        while (iterator.hasNext()) {
            final TimeTable.TimeTableData ttd = iterator.next();

            javafx.application.Platform.runLater(() -> {
                if (ttd.getLabelContainer() != null) {
                    ttd.getLabelContainer().setTrains(ttd.getZuege());
                }
            });
        }
        timeTable.getRefresh().clear();
    }

    public void entferneVergangenheit() {
        timeTable.entferneVergangenheit();

        if (gp.getChildren().size() - signalBox.getAnzahlBahnsteige() > 0) {
            javafx.application.Platform.runLater(() -> {
                gpTime.getChildren().remove(0);
                gp.getChildren().remove(0, signalBox.getAnzahlBahnsteige());
            });
        }
    }

    public void update() {
        //System.out.println(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + " start");

        boolean wasUpdate = false;
        for (Train z : signalBox.getTrains()) {
            if (z.isNewTrain()) {
                timeTable.addZug(z);
                wasUpdate = true;
            } else if (z.isSchwerwiegendesUpdate()) {
                timeTable.updateZug(z);
                wasUpdate = true;
            } else {
                for (TrainStop fh : z.getSchedule()) {
                    if (fh.isNeedUpdate()) {
                        wasUpdate = true;
                        timeTable.updateFahrplanhalt(fh);
                    }
                }
            }
        }

        //if(gp.getChildren().size() == 0) zeichneTabelle();
        if (wasUpdate)
            aktualisiereTabelle();

        //System.out.println(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + " ende");
    }

    public void sortiereGleise(Runnable callback) {
        javafx.application.Platform.runLater(() -> {
            SortedSet<Platform> gleisSet = new TreeSet<>();
            for (Station station : signalBox.getStations()) {
                station.getBahnsteigOrderSet(gleisSet);
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
            for (Platform b : sortierteGleise) {
                gpPlatform.addColumn(x, b.getGleisLabel().getLabel());
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

    public void erzeugeBahnsteigLabel() {
        int aufeinanderfolgendeBahnsteige = 1;
        Platform letzterPlatform = null;
        ArrayList<Platform> bahnsteige = new ArrayList<>();
        int counter = 0;

        gpBahnhof.getChildren().clear();
        for (Station b : signalBox.getStations()) {
            b.clearBahnhofTeile();
        }

        for (Platform platform : sortierteGleise) {
            if (platform.getVisible()) {
                if (platform.getGleisLabel().isLetzterBahnsteig()) {
                    platform.getGleisLabel().setLetzterBahnsteig(false);
                    platform.getGleisLabel().getAussehen().raender
                            .setze(0, 1, 5, 0);
                    platform.getGleisLabel().getLabel().setStyle(
                            platform.getGleisLabel().getAussehen()
                                    .toCSSStyle());
                }
                /*for(LabelContainer lc : platform.getSpalte()){
                    lc.setLetzterBahnsteig(false);
                    lc.updateLabel();
                }*/

                if (letzterPlatform != null
                        && letzterPlatform.getStation().getId() == platform
                        .getStation().getId()) {
                    if (platform.getVisible()) {
                        aufeinanderfolgendeBahnsteige++;
                        bahnsteige.add(letzterPlatform);
                    }
                } else if (letzterPlatform != null) {
                    LabelContainer lc = new LabelContainer(-1, null);
                    if (!letzterPlatform.getStation().getNameByUser()
                            .equals("")) {
                        lc.getLabel().setText(letzterPlatform.getStation()
                                .getNameByUser() + (!letzterPlatform
                                .getStation().getName().equals("") ?
                                " (" + letzterPlatform.getStation().getName()
                                        + ")" :
                                ""));
                    } else if (!letzterPlatform.getStation().getName()
                            .equals(""))
                        lc.getLabel().setText(
                                letzterPlatform.getStation().getName());
                    else
                        lc.getLabel().setText(signalBox.getSignalBoxName());

                    bahnsteige.add(letzterPlatform);
                    letzterPlatform.getStation()
                            .addBahnhofLabel(lc, new ArrayList<>(bahnsteige));
                    bahnsteige.clear();

                    final int tempCounter = aufeinanderfolgendeBahnsteige;
                    final int temp = counter;
                    javafx.application.Platform.runLater(() -> {
                        lc.getAussehen().textFarbe = "#fff";
                        lc.getAussehen().raender.farbeUnten = "#505050";
                        lc.getAussehen().raender.farbeRechts = "#505050";
                        lc.getAussehen().raender.setze(0, 5, 1, 0);
                        lc.getLabel().setMinWidth(
                                Settings.columnWidth * tempCounter);
                        lc.getLabel().setMaxWidth(
                                Settings.columnWidth * tempCounter);
                        lc.getLabel().setStyle(lc.getAussehen().toCSSStyle());
                        gpBahnhof.add(lc.getLabel(), temp, 0);
                    });

                    letzterPlatform.getGleisLabel().setLetzterBahnsteig(true);
                    letzterPlatform.getGleisLabel().getAussehen().raender
                            .setze(0, 5, 5, 0);
                    letzterPlatform.getGleisLabel().getLabel().setStyle(
                            letzterPlatform.getGleisLabel().getAussehen()
                                    .toCSSStyle());

                    /*for(LabelContainer labelContainer : letzterPlatform.getSpalte()){
                        labelContainer.setLetzterBahnsteig(true);
                        labelContainer.updateLabel();
                    }*/

                    aufeinanderfolgendeBahnsteige = 1;
                }

                counter++;
                letzterPlatform = platform;
            }
        }

        LabelContainer lc = new LabelContainer(-1, null);
        if (letzterPlatform != null && letzterPlatform.getStation() != null
                && letzterPlatform.getStation().getNameByUser() != null
                && !letzterPlatform.getStation().getNameByUser()
                .equals("")) {
            lc.getLabel().setText(
                    letzterPlatform.getStation().getNameByUser()
                            + (!letzterPlatform.getStation().getName()
                            .equals("") ?
                            " (" + letzterPlatform.getStation().getName()
                                    + ")" :
                            ""));
        } else if (!letzterPlatform.getStation().getName().equals(""))
            lc.getLabel().setText(letzterPlatform.getStation().getName());
        else
            lc.getLabel().setText(signalBox.getSignalBoxName());

        bahnsteige.add(letzterPlatform);
        letzterPlatform.getStation()
                .addBahnhofLabel(lc, new ArrayList<>(bahnsteige));
        bahnsteige.clear();

        final int tempCounter = aufeinanderfolgendeBahnsteige;
        final int temp = counter;
        javafx.application.Platform.runLater(() -> {
            lc.getAussehen().textFarbe = "#fff";
            lc.getAussehen().raender.farbeUnten = "#505050";
            lc.getAussehen().raender.farbeRechts = "#505050";
            lc.getAussehen().raender.setze(0, 5, 1, 0);
            lc.getLabel().setStyle(lc.getAussehen().toCSSStyle());

            lc.getLabel()
                    .setMinWidth(Settings.columnWidth * tempCounter);
            lc.getLabel()
                    .setMaxWidth(Settings.columnWidth * tempCounter);
            gpBahnhof.add(lc.getLabel(), temp, 0);
        });

        letzterPlatform.getGleisLabel().setLetzterBahnsteig(true);
        letzterPlatform.getGleisLabel().getAussehen().raender
                .setze(0, 5, 5, 0);
        letzterPlatform.getGleisLabel().getLabel().setStyle(
                letzterPlatform.getGleisLabel().getAussehen().toCSSStyle());
        /*for(LabelContainer labelContainer : letzterPlatform.getSpalte()){
            labelContainer.setLetzterBahnsteig(true);
            labelContainer.updateLabel();
        }*/
    }

    public void zeigeOrderIds() {
        for (Station b : signalBox.getStations()) {
            for (Platform ba : b.getPlatforms()) {
                ba.getGleisLabel().getLabel().setText(
                        ba.getName() + " (" + (ba.getOrderId() + 1) + ")");
            }
        }
    }

    public void versteckeOrderIds() {
        for (Station b : signalBox.getStations()) {
            for (Platform ba : b.getPlatforms()) {
                ba.getGleisLabel().getLabel().setText(ba.getName());
            }
        }
    }

    public void updateUi(double stageWidth, double stageHeight) {
        scrollBarWidth.setPrefWidth(stageWidth - 15);
        scrollBarWidth.setTranslateY(stageHeight - 90);

        scrollBarHeight.setPrefHeight(stageHeight - 91);
        scrollBarHeight.setTranslateX(stageWidth - 30);

        spTime.setTranslateY(45);
        spTime.setMinHeight(stageHeight - 120);
        spTime.setMaxHeight(stageHeight - 120);

        spContent.setTranslateY(45);
        spContent.setTranslateX(Settings.columnWidth);
        spContent.setMinHeight(stageHeight - 120);
        spContent.setMaxHeight(stageHeight - 120);

        spBahnhof.setTranslateX(Settings.columnWidth);
        spBahnhof.setMinWidth(stageWidth - 95);
        spBahnhof.setMaxWidth(stageWidth - 95);

        spPlatform.setTranslateY(20);
        spPlatform.setTranslateX(Settings.columnWidth);
        spPlatform.setMinWidth(stageWidth - 95);


        if (Settings.showTrainInformations) {
            scrollBarWidth.setPrefWidth(
                    stageWidth - Settings.trainInformationWidth - 15);
            scrollBarWidth.setTranslateY(stageHeight - 90);

            scrollBarHeight.setPrefHeight(stageHeight - 91);
            scrollBarHeight.setTranslateX(
                    stageWidth - Settings.trainInformationWidth - 30);

            spContent.setMinWidth(
                    stageWidth - Settings.trainInformationWidth - 115);
            spContent.setMaxWidth(
                    stageWidth - Settings.trainInformationWidth - 115);

            spBahnhof.setMinWidth(
                    stageWidth - Settings.trainInformationWidth - 135);
            spBahnhof.setMaxWidth(
                    stageWidth - Settings.trainInformationWidth - 135);

            spPlatform.setMinWidth(
                    stageWidth - Settings.trainInformationWidth - 115);
            spPlatform.setMaxWidth(
                    stageWidth - Settings.trainInformationWidth - 115);
        } else {
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

    public ArrayList<LabelContainer> getLabelTime() {
        //return labelTime;
        return null;
    }

    public Label getFirstLabel() {
        return firstLabel;
    }

    public Pane getContent() {
        return content;
    }

    public TimeTable getTimeTable() {
        return timeTable;
    }
}
