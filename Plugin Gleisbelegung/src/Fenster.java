/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Erzeugen und Updaten der Gui (u.a. die Tabelle und die Informationen)
 */

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.Date;

public class Fenster extends Main {
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

    private ArrayList<ArrayList<LabelContainer>> grid;  //Speichert alle LabelContainer die die Tabell an sich darstellen
    public ArrayList<LabelContainer> labelTime;         //Speichert alle LabelContainer die eine Zeit anzeigen
    private ArrayList<LabelContainer> labelPlatform;    //Speichert alle LabelContainer die Bahnsteigsnamen enthalten

    //Erzeugt alle Listen und erzeugt die LabelContainer mit Informationen, welche vorher von der @VErbindungs-Klasse bereitgestellt werden
    public Fenster() {
        grid = new ArrayList<>();
        labelTime = new ArrayList<>();
        labelPlatform = new ArrayList<>();

        Date dNow = new Date(currentGameTime);
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");

        pluginName = new Label();
        pluginName.setText("Plugin: Gleisbelegung");
        pluginName.setStyle("-fx-text-fill: #fff;");
        pluginName.setFont(Font.font(settingsFontSize));

        simZeit = new Label();
        simZeit.setText("Simzeit: " + ft.format(dNow));
        simZeit.setStyle("-fx-text-fill: white;");
        simZeit.setTranslateX(stageWidth - 360);
        simZeit.setFont(Font.font(settingsFontSize));

        einstellungen = new Button();
        einstellungen.setText("Einstellungen");
        einstellungen.setFont(Font.font(settingsFontSize - 2));
        einstellungen.setTranslateX(stageWidth / 2 - 150);
        einstellungen.setOnAction(e -> settings());

        refresh.setText("Refresh");
        refresh.setFont(Font.font(settingsFontSize - 2));
        refresh.setTranslateX(stageWidth / 2);

        Pane topP = new Pane();
        topP.getChildren().addAll(pluginName, simZeit, einstellungen, refresh);

        scrollBarWidth = new ScrollBar();
        scrollBarWidth.setOrientation(Orientation.HORIZONTAL);
        scrollBarWidth.setTranslateX(0);
        scrollBarWidth.setTranslateY(stageHeight-50);
        scrollBarWidth.setMin(0);
        scrollBarWidth.setMax(1);
        scrollBarWidth.setUnitIncrement(0.03);
        scrollBarWidth.setVisibleAmount(0.3);
        scrollBarWidth.valueProperty().addListener((ov, old_val, new_val) -> spContent.setHvalue(scrollBarWidth.getValue()));
        if(settingsShowInformations) scrollBarWidth.setPrefWidth(stageWidth-215);
        if(!settingsShowInformations) scrollBarWidth.setPrefWidth(stageHeight-15);

        scrollBarHeight = new ScrollBar();
        scrollBarHeight.setOrientation(Orientation.VERTICAL);
        scrollBarHeight.setTranslateY(0);
        scrollBarHeight.setPrefHeight(stageHeight);
        scrollBarHeight.setMin(0);
        scrollBarHeight.setMax(1);
        scrollBarHeight.setUnitIncrement(0.02);
        scrollBarHeight.setVisibleAmount(0.3);
        scrollBarHeight.valueProperty().addListener((ov, old_val, new_val) -> spContent.setVvalue(scrollBarHeight.getValue()));
        if(settingsShowInformations) scrollBarHeight.setTranslateX(stageWidth-214);
        if(!settingsShowInformations) scrollBarHeight.setTranslateX(stageWidth-14);

        gp = new GridPane();
        gp.setHgap(0);
        gp.setVgap(0);
        gp.setPadding(new Insets(0,20,0,0));
        spContent = new ScrollPane();
        spContent.setTranslateX(100);
        spContent.setTranslateY(25);
        spContent.setContent(gp);
        spContent.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spContent.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spContent.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spContent.setMaxHeight(stageHeight-80);
        spContent.setMaxWidth(stageWidth-100);
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
        spTime.setTranslateY(25);
        spTime.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spTime.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spTime.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spTime.setMaxHeight(stageHeight-80);

        gpPlatform = new GridPane();
        gpPlatform.setHgap(0);
        gpPlatform.setVgap(0);
        gpPlatform.setPadding(new Insets(0,20,0,0));
        spPlatform = new ScrollPane();
        spPlatform.setContent(gpPlatform);
        spPlatform.setTranslateX(100);
        spPlatform.setStyle("-fx-background: #303030; -fx-padding: 0;");
        spPlatform.setMaxWidth(stageWidth-100);
        spPlatform.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spPlatform.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Pane content = new Pane(spPlatform, spContent, spTime, scrollBarWidth, scrollBarHeight);

        informations = new Pane();
        informations.setStyle("-fx-background-color: #303030");
        informations.setPrefWidth(250);
        informations.setMinHeight(250);
        informations.setMaxHeight(1000);
        spInformations = new ScrollPane(informations);
        spInformations.setStyle("-fx-background-color: #303030; -fx-padding: 0;");
        spInformations.setPrefHeight(stageHeight/2-20);
        spInformations.setPrefWidth(250);
        spInformations.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spInformations.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        fehlerMeldungen.setStyle("-fx-background-color: #404040;");
        fehlerMeldungen.setTranslateY(stageHeight/2-19);
        fehlerMeldungen.setPrefHeight(stageHeight/2-19);
        fehlerMeldungen.setPrefWidth(250);

        Label lZugSuche = new Label("Zugsuche:");
        lZugSuche.setFont(Font.font(settingsFontSize-2));
        lZugSuche.setStyle("-fx-text-fill: white;");

        zugSuche = new TextField();
        zugSuche.setFont(Font.font(settingsFontSize-5));
        zugSuche.setPrefWidth(110);
        zugSuche.setTranslateX(110);
        zugSuche.setStyle("-fx-text-fill: black;");
        zugSuche.textProperty().addListener((observable, oldVal, newVal) -> {
            try{
                searchTrain(zugSuche.getText());
            } catch(Exception e){
                System.out.println("INFORMATION: Fehler beim autom. Scrollen!");
            }
        });

        pZugSuche = new Pane(lZugSuche, zugSuche);
        pZugSuche.setStyle("-fx-background-color: #303030");

        Pane infoFehler = new Pane(spInformations, fehlerMeldungen, pZugSuche);

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #303030;");
        bp.setTop(topP);
        bp.setCenter(content);
        bp.setRight(infoFehler);

        //prepare Grid
        for (int i = 0; i < settingsVorschau; i++) {
            labelTime.add(i, new LabelContainer(i, -1, labelTime));

            dNow = new Date(currentGameTime + i*1000*60);
            ft = new SimpleDateFormat("HH:mm");
            labelTime.get(i).updateLabel(ft.format(dNow));
            //labelTime.get(i).getLabel().setStyle(labelTime.get(i).getLabel().getStyle() + "-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 5 1 0");

            gpTime.add(labelTime.get(i).getLabel(), 0, i);
        }

        for (int i = 0; i < bahnsteige.length; i++) {
            labelPlatform.add(i, new LabelContainer(i,-2, labelTime));
            labelPlatform.get(i).updateLabel(bahnsteige[i]);
            labelPlatform.get(i).getLabel().setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 5 0");

            gpPlatform.add(labelPlatform.get(i).getLabel(), i, 0);
        }

        for (int i = 0; i < settingsVorschau; i++) { //x
            grid.add(i, new ArrayList<>());

            for (int j = 0; j < bahnsteige.length; j++) { //y
                grid.get(i).add(j, new LabelContainer(i,j, labelTime));
                grid.get(i).get(j).updateLabel("", currentGameTime + i*1000*60);
                gp.add(grid.get(i).get(j).getLabel(), j+1, i+1);
            }
            labelIndexCounter = i;
        }

        s = new Scene(bp, stageWidth, stageHeight);

        primaryStage.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> updateUi());
        primaryStage.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> updateUi());

        boolean temp = settingsShowInformations;
        settingsShowInformations = true;
        updateUi();
        settingsShowInformations = false;
        updateUi();
        settingsShowInformations = temp;
        updateUi();
    }

    //Methode, die aufgerufen wird, wenn der Text in dem Text-Suchfeld für Züge verändert wird
    private void searchTrain(String text){
        ArrayList<Zug> trains = new ArrayList<>();
        for(Zug zTemp : zuege){
            if(!text.equals("") && zTemp.getZugName().contains(text)){
                trains.add(zTemp);
            }
        }

        if(trains.size() > 0){
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

                        Label l = new Label("Gleis: " + z.getFahrplan(i).getGleis() + " " + ft.format(anunft) + " - " + ft.format(abfahrt));
                        l.setFont(Font.font(settingsFontSize-5));
                        l.setTranslateY(heightCounter + 55);
                        l.setPrefWidth(settingsInformationWith - 25);

                        if(z.getGleis().equals(z.getFahrplan(i).getGleis()) && z.getAmGleis()) {
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

                        Label l = new Label("Gleis: " + z.getFahrplan(i).getGleis() + " " + ft.format(anunft) + " - " + ft.format(abfahrt));
                        l.setFont(Font.font(settingsFontSize-5));
                        l.setTranslateY(heightCounter + 55);
                        l.setPrefWidth(215);

                        if(z.getGleis().equals(z.getFahrplan(i).getGleis()) && z.getAmGleis()) {
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
                                scrollPaneTo((double) lc.getBahnsteig() / (double) bahnsteige.length, (double) lc.getLabelIndex() / (double) settingsVorschau, fh);
                            } catch(Exception e){
                                System.out.println("INFORMATION: Fehler beim autom. Scrollen!");
                            }

                            Runnable r = () -> {
                                try {
                                    Thread.sleep(2000);
                                    zugSuche.selectAll();
                                } catch (InterruptedException e) {
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

    //Scrollt den in der vorherigen Methode bestiommten Zug in das Sichtfeld der Tabelle
    private void scrollPaneTo(double x, double y, FahrplanHalt fh) throws Exception{
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

    //Setzt die Scene s, sobal der Konstruktor der Klasse @Fenster zuende ausgeführt wurde
    public void setGridScene(){
        primaryStage.setScene(s);
    }

    //Zeichnet jeweils einen Zug auf die für ihn bestimmten LabelContainer
    private void drawTrain(Zug z){
        z.removeFromGrid();
        try{
            for (int i = 0; i < z.getFahrplan().length; i++) {
                for (int j = 0; j < bahnsteige.length; j++) {
                    if(bahnsteige != null && bahnsteige[j] != null && z.getFahrplan(i) != null && z.getFahrplan(i).getGleis().equals(bahnsteige[j])){
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

                                for (int k = 0; k < grid.size(); k++) {
                                    LabelContainer lc = grid.get(k).get(j);
                                    if(ankunft <= lc.getTime() && abfahrt >= lc.getTime() - 1000*60){
                                        z.getFahrplan(i).addDrawnTo(lc);

                                        if(z.getFahrplan(i).isCrossing()){
                                            Platform.runLater(() -> lc.getLabel().setText(lc.getLabel().getText() + " D"));
                                            System.out.println(z.getZugName() + " Durchfahrt");
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

                                for (int k = 0; k < grid.size(); k++) {
                                    LabelContainer lc = grid.get(k).get(j);
                                    if(ankunft <= lc.getTime() && abfahrt >= lc.getTime() - 1000*60){
                                        z.getFahrplan(i).addDrawnTo(lc);

                                        if(z.getFahrplan(i).isCrossing()){
                                            Platform.runLater(() -> lc.getLabel().setFont(Font.font("", FontPosture.ITALIC, settingsFontSize-5)));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            debugMessage("OK!", true);
        } catch(Exception e){
            e.printStackTrace();
            addMessageToErrorPane(z.getZugName() + ": Fehler bei der Darstellung!");
            debugMessage("Zug " + z.getZugName() + ": Darstellungsfehler", true);
        }
    }

    //Aktualisiert die Aktuelle-Simzeit sekündlich. (Aufgerufen durch @Main.run())
    public void updateSimTime(int updatingIn){
        Date dNow = new Date(currentGameTime);
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");
        Platform.runLater(() -> simZeit.setText("Simzeit: " + ft.format(dNow) + "     Aktualisierung in: " + updatingIn + "s"));

        Date dauer = new Date(System.currentTimeMillis() - spielStart - 1000*60*60);
        Platform.runLater(() -> pluginName.setText("Plugin: Gleisbelegung     Spieldauer: " + ft.format(dauer)));
    }

    //Checkt alle Züge ob sie eine aktualisierung benötigen und führt diese ggf. aus.
    public void update(){
        for(Zug z : zuege){
            if(z.isNeedUpdate()){
                debugMessage("ZUG: " + z.getZugName() + ": Aktualisiere...", false);

                drawTrain(z);
                z.setNeedUpdate(false);
                z.setNewTrain(false);
            }
        }
    }

    //Aktualisiert die Gui bei einer Veränderung der Fenstegröße
    public void updateUi(){
        stageHeight = primaryStage.getHeight();
        stageWidth = primaryStage.getWidth();

        scrollBarWidth.setTranslateY(stageHeight-90);
        if(settingsShowInformations) scrollBarWidth.setPrefWidth(stageWidth-265);
        if(!settingsShowInformations) scrollBarWidth.setPrefWidth(stageWidth-15);

        scrollBarHeight.setPrefHeight(stageHeight-90);
        if(settingsShowInformations) scrollBarHeight.setTranslateX(stageWidth-settingsInformationWith-12);
        if(!settingsShowInformations) scrollBarHeight.setTranslateX(stageWidth-30);

        spContent.setMaxHeight(stageHeight-120);
        if(settingsShowInformations) spContent.setMaxWidth(stageWidth-360);
        if(!settingsShowInformations) spContent.setMaxWidth(stageWidth-130);

        spTime.setMaxHeight(stageHeight-120);
        if(settingsShowInformations) spPlatform.setMaxWidth(stageWidth-360);
        if(!settingsShowInformations) spPlatform.setMaxWidth(stageWidth-130);

        simZeit.setTranslateX(primaryStage.getWidth() - 360);
        einstellungen.setTranslateX(stageWidth / 2 - einstellungen.getWidth() / 2 - 75);
        refresh.setTranslateX(stageWidth / 2 - einstellungen.getWidth() / 2 + 50);

        informations.setMinHeight(stageHeight/2-50);
        spInformations.setPrefHeight(stageHeight/2-50);
        spInformations.setTranslateY(30);
        fehlerMeldungen.setPrefHeight(stageHeight/2-20);
        fehlerMeldungen.setTranslateY(stageHeight/2-20);

        if(!settingsShowInformations){
            informations.setMaxWidth(0);
            informations.setTranslateX(0);
            spInformations.setMaxWidth(0);
            spInformations.setTranslateX(settingsInformationWith);
            fehlerMeldungen.setMaxWidth(0);
            fehlerMeldungen.setTranslateX(settingsInformationWith);
            pZugSuche.setMaxWidth(0);
            pZugSuche.setTranslateX(settingsInformationWith);
        } else{
            informations.setMinWidth(settingsInformationWith-15);
            informations.setPrefWidth(settingsInformationWith-15);
            spInformations.setMinWidth(settingsInformationWith-20);
            spInformations.setPrefWidth(settingsInformationWith-20);
            spInformations.setTranslateX(0);
            fehlerMeldungen.setMinWidth(settingsInformationWith-20);
            fehlerMeldungen.setPrefWidth(settingsInformationWith-20);
            fehlerMeldungen.setTranslateX(0);
            pZugSuche.setMinWidth(settingsInformationWith-20);
            pZugSuche.setPrefWidth(settingsInformationWith-20);
            pZugSuche.setMaxWidth(settingsInformationWith-20);
            pZugSuche.setTranslateX(0);
        }
    }

    //Entfernt die erste Zeile der Tabelle und fügt am Ende der Tabell eine nue Zeile hinzu (minütlich, aufgerufen von @Main.run())
    public void refreshGrid(){
        for (int i = 0; i < bahnsteige.length; i++) {
            Platform.runLater(() -> {
                gp.getChildren().remove(0);
                //grid.remove()
            });
        }
        Platform.runLater(() -> {
            gpTime.getChildren().remove(0);
            labelTime.remove(0);
            grid.remove(0);
        });


        Date dNow = new Date(currentGameTime + settingsVorschau*1000*60 - 2000);
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

        LabelContainer lc = new LabelContainer(labelIndexCounter-1,-1, labelTime);
        lc.updateLabel(ft.format(dNow));
        Platform.runLater(() -> {
            //lc.getLabel().setStyle(lc.getLabel().getStyle() + "-fx-border-width: 0 5 1 0; -fx-border-color: #505050");
            gpTime.add(lc.getLabel(), 0, labelIndexCounter+1);
            labelTime.add(labelTime.size(), lc);
        });

        ArrayList<LabelContainer> labelContainer = new ArrayList<>();
        Platform.runLater(() -> {
            for(int i = 0; i < bahnsteige.length; i++){
                labelContainer.add(new LabelContainer(labelIndexCounter,i, labelTime));

                gp.add(labelContainer.get(i).getLabel(), i+1, labelIndexCounter+1);
                labelContainer.get(i).updateLabel("", currentGameTime + settingsVorschau*1000*60 - 2000);

                if(bahnsteigeSichtbar[i]){
                    labelContainer.get(i).getLabel().setPrefWidth(settingsGridWidth);
                } else{
                    labelContainer.get(i).getLabel().setMaxWidth(0);
                    labelContainer.get(i).getLabel().setPrefWidth(0);
                    labelContainer.get(i).getLabel().setMinWidth(0);
                }
                //labelContainer.get(i).updateLabel();

            }
        });


        Platform.runLater(() -> {
            grid.add(labelContainer);
        });

        labelIndexCounter++;

        updateSomeTrains(currentGameTime + settingsVorschau*1000*60 - 2000);
    }

    //Aktualisiert die Züge, die Innerhalb der gegebenn Zeit eine Abfahrtszeit haben (Aufgerufen durch @Fenster.refreshGrid(), nachdem einen neue Zeile hinzugefügt wurde)
    private void updateSomeTrains(long time){
        for(Zug z : zuege){
            try{
                if(z.getFahrplan() != null){
                    for (int i = 0; i < z.getFahrplan().length; i++) {
                        if(z.getFahrplan(i) != null && z.getFahrplan(i).getFlaggedTrain() != null){
                            Zug eFlag = z.getFahrplan(i).getFlaggedTrain();

                            if(eFlag != null){
                                long ankunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung();
                                long abfahrt = eFlag.getFahrplan(0).getAbfahrt() + eFlag.getVerspaetung();

                                if(ankunft <= time && abfahrt >= time){
                                    z.setNeedUpdate(true);
                                }
                            }
                        } else{
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
                addMessageToErrorPane(z.getZugName() + ": Darstellungsfehler!");
                System.out.println("ZUG: " + z.getZugName() + ": Darstellungsfehler!");
            }
        }
    }

    //Methode welches ein neues Fenster für die Einstellungen öffnet und es befüllt
    private void settings() {
        int stageWidth = 500;
        int stageHeight = 200;
        Stage stage = new Stage();

        einstellungen.setDisable(true);

        Label ai = new Label("Aktualisierungs-Intervall (in s):");
        ai.setFont(Font.font(18));
        ai.setTranslateY(10);
        ai.setTranslateX(10);
        TextField tfai = new TextField(String.valueOf(settingsUpdateInterwall));
        tfai.setTranslateX(300);
        tfai.setTranslateY(10);

        Label v = new Label("Vorschau (in m):");
        v.setFont(Font.font(18));
        v.setTranslateY(40);
        v.setTranslateX(10);
        TextField tfv = new TextField(String.valueOf(settingsVorschau));
        tfv.setTranslateX(300);
        tfv.setTranslateY(40);
        tfv.setDisable(true);

        Label sb = new Label("Spaltenbreite (in px):");
        sb.setFont(Font.font(18));
        sb.setTranslateY(70);
        sb.setTranslateX(10);
        TextField tfsb = new TextField(String.valueOf(settingsGridWidth));
        tfsb.setTranslateX(300);
        tfsb.setTranslateY(70);

        Label fs = new Label("Standartschriftgroesse:");
        fs.setFont(Font.font(18));
        fs.setTranslateY(100);
        fs.setTranslateX(10);
        TextField tffs = new TextField(String.valueOf(settingsFontSize));
        tffs.setTranslateX(300);
        tffs.setTranslateY(100);

        Label zib = new Label("Zuginformations-Breite:");
        zib.setFont(Font.font(18));
        zib.setTranslateY(130);
        zib.setTranslateX(10);
        TextField tfzib = new TextField(String.valueOf(settingsInformationWith));
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
        if(settingsShowInformations) cbezi.setSelected(true);
        if(! settingsShowInformations) cbezi.setSelected(false);

        Label dm = new Label("Schreibe Debug-Informationen:");
        dm.setFont(Font.font(18));
        dm.setTranslateY(190);
        dm.setTranslateX(10);
        CheckBox cbdm = new CheckBox();
        cbdm.setTranslateX(300);
        cbdm.setTranslateY(190);
        cbdm.setFont(Font.font(18));
        if(settingsDebug) cbdm.setSelected(true);
        if(! settingsDebug) cbdm.setSelected(false);

        Label tmb = new Label("Ton bei Mehrfachbelegung:");
        tmb.setFont(Font.font(18));
        tmb.setTranslateY(220);
        tmb.setTranslateX(10);
        CheckBox cbtmb = new CheckBox();
        cbtmb.setTranslateX(300);
        cbtmb.setTranslateY(220);
        cbtmb.setFont(Font.font(18));
        if(settingsPlaySound) cbtmb.setSelected(true);
        if(! settingsPlaySound) cbtmb.setSelected(false);

        Pane gleise = new Pane();
        gleise.setTranslateX(0);
        gleise.setTranslateY(290);

        CheckBox[] cb = new CheckBox[bahnsteige.length];
        int tempX = 10;
        int tempY = 0;
        for (int i = 0; i < bahnsteige.length; i++) {
            cb[i] = new CheckBox(bahnsteige[i]);
            cb[i].setTranslateX(tempX);
            cb[i].setTranslateY(tempY);
            cb[i].setFont(Font.font(18));
            cb[i].setSelected(bahnsteigeSichtbar[i]);

            if ((i + 1) % 3 == 0) {
                stageHeight += 30;
                tempY += 30;
                tempX = 10;
            } else {
                tempX += 180;
            }

            gleise.getChildren().add(cb[i]);
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
            for(int i = 0; i < cb.length; i++){
                if(cbaaoaw.isSelected()){
                    cb[i].setSelected(true);
                } else{
                    cb[i].setSelected(false);
                }
            }
        });

        Button speichern = new Button("Speichern");
        speichern.setFont(Font.font(18));
        speichern.setTranslateX(170);
        speichern.setTranslateY(stageHeight - 50);
        speichern.setOnAction(e -> {
            settingsUpdateInterwall = Integer.parseInt(tfai.getText());
            settingsVorschau = Integer.parseInt(tfv.getText());
            settingsGridWidth = Integer.parseInt(tfsb.getText());
            settingsFontSize = Integer.parseInt(tffs.getText());
            settingsInformationWith = Integer.parseInt(tfzib.getText());
            settingsShowInformations = cbezi.isSelected();
            settingsPlaySound = cbtmb.isSelected();
            settingsDebug = cbdm.isSelected();

            for (int i = 0; i < cb.length; i++) {
                if (cb[i].isSelected()) {
                    bahnsteigeSichtbar[i] = true;
                    showPlatform(i, true);
                } else {
                    bahnsteigeSichtbar[i] = false;
                    showPlatform(i, false);
                }
            }

            einstellungen.setDisable(false);
            stage.close();
            writeSettings();

            updateSettings();
            updateUi();
        });

        Pane p = new Pane();
        p.setPrefWidth(stageWidth);
        p.setPrefHeight(stageHeight);
        p.getChildren().addAll(ai, tfai, v, tfv, sb, tfsb, fs, tffs, zib, tfzib, ezi, laaoaw, cbaaoaw, cbezi, gleise, speichern, tmb, cbtmb, dm, cbdm);
        p.setStyle("-fx-background: #303030; -fx-padding: 0;");

        ScrollPane sp = new ScrollPane(p);
        sp.setStyle("-fx-background: #303030; -fx-padding: 0;");
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

    //Unter dem Einstellungsfenster führt ein Klick auf "Speicern" zu dieser Methode. Hier werden alle EInstellungen in einer Textdatei gespeichert
    private void writeSettings(){
        try {
            File f = File.createTempFile("temp", ".txt");
            String filePath = f.getAbsolutePath().replace(f.getName(), "");
            f.delete();

            PrintWriter pw = new PrintWriter(new FileWriter(filePath + "Plugin_Gleisbelegung_Settings.txt"));

            pw.println(settingsUpdateInterwall);
            pw.println(settingsVorschau);
            pw.println(settingsGridWidth);
            pw.println(settingsFontSize);
            pw.println(settingsShowInformations);
            pw.println(settingsPlaySound);
            pw.println(settingsDebug);
            pw.println(settingsInformationWith);

            pw.close();
        } catch (Exception e) {
            e.printStackTrace(logFile);
            e.printStackTrace();
        }
    }

    //Hier werden die geänderten Einstellungen auf die Gui angewendet
    private void updateSettings(){
        for (int i = 0; i < grid.size(); i++) {
            for (int j = 0; j < grid.get(i).size(); j++) {
                LabelContainer lc = grid.get(i).get(j);
                lc.getLabel().setFont(Font.font(settingsFontSize-5));
                lc.getLabel().setMaxWidth(settingsGridWidth);
                lc.getLabel().setMinWidth(settingsGridWidth);
            }
        }

        for (int i = 0; i < labelTime.size(); i++) {
            LabelContainer lc = labelTime.get(i);
            lc.getLabel().setFont(Font.font(settingsFontSize-5));
        }

        for (int i = 0; i < labelPlatform.size(); i++) {
            LabelContainer lc = labelPlatform.get(i);
            lc.getLabel().setFont(Font.font(settingsFontSize-5));
            lc.getLabel().setMaxWidth(settingsGridWidth);
            lc.getLabel().setMinWidth(settingsGridWidth);
        }

        pluginName.setFont(Font.font(settingsFontSize));
        simZeit.setFont(Font.font(settingsFontSize));
        einstellungen.setFont(Font.font(settingsFontSize));
    }

    //Ist ein Bahnsteig sichtbar oder nicht. Hier wird das esetzt
    private void showPlatform(int index, boolean visible){
        if(visible){
            for (int i = 0; i < grid.size(); i++) {
                Label l = grid.get(i).get(index).getLabel();
                l.setMinWidth(settingsGridWidth);
                l.setMaxWidth(settingsGridWidth);
            }
            labelPlatform.get(index).getLabel().setMaxWidth(settingsGridWidth);
            labelPlatform.get(index).getLabel().setMinWidth(settingsGridWidth);
        } else{
            for (int i = 0; i < grid.size(); i++) {
                final int temp = i;
                Platform.runLater(() -> {
                    Label l = grid.get(temp).get(index).getLabel();
                    l.setMaxWidth(0);
                    l.setPrefWidth(0);
                    l.setMinWidth(0);
                });
            }

            Platform.runLater(() -> {
                labelPlatform.get(index).getLabel().setMaxWidth(0);
                labelPlatform.get(index).getLabel().setMinWidth(0);
                labelPlatform.get(index).getLabel().setPrefWidth(0);
            });
        }
    }
}