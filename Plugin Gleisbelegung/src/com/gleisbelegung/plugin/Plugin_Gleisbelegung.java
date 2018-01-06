package com.gleisbelegung.plugin;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

In dieser Klasse werden viele Variablen gespeichert, da jede Klasse diese Klasse extendet.
Hier befindet sich auch die Hauptschleife des Plugins.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class Plugin_Gleisbelegung extends Application implements Runnable{
    static long currentGameTime;                            //Speichert die aktuelle Zeit in Milisekunden, heißt 1 Minute entspricht 1000*60
    static boolean lastUpdateSuccessful;                    //Aktuall keine Verwendung

    static ArrayList<Zug> zuege;                            //Hier werden alle Zug-Objekte gespeichert
    public static String[] bahnsteige;                             //Speichert die Namen aller Bahnsteige in einem Array

    static Stage primaryStage;                              //Ist das Objekt für das aktuelle Fenster
    static Button refresh;                                 //Button um das Plugin neu zu laden

    static int settingsUpdateInterwall = 15;                //Wie oft das Fenster (die Tabelle) aktualisiert wird.      (Wird bei vorhandenen Einstellungen durch den dortigen Wert überschrieben)
    static int settingsVorschau = 60;                       //Wie viele Zeilen die Tabelle hat.                         (Wird bei vorhandenen Einstellungen durch den dortigen Wert überschrieben)
    static int settingsGridWidth = 100;                     //Wie breit die einzelnen Spalten sind.                     (Wird bei vorhandenen Einstellungen durch den dortigen Wert überschrieben)
    static int settingsFontSize = 18;                       //Wie groß die Schriftgröße ist.                            (Wird bei vorhandenen Einstellungen durch den dortigen Wert überschrieben)
    static boolean settingsPlaySound = true;                //Soll bei einer Mehrfahcbelegung ein Ton abgespielt werden.(Wird bei vorhandenen Einstellungen durch den dortigen Wert überschrieben)
    static boolean settingsShowInformations = true;         //Zeige die Informationenpanel.                             (Wird bei vorhandenen Einstellungen durch den dortigen Wert überschrieben)
    static boolean settingsDebug = false;                   //Sollen zusätzliche Informationen geschrieben werden.      (Wird bei vorhandenen Einstellungen durch den dortigen Wert überschrieben)
    static int settingsInformationWith = 300;               //Breite des Informations-Panels auf der rechten Seite

    static long spielStart = System.currentTimeMillis();    //Zu welcher Uhrzeit das Spiel gestartet wurde.             (Wird auch bei automatischem Neustart neu gesetzt)
    private long lastRefresh = System.currentTimeMillis();  //Zu welcher Uhrzeit das Plugin zum letzten mal aktualisiert wurde
    static Pane fehlerMeldungen;                            //Panel für Fehlermeldungen auf der rechten Seite.          (Wird bald entfernt)
    static Pane informations;                               //Panel für alle Zuginformation                             (Zugnummer, Verspätung etc.)
    static PrintWriter logFile;                             //
    static Pane pZugSuche;                                  //Panel mit der Zugsuche
    static int errorCounter = 0;                            //Zählt alle auftreten Fehler für einen eventuellen automatischen Neustart
    static int maxErrorCounter = 10;                        //Ist der obrige Wert größer als dieser, wird das Plugin automatisch neu gestartet.
    static double stageWidth = 1000;                        //Standartmäßige Fenster-Breite                             (Wir bei Veränderung der Breite aktualisiert)
    static double stageHeight = 500;                        //Standartmäßige Fenster-Höhe                               (Wir bei Veränderung der Höhe aktualisiert)
    static StackPane firstSP;                               //Erste Benutzeroberfläche, die beim start angezeigt wird.
    static String bahnhofName;                              //Name des Bahnhofes
    static ArrayList<Gleis> gleise;

    private Verbindung v;                                   //Objekt der Verbindungs-Klasse                             (Übernimmt Kommunikation mit der Schnittstelle)
    private Update u;                                       //Objekt der Update-Klasse                                  (Lässte ein Fenster erscheinen, sobald eine neuere Version verfügbar ist)
    private static Fenster f;                                      //Objekt der Fenster-Klasse                                 (Kümmert sich um die Aktualisierung des UI)

    private String host = "192.168.1.25";                   //Die Ip des Rechnsers, auf welchem die Sim läuft           (Wird bei einer Änderung beim Pluginstart aktualisiert)
    private int version = 13;                                //Aktualle Version des Plugins
    private static AudioClip audio;                         //momentan ohne Verwendung
    private Socket socket;                                  //hält die Kommunikation mit dem dem SIM aufrecht
    private Thread mainLoop;                                //Dient zur Abbruchbedingung des Threads
    private boolean ableToUpdate = false;                   //Auf false, wenn der Thread keine Aktualisierungen ausführen soll, z.B. bei einem Neustart


    //Erste Aufgerufene Methode (Ip-Abfrage, Updates-checken, Fenster erzeugen)
    @Override
    public void start(Stage primaryStage) throws Exception{
        refresh = new Button();
        refresh.setOnAction(e -> Platform.runLater(() -> startLoading()));
        fehlerMeldungen = new Pane();

        try{
            audio = new AudioClip(getClass().getResource("Train_Horn.wav").toURI().toString());
        } catch(Exception e){
            audio = new AudioClip("Train_Horn.wav");
        }


        Rectangle2D size = Screen.getPrimary().getVisualBounds();

        stageHeight = size.getHeight();
        stageWidth = size.getWidth();
        int sceneWidth = (int) stageWidth;
        int sceneHeight = (int) stageHeight;

        firstSP = new StackPane();
        firstSP.setStyle("-fx-background-color: #303030");

        Label lHint = new Label("IP-Adress nur ändern, wenn SIM auf einem anderen Rechner läuft. \nWenn dies der Fall ist, muss auf dem SIM-Rechner die Windows-Firewall\n (wahrscheinlich auch jede andere), deaktiviert werden.");
        lHint.setStyle("-fx-text-fill: white;");
        lHint.setFont(Font.font(settingsFontSize));
        lHint.setTranslateY(0);
        lHint.applyCss();
        lHint.layout();

        Label lHost = new Label("Bitte die IP des Rechners eingeben: ");
        lHost.setStyle("-fx-text-fill: white;");
        lHost.setFont(Font.font(settingsFontSize));
        lHost.setTranslateY(80);
        lHost.setTranslateX(-120);
        lHost.applyCss();
        lHost.layout();

        TextField tfHost = new TextField();
        tfHost.setText("localhost");

        tfHost.setStyle("-fx-text-fill: black;");
        tfHost.setFont(Font.font(settingsFontSize));
        tfHost.setTranslateX(120);
        tfHost.setTranslateY(80);
        tfHost.setMinWidth(150);
        tfHost.setMaxWidth(150);
        tfHost.applyCss();
        tfHost.layout();
        //p.widthProperty().addListener((observable, oldValue, newValue) -> tfHost.setTranslateX(newValue.doubleValue()/2 - 75));

        Button btLoad = new Button("Verbinden");
        btLoad.setStyle("-fx-text-fill: black;");
        btLoad.setFont(Font.font(settingsFontSize));
        btLoad.setTranslateX((lHost.getWidth() + tfHost.getWidth())/2);
        btLoad.setTranslateY(130);
        btLoad.setMinWidth(150);
        btLoad.applyCss();
        btLoad.layout();
        btLoad.setOnAction(e -> {
            if(socket == null){
                host = tfHost.getText();
            }

            firstSP.setStyle("-fx-background-color: #505050");
            firstSP.applyCss();
            firstSP.layout();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            Platform.runLater(() -> {
                try {
                    if(socket == null) socket = new Socket(tfHost.getText(), 3691);
                } catch (Exception ex) {
                    //ex.printStackTrace(); //Erfolg wird später geprüft
                }
                startLoading();
            });
        });


        firstSP.getChildren().addAll(lHint, lHost, tfHost, btLoad);
        firstSP.applyCss();
        firstSP.layout();
        //p.setMinWidth(lHost.getWidth() + tfHost.getWidth());
        //p.setMinHeight(lHost.getHeight() + btLoad.getHeight());


        Scene s = new Scene(firstSP, sceneWidth, sceneHeight);

        primaryStage.setScene(s);
        primaryStage.setTitle("Plugin: Gleisbelegung");
        primaryStage.show();
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(500);
        primaryStage.setMaximized(true);

        try{
            primaryStage.getIcons().add(new Image(Plugin_Gleisbelegung.class.getResourceAsStream("icon.png")));
        } catch(Exception e){
            try{
                primaryStage.getIcons().add(new Image("icon.png"));
            } catch (Exception e1){
                e.printStackTrace();
                e1.printStackTrace();
            }
        }

        primaryStage.setOnCloseRequest(we -> checkLogOnClosing());

        Plugin_Gleisbelegung.primaryStage = primaryStage;

        readSettings();

        setOutputStreams();

        u = new Update();
        u.checkForNewVersion(version);


        for (int i = 0; i < 255; i++) {
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
        }

    }

    private void setOutputStreams(){
        try{
            File f = File.createTempFile("temp", ".txt");
            String filePath = f.getAbsolutePath().replace(f.getName(), "");
            f.delete();

            f = new File(filePath + "Plugin_Gleisbelegung_Log.txt");

            if(!f.exists()){
                f.createNewFile();
            }

            if(f.exists() && f.canRead()){
                System.out.println("Fehler und Meldungen werden in log-Datei geschrieben! Speicherort: " + f.getAbsolutePath());

                FileOutputStream fos = new FileOutputStream(f, false);
                PrintStream ps = new PrintStream(fos);
                System.setErr(ps);
                System.setOut(ps);

                //System.out.println("\n\n\n\n\n**********************************************************************************\n\t\t\t\t\tPlugin Start\n**********************************************************************************");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //Schreibt DEBUG-Informationen auf dei Konsole (nur wenn settingsDebug = true)
    public static void debugMessage(String message, boolean newLine){
        if(settingsDebug && newLine) System.out.println(message);
        else if(settingsDebug) System.out.print(message);
    }

    //Startet die Verbindung zur Schnitstelle
    private void startLoading() {
        boolean erfolgreich = true;

        refresh.setDisable(true);
        if(v == null){
            try{
                v = new Verbindung(socket);
            } catch (Exception e){
                erfolgreich = false;
                //e.printStackTrace();
            }
        } else{
            ableToUpdate = false;
            f.clearOldData();
            System.out.println("INFORMATION: Das Plugin wird neu gestartet!");
        }

        if(erfolgreich){
            Runnable r = () -> {
                try{
                    zuege = new ArrayList<>();

                    f = new Fenster();
                    if(!v.isAktualisiere()){
                        v.update();
                    }
                    f.update();
                    f.setGridScene();

                    mainLoop = null;
                    Runnable r1 = () -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mainLoop = new Thread(this, "App-Schleife");
                        mainLoop.setDaemon(true);
                        mainLoop.start();

                        ableToUpdate = true;
                    };
                    new Thread(r1).start();

                } catch(Exception e){
                    e.printStackTrace();
                }

                refresh.setDisable(false);
            };
            new Thread(r).start();
        } else {
            Stage error = new Stage();
            Scene s;
            Pane p;
            Label lText;

            lText = new Label("Die Verbindung mit dem SIM kam nicht zur stande.\n\nPrüfe ob die Plugin-Schnitstelle aktiviert ist!");
            lText.setFont(Font.font(settingsFontSize));
            lText.setStyle("-fx-text-fill: white;");
            lText.setTranslateY(20);
            lText.setTranslateX(20);
            lText.setPrefWidth(500);
            lText.setWrapText(true);

            p = new Pane(lText);
            p.setStyle("-fx-background: #303030");

            s = new Scene(p);
            p.setStyle("-fx-background: #303030");

            error.setTitle("Ferhlermeldung");
            error.setWidth(500);
            error.setHeight(200);
            error.setScene(s);
            error.setAlwaysOnTop(true);
            error.show();
        }
    }

    //Liest die Vorhanden Einstellungen und überschreibt die Standart-Werte
    private void readSettings(){
        try {
            File f = File.createTempFile("temp", ".txt");
            String filePath = f.getAbsolutePath().replace(f.getName(), "");
            f.delete();

            BufferedReader br = new BufferedReader(new FileReader(filePath + "Plugin_Gleisbelegung_Settings.txt"));

            settingsUpdateInterwall = Integer.parseInt(br.readLine());
            settingsVorschau = Integer.parseInt(br.readLine());
            settingsGridWidth = Integer.parseInt(br.readLine());
            settingsFontSize = Integer.parseInt(br.readLine());
            settingsShowInformations = Boolean.parseBoolean(br.readLine());
            settingsPlaySound = Boolean.parseBoolean(br.readLine());
            settingsDebug = Boolean.parseBoolean(br.readLine());
            settingsInformationWith = Integer.parseInt(br.readLine());

            //Aufgrund zu großer Log-Dateien Standartmäßig auf false setzen: Workaround
            settingsDebug = false;
        } catch (Exception e) {
            System.out.println("Die Einstellungsdatei wurde nicht gefunden, oder enthielt zu wenige Angaben!");
        }
    }

    //Hier werden Fehlermeldungen in ein neues Fenster eingetragen (wie z.B. beim automatischen Neustart)
    public static void errorWindow(int exitPoint, String fehlermeldung){
        Runnable r = () -> {
            Platform.runLater(() -> {
                Stage error = new Stage();
                Scene s;
                Pane p;
                Button b;
                Label lTitle;
                Label lText;

                lTitle = new Label("Es ist ein unerwarteter Fehler aufgetreten! (Status " + exitPoint + ")");
                lTitle.setFont(Font.font(settingsFontSize));
                lTitle.setStyle("-fx-text-fill: red;");
                lTitle.setTranslateY(10);
                lTitle.setTranslateX(10);

                lText = new Label(fehlermeldung);
                lText.setFont(Font.font(settingsFontSize));
                lText.setStyle("-fx-text-fill: white;");
                lText.setTranslateY(60);
                lText.setTranslateX(10);
                lText.setPrefWidth(primaryStage.getWidth()/2-20);
                lText.setWrapText(true);

                b = new Button("Ok");
                b.setFont(Font.font(settingsFontSize));
                b.setTranslateX(primaryStage.getWidth()/4-20);
                b.setTranslateY(primaryStage.getHeight()/2-100);
                b.setOnAction(e -> error.close());

                p = new Pane(lTitle, lText, b);
                p.setStyle("-fx-background: #303030");

                s = new Scene(p);
                p.setStyle("-fx-background: #303030");

                error.setTitle("Ferhlermeldung");
                error.setWidth(primaryStage.getWidth()/2);
                error.setHeight(primaryStage.getHeight()/2);
                error.setScene(s);
                error.setAlwaysOnTop(true);
                error.show();
            });
        };
        new Thread(r).start();
    }

    //Der Sound, der der Gespielt wird, wenn eine Mehrachbelegung entsteht
    public static void playColisonSound(int gleisId){
        if(settingsPlaySound && !audio.isPlaying()){
            for(Gleis g : gleise){
                if(g.getId() == gleisId){
                    if(g.isSichtbar()){
                        try{
                            audio.setVolume(0.04);
                            audio.play();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }
        }
    }

    //Schreibt Fehlermeldungen auf das Panel fehlerMeldungen
    public static void addMessageToErrorPane(String message){
        Label l = new Label(message);
        l.setFont(Font.font(settingsFontSize-5));
        l.setStyle("-fx-text-fill: white;");
        l.setTranslateY(fehlerMeldungen.getChildren().size()*20);

        if(fehlerMeldungen.getChildren().size() >= 10){
            Platform.runLater(() -> {
                fehlerMeldungen.getChildren().remove(0);
                fehlerMeldungen.getChildren().add(l);

                for(int i = 0; i < fehlerMeldungen.getChildren().size(); i++){
                    fehlerMeldungen.getChildren().get(i).setTranslateY(i*20);
                }
            });
        } else{
            Platform.runLater(() -> fehlerMeldungen.getChildren().add(l));
        }
    }

    private void checkLogOnClosing(){
        try {
            File f = File.createTempFile("temp", ".txt");
            String filePath = f.getAbsolutePath().replace(f.getName(), "");
            f.delete();

            f = new File(filePath + "Plugin_Gleisbelegung_Log.txt");

            RandomAccessFile raf = new RandomAccessFile(f, "r");
            String temp = raf.readLine();
            while(temp != null && !temp.contains("java") && !temp.contains("com.") && !temp.contains("GITHUB")){
                temp = raf.readLine();
            }

            if(temp != null && (temp.contains("java") || temp.contains("com.") || temp.contains("GITHUB"))){
                openLogWindow();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openLogWindow(){
        Stage stage = new Stage();

        //Label l = new Label("Während deiner aktuellen Sitzung sind Fehler aufgetreten. Durch einen Klick auf Weiter werden deine Log-Datei und deine Anregungen anonym hochgeladen.");
        Label l = new Label("Während deiner aktuellen Sitzung sind Fehler aufgetreten. Durch einen Klick auf Weiter wird deine Log-Datei anonym hochgeladen.");
        l.setStyle("-fx-text-fill: white");
        l.setFont(Font.font(settingsFontSize));
        l.setWrapText(true);
        l.setMaxWidth(450);
        l.setTranslateY(25);
        l.setTranslateX(25);

        /*TextField ta = new TextField();
        ta.setFont(Font.font(settingsFontSize-3));
        ta.setTranslateX(25);
        ta.setTranslateY(125);
        ta.setPrefWidth(450);
        ta.setPrefHeight(100);*/


        Button bno = new Button("Abbrechen");
        bno.setFont(Font.font(settingsFontSize));
        bno.setOnAction(e -> stage.close());
        bno.setTranslateX(250);
        bno.setTranslateY(150);

        Button byes = new Button("Weiter");
        byes.setFont(Font.font(settingsFontSize));
        byes.setTranslateX(150);
        byes.setTranslateY(150);
        byes.setOnAction(e -> {
            byes.setDisable(true);
            bno.setDisable(true);

            Runnable r = () -> {
                sendLogFile(l);
            };
            new Thread(r).start();
        });

        Pane p = new Pane(l, byes, bno);
        p.setStyle("-fx-background-color: #303030");
        p.setMinSize(500,200);
        p.setMaxSize(500, 200);

        Scene s = new Scene(p,500,200);


        stage.setScene(s);
        stage.setTitle("Log-Datei senden?");

        stage.setAlwaysOnTop(true);
        stage.show();
    }

    private void sendLogFile(Label l){
        try {
            Platform.runLater(() -> l.setText("Lese Log-Datei..."));

            File f = File.createTempFile("temp", ".txt");
            String filePath = f.getAbsolutePath().replace(f.getName(), "");
            f.delete();

            f = new File(filePath + "Plugin_Gleisbelegung_Log.txt");

            RandomAccessFile raf = new RandomAccessFile(f, "r");
            String newLine = raf.readLine();
            String log = newLine;
            while(newLine != null){
                log += "\n" + newLine;
                newLine = raf.readLine();
            }

            Platform.runLater(() -> l.setText("Lade hoch..."));

            String baseUrl = "http://manuel-serret.bplaced.net/Gleisbelegung/request.php";
            //String baseUrl = "http://localhost/Webseiten/Gleisbelegung/request.php";


            int counter = 0;
            char[] c = log.toCharArray();
            ArrayList<String> logArray = new ArrayList<>();
            logArray.add("");

            for (int i = 0; i < log.length(); i++) {
                logArray.set(counter, logArray.get(counter) + c[i]);

                if(logArray.get(counter).length() >= 2000 - 10){ //-10 wegen versionsnummer
                    logArray.set(counter, logArray.get(counter).replace(" ","%20"));
                    logArray.set(counter, logArray.get(counter).replace("\n","%0A"));

                    counter++;
                    logArray.add(counter, "");
                }
            }
            logArray.set(counter, logArray.get(counter).replace(" ","%20"));        //für das letzte element
            logArray.set(counter, logArray.get(counter).replace("\n","%0A"));

            URL url = new URL( baseUrl + "?action=new&message=v." + version + "&log=" + URLEncoder.encode(logArray.get(0),java.nio.charset.StandardCharsets.UTF_8.toString()));
            URLConnection con = url.openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

            Scanner sc = new Scanner(con.getInputStream());
            int id = Integer.parseInt(sc.nextLine());

            counter = 0;
            while (counter < logArray.size()) {
                try{
                    final int temp = counter;
                    Platform.runLater(() -> l.setText("Lade " + temp + " von " + (logArray.size()-1) + " hoch..."));

                    url = new URL(baseUrl + "?action=add&id="+id+"&log=" + URLEncoder.encode(logArray.get(counter), StandardCharsets.UTF_8.toString()));
                    con = url.openConnection();
                    con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

                    con.getInputStream();

                    counter++;
                } catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            Platform.runLater(() -> l.setText("Fertig. Vielen Dank für deine Hilfe!"));
            Runnable r = () -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.exit();
            };
            new Thread(r).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sortiereGleiseListener(){
        try{
            f.sortiereGleise();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //Hauptschleife des Plugins
    @Override
    public void run() {
        long timeFromLastUpdate = 0;
        Thread thisThread = Thread.currentThread();
        while(mainLoop == thisThread && errorCounter < maxErrorCounter){
            try {
                int time = (int) ((System.currentTimeMillis() - timeFromLastUpdate) / 1000);

                if(ableToUpdate){
                    if(time >= settingsUpdateInterwall){
                        timeFromLastUpdate = System.currentTimeMillis();

                        Runnable r = () -> {
                            try{
                                if(v != null && f != null && !v.isAktualisiere()){
                                    v.update();
                                    f.update();
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        };
                        new Thread(r, "Aktualisiere Verbindung und Fenster").start();
                    }

                    f.updateSimTime(settingsUpdateInterwall - time);

                    if(currentGameTime % (1000*60) >= 0 && currentGameTime % (1000*60) <= 1000){
                        Runnable r = () -> {
                            try{
                                debugMessage("INFORMATION: Aktualisiere Tabelle...", false);
                                f.refreshGrid();
                                debugMessage("Beendet!", true);
                                System.out.println("INFORMATION: Du benutzt das Plugin seit " + ((System.currentTimeMillis()-spielStart)/(1000*60)) + " Minute(n)!");
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        };
                        new Thread(r, "Aktualisiere Tabelle").start();
                    }

                    if((System.currentTimeMillis() - lastRefresh) % (1000*60*45) < 1000){
                        lastRefresh = System.currentTimeMillis()-1000;
                        break;
                    }
                }

                currentGameTime += 1000;
                Thread.sleep(1000);
            } catch (Exception e) {
                debugMessage("FEHLER: Bitte folgenden Code beachten:", true);
                e.printStackTrace();
            }
        }

        if(errorCounter >= maxErrorCounter){
            errorCounter = 0;
            System.out.println("FEHLER: Das Plugin wird neu gestartet.");
            Platform.runLater(() -> errorWindow(0, "Das Plugin wurde aufgrund einiger Fehler neu gestartet!"));
            Platform.runLater(() -> startLoading());
        }
    }
}