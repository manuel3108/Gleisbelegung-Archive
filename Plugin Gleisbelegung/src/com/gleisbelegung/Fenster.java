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

public class Fenster{
    private Label pluginName;                           //Textfeld welches den Namen des Plugin in der Linken oberen Ecke speichert
    private Label simZeit;                              //Textfeld welches die Simzeit in der rechte oberen Ecke speichert
    private Button einstellungen;                       //Button um das Fenster mit den Einstellungen zu öffnen
    private Scene s;                                    //Hält die Scene mit der Tabelle gespeichert,
    private TextField zugSuche;                         //Textbox um den gesuchrten Zugnamen einzugeben

    static Pane informations;                               //Panel für alle Zuginformation                             (Zugnummer, Verspätung etc.)
    private PrintWriter logFile;                             //
    private Pane pZugSuche;                                  //Panel mit der Zugsuche
    private double stageWidth = 1000;                        //Standartmäßige Fenster-Breite                             (Wir bei Veränderung der Breite aktualisiert)
    private double stageHeight = 500;                        //Standartmäßige Fenster-Höhe                               (Wir bei Veränderung der Höhe aktualisiert)
    private Stage primaryStage;
    private Button refresh;
    private Button sichtwechsel;
    private Pane infoFehler;
    private ScrollPane spInformations;                  //Scroll-Feld für das in der @Main-Klasse deklariert Feld informations

    public Gleisbelegung gleisbelegung;
    public Stellwerksuebersicht stellwerksuebersicht;
    private Stellwerk stellwerk;

    Fenster(Stellwerk stellwerk, Stage primaryStage, Button refresh) {
        this.stellwerk = stellwerk;
        this.primaryStage = primaryStage;
        this.refresh = refresh;

        gleisbelegung = new Gleisbelegung(stellwerk);
        stellwerksuebersicht = new Stellwerksuebersicht(stellwerk);

        stageHeight = primaryStage.getHeight();
        stageWidth = primaryStage.getWidth();

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

        sichtwechsel = new Button("Stellwerksübersicht");
        sichtwechsel.setFont(Font.font(Einstellungen.schriftgroesse - 2));

        refresh.setText("Neustart");
        refresh.setFont(Font.font(Einstellungen.schriftgroesse - 2));

        Pane topP = new Pane();
        Platform.runLater(() -> topP.getChildren().addAll(pluginName, simZeit, einstellungen, refresh, sichtwechsel));

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
        pZugSuche.setStyle("-fx-background-color: #404040;");

        informations = new Pane();
        informations.setStyle("-fx-background-color: #404040;");
        informations.setMinWidth(Einstellungen.informationenBreite);
        informations.setMaxWidth(Einstellungen.informationenBreite);
        spInformations = new ScrollPane(informations);
        spInformations.setStyle("-fx-background-color: #404040; -fx-padding: 0;");
        spInformations.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        spInformations.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        infoFehler = new Pane(spInformations, pZugSuche);
        infoFehler.setStyle("-fx-background-color: #404040;");

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #303030;");
        bp.setTop(topP);
        bp.setCenter(gleisbelegung.getContent());
        bp.setRight(infoFehler);

        Einstellungen.sicht = 1;
        sichtwechsel.setOnAction(e -> {
            if(Einstellungen.sicht == 1){
                Einstellungen.sicht = 2;
                sichtwechsel.setText("Gleisbelegung");
                bp.setCenter(stellwerksuebersicht.getContent());
            } else if(Einstellungen.sicht == 2){
                Einstellungen.sicht = 1;
                sichtwechsel.setText("Stellwerksübersicht");
                bp.setCenter(gleisbelegung.getContent());
            }
        });

        s = new Scene(bp, stageWidth, stageHeight);

        Platform.runLater(() -> {
            primaryStage.setScene(s);
        });

        primaryStage.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> { try{ updateUi(); }catch(Exception e) { e.printStackTrace(); } });
        primaryStage.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> { try{ updateUi(); }catch(Exception e) { e.printStackTrace(); } });

        updateUi();
        updateSettings();

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
                trainName.setStyle("-fx-text-fill: whiteM");
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
                vonBis.setStyle("-fx-text-fill: white;");
                vonBis.setFont(Font.font(Einstellungen.schriftgroesse-5));
                vonBis.setTranslateY(heightCounter + 25);

                informations.getChildren().addAll(trainName, vonBis);

                for(int i = 0; i < z.getFahrplan().size(); i++){
                    if(z.getFahrplan(i).getFlaggedTrain() != null){
                        Zug flagged = z.getFahrplan(i).getFlaggedTrain();

                        long lAnkunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetungInMinuten()*1000*60;
                        long lAbfahrt = flagged.getFahrplan(0).getAbfahrt() + flagged.getVerspaetungInMinuten()*1000*60;
                        if(flagged.getVerspaetungInMinuten() > 3 && (lAbfahrt-lAnkunft)/1000/60 > 3){
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
                            l.setStyle("-fx-text-fill: white; -fx-background-color: green;");
                        } else{
                            l.setStyle("-fx-text-fill: white;");
                        }

                        informations.getChildren().add(l);
                    } else{
                        long lAnkunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetungInMinuten()*1000*60;
                        long lAbfahrt = z.getFahrplan(i).getAbfahrt() + z.getVerspaetungInMinuten()*1000*60;
                        if(z.getVerspaetungInMinuten() > 3 && (lAbfahrt-lAnkunft)/1000/60 > 3){
                            lAbfahrt = lAnkunft + 4*1000*60;
                        }
                        if (z.getFahrplan(i).getVorgaenger() != null)
                            lAnkunft = z.getFahrplan(i).getVorgaenger().getAnkuft() + z.getFahrplan(i).getVorgaenger().getZug().getVerspaetungInMinuten() * 1000 * 60;
                        Date anunft = new Date(lAnkunft);
                        Date abfahrt = new Date(lAbfahrt);
                        SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

                        Label l = new Label("Bahnsteig: " + z.getFahrplan(i).getBahnsteig().getName() + " " + ft.format(anunft) + " - " + ft.format(abfahrt));
                        l.setFont(Font.font(Einstellungen.schriftgroesse-5));
                        l.setTranslateY(heightCounter + 55);
                        l.setPrefWidth(215);

                        if(z.getBahnsteig().getName().equals(z.getFahrplan(i).getBahnsteig().getName()) && z.getAmGleis()) {
                            l.setStyle("-fx-text-fill: white; -fx-background-color: green;");
                        } else{
                            l.setStyle("-fx-text-fill: white;");
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
                    if (fh != null && fh.getDrawnTo().size() > 0) {
                        LabelContainer lc = fh.getDrawnTo(0);
                        if(lc != null){
                            try{
                                gleisbelegung.scrollPaneTo((double) lc.getBahnsteig().getOrderId() / (double) stellwerk.getAnzahlBahnsteige(), (double) lc.getLabelIndex() / (double) Einstellungen.vorschau, fh);
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

    public void updateSimTime(int updatingIn){
        Date dNow = new Date(stellwerk.getSpielzeit());
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");

        Platform.runLater(() -> simZeit.setText("Simzeit: " + ft.format(dNow) + "     Aktualisierung in: " + updatingIn + "s"));
        Date dauer = new Date(System.currentTimeMillis() - stellwerk.getStartzeit() - 1000*60*60);

        Platform.runLater(() -> pluginName.setText("Plugin: Gleisbelegung     Spieldauer: " + ft.format(dauer)));
    }

    public void update(){
        gleisbelegung.update();
        stellwerksuebersicht.update();
    }

    public Stellwerk getStellwerk() {
      return this.stellwerk;
    }

    private void updateUi(){
        Einstellungen.maximiert = primaryStage.isMaximized();
        stageHeight = primaryStage.getHeight();
        stageWidth = primaryStage.getWidth();

        einstellungen.setTranslateX(stageWidth/2 - 200);

        sichtwechsel.setTranslateX(stageWidth/2 + 60);

        refresh.setTranslateX(stageWidth/2 - 50);

        simZeit.setTranslateX(stageWidth - simZeit.getWidth() - 30);

        pluginName.setTranslateX(10);

        zugSuche.setTranslateX(80);

        if(Einstellungen.informationenAnzeigen){
            infoFehler.setMinWidth(Einstellungen.informationenBreite);
            infoFehler.setMaxWidth(Einstellungen.informationenBreite);
        } else{
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
        TextField tfai = new TextField(String.valueOf(Einstellungen.update));
        tfai.setTranslateX(300);
        tfai.setTranslateY(10);

        Label v = new Label("Vorschau (in min):");
        v.setFont(Font.font(18));
        v.setTranslateY(40);
        v.setTranslateX(10);
        TextField tfv = new TextField(String.valueOf(Einstellungen.vorschau));
        tfv.setTranslateX(300);
        tfv.setTranslateY(40);

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

        Pane gleise = new Pane();
        gleise.setTranslateX(0);
        gleise.setTranslateY(290);

        stageHeight = 290;

        CheckBox[] cbBahnhof = new CheckBox[stellwerk.getBahnhoefe().size()];
        CheckBox[] cbGleis = new CheckBox[stellwerk.getAnzahlBahnsteige()];
        int tempX = 190;
        int tempY = -30;
        int counterBhf = 0;
        int counterGleis = 0;
        for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
            // zuerst Bahnhof in die linke Spalte schreiben
            tempY += 30;
            CheckBox cb = new CheckBox(bahnhof.getName());
            if ("".equals(cb.getText())) {
                cb.setText(stellwerk.getStellwerksname());
            }
            if(!bahnhof.getAlternativName().equals("")){
                if(!bahnhof.getName().equals("")) cb.setText(bahnhof.getAlternativName() + " (" + bahnhof.getName() + ")");
                else cb.setText(bahnhof.getAlternativName());
            }
            cbBahnhof[counterBhf] = cb;
            cbBahnhof[counterBhf].setTranslateX(10);
            cbBahnhof[counterBhf].setTranslateY(tempY);
            cbBahnhof[counterBhf].setFont(Font.font(18));
            cbBahnhof[counterBhf].setSelected(bahnhof.isSichtbar());
            cbBahnhof[counterBhf].setOnAction(new javafx.event.EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for (Bahnsteig b : bahnhof.getBahnsteige())
                        b.setSichtbar(cb.isSelected());
                }
            });
            gleise.getChildren().add(cbBahnhof[counterBhf]);
            counterBhf++;
            //dann alle Gleise in die mittlere und rechte Spalte schreiben
            for (int i = 0; i < bahnhof.getAnzahlBahnsteige(); i++) {
                cbGleis[counterGleis] = new CheckBox(bahnhof.getBahnsteig(i).getName());
                cbGleis[counterGleis].setTranslateX(tempX);
                cbGleis[counterGleis].setTranslateY(tempY);
                cbGleis[counterGleis].setFont(Font.font(18));
                cbGleis[counterGleis].selectedProperty().bindBidirectional(bahnhof.getBahnsteig(i).getSichtbarProperty());

                if (i % 2 != 0 && i < bahnhof.getBahnsteige().size() - 1) {
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
            try{
                Einstellungen.update = Integer.parseInt(tfai.getText());
                Einstellungen.spaltenbreite = Integer.parseInt(tfsb.getText());
                Einstellungen.schriftgroesse = Integer.parseInt(tffs.getText());
                Einstellungen.informationenBreite = Integer.parseInt(tfzib.getText());
                Einstellungen.informationenAnzeigen = cbezi.isSelected();

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
                            if (cbGleis[counterOne].isSelected()) {
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

                    updateSettings();
                    updateUi();
                    gleisbelegung.erzeugeBahnsteigLabel();
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });

        Pane p = new Pane();
        p.setPrefWidth(stageWidth);
        p.setPrefHeight(stageHeight);
        p.getChildren().addAll(ai, tfai, v, tfv, sb, tfsb, fs, tffs, zib, tfzib, ezi, laaoaw, cbaaoaw, cbezi, gleise, speichern);
        p.setStyle("-fx-background: #303030; -fx-padding: 0;");

        ScrollPane sp = new ScrollPane(p);
        sp.setStyle("-fx-background: #505050; -fx-padding: 0;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene;
        if(stageHeight > primaryStage.getHeight() - 100){ //Fenster kann nun nicht mehr unter der Taskleiste flimmern
            scene = new Scene(sp, stageWidth, primaryStage.getHeight() - 100); //Fenster kann nun nicht mehr unter der Taskleiste flimmern
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

        /*for (LabelContainer lc : gleisbelegung.getLabelTime()) {
            lc.getLabel().setFont(Font.font(Einstellungen.schriftgroesse - 5));
            lc.getLabel().setMaxWidth(Einstellungen.spaltenbreite);
            lc.getLabel().setMinWidth(Einstellungen.spaltenbreite);
        }*/

        for (Bahnhof b : stellwerk.getBahnhoefe()) {
            for(Bahnsteig ba : b.getBahnsteige()){
                ba.getGleisLabel().getLabel().setFont(Font.font(Einstellungen.schriftgroesse - 5));
                if(ba.isSichtbar()){
                    ba.getGleisLabel().getLabel().setMaxWidth(Einstellungen.spaltenbreite);
                    ba.getGleisLabel().getLabel().setMinWidth(Einstellungen.spaltenbreite);
                } else{
                    ba.getGleisLabel().getLabel().setMaxWidth(0);
                    ba.getGleisLabel().getLabel().setMinWidth(0);
                }

                /*for(LabelContainer lc : ba.getSpalte()){
                    lc.getLabel().setFont(Font.font(Einstellungen.schriftgroesse - 5));
                    if(ba.isSichtbar()){
                        lc.getLabel().setMaxWidth(Einstellungen.spaltenbreite);
                        lc.getLabel().setMinWidth(Einstellungen.spaltenbreite);
                    } else{
                        lc.getLabel().setMaxWidth(0);
                        lc.getLabel().setMinWidth(0);
                    }
                }*/
            }
        }

        pluginName.setFont(Font.font(Einstellungen.schriftgroesse));
        simZeit.setFont(Font.font(Einstellungen.schriftgroesse));
        einstellungen.setFont(Font.font(Einstellungen.schriftgroesse-2));
        refresh.setFont(Font.font(Einstellungen.schriftgroesse-2));
        sichtwechsel.setFont(Font.font(Einstellungen.schriftgroesse-2));
        gleisbelegung.getFirstLabel().setFont(Font.font(Einstellungen.schriftgroesse-5));

        gleisbelegung.getFirstLabel().setMaxWidth(Einstellungen.spaltenbreite);
        gleisbelegung.getFirstLabel().setMinWidth(Einstellungen.spaltenbreite);
    }
}