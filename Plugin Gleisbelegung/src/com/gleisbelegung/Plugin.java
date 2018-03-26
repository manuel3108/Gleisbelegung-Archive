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
import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
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
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class Plugin extends Application implements Runnable{
    private Stage primaryStage;
    private StackPane firstSP;                               //Erste Benutzeroberfläche, die beim start angezeigt wird.

    private Update u;                                       //Objekt der Update-Klasse                                  (Lässte ein Fenster erscheinen, sobald eine neuere Version verfügbar ist)
    private Fenster f;

    private String host = "192.168.1.25";                   //Die Ip des Rechnsers, auf welchem die Sim läuft           (Wird bei einer Änderung beim Pluginstart aktualisiert)
    private int version = 15;                                //Aktualle Version des Plugins

    private Stellwerk stellwerk;
    public static Einstellungen einstellungen;
    private boolean update = true;
    private Button refresh;

    @Override
    public void start(Stage primaryStage) {
        Rectangle2D size = Screen.getPrimary().getVisualBounds();

        TextField tfHost = new TextField();

        Socket socketGefunden = null;
        try {
            final java.util.Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (socketGefunden == null && nics.hasMoreElements()) {
                java.net.NetworkInterface nic = nics.nextElement();
                final java.util.Enumeration<InetAddress> inetAddresses = nic.getInetAddresses();
                while (socketGefunden == null && inetAddresses.hasMoreElements()) {
                    final InetAddress inetAddress = inetAddresses.nextElement();
                    final Socket socket = new Socket();
                    try {
                        socket.bind(null);
                        socket.connect(new InetSocketAddress(inetAddress, 3691));
                        if (socketGefunden == null || cmpInetAddress(socket.getLocalAddress(), socketGefunden.getLocalAddress()) < 0) {
                            socket.setSoTimeout(1000);
                            socketGefunden = socket;
                            Platform.runLater(() -> tfHost.setText(inetAddress.getHostAddress()));
                        }
                    } catch (IOException e) {}
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

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

        final Socket tempSocket = socketGefunden;

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
            Platform.runLater(() -> startLoading(tempSocket));
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

        primaryStage.setOnCloseRequest(we -> {
            checkLogOnClosing();
            Plugin.einstellungen.schreibeEinstellungen();
        });
        this.primaryStage = primaryStage;

        u = new Update();
        u.checkForNewVersion(version);

        einstellungen = new Einstellungen();
        this.primaryStage.setMaximized(Einstellungen.maximiert);

        setOutputStreams();
    }

    /**
     * Vergleicht zwei IP Adressen.
     * * Link local < local < unique < multicast
     * * loopback < IPv4 < IPv6
     *
     * @param address0
     * @param address1
     *
     * @return
     */
    private static int cmpInetAddress(final InetAddress address0, final InetAddress address1) {
        if (address0.isLoopbackAddress() ^ address1.isLoopbackAddress()) {
            return address0.isLoopbackAddress() ? -1 : 1;
        }
        if (address0.isLinkLocalAddress() ^ address1.isLinkLocalAddress()) {
            return address0.isLinkLocalAddress() ? -1 : 1;
        }
        if (address0.isSiteLocalAddress() ^ address1.isSiteLocalAddress()) {
            return address0.isSiteLocalAddress() ? -1 : 1;
        }
        if (address0.isAnyLocalAddress() ^ address1.isAnyLocalAddress()) {
            return address0.isAnyLocalAddress() ? -1 : 1;
        }
        if (address0.isMulticastAddress() ^ address1.isMulticastAddress()) {
            return address0.isMulticastAddress() ? 1 : -1;
        }
        // IPv4 < IPv6
        if (InetAddress.class.isAssignableFrom(address0.getClass()) ^ InetAddress.class.isAssignableFrom(address1.getClass()) ) {
            return InetAddress.class.isAssignableFrom(address0.getClass()) ? -1 : 1;
        }
        return 0;
    }

    private void startLoading(Socket socketGefunden){
        Runnable r = () -> {
            refresh = new Button();
            refresh.setDisable(true);
            refresh.setOnAction(e -> neustart());

            try {
                if(stellwerk != null && stellwerk.getSocket() != null) stellwerk = new Stellwerk(stellwerk.getSocket(), "Gleisbelegung", "Darstellung der Gleisbelegung", "Manuel Serret", version);
                else if(socketGefunden != null) stellwerk = new Stellwerk(socketGefunden, "Gleisbelegung", "Darstellung der Gleisbelegung", "Manuel Serret", version);
                else stellwerk = new Stellwerk(host, 3691, "Gleisbelegung", "Darstellung der Gleisbelegung", "Manuel Serret", version);
            } catch (IOException e) {
                System.out.println("Die Verbindung mit dem SIM kam nicht zustande. Prüfe ob die Plugin-Schnitstelle aktiviert ist!");
                System.exit(1);
            }
            f = new Fenster(stellwerk, primaryStage, refresh);
            Einstellungen.fenster = f;
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
                System.out.println("INFORMATION: Neustart");
                update = false;
                refresh.setDisable(true);
                Thread.sleep(2000);
                startLoading(null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }

    private void setOutputStreams(){
        try{
            File f = new File(Einstellungen.appOrdner + File.separator + "Log.txt");

            if(!f.exists()){
                f.createNewFile();
            }

            if(f.exists() && f.canRead()){
                System.out.println("Fehler und Meldungen werden in log-Datei geschrieben! Speicherort: " + f.getAbsolutePath());

                FileOutputStream fos = new FileOutputStream(f, false);
                PrintStream ps = new PrintStream(fos);
                System.setErr(ps);
                System.setOut(ps);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void checkLogOnClosing(){
        try {
            File f = new File(Einstellungen.appOrdner + File.separator + "Log.txt");

            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            String temp = raf.readLine();
            while(temp != null && !temp.contains("java") && !temp.contains("com.") && !temp.contains("GITHUB")){
                temp = raf.readLine();
            }

            if(temp != null && (temp.contains("java") || temp.contains("com.") || temp.contains("GITHUB"))){
                if(u.getNeusteVersion() <= version){
                    openLogWindow();
                }
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
        l.setFont(Font.font(Einstellungen.schriftgroesse));
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
        bno.setFont(Font.font(Einstellungen.schriftgroesse));
        bno.setOnAction(e -> stage.close());
        bno.setTranslateX(250);
        bno.setTranslateY(150);

        Button byes = new Button("Weiter");
        byes.setFont(Font.font(Einstellungen.schriftgroesse));
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

            File f = new File(Einstellungen.appOrdner + File.separator + "Log.txt");

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