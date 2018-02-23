package com.gleisbelegung;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Erzeugen und Updaten der Gui (u.a. die Tabelle und die Informationen)
 */

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.FahrplanHalt;
import com.gleisbelegung.lib.data.Zug;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class Fenster{
    private Label pluginName;                           //Textfeld welches den Namen des Plugin in der Linken oberen Ecke speichert
    private Label simZeit;                              //Textfeld welches die Simzeit in der rechte oberen Ecke speichert
    private Button einstellungen;                       //Button um das Fenster mit den Einstellungen zu öffnen
    private int labelIndexCounter;                      //Zählt die Textfelder auf der y-Achse mit um eine Identifikation zu ermöglichen
    private Scene s;                                    //Hält die Scene mit der Tabelle gespeichert,
    private TextField zugSuche;                         //Textbox um den gesuchrten Zugnamen einzugeben

    private GridPane gp;                                //Das ist die Tabelle, die alles enthält.                       (Vereinfacht einige Sachen, könnte/sollte irgendwann entfernt werden
    private ScrollPane spContent;                       //Das ist das Scroll-Feld welches um die Tabelle von oben gewrappt wird.
    private GridPane gpTime;                            //Speichert die Zeiten in einer einzelnen Tabelle
    private ScrollPane spTime;                          //Wrappt die Zeiten-Tabelle in ein Scroll-Feld
    private GridPane gpPlatform;                        //Eine Tabelle für die Bahnsteige
    private ScrollPane spPlatform;                      //Wrappt die Bahnssteige in ein Scroll-Feld
    private ScrollPane spInformations;                  //Scroll-Feld fr das in der @Main-Klasse deklariert Feld informations
    private ScrollBar scrollBarWidth;                   //Scroll-Balken um die Tabelle bewegen zu können
    private ScrollBar scrollBarHeight;                  //Scroll-Balken um die Tabelle bewegen zu können
    private Label firstLabel;                           //neues Label oben links mit Bahnhofs-Namen

    static Pane informations;                               //Panel für alle Zuginformation                             (Zugnummer, Verspätung etc.)
    private PrintWriter logFile;                             //
    private Pane pZugSuche;                                  //Panel mit der Zugsuche
    private double stageWidth = 1000;                        //Standartmäßige Fenster-Breite                             (Wir bei Veränderung der Breite aktualisiert)
    private double stageHeight = 500;                        //Standartmäßige Fenster-Höhe                               (Wir bei Veränderung der Höhe aktualisiert)
    private Stage primaryStage;
    private Button refresh;
    private Pane infoFehler;

    private ArrayList<LabelContainer> labelTime;

    private Stellwerk stellwerk;

    Fenster(Stellwerk stellwerk, Stage primaryStage, Button refresh) {
        this.stellwerk = stellwerk;
        this.primaryStage = primaryStage;
        this.refresh = refresh;

        stageHeight = primaryStage.getHeight();
        stageWidth = primaryStage.getWidth();

        labelTime = new ArrayList<>();

        Date dNow = new Date(stellwerk.getSpielzeit());
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");

        pluginName = new Label();
        pluginName.setText("Plugin: Gleisbelegung");
        pluginName.setStyle("-fx-text-fill: #fff;");
        pluginName.setFont(Font.font(Einstellungen.schriftgroesse));

        simZeit = new Label();
        simZeit.setText("Simzeit: " + ft.format(dNow));
        simZeit.setStyle("-fx-text-fill: white;");
        simZeit.setFont(Font.font(Einstellungen.schriftgroesse));

        einstellungen = new Button();
        einstellungen.setText("Einstellungen");
        einstellungen.setFont(Font.font(Einstellungen.schriftgroesse - 2));
        einstellungen.setOnAction(e -> { try{ settings(); }catch(Exception ex) { ex.printStackTrace(); } });

        refresh.setText("Neustart");
        refresh.setFont(Font.font(Einstellungen.schriftgroesse - 2));

        Pane topP = new Pane();
        Platform.runLater(() -> topP.getChildren().addAll(pluginName, simZeit, einstellungen, refresh));

        firstLabel = new Label("Bahnhofsname");
        firstLabel.setFont(Font.font(Einstellungen.schriftgroesse-5));
        firstLabel.setMinWidth(Einstellungen.spaltenbreite);
        firstLabel.setMaxWidth(Einstellungen.spaltenbreite);
        firstLabel.setAlignment(Pos.CENTER);
        Platform.runLater(() -> {
            firstLabel.setText(stellwerk.getStellwerksname());
            firstLabel.setStyle("-fx-text-fill: white; -fx-border-color: #fff #505050 #505050 #fff; -fx-border-width: 0 5 5 0; ");
        });

        scrollBarWidth = new ScrollBar();
        scrollBarWidth.setOrientation(Orientation.HORIZONTAL);
        scrollBarWidth.setMin(0);
        scrollBarWidth.setMax(1);
        scrollBarWidth.setUnitIncrement(0.03);
        scrollBarWidth.setVisibleAmount(0.3);
        scrollBarWidth.valueProperty().addListener((ov, old_val, new_val) -> spContent.setHvalue(scrollBarWidth.getValue()));

        scrollBarHeight = new ScrollBar();
        scrollBarHeight.setOrientation(Orientation.VERTICAL);
        scrollBarHeight.setPrefHeight(stageHeight);
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

        gpTime = new GridPane();
        gpTime.setHgap(0);
        gpTime.setVgap(0);
        spTime = new ScrollPane();
        spTime.setContent(gpTime);
        spTime.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spTime.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spTime.setStyle("-fx-background: #303030; -fx-padding: 0;");

        gpPlatform = new GridPane();
        gpPlatform.setHgap(0);
        gpPlatform.setVgap(0);
        gpPlatform.setPadding(new Insets(0,20,0,0));
        spPlatform = new ScrollPane();
        spPlatform.setContent(gpPlatform);
        spPlatform.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spPlatform.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spPlatform.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Pane content = new Pane(spPlatform, spContent, spTime, scrollBarWidth, scrollBarHeight, firstLabel);

        informations = new Pane();
        informations.setStyle("-fx-background-color: #404040");
        spInformations = new ScrollPane(informations);
        spInformations.setStyle("-fx-background-color: #404040; -fx-padding: 0;");
        spInformations.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spInformations.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Label lZugSuche = new Label("Zugsuche:");
        lZugSuche.setFont(Font.font(Einstellungen.schriftgroesse-2));
        lZugSuche.setStyle("-fx-text-fill: white;");

        zugSuche = new TextField();
        zugSuche.setFont(Font.font(Einstellungen.schriftgroesse-5));
        zugSuche.setStyle("-fx-text-fill: black;");
        zugSuche.textProperty().addListener((observable, oldVal, newVal) -> {
            try{
                searchTrain(zugSuche.getText());
            } catch(Exception e){
                System.out.println("INFORMATION: Fehler beim autom. Scrollen!");
            }
        });

        pZugSuche = new Pane(lZugSuche, zugSuche);
        pZugSuche.setStyle("-fx-background-color: #404040");


        infoFehler = new Pane(spInformations, pZugSuche);
        infoFehler.setStyle("-fx-background-color: #404040");

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #303030;");
        bp.setTop(topP);
        bp.setCenter(content);
        bp.setRight(infoFehler);

        s = new Scene(bp, stageWidth, stageHeight);

        Platform.runLater(() -> {
            primaryStage.setScene(s);
        });

        primaryStage.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> { try{ updateUi(); }catch(Exception e) { e.printStackTrace(); } });
        primaryStage.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> { try{ updateUi(); }catch(Exception e) { e.printStackTrace(); } });

        /*boolean ssh = Einstellungen.informationenAnzeigen;
        Einstellungen.informationenAnzeigen = true;
        updateUi();
        Einstellungen.informationenAnzeigen = false;
        updateUi();
        Einstellungen.informationenAnzeigen = ssh;
        updateUi();*/
        updateUi();

        for (int i = 0; i < Einstellungen.vorschau; i++) {
            labelTime.add(i, new LabelContainer(i, null));

            dNow = new Date(stellwerk.getSpielzeit() + i*1000*60);
            ft = new SimpleDateFormat("HH:mm");
            labelTime.get(i).updateLabel(ft.format(dNow), false);

            int temp = i;
            Platform.runLater(() -> gpTime.add(labelTime.get(temp).getLabel(), 0, temp));
        }

        for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
            for (int i = 0; i < bahnhof.getBahnsteige().size(); i++) {
                LabelContainer lc = new LabelContainer(i,null);
                lc.updateLabel(bahnhof.getBahnsteig(i).getName(), true);
                lc.getLabel().setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 5 0");

                int temp = bahnhof.getBahnsteig(i).getId();
                Platform.runLater(() -> gpPlatform.add(lc.getLabel(), temp, 0));

                final int tempI = i;
                lc.getLabel().setOnMouseClicked(e -> {
                    if(e.getButton() == MouseButton.PRIMARY){
                        bahnhof.getBahnsteig(tempI).hebeHervor();
                    } else if(e.getButton() == MouseButton.SECONDARY){
                        aendereReihenfolge(bahnhof.getBahnsteig(tempI));
                    }
                });
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
            //System.out.println(b.getName() + " " + b.getBahnsteige().size());
            /*for(LabelContainer lc : b.getBahnsteige().get(b.getBahnsteige().size()-1).getSpalte()){
                lc.getLabel().setStyle(lc.getLabel().getStyle() + "; -fx-border-width: 0 5 1 0");
            }*/
        }

        this.refresh.setDisable(false);

        Runnable r = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                if(primaryStage.isMaximized()){
                    primaryStage.setMaximized(false);
                    primaryStage.setMaximized(true);
                }
            });
        };
        new Thread(r).start();
    }

    private void searchTrain(String text){
        ArrayList<Zug> trains = new ArrayList<>();
        for(Zug zTemp : stellwerk.getZuege()){
            if(!text.equals("") && zTemp.getZugName().contains(text)){
                trains.add(zTemp);
            }
        }

        if(trains.size() > 0){
            int heightCounter = 0;
            informations.getChildren().clear();
            for(Zug z : trains){
                Label trainName = new Label(z.getZugName() + z.getVerspaetungToString());
                trainName.setStyle("-fx-text-fill: white");
                trainName.setFont(Font.font(Einstellungen.schriftgroesse-2));
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
                vonBis.setFont(Font.font(Einstellungen.schriftgroesse-5));
                vonBis.setTranslateY(heightCounter + 25);

                informations.getChildren().addAll(trainName, vonBis);

                for(int i = 0; i < z.getFahrplan().size(); i++){
                    if(z.getFahrplan(i).getFlaggedTrain() != null){
                        Zug flagged = z.getFahrplan(i).getFlaggedTrain();

                        long lAnkunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung()*1000*60;
                        long lAbfahrt = flagged.getFahrplan(0).getAbfahrt() + flagged.getVerspaetung()*1000*60;
                        if(flagged.getVerspaetung() > 3 && (lAbfahrt-lAnkunft)/1000/60 > 3){
                            lAbfahrt = lAnkunft + 4*1000*60;
                        }

                        Date anunft = new Date(lAnkunft);
                        Date abfahrt = new Date(lAbfahrt);
                        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

                        Label l = new Label("Bahnsteig: " + z.getFahrplan(i).getBahnsteig().getName() + " " + ft.format(anunft) + " - " + ft.format(abfahrt));
                        l.setFont(Font.font(Einstellungen.schriftgroesse-5));
                        l.setTranslateY(heightCounter + 55);
                        l.setPrefWidth(Einstellungen.spaltenbreite - 25);

                        if(z.getBahnsteig().getName().equals(z.getFahrplan(i).getBahnsteig().getName()) && z.getAmGleis()) {
                            l.setStyle("-fx-text-fill: white; -fx-background-color: green");
                        } else{
                            l.setStyle("-fx-text-fill: white");
                        }

                        informations.getChildren().add(l);
                    } else{
                        long lAnkunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung()*1000*60;
                        long lAbfahrt = z.getFahrplan(i).getAbfahrt() + z.getVerspaetung()*1000*60;
                        if(z.getVerspaetung() > 3 && (lAbfahrt-lAnkunft)/1000/60 > 3){
                            lAbfahrt = lAnkunft + 4*1000*60;
                        }

                        Date anunft = new Date(lAnkunft);
                        Date abfahrt = new Date(lAbfahrt);
                        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

                        Label l = new Label("Bahnsteig: " + z.getFahrplan(i).getBahnsteig().getName() + " " + ft.format(anunft) + " - " + ft.format(abfahrt));
                        l.setFont(Font.font(Einstellungen.schriftgroesse-5));
                        l.setTranslateY(heightCounter + 55);
                        l.setPrefWidth(215);

                        if(z.getBahnsteig().getName().equals(z.getFahrplan(i).getBahnsteig().getName()) && z.getAmGleis()) {
                            l.setStyle("-fx-text-fill: white; -fx-background-color: green");
                        } else{
                            l.setStyle("-fx-text-fill: white");
                        }

                        informations.getChildren().add(l);
                    }

                    heightCounter += 20;
                }

                heightCounter += 75;
            }

            if(heightCounter > (stageHeight/2-70)){
                //informations.setMinHeight(heightCounter);
                informations.setPrefHeight(heightCounter);
            } else{
                informations.setMinHeight(stageHeight/2-70);
            }
        }

        Platform.runLater(() -> {
            if(trains.size() == 1){
                for(FahrplanHalt fh : trains.get(0).getFahrplan()){
                    if(fh != null && fh.isDrawable() && fh.getDrawnTo().size() > 0){
                        LabelContainer lc = fh.getDrawnTo(0);
                        if(lc != null){
                            try{
                                scrollPaneTo((double) lc.getBahnsteig().getOrderId() / (double) stellwerk.getAnzahlBahnsteige(), (double) lc.getLabelIndex() / (double) Einstellungen.vorschau, fh);
                            } catch(Exception e){
                                System.out.println("INFORMATION: Fehler beim autom. Scrollen!");
                            }

                            Runnable r = () -> {
                                try {
                                    Thread.sleep(2000);
                                    zugSuche.selectAll();
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                }
                            };

                            try{
                                new Thread(r).start();
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    private void scrollPaneTo(double x, double y, FahrplanHalt fh){
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
                    for(Bahnhof b : stellwerk.getBahnhoefe()){
                        for (int j = 0; j < b.getBahnsteige().size(); j++) {
                            Bahnsteig g = b.getBahnsteig(j);
                            if(g != null && z.getFahrplan(i) != null && z.getFahrplan(i).getBahnsteig().getName().equals(g.getName())){
                                if(z.getFahrplan(i).getFlaggedTrain() != null){
                                    Zug eFlag = z.getFahrplan(i).getFlaggedTrain();

                                    if(z.getFahrplan(i).isDrawable()){
                                        long ankunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung()*1000*60;
                                        long abfahrt = eFlag.getFahrplan(0).getAbfahrt() + eFlag.getVerspaetung()*1000*60;
                                        if(eFlag.getVerspaetung() < 0 && !z.getFahrplan(i).isCrossing()){
                                            abfahrt = eFlag.getFahrplan(0).getAbfahrt();
                                        } else if(z.getVerspaetung() > 3 && (abfahrt-ankunft)/1000/60 > 3){
                                            abfahrt = ankunft + 4*1000*60;
                                        }

                                        for (int k = 0; k < Einstellungen.vorschau; k++) {
                                            if (g.getSpalte() != null && k<g.getSpalte().size()&&b.getBahnsteig(j).getSpalte().get(k) != null) {
                                                LabelContainer lc = b.getBahnsteig(j).getSpalte().get(k);
                                                if(ankunft <= lc.getTime() && abfahrt >= lc.getTime() - 1000*60){
                                                    z.getFahrplan(i).addDrawnTo(lc);

                                                    if(z.getFahrplan(i).isCrossing()){
                                                        Platform.runLater(() -> lc.getLabel().setText(lc.getLabel().getText() + " D"));
                                                        System.out.println(z.getZugName() + " Durchfahrt");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else{
                                    if(z.getFahrplan(i).isDrawable()){
                                        long ankunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung()*1000*60;
                                        long abfahrt = z.getFahrplan(i).getAbfahrt() + z.getVerspaetung()*1000*60;
                                        if(z.getVerspaetung() < 0 && !z.getFahrplan(i).isCrossing()){
                                            abfahrt = z.getFahrplan(i).getAbfahrt();
                                        } else if(z.getVerspaetung() > 3 && (abfahrt-ankunft)/1000/60 > 3){
                                            abfahrt = ankunft + 4*1000*60;
                                        }

                                        for (int k = 0; k < Einstellungen.vorschau; k++) {
                                            if (g.getSpalte() != null && k<g.getSpalte().size()&&b.getBahnsteig(j).getSpalte().get(k) != null) {
                                                LabelContainer lc = b.getBahnsteig(j).getSpalte().get(k);
                                                if(ankunft <= lc.getTime() && abfahrt >= lc.getTime() - 1000*60){
                                                    z.getFahrplan(i).addDrawnTo(lc);

                                                    if(z.getFahrplan(i).isCrossing()){
                                                        Platform.runLater(() -> lc.getLabel().setFont(Font.font("", FontPosture.ITALIC, Einstellungen.schriftgroesse-5)));
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

    public void updateSimTime(int updatingIn){
        Date dNow = new Date(stellwerk.getSpielzeit());
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");

        Platform.runLater(() -> simZeit.setText("Simzeit: " + ft.format(dNow) + "     Aktualisierung in: " + updatingIn + "s"));
        Date dauer = new Date(System.currentTimeMillis() - stellwerk.getStartzeit() - 1000*60*60);

        Platform.runLater(() -> pluginName.setText("Plugin: Gleisbelegung     Spieldauer: " + ft.format(dauer)));
    }

    public void update(){
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

    private void updateUi(){
        stageHeight = primaryStage.getHeight();
        stageWidth = primaryStage.getWidth();

        scrollBarWidth.setPrefWidth(stageWidth - 15);
        scrollBarWidth.setTranslateY(stageHeight - 90);

        scrollBarHeight.setPrefHeight(stageHeight - 91);
        scrollBarHeight.setTranslateX(stageWidth - 30);

        spTime.setTranslateY(25);
        spTime.setMinHeight(stageHeight - 120);
        spTime.setMaxHeight(stageHeight - 120);

        spContent.setTranslateY(25);
        spContent.setTranslateX(Einstellungen.spaltenbreite);
        spContent.setMinHeight(stageHeight - 120);
        spContent.setMaxHeight(stageHeight - 120);

        spPlatform.setTranslateX(Einstellungen.spaltenbreite);
        spPlatform.setMinWidth(stageWidth - 95);
        spPlatform.setMaxWidth(stageWidth - 95);

        einstellungen.setTranslateX(stageWidth/2 - 150);

        refresh.setTranslateX(stageWidth/2);

        simZeit.setTranslateX(stageWidth - simZeit.getWidth() - 30);

        pluginName.setTranslateX(10);

        zugSuche.setTranslateX(80);

        if(Einstellungen.informationenAnzeigen){
            scrollBarWidth.setPrefWidth(stageWidth - Einstellungen.informationenBreite - 15);
            scrollBarWidth.setTranslateY(stageHeight - 90);

            scrollBarHeight.setPrefHeight(stageHeight - 91);
            scrollBarHeight.setTranslateX(stageWidth - Einstellungen.informationenBreite - 30);

            spContent.setMinWidth(stageWidth - Einstellungen.informationenBreite - 115);
            spContent.setMaxWidth(stageWidth - Einstellungen.informationenBreite - 115);

            spPlatform.setMinWidth(stageWidth - Einstellungen.informationenBreite - 115);
            spPlatform.setMaxWidth(stageWidth - Einstellungen.informationenBreite - 115);

            infoFehler.setMinWidth(Einstellungen.informationenBreite);
            infoFehler.setMaxWidth(Einstellungen.informationenBreite);
        } else{
            scrollBarWidth.setPrefWidth(stageWidth - 15);
            scrollBarWidth.setTranslateY(stageHeight - 90);

            scrollBarHeight.setPrefHeight(stageHeight - 91);
            scrollBarHeight.setTranslateX(stageWidth - 30);

            spContent.setMinWidth(stageWidth - 95);
            spContent.setMaxWidth(stageWidth - 95);

            spPlatform.setMinWidth(stageWidth - 95);
            spPlatform.setMaxWidth(stageWidth - 95);

            infoFehler.setMinWidth(0);
            infoFehler.setMaxWidth(0);
        }
    }

    public void refreshGrid(){
        Platform.runLater(() -> {
            gpTime.getChildren().remove(0);
            labelTime.remove(0);

            for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
                for(Bahnsteig bahnsteig : bahnhof.getBahnsteige()){
                    gp.getChildren().remove(bahnsteig.getSpalte().get(0).getLabel());
                    bahnsteig.getSpalte().remove(0);
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
                if(z.getFahrplan() != null && z.getFahrplan(0) != null){
                    for (int i = 0; i < z.getFahrplan().size(); i++) {
                        if(z.getFahrplan(i) != null && z.getFahrplan(i).getFlaggedTrain() != null){
                            Zug eFlag = z.getFahrplan(i).getFlaggedTrain();

                            if(z.getFahrplan()!= null && z.getFahrplan(i) != null && eFlag != null && eFlag.getFahrplan() != null && eFlag.getFahrplan(0) != null){
                                long ankunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung();
                                long abfahrt = eFlag.getFahrplan(0).getAbfahrt() + eFlag.getVerspaetung();

                                if(ankunft <= time && abfahrt >= time){
                                    z.setNeedUpdate(true);
                                }
                            }
                        } else if(z.getFahrplan() != null && z.getFahrplan(i) != null){
                            long ankunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung();
                            long abfahrt = z.getFahrplan(i).getAbfahrt() + z.getVerspaetung();

                            if(ankunft <= time && abfahrt >= time){
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

    private void settings() {
        int stageWidth = 500;
        int stageHeight = 200;
        Stage stage = new Stage();

        einstellungen.setDisable(true);

        Label ai = new Label("Aktualisierungs-Intervall (in s):");
        ai.setFont(Font.font(18));
        ai.setTranslateY(10);
        ai.setTranslateX(10);
        TextField tfai = new TextField(String.valueOf(Einstellungen.update));
        tfai.setTranslateX(300);
        tfai.setTranslateY(10);

        Label v = new Label("Vorschau (in m):");
        v.setFont(Font.font(18));
        v.setTranslateY(40);
        v.setTranslateX(10);
        TextField tfv = new TextField(String.valueOf(Einstellungen.vorschau));
        tfv.setTranslateX(300);
        tfv.setTranslateY(40);
        tfv.setDisable(true);

        Label sb = new Label("Spaltenbreite (in px):");
        sb.setFont(Font.font(18));
        sb.setTranslateY(70);
        sb.setTranslateX(10);
        TextField tfsb = new TextField(String.valueOf(Einstellungen.spaltenbreite));
        tfsb.setTranslateX(300);
        tfsb.setTranslateY(70);

        Label fs = new Label("Standartschriftgroesse:");
        fs.setFont(Font.font(18));
        fs.setTranslateY(100);
        fs.setTranslateX(10);
        TextField tffs = new TextField(String.valueOf(Einstellungen.schriftgroesse));
        tffs.setTranslateX(300);
        tffs.setTranslateY(100);

        Label zib = new Label("Zuginformations-Breite:");
        zib.setFont(Font.font(18));
        zib.setTranslateY(130);
        zib.setTranslateX(10);
        TextField tfzib = new TextField(String.valueOf(Einstellungen.informationenBreite));
        tfzib.setTranslateX(300);
        tfzib.setTranslateY(130);

        Label ezi = new Label("Zuginformationen anzeigen:");
        ezi.setFont(Font.font(18));
        ezi.setTranslateY(160);
        ezi.setTranslateX(10);
        CheckBox cbezi = new CheckBox();
        cbezi.setTranslateX(300);
        cbezi.setTranslateY(160);
        cbezi.setFont(Font.font(18));
        if(Einstellungen.informationenAnzeigen) cbezi.setSelected(true);
        if(! Einstellungen.informationenAnzeigen) cbezi.setSelected(false);

        Label tmb = new Label("Ton bei Mehrfachbelegung:");
        tmb.setFont(Font.font(18));
        tmb.setTranslateY(220);
        tmb.setTranslateX(10);
        CheckBox cbtmb = new CheckBox();
        cbtmb.setTranslateX(300);
        cbtmb.setTranslateY(220);
        cbtmb.setFont(Font.font(18));
        if(Einstellungen.soundAbspielen) cbtmb.setSelected(true);
        if(! Einstellungen.soundAbspielen) cbtmb.setSelected(false);


        Pane gleise = new Pane();
        gleise.setTranslateX(0);
        gleise.setTranslateY(290);

        CheckBox[] cb = new CheckBox[stellwerk.getAnzahlBahnsteige()];
        int tempX = 10;
        int tempY = 0;
        int counter = 0;
        for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
            for (int i = 0; i < bahnhof.getBahnsteige().size(); i++) {
                cb[counter] = new CheckBox(bahnhof.getBahnsteig(i).getName());
                cb[counter].setTranslateX(tempX);
                cb[counter].setTranslateY(tempY);
                cb[counter].setFont(Font.font(18));
                cb[counter].setSelected(bahnhof.getBahnsteig(i).isSichtbar());

                if ((counter + 1) % 3 == 0) {
                    stageHeight += 30;
                    tempY += 30;
                    tempX = 10;
                } else {
                    tempX += 180;
                }

                gleise.getChildren().add(cb[counter]);

                counter++;
            }
        }
        stageHeight += 180;

        Label laaoaw = new Label("Alle Gleise An- oder Abwählen:");
        laaoaw.setFont(Font.font(18));
        laaoaw.setTranslateX(10);
        laaoaw.setTranslateY(250);

        CheckBox cbaaoaw = new CheckBox();
        cbaaoaw.setTranslateX(300);
        cbaaoaw.setTranslateY(250);
        cbaaoaw.setFont(Font.font(18));
        cbaaoaw.setSelected(true);
        cbaaoaw.setOnAction(e -> {
            for (CheckBox aCb : cb) {
                if (cbaaoaw.isSelected()) {
                    aCb.setSelected(true);
                } else {
                    aCb.setSelected(false);
                }
            }
        });

        Button speichern = new Button("Speichern");
        speichern.setFont(Font.font(18));
        speichern.setTranslateX(170);
        speichern.setTranslateY(stageHeight - 50);
        speichern.setOnAction(e -> {
            try{
                Einstellungen.update = Integer.parseInt(tfai.getText());
                Einstellungen.spaltenbreite = Integer.parseInt(tfsb.getText());
                Einstellungen.schriftgroesse = Integer.parseInt(tffs.getText());
                Einstellungen.spaltenbreite = Integer.parseInt(tfsb.getText());
                Einstellungen.informationenBreite = Integer.parseInt(tfzib.getText());
                Einstellungen.informationenAnzeigen = cbezi.isSelected();
                Einstellungen.soundAbspielen = cbtmb.isSelected();

                if(Einstellungen.vorschau != Integer.parseInt(tfv.getText())){
                    Einstellungen.vorschau = Integer.parseInt(tfv.getText());

                    einstellungen.setDisable(false);
                    stage.close();
                    Plugin.einstellungen.schreibeEinstellungen();

                    refresh.fire();
                } else{
                    int counterOne = 0;
                    for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
                        for (Bahnsteig b : bahnhof.getBahnsteige()) {
                            if (cb[counterOne].isSelected()) {
                                b.setSichtbar(true);
                                b.setLabelContainerToWith(Einstellungen.spaltenbreite);
                            } else {
                                b.setSichtbar(false);
                                b.setLabelContainerToWith(0);
                            }
                            counterOne++;
                        }
                    }

                    einstellungen.setDisable(false);
                    stage.close();
                    Plugin.einstellungen.schreibeEinstellungen();

                    updateSettings();
                    updateUi();
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });

        Pane p = new Pane();
        p.setPrefWidth(stageWidth);
        p.setPrefHeight(stageHeight);
        p.getChildren().addAll(ai, tfai, v, tfv, sb, tfsb, fs, tffs, zib, tfzib, ezi, laaoaw, cbaaoaw, cbezi, gleise, speichern, tmb, cbtmb);
        p.setStyle("-fx-background: #303030; -fx-padding: 0;");

        ScrollPane sp = new ScrollPane(p);
        sp.setStyle("-fx-background: #505050; -fx-padding: 0;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene;
        if(stageHeight > primaryStage.getHeight()){
            scene = new Scene(sp, stageWidth, primaryStage.getHeight());
        } else{
            scene = new Scene(sp, stageWidth, stageHeight);
        }

        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> einstellungen.setDisable(false));
    }

    private void updateSettings(){
        for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
            for(Bahnsteig b : bahnhof.getBahnsteige()){
                if(b.isSichtbar()) b.setLabelContainerToWith(Einstellungen.spaltenbreite);
            }
        }

        for (LabelContainer lc : labelTime) {
            lc.getLabel().setFont(Font.font(Einstellungen.schriftgroesse - 5));
            lc.getLabel().setMaxWidth(Einstellungen.spaltenbreite);
            lc.getLabel().setMinWidth(Einstellungen.spaltenbreite);
        }

        for (Bahnhof b : stellwerk.getBahnhoefe()) {
            for(Bahnsteig ba : b.getBahnsteige()){
                ba.getGleisLabel().getLabel().setFont(Font.font(Einstellungen.schriftgroesse - 5));
                ba.getGleisLabel().getLabel().setMaxWidth(Einstellungen.spaltenbreite);
                ba.getGleisLabel().getLabel().setMinWidth(Einstellungen.spaltenbreite);
                for(LabelContainer lc : ba.getSpalte()){
                    lc.getLabel().setFont(Font.font(Einstellungen.schriftgroesse - 5));
                    lc.getLabel().setMaxWidth(Einstellungen.spaltenbreite);
                    lc.getLabel().setMinWidth(Einstellungen.spaltenbreite);
                }
            }
        }

        pluginName.setFont(Font.font(Einstellungen.schriftgroesse));
        simZeit.setFont(Font.font(Einstellungen.schriftgroesse));
        einstellungen.setFont(Font.font(Einstellungen.schriftgroesse));
        refresh.setFont(Font.font(Einstellungen.schriftgroesse));
        firstLabel.setFont(Font.font(Einstellungen.schriftgroesse-5));

        firstLabel.setMaxWidth(Einstellungen.spaltenbreite);
        firstLabel.setMinWidth(Einstellungen.spaltenbreite);
    }

    private void sortiereGleise(){
        Platform.runLater(() -> {
            ArrayList<Bahnsteig> sortierteGleise = new ArrayList<>();
            for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
                sortierteGleise.addAll(bahnhof.getBahnsteige());
            }

            sortierteGleise.sort(Comparator.comparing(Bahnsteig::getOrderId));

            gpPlatform.getChildren().clear();
            gp.getChildren().clear();


            int x = 0;
            int y = 0;
            for(Bahnsteig b : sortierteGleise){
                gpPlatform.addColumn(x,b.getGleisLabel().getLabel());
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
        });
    }

    private void aendereReihenfolge(Bahnsteig bahnsteig){
        Stage stage = new Stage();

        Label l = new Label("Reihenfolge festlegen:");
        l.setStyle("-fx-text-fill: white");
        l.setFont(Font.font(Einstellungen.schriftgroesse));
        l.setTranslateY(25);
        l.setTranslateX(25);

        TextField tf = new TextField(String.valueOf(bahnsteig.getOrderId()+1));
        tf.setFont(Font.font(Einstellungen.schriftgroesse-3));
        tf.setTranslateX(25);
        tf.setTranslateY(60);

        Button b = new Button("Speichern");
        b.setFont(Font.font(Einstellungen.schriftgroesse));
        b.setTranslateX(25);
        b.setTranslateY(120);
        b.setOnAction(e -> {
            bahnsteig.setOrderId(Integer.parseInt(tf.getText())-1);
            stage.close();
            sortiereGleise();
        });

        Pane p = new Pane(l,tf,b);
        p.setStyle("-fx-background-color: #303030");
        p.setMinSize(500,200);
        p.setMaxSize(500, 200);

        Scene scene = new Scene(p, 300,200);

        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);
    }
}