package com.gleisbelegung;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Erzeugen und Updaten der Gui (u.a. die Tabelle und die Informationen)
 */

import com.gleisbelegung.lib.SignalBox;
import com.gleisbelegung.lib.data.Platform;
import com.gleisbelegung.lib.data.Station;
import com.gleisbelegung.lib.data.Train;
import com.gleisbelegung.lib.data.TrainStop;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Fenster {

    static Pane informations;
    public Gleisbelegung gleisbelegung;
    public Stellwerksuebersicht stellwerksuebersicht;
    private Label pluginName;
    //Textfeld welches den Namen des Plugin in der Linken oberen Ecke speichert
    private Label simZeit;
    //Textbox um den gesuchrten Zugnamen einzugeben
    //Textfeld welches die Simzeit in der rechte oberen Ecke speichert
    private Button einstellungen;
    //Button um das Fenster mit den Settings zu öffnen
    private Scene s;
    //Hält die Scene mit der Tabelle gespeichert,
    private TextField zugSuche;
    //Panel für alle Zuginformation                             (Zugnummer, Verspätung etc.)
    private PrintWriter logFile;
    private Pane pZugSuche;
    //Panel mit der Zugsuche
    private double stageWidth = 1000;
    //Standartmäßige Fenster-Breite                             (Wir bei Veränderung der Breite aktualisiert)
    private double stageHeight = 500;
    //Standartmäßige Fenster-Höhe                               (Wir bei Veränderung der Höhe aktualisiert)
    private Stage primaryStage;
    private Button refresh;
    private Button sichtwechsel;
    //Scroll-Feld für das in der @Main-Klasse deklariert Feld informations
    private Pane infoFehler;
    private ScrollPane spInformations;
    private SignalBox signalBox;

    Fenster(SignalBox signalBox, Stage primaryStage, Button refresh) {
        this.signalBox = signalBox;
        this.primaryStage = primaryStage;
        this.refresh = refresh;

        gleisbelegung = new Gleisbelegung(signalBox);
        stellwerksuebersicht = new Stellwerksuebersicht(signalBox);

        stageHeight = primaryStage.getHeight();
        stageWidth = primaryStage.getWidth();

        Date dNow = new Date(signalBox.getPlayingTime());
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");

        pluginName = new Label();
        pluginName.setText("Plugin: Gleisbelegung");
        pluginName.setStyle("-fx-text-fill: #fff;");
        pluginName.setFont(Font.font(Settings.fontSize));

        simZeit = new Label();
        simZeit.setText("Simzeit: " + ft.format(dNow));
        simZeit.setStyle("-fx-text-fill: white;");
        simZeit.setFont(Font.font(Settings.fontSize));

        einstellungen = new Button();
        einstellungen.setText("Settings");
        einstellungen.setFont(Font.font(Settings.fontSize - 2));
        einstellungen.setOnAction(e -> {
            try {
                settings();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        sichtwechsel = new Button("Stellwerksübersicht");
        sichtwechsel.setFont(Font.font(Settings.fontSize - 2));

        refresh.setText("Neustart");
        refresh.setFont(Font.font(Settings.fontSize - 2));

        Pane topP = new Pane();
        javafx.application.Platform.runLater(() -> topP.getChildren()
                .addAll(pluginName, simZeit, einstellungen, refresh,
                        sichtwechsel));

        Label lZugSuche = new Label("Zugsuche:");
        lZugSuche.setFont(Font.font(Settings.fontSize - 2));
        lZugSuche.setStyle("-fx-text-fill: white;");

        zugSuche = new TextField();
        zugSuche.setFont(Font.font(Settings.fontSize - 5));
        zugSuche.setStyle("-fx-text-fill: black;");
        zugSuche.textProperty().addListener((observable, oldVal, newVal) -> {
            try {
                searchTrain(zugSuche.getText());
            } catch (Exception e) {
                System.out.println("INFORMATION: Fehler beim autom. Scrollen!");
            }
        });

        pZugSuche = new Pane(lZugSuche, zugSuche);
        pZugSuche.setStyle("-fx-background-color: #404040;");

        informations = new Pane();
        informations.setStyle("-fx-background-color: #404040;");
        informations.setMinWidth(Settings.trainInformationWidth);
        informations.setMaxWidth(Settings.trainInformationWidth);
        spInformations = new ScrollPane(informations);
        spInformations
                .setStyle("-fx-background-color: #404040; -fx-padding: 0;");
        spInformations.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        spInformations.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        infoFehler = new Pane(spInformations, pZugSuche);
        infoFehler.setStyle("-fx-background-color: #404040;");

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #303030;");
        bp.setTop(topP);
        bp.setCenter(gleisbelegung.getContent());
        bp.setRight(infoFehler);

        Settings.view = 1;
        sichtwechsel.setOnAction(e -> {
            if (Settings.view == 1) {
                Settings.view = 2;
                sichtwechsel.setText("Gleisbelegung");
                bp.setCenter(stellwerksuebersicht.getContent());
            } else if (Settings.view == 2) {
                Settings.view = 1;
                sichtwechsel.setText("Stellwerksübersicht");
                bp.setCenter(gleisbelegung.getContent());
            }
        });

        s = new Scene(bp, stageWidth, stageHeight);

        javafx.application.Platform.runLater(() -> {
            primaryStage.setScene(s);
        });

        primaryStage.widthProperty().addListener(
                (observableValue, oldSceneWidth, newSceneWidth) -> {
                    try {
                        updateUi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        primaryStage.heightProperty().addListener(
                (observableValue, oldSceneHeight, newSceneHeight) -> {
                    try {
                        updateUi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        updateUi();
        updateSettings();

        this.refresh.setDisable(false);

        Runnable r = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            javafx.application.Platform.runLater(() -> {
                if (primaryStage.isMaximized()) {
                    primaryStage.setMaximized(false);
                    primaryStage.setMaximized(true);
                }
            });
        };
        new Thread(r).start();
    }

    private void searchTrain(String text) {
        ArrayList<Train> trains = new ArrayList<>();
        for (Train zTemp : signalBox.getTrains()) {
            if (!text.equals("") && zTemp.getTrainName().contains(text)) {
                trains.add(zTemp);
            }
        }

        if (trains.size() > 0) {
            int heightCounter = 0;
            informations.getChildren().clear();
            for (Train z : trains) {
                Label trainName =
                        new Label(z.getTrainName() + z.getVerspaetungToString());
                trainName.setStyle("-fx-text-fill: whiteM");
                trainName.setFont(Font.font(Settings.fontSize - 2));
                trainName.setTranslateY(heightCounter);
                if (z.getSchedule() != null) {
                    for (TrainStop fh : z.getSchedule()) {
                        if (fh.getFlaggedTrain() != null) {
                            trainName.setText(trainName.getText() + " => " + fh
                                    .getFlaggedTrain().getTrainName() + fh
                                    .getFlaggedTrain()
                                    .getVerspaetungToString());
                            break;
                        }
                    }
                }

                Label vonBis = new Label(z.getFrom() + " - " + z.getTo());
                vonBis.setStyle("-fx-text-fill: white;");
                vonBis.setFont(Font.font(Settings.fontSize - 5));
                vonBis.setTranslateY(heightCounter + 25);

                informations.getChildren().addAll(trainName, vonBis);

                for (int i = 0; i < z.getSchedule().size(); i++) {
                    if (z.getFahrplan(i).getFlaggedTrain() != null) {
                        Train flagged = z.getFahrplan(i).getFlaggedTrain();

                        long lAnkunft = z.getFahrplan(i).getArrivalTime()
                                + z.getVerspaetungInMinuten() * 1000 * 60;
                        long lAbfahrt = flagged.getFahrplan(0).getDepartureTime()
                                + flagged.getVerspaetungInMinuten() * 1000 * 60;
                        if (flagged.getVerspaetungInMinuten() > 3
                                && (lAbfahrt - lAnkunft) / 1000 / 60 > 3) {
                            lAbfahrt = lAnkunft + 4 * 1000 * 60;
                        }

                        Date anunft = new Date(lAnkunft);
                        Date abfahrt = new Date(lAbfahrt);
                        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

                        Label l = new Label(
                                "Platform: " + z.getFahrplan(i).getBahnsteig()
                                        .getName() + " " + ft.format(anunft)
                                        + " - " + ft.format(abfahrt));
                        l.setFont(Font.font(Settings.fontSize - 5));
                        l.setTranslateY(heightCounter + 55);
                        l.setPrefWidth(215);

                        if (z.getBahnsteig().getName()
                                .equals(z.getFahrplan(i).getBahnsteig()
                                        .getName()) && z.getAtPlatform()) {
                            l.setStyle(
                                    "-fx-text-fill: white; -fx-background-color: green;");
                        } else {
                            l.setStyle("-fx-text-fill: white;");
                        }

                        informations.getChildren().add(l);
                    } else {
                        long lAnkunft = z.getFahrplan(i).getArrivalTime()
                                + z.getVerspaetungInMinuten() * 1000 * 60;
                        long lAbfahrt = z.getFahrplan(i).getDepartureTime()
                                + z.getVerspaetungInMinuten() * 1000 * 60;
                        if (z.getVerspaetungInMinuten() > 3
                                && (lAbfahrt - lAnkunft) / 1000 / 60 > 3) {
                            lAbfahrt = lAnkunft + 4 * 1000 * 60;
                        }
                        if (z.getFahrplan(i).getVorgaenger() != null)
                            lAnkunft =
                                    z.getFahrplan(i).getVorgaenger().getArrivalTime()
                                            + z.getFahrplan(i).getVorgaenger()
                                            .getZug().getVerspaetungInMinuten()
                                            * 1000 * 60;
                        Date anunft = new Date(lAnkunft);
                        Date abfahrt = new Date(lAbfahrt);
                        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

                        Label l = new Label(
                                "Platform: " + z.getFahrplan(i).getBahnsteig()
                                        .getName() + " " + ft.format(anunft)
                                        + " - " + ft.format(abfahrt));
                        l.setFont(Font.font(Settings.fontSize - 5));
                        l.setTranslateY(heightCounter + 55);
                        l.setPrefWidth(215);

                        if (z.getBahnsteig().getName()
                                .equals(z.getFahrplan(i).getBahnsteig()
                                        .getName()) && z.getAtPlatform()) {
                            l.setStyle(
                                    "-fx-text-fill: white; -fx-background-color: green;");
                        } else {
                            l.setStyle("-fx-text-fill: white;");
                        }

                        informations.getChildren().add(l);
                    }

                    heightCounter += 20;
                }

                heightCounter += 75;
            }

            if (heightCounter > (stageHeight / 2 - 70)) {
                //informations.setMinHeight(heightCounter);
                informations.setPrefHeight(heightCounter);
            } else {
                informations.setMinHeight(stageHeight / 2 - 70);
            }
        }

        javafx.application.Platform.runLater(() -> {
            if (trains.size() == 1) {
                for (TrainStop fh : trains.get(0).getSchedule()) {
                    if (fh != null && fh.getDrawnTo().size() > 0) {
                        LabelContainer lc = fh.getDrawnTo(0);
                        if (lc != null) {
                            try {
                                gleisbelegung.scrollPaneTo(
                                        (double) lc.getPlatform().getOrderId()
                                                / (double) signalBox
                                                .getAnzahlBahnsteige(),
                                        (double) lc.getLabelIndex()
                                                / (double) gleisbelegung
                                                .getTimeTable().getRows()
                                                .size(), fh);
                            } catch (Exception e) {
                                System.out.println(
                                        "INFORMATION: Fehler beim autom. Scrollen!");
                            }

                            Runnable r = () -> {
                                try {
                                    Thread.sleep(2000);
                                    zugSuche.selectAll();
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                }
                            };

                            try {
                                new Thread(r).start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    public void updateSimTime(int updatingIn) {
        Date dNow = new Date(signalBox.getPlayingTime());
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");

        javafx.application.Platform.runLater(() -> simZeit.setText(
                "Simzeit: " + ft.format(dNow) + "     Aktualisierung in: "
                        + updatingIn + "s"));
        Date dauer = new Date(
                System.currentTimeMillis() - signalBox.getStartingTime()
                        - 1000 * 60 * 60);

        javafx.application.Platform.runLater(() -> pluginName.setText(
                "Plugin: Gleisbelegung     Spieldauer: " + ft.format(dauer)));
    }

    public void update() {
        gleisbelegung.update();
        stellwerksuebersicht.update();
    }

    public SignalBox getSignalBox() {
        return this.signalBox;
    }

    private void updateUi() {
        Settings.fullscreen = primaryStage.isMaximized();
        stageHeight = primaryStage.getHeight();
        stageWidth = primaryStage.getWidth();

        einstellungen.setTranslateX(stageWidth / 2 - 200);

        sichtwechsel.setTranslateX(stageWidth / 2 + 60);

        refresh.setTranslateX(stageWidth / 2 - 50);

        simZeit.setTranslateX(stageWidth - simZeit.getWidth() - 30);

        pluginName.setTranslateX(10);

        zugSuche.setTranslateX(80);

        if (Settings.showTrainInformations) {
            infoFehler.setMinWidth(Settings.trainInformationWidth);
            infoFehler.setMaxWidth(Settings.trainInformationWidth);
        } else {
            infoFehler.setMinWidth(0);
            infoFehler.setMaxWidth(0);
        }

        gleisbelegung.updateUi(stageWidth, stageHeight);
        stellwerksuebersicht.updateUi(stageWidth, stageHeight);
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
        TextField tfai = new TextField(String.valueOf(Settings.updateIntervall));
        tfai.setTranslateX(300);
        tfai.setTranslateY(10);

        Label sb = new Label("Spaltenbreite (in px):");
        sb.setFont(Font.font(18));
        sb.setTranslateY(70);
        sb.setTranslateX(10);
        TextField tfsb =
                new TextField(String.valueOf(Settings.columnWidth));
        tfsb.setTranslateX(300);
        tfsb.setTranslateY(70);

        Label fs = new Label("Standartschriftgroesse:");
        fs.setFont(Font.font(18));
        fs.setTranslateY(100);
        fs.setTranslateX(10);
        TextField tffs =
                new TextField(String.valueOf(Settings.fontSize));
        tffs.setTranslateX(300);
        tffs.setTranslateY(100);

        Label zib = new Label("Zuginformations-Breite:");
        zib.setFont(Font.font(18));
        zib.setTranslateY(130);
        zib.setTranslateX(10);
        TextField tfzib = new TextField(
                String.valueOf(Settings.trainInformationWidth));
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
        if (Settings.showTrainInformations)
            cbezi.setSelected(true);
        if (!Settings.showTrainInformations)
            cbezi.setSelected(false);

        Pane gleise = new Pane();
        gleise.setTranslateX(0);
        gleise.setTranslateY(290);

        stageHeight = 290;

        CheckBox[] cbBahnhof = new CheckBox[signalBox.getStations().size()];
        CheckBox[] cbGleis = new CheckBox[signalBox.getAnzahlBahnsteige()];
        int tempX = 190;
        int tempY = -30;
        int counterBhf = 0;
        int counterGleis = 0;
        for (Station station : signalBox.getStations()) {
            // zuerst Station in die linke Spalte schreiben
            tempY += 30;
            CheckBox cb = new CheckBox(station.getName());
            if ("".equals(cb.getText())) {
                cb.setText(signalBox.getSignalBoxName());
            }
            if (!station.getNameByUser().equals("")) {
                if (!station.getName().equals(""))
                    cb.setText(station.getNameByUser() + " (" + station
                            .getName() + ")");
                else
                    cb.setText(station.getNameByUser());
            }
            cbBahnhof[counterBhf] = cb;
            cbBahnhof[counterBhf].setTranslateX(10);
            cbBahnhof[counterBhf].setTranslateY(tempY);
            cbBahnhof[counterBhf].setFont(Font.font(18));
            cbBahnhof[counterBhf].setSelected(station.isSichtbar());
            cbBahnhof[counterBhf]
                    .setOnAction(new javafx.event.EventHandler<ActionEvent>() {

                        @Override public void handle(ActionEvent event) {
                            for (Platform b : station.getPlatforms())
                                b.setVisible(cb.isSelected());
                        }
                    });
            gleise.getChildren().add(cbBahnhof[counterBhf]);
            counterBhf++;
            //dann alle Gleise in die mittlere und rechte Spalte schreiben
            for (int i = 0; i < station.getAnzahlBahnsteige(); i++) {
                cbGleis[counterGleis] =
                        new CheckBox(station.getBahnsteig(i).getName());
                cbGleis[counterGleis].setTranslateX(tempX);
                cbGleis[counterGleis].setTranslateY(tempY);
                cbGleis[counterGleis].setFont(Font.font(18));
                cbGleis[counterGleis].selectedProperty().bindBidirectional(
                        station.getBahnsteig(i).getSichtbarProperty());

                if (i % 2 != 0 && i < station.getPlatforms().size() - 1) {
                    tempY += 30;
                    tempX = 190;
                } else {
                    tempX += 180;
                }

                gleise.getChildren().add(cbGleis[counterGleis]);

                counterGleis++;

            }
            tempX = 190;
        }

        stageHeight += tempY + 100; //+100 der Schönheit wegen

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
            for (CheckBox aCb : cbBahnhof) {
                aCb.setSelected(cbaaoaw.isSelected());
            }
            for (CheckBox aCb : cbGleis) {
                aCb.setSelected(cbaaoaw.isSelected());
            }
        });

        Button speichern = new Button("Speichern");
        speichern.setFont(Font.font(18));
        speichern.setTranslateX(170);
        speichern.setTranslateY(stageHeight - 50);
        speichern.setOnAction(e -> {
            try {
                Settings.updateIntervall = Integer.parseInt(tfai.getText());
                Settings.columnWidth = Integer.parseInt(tfsb.getText());
                Settings.fontSize = Integer.parseInt(tffs.getText());
                Settings.trainInformationWidth =
                        Integer.parseInt(tfzib.getText());
                Settings.showTrainInformations = cbezi.isSelected();

                int counterOne = 0;
                for (Station station : signalBox.getStations()) {
                    for (Platform b : station.getPlatforms()) {
                        if (cbGleis[counterOne].isSelected()) {
                            b.setVisible(true);
                            b.setLabelContainerToWith(
                                    Settings.columnWidth);
                        } else {
                            b.setVisible(false);
                            b.setLabelContainerToWith(0);
                        }
                        counterOne++;
                    }
                }

                einstellungen.setDisable(false);
                stage.close();

                updateSettings();
                updateUi();
                gleisbelegung.erzeugeBahnsteigLabel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Pane p = new Pane();
        p.setPrefWidth(stageWidth);
        p.setPrefHeight(stageHeight);
        p.getChildren()
                .addAll(ai, tfai, sb, tfsb, fs, tffs, zib, tfzib, ezi, laaoaw,
                        cbaaoaw, cbezi, gleise, speichern);
        p.setStyle("-fx-background: #303030; -fx-padding: 0;");

        ScrollPane sp = new ScrollPane(p);
        sp.setStyle("-fx-background: #505050; -fx-padding: 0;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene;
        if (stageHeight > primaryStage.getHeight()
                - 100) { //Fenster kann nun nicht mehr unter der Taskleiste flimmern
            scene = new Scene(sp, stageWidth, primaryStage.getHeight()
                    - 100); //Fenster kann nun nicht mehr unter der Taskleiste flimmern
        } else {
            scene = new Scene(sp, stageWidth, stageHeight);
        }

        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> einstellungen.setDisable(false));
    }

    private void updateSettings() {
        for (Station station : signalBox.getStations()) {
            for (Platform b : station.getPlatforms()) {
                if (b.getVisible())
                    b.setLabelContainerToWith(Settings.columnWidth);
            }
        }

        /*for (LabelContainer lc : gleisbelegung.getLabelTime()) {
            lc.getLabel().setFont(Font.font(Settings.fontSize - 5));
            lc.getLabel().setMaxWidth(Settings.columnWidth);
            lc.getLabel().setMinWidth(Settings.columnWidth);
        }*/

        for (Station b : signalBox.getStations()) {
            for (Platform ba : b.getPlatforms()) {
                ba.getGleisLabel().getLabel()
                        .setFont(Font.font(Settings.fontSize - 5));
                if (ba.getVisible()) {
                    ba.getGleisLabel().getLabel()
                            .setMaxWidth(Settings.columnWidth);
                    ba.getGleisLabel().getLabel()
                            .setMinWidth(Settings.columnWidth);
                } else {
                    ba.getGleisLabel().getLabel().setMaxWidth(0);
                    ba.getGleisLabel().getLabel().setMinWidth(0);
                }

                /*for(LabelContainer lc : ba.getSpalte()){
                    lc.getLabel().setFont(Font.font(Settings.fontSize - 5));
                    if(ba.getVisible()){
                        lc.getLabel().setMaxWidth(Settings.columnWidth);
                        lc.getLabel().setMinWidth(Settings.columnWidth);
                    } else{
                        lc.getLabel().setMaxWidth(0);
                        lc.getLabel().setMinWidth(0);
                    }
                }*/
            }
        }

        pluginName.setFont(Font.font(Settings.fontSize));
        simZeit.setFont(Font.font(Settings.fontSize));
        einstellungen.setFont(Font.font(Settings.fontSize - 2));
        refresh.setFont(Font.font(Settings.fontSize - 2));
        sichtwechsel.setFont(Font.font(Settings.fontSize - 2));
        gleisbelegung.getFirstLabel()
                .setFont(Font.font(Settings.fontSize - 5));

        gleisbelegung.getFirstLabel().setMaxWidth(Settings.columnWidth);
        gleisbelegung.getFirstLabel().setMinWidth(Settings.columnWidth);
    }
}
