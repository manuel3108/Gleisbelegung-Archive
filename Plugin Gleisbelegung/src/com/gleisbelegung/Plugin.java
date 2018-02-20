package com.gleisbelegung;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

In dieser Klasse werden viele Variablen gespeichert, da jede Klasse diese Klasse extendet.
Hier befindet sich auch die Hauptschleife des Plugins.
 */

import com.gleisbelegung.lib.Stellwerk;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Plugin extends Application implements Runnable{
    private Stage primaryStage;
    private StackPane firstSP;                               //Erste Benutzeroberfläche, die beim start angezeigt wird.

    private Update u;                                       //Objekt der Update-Klasse                                  (Lässte ein Fenster erscheinen, sobald eine neuere Version verfügbar ist)
    private Fenster f;

    private String host = "192.168.1.25";                   //Die Ip des Rechnsers, auf welchem die Sim läuft           (Wird bei einer Änderung beim Pluginstart aktualisiert)
    private int version = 15;                                //Aktualle Version des Plugins

    private Stellwerk stellwerk;
    private boolean update = true;
    private Button refresh;

    @Override
    public void start(Stage primaryStage) {
        Rectangle2D size = Screen.getPrimary().getVisualBounds();

        //stageHeight = size.getHeight();
        //stageWidth = size.getWidth();
        int sceneWidth = 1000;
        int sceneHeight = 500;

        firstSP = new StackPane();
        firstSP.setStyle("-fx-background-color: #303030");

        Label lHint = new Label("IP-Adress nur ändern, wenn SIM auf einem anderen Rechner läuft. \nWenn dies der Fall ist, muss auf dem SIM-Rechner die Windows-Firewall\n (wahrscheinlich auch jede andere), deaktiviert werden.");
        lHint.setStyle("-fx-text-fill: white;");
        lHint.setFont(Font.font(Einstellungen.schriftgroesse));
        lHint.setTranslateY(0);
        lHint.applyCss();
        lHint.layout();

        Label lHost = new Label("Bitte die IP des Rechners eingeben: ");
        lHost.setStyle("-fx-text-fill: white;");
        lHost.setFont(Font.font(Einstellungen.schriftgroesse));
        lHost.setTranslateY(80);
        lHost.setTranslateX(-120);
        lHost.applyCss();
        lHost.layout();

        TextField tfHost = new TextField();
        tfHost.setText("localhost");

        tfHost.setStyle("-fx-text-fill: black;");
        tfHost.setFont(Font.font(Einstellungen.schriftgroesse));
        tfHost.setTranslateX(120);
        tfHost.setTranslateY(80);
        tfHost.setMinWidth(150);
        tfHost.setMaxWidth(150);
        tfHost.applyCss();
        tfHost.layout();
        //p.widthProperty().addListener((observable, oldValue, newValue) -> tfHost.setTranslateX(newValue.doubleValue()/2 - 75));

        Button btLoad = new Button("Verbinden");
        btLoad.setStyle("-fx-text-fill: black;");
        btLoad.setFont(Font.font(Einstellungen.schriftgroesse));
        btLoad.setTranslateX((lHost.getWidth() + tfHost.getWidth())/2);
        btLoad.setTranslateY(130);
        btLoad.setMinWidth(150);
        btLoad.applyCss();
        btLoad.layout();
        btLoad.setOnAction(e -> {
            host = tfHost.getText();
            Platform.runLater(this::startLoading);
        });

        firstSP.getChildren().addAll(lHint, lHost, tfHost, btLoad);
        firstSP.applyCss();
        firstSP.layout();

        Scene s = new Scene(firstSP, sceneWidth, sceneHeight);

        primaryStage.setScene(s);
        primaryStage.setTitle("Plugin: Gleisbelegung");
        primaryStage.show();
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(500);
        primaryStage.setMaximized(true);

        try{
            primaryStage.getIcons().add(new Image(Plugin.class.getResourceAsStream("res/icon.png")));
        } catch(Exception e){
            try{
                primaryStage.getIcons().add(new Image("res/icon.png"));
            } catch (Exception e1){
                e.printStackTrace();
                e1.printStackTrace();
            }
        }

        this.primaryStage = primaryStage;

        u = new Update();
        u.checkForNewVersion(version);

        /*for (int i = 0; i < 255; i++) {
            if(socket == null){
                final int temp = i;
                Runnable r = () -> {
                    try {
                        if(InetAddress.getByName("192.168.1." + temp).isReachable(1000)){
                            try{
                                socket = new Socket("192.168.1." + temp, 3691);
                                socket.setSoTimeout(1000);

                                Platform.runLater(() -> tfHost.setText("192.168.1." + temp));
                            } catch (Exception e){
                                socket = null;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.start();
            }
        }*/

    }

    private void startLoading(){
        Runnable r = () -> {
            refresh = new Button();
            refresh.setDisable(true);
            refresh.setOnAction(e -> neustart());

            stellwerk = new Stellwerk(host, 3691, "Gleisbelegung", "Darstellung der Gleisbelegung", "Manuel Serret", version);
            f = new Fenster(stellwerk, primaryStage, refresh);
            update = true;

            Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
        };
        new Thread(r).start();
    }

    public void neustart(){
        Runnable r = () -> {
            try {
                update = false;
                refresh.setDisable(true);
                Thread.sleep(2000);
                startLoading();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }

    @Override
    public void run(){
        long timeFromLastUpdate = 0;

        while(update) {
            try {
                int time = (int) ((System.currentTimeMillis() - timeFromLastUpdate) / 1000);
                if(!stellwerk.isAktualisiere()) stellwerk.aktualisiereSimZeit();

                if (time >= Einstellungen.update) {
                    timeFromLastUpdate = System.currentTimeMillis();

                    Runnable r = () -> {
                        try {
                            if (stellwerk != null && f != null) {
                                if(stellwerk.aktualisiereDaten()){
                                    f.update();
                                } else{
                                    System.out.println("Update ausgefallen!");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    new Thread(r, "Aktualisierungs-Thread").start();
                }

                f.updateSimTime(Einstellungen.update - time);

                if (stellwerk.getSpielzeit() % (1000 * 60) >= 0 && stellwerk.getSpielzeit() % (1000 * 60) <= 1000) {
                    Runnable r = () -> {
                        try {
                            f.refreshGrid();
                            System.out.println("INFORMATION: Du benutzt das Plugin seit " + ((System.currentTimeMillis() - stellwerk.getStartzeit()) / (1000 * 60)) + " Minute(n)!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    new Thread(r, "Aktualisiere Tabelle").start();
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}