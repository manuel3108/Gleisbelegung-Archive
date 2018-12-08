package com.gleisbelegung.lib.data;

import com.gleisbelegung.Settings;
import com.gleisbelegung.LabelContainer;
import com.sun.javafx.geom.Vec2d;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.*;


public class Station {

    private int id;
    private String name;
    private String nameByUser = "";
    private Map<String, Platform> platforms = new HashMap<>();
    private List<Platform> platformsList = new ArrayList<>();
    private List<BahnhofTeil> platformPartsList = new ArrayList<>();
    private Map<Integer, BahnhofVerbindung> platformConnections =
            new HashMap<>();
    private Vec2d position;
    private Platform lastPlatform;

    public Station(int id, String name) {
        this.id = id;
        this.name = name;
        position = new Vec2d(30, id * 40 + 40);
    }

    private BahnhofVerbindung getConnection(Station station) {
        synchronized (platformConnections) {
            return platformConnections.get(station.id);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Collection<Platform> getPlatforms() {
        synchronized (platformsList) {
            return Collections
                    .unmodifiableList(new ArrayList<>(platformsList));
        }
    }

    public Platform getBahnsteig(int index) {
        synchronized (platformsList) {
            return platformsList.get(index);
        }
    }

    public int getAnzahlBahnsteige() {
        synchronized (platforms) {
            return platforms.size();
        }
    }

    public boolean isSichtbar() {
        synchronized (platforms) {
            for (Platform b : platforms.values()) {
                if (!b.getVisible())
                    return false;

            }
        }
        return true;
    }

    public void setSichtbar(boolean sichtbar) {
        synchronized (platforms) {
            for (Platform b : platforms.values())
                b.setVisible(sichtbar);
        }
    }

    public List<BahnhofTeil> getPlatformPartsList() {
        synchronized (platformPartsList) {
            return Collections.unmodifiableList(platformPartsList);
        }
    }

    public BahnhofTeil getBahnhofTeile(int index) {
        synchronized (platformPartsList) {
            return platformPartsList.get(index);
        }
    }

    public void addBahnhofLabel(LabelContainer bahnhofLabel,
            List<Platform> bahnsteige) {
        BahnhofTeil bt = new BahnhofTeil(bahnhofLabel, bahnsteige);
        synchronized (platformPartsList) {
            platformPartsList.add(bt);
        }

        bahnhofLabel.getLabel().setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                bt.hervorheben();
                System.out.println(bt.getBahnsteigSize());
            } else if (e.getButton() == MouseButton.SECONDARY) {
                einstellungen(bt);
            }
        });
    }

    public String getNameByUser() {
        return nameByUser;
    }

    public void setNameByUser(String nameByUser) {
        this.nameByUser = nameByUser;
    }

    public void einstellungen(BahnhofTeil bt) {
        Settings.window.gleisbelegung.zeigeOrderIds();
        Stage stage = new Stage();

        Label name = new Label("Name:");
        name.setStyle("-fx-text-fill: white;");
        name.setFont(Font.font(Settings.fontSize));
        name.setTranslateY(25);
        name.setTranslateX(25);

        TextField tname = new TextField(nameByUser);
        tname.setFont(Font.font(Settings.fontSize - 3));
        tname.setTranslateX(25);
        tname.setTranslateY(60);

        Label l = new Label("Reihenfolge festlegen:");
        l.setStyle("-fx-text-fill: white;");
        l.setFont(Font.font(Settings.fontSize));
        l.setTranslateX(25);
        l.setTranslateY(95);

        TextField tf = new TextField(String.valueOf(
                platforms.entrySet().iterator().next().getValue().getOrderId()
                        + 1));
        tf.setFont(Font.font(Settings.fontSize - 3));
        tf.setTranslateX(25);
        tf.setTranslateY(130);

        Button b = new Button("Speichern");
        b.setFont(Font.font(Settings.fontSize));
        b.setTranslateX(25);
        b.setTranslateY(190);
        b.setOnAction(e -> {
            int order = Integer.parseInt(tf.getText()) - 1;

            List<Platform> newPlatformOrder = new ArrayList<>();
            final SortedSet<Platform> bahnsteigeThis = new TreeSet<>();
            Station.this.getBahnsteigOrderSet(bahnsteigeThis);
            boolean nachZiel = false;
            for (Station bhf : Settings.window.getSignalBox()
                    .getStations()) {
                if (bhf == Station.this) {
                    continue;
                }
                final SortedSet<Platform> set = new TreeSet<>();
                bhf.getBahnsteigOrderSet(set);
                Iterator<Platform> iter = set.iterator();
                while (!nachZiel && iter.hasNext()
                        && newPlatformOrder.size() < order) {
                    newPlatformOrder.add(iter.next());
                }
                if (!nachZiel && newPlatformOrder.size() == order) {
                    // Zielposition erreicht, alle Bahnsteige dieses Bahnhofs einsortiern
                    for (Platform bst : bahnsteigeThis) {
                        bst.setOrderId(order++);
                        newPlatformOrder.add(bst);
                    }
                    nachZiel = true;
                }
                Platform bst = null;
                while (iter.hasNext()) {
                    bst = iter.next();
                    if (nachZiel && bst.getOrderId() == order) {
                        // fertig
                        break;
                    }
                    bst.setOrderId(order++);
                    newPlatformOrder.add(bst);
                }
                if (nachZiel && bst != null && bst.getOrderId() == order) {
                    // fertig
                    break;
                }
            }

            nameByUser = tname.getText();

            stage.close();
            Settings.window.gleisbelegung.versteckeOrderIds();
            Settings.window.gleisbelegung.sortiereGleise(null);
            Settings.window.stellwerksuebersicht
                    .aktualisiereBahnhofsNamen();
        });

        Pane p = new Pane(name, tname, l, tf, b);
        p.setStyle("-fx-background-color: #303030;");
        p.setMinSize(500, 200);
        p.setMaxSize(500, 200);

        Scene scene = new Scene(p, 300, 250);

        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);

        stage.setOnCloseRequest(e -> {
            Settings.window.gleisbelegung.versteckeOrderIds();
        });
    }

    @Override public String toString() {
        return "Station{" + "id=" + id + ", name='" + name + '\''
                + ", platforms=" + platforms + '}';
    }

    public Vec2d getPosition() {
        return position;
    }

    public void setPosition(Vec2d position) {
        this.position = position;
    }

    public Collection<BahnhofVerbindung> getPlatformConnections() {
        synchronized (platformConnections) {
            return Collections
                    .unmodifiableCollection(platformConnections.values());
        }
    }

    public void addBahnhofVerbindung(Station station, double laenge, Line linie,
            Vec2d linienPos) {
        BahnhofVerbindung verbindung =
                new BahnhofVerbindung(station, laenge, linie, linienPos);
        synchronized (platformConnections) {
            platformConnections.put(Integer.valueOf(station.id), verbindung);
        }
    }

    public Line getLinie(Station station) {
        BahnhofVerbindung bv = getConnection(station);

        if (bv == null) {
            return null;
        }

        return bv.linie;
    }

    public Set<Station> getVerbindungsBahnhoefe() {
        Collection<BahnhofVerbindung> bvs = getPlatformConnections();
        Set<Station> stationSet = new HashSet<>();
        for (BahnhofVerbindung bv : bvs) {
            stationSet.add(bv.getZielBahnhof());
        }

        return stationSet;
    }

    public Vec2d getLinienPos(Station station) {
        BahnhofVerbindung bv = getConnection(station);
        if (bv == null) {
            return null;
        }

        return bv.getLinienPos();
    }

    public boolean setLinienPos(Station station, Vec2d linienPos) {
        BahnhofVerbindung bv = getConnection(station);
        if (bv == null) {
            return false;
        }

        bv.setLinienPos(linienPos);

        return true;
    }

    public Platform getBahnsteigByName(String name) {
        synchronized (platforms) {
            return platforms.get(name);
        }
    }

    public void addBahnsteig(Platform platform) {
        synchronized (platforms) {
            this.platforms.put(platform.getName(), platform);
            this.lastPlatform = platform;
        }
        synchronized (platformsList) {
            platformsList.add(platform);
        }
    }

    public Platform getLastPlatform() {
        synchronized (platforms) {
            return lastPlatform;
        }
    }

    public void clearBahnhofVerbindungen() {
        synchronized (platformConnections) {
            this.platformConnections.clear();
        }

    }

    public void getBahnsteigOrderSet(Set<Platform> set) {
        synchronized (platforms) {
            set.addAll(platforms.values());
        }
    }

    public void clearBahnhofTeile() {
        synchronized (platformPartsList) {
            platformPartsList.clear();
        }

    }

    /**
     * Vebindung zwischen Station.this and BahnhofVerbindung.ziel
     */
    private class BahnhofVerbindung {

        private double laenge; // dead variable
        private Line linie;
        private Vec2d linienPos;
        private Station ziel;

        public BahnhofVerbindung(Station station, double laenge, Line linie,
                Vec2d linienPos) {
            this.laenge = laenge;
            this.linie = linie;
            this.linienPos = linienPos;
            ziel = station;
        }

        public Vec2d getLinienPos() {
            return linienPos;
        }

        public void setLinienPos(Vec2d linienPos) {
            this.linienPos = linienPos;
        }

        public Station getStartBahnhof() {
            return Station.this;
        }

        public Station getZielBahnhof() {
            return ziel;
        }
    }

    private class BahnhofTeil implements Iterable<Platform> {

        private LabelContainer bahnhofsLabel;
        private List<Platform> bahnsteige;

        public BahnhofTeil(LabelContainer bahnhofsLabel,
                List<Platform> bahnsteige) {
            this.bahnhofsLabel = bahnhofsLabel;
            this.bahnsteige = bahnsteige;

        }

        void hervorheben() {
            for (Platform b : bahnsteige) {
                b.hebeHervor();
            }
        }

        public int getBahnsteigSize() {
            return bahnsteige.size();
        }

        public Iterator<Platform> iterator() {
            return bahnsteige.iterator();
        }
    }
}
