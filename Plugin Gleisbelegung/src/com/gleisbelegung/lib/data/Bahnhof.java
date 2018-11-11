package com.gleisbelegung.lib.data;

import com.gleisbelegung.Einstellungen;
import com.gleisbelegung.LabelContainer;
import com.sun.javafx.geom.Vec2d;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class Bahnhof {
    /**
     * Vebindung zwischen Bahnhof.this and BahnhofVerbindung.ziel
     */
    private class BahnhofVerbindung {
        private double laenge; // dead variable
        private Line linie;
        private Vec2d linienPos;
        private Bahnhof ziel;

        public BahnhofVerbindung(Bahnhof bahnhof, double laenge, Line linie, Vec2d linienPos) {
            this.laenge = laenge;
            this.linie = linie;
            this.linienPos = linienPos;
            ziel = bahnhof;
        }

        public void setLinienPos(Vec2d linienPos) {
            this.linienPos = linienPos;
        }

        public Vec2d getLinienPos() {
            return linienPos;
        }

        public Bahnhof getStartBahnhof() {
            return Bahnhof.this;
        }

        public Bahnhof getZielBahnhof() {
            return ziel;
        }
    }

    private class BahnhofTeil implements Iterable<Bahnsteig> {
        private LabelContainer bahnhofsLabel;
        private List<Bahnsteig> bahnsteige;

        public BahnhofTeil(LabelContainer bahnhofsLabel, List<Bahnsteig> bahnsteige) {
            this.bahnhofsLabel = bahnhofsLabel;
            this.bahnsteige = bahnsteige;

        }

        void hervorheben() {
            for (Bahnsteig b : bahnsteige) {
                b.hebeHervor();
            }
        }

        public int getBahnsteigSize() {
            return bahnsteige.size();
        }

        public Iterator<Bahnsteig> iterator() {
            return bahnsteige.iterator();
        }
    }

    private int id;
    private String name;
    private String alternativName = "";
    private Map<String, Bahnsteig> bahnsteige = new HashMap<>();
    private List<Bahnsteig> bahnsteigListe = new ArrayList<>();
    private List<BahnhofTeil> bahnhofTeile = new ArrayList<>();
    private Map<Integer, BahnhofVerbindung> bahnhofVerbindungen = new HashMap<>();
    private boolean sichtbar;
    private Vec2d pos;
    private Bahnsteig lastBahnsteig;

    public Bahnhof(int id, String name) {
        this.id = id;
        this.name = name;
        pos = new Vec2d(30, id * 40 + 40);
    }

    private BahnhofVerbindung getVerbindung(Bahnhof bahnhof) {
        synchronized (bahnhofVerbindungen) {
            return bahnhofVerbindungen.get(Integer.valueOf(bahnhof.id));
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Collection<Bahnsteig> getBahnsteige() {
        synchronized (bahnsteigListe) {
            return Collections.unmodifiableList(new ArrayList<>(bahnsteigListe));
        }
    }

    public Bahnsteig getBahnsteig(int index) {
        synchronized (bahnsteigListe) {
            return bahnsteigListe.get(index);
        }
    }

    public int getAnzahlBahnsteige() {
        synchronized (bahnsteige) {
            return bahnsteige.size();
        }
    }

    public boolean isSichtbar() {
        synchronized (bahnsteige) {
            for (Bahnsteig b : bahnsteige.values()) {
                if (!b.isSichtbar())
                    return false;

            }
        }
        return true;
    }

    public void setSichtbar(boolean sichtbar) {
        synchronized (bahnsteige) {
            for (Bahnsteig b : bahnsteige.values())
                b.setSichtbar(sichtbar);
        }
    }

    public List<BahnhofTeil> getBahnhofTeile() {
        synchronized (bahnhofTeile) {
            return Collections.unmodifiableList(bahnhofTeile);
        }
    }

    public void addBahnhofLabel(LabelContainer bahnhofLabel, List<Bahnsteig> bahnsteige) {
        BahnhofTeil bt = new BahnhofTeil(bahnhofLabel, bahnsteige);
        synchronized (bahnhofTeile) {
            bahnhofTeile.add(bt);
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

    public String getAlternativName() {
        return alternativName;
    }

    private void einstellungen(BahnhofTeil bt) {
        Einstellungen.fenster.gleisbelegung.zeigeOrderIds();
        Stage stage = new Stage();

        Label name = new Label("Name:");
        name.setStyle("-fx-text-fill: white;");
        name.setFont(Font.font(Einstellungen.schriftgroesse));
        name.setTranslateY(25);
        name.setTranslateX(25);

        TextField tname = new TextField(alternativName);
        tname.setFont(Font.font(Einstellungen.schriftgroesse - 3));
        tname.setTranslateX(25);
        tname.setTranslateY(60);

        Label l = new Label("Reihenfolge festlegen:");
        l.setStyle("-fx-text-fill: white;");
        l.setFont(Font.font(Einstellungen.schriftgroesse));
        l.setTranslateX(25);
        l.setTranslateY(95);

        TextField tf = new TextField(String.valueOf(bahnsteige.entrySet().iterator().next().getValue().getOrderId() + 1));
        tf.setFont(Font.font(Einstellungen.schriftgroesse - 3));
        tf.setTranslateX(25);
        tf.setTranslateY(130);

        Button b = new Button("Speichern");
        b.setFont(Font.font(Einstellungen.schriftgroesse));
        b.setTranslateX(25);
        b.setTranslateY(190);
        b.setOnAction(e -> {
            int order = Integer.parseInt(tf.getText()) - 1;
            for (Bahnsteig ba : bt) {
                ba.setOrderId(order);
                alternativName = tname.getText();
            }

            stage.close();
            Einstellungen.fenster.gleisbelegung.versteckeOrderIds();
            Einstellungen.fenster.gleisbelegung.sortiereGleise();
            Einstellungen.fenster.stellwerksuebersicht.aktualisiereBahnhofsNamen();
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
            Einstellungen.fenster.gleisbelegung.versteckeOrderIds();
        });
    }

    @Override
    public String toString() {
        return "Bahnhof{" + "id=" + id + ", name='" + name + '\'' + ", bahnsteige=" + bahnsteige + '}';
    }

    public Vec2d getPos() {
        return pos;
    }

    public void setPos(Vec2d pos) {
        this.pos = pos;
    }

    public Collection<BahnhofVerbindung> getBahnhofVerbindungen() {
        synchronized (bahnhofVerbindungen) {
            return Collections.unmodifiableCollection(bahnhofVerbindungen.values());
        }
    }

    public void addBahnhofVerbindung(Bahnhof bahnhof, double laenge, Line linie, Vec2d linienPos) {
        BahnhofVerbindung verbindung = new BahnhofVerbindung(bahnhof, laenge, linie, linienPos);
        synchronized (bahnhofVerbindungen) {
            bahnhofVerbindungen.put(Integer.valueOf(bahnhof.id), verbindung);
        }
    }

    public Line getLinie(Bahnhof bahnhof) {
        BahnhofVerbindung bv = getVerbindung(bahnhof);

        if (bv == null) {
            return null;
        }

        return bv.linie;
    }

    public Set<Bahnhof> getVerbindungsBahnhoefe() {
        Collection<BahnhofVerbindung> bvs = getBahnhofVerbindungen();
        Set<Bahnhof> bahnhofSet = new HashSet<>();
        for (BahnhofVerbindung bv : bvs) {
            bahnhofSet.add(bv.getZielBahnhof());
        }

        return bahnhofSet;
    }

    public Vec2d getLinienPos(Bahnhof bahnhof) {
        BahnhofVerbindung bv = getVerbindung(bahnhof);
        if (bv == null) {
            return null;
        }

        return bv.getLinienPos();
    }

    public boolean setLinienPos(Bahnhof bahnhof, Vec2d linienPos) {
        BahnhofVerbindung bv = getVerbindung(bahnhof);
        if (bv == null) {
            return false;
        }

        bv.setLinienPos(linienPos);

        return true;
    }

    public Bahnsteig getBahnsteigByName(String name) {
        synchronized (bahnsteige) {
            return bahnsteige.get(name);
        }
    }

    public void addBahnsteig(Bahnsteig bahnsteig) {
        synchronized (bahnsteige) {
            this.bahnsteige.put(bahnsteig.getName(), bahnsteig);
            this.lastBahnsteig = bahnsteig;
        }
        synchronized (bahnsteigListe) {
            bahnsteigListe.add(bahnsteig);
        }
    }

    public Bahnsteig getLastBahnsteig() {
        synchronized (bahnsteige) {
            return lastBahnsteig;
        }
    }

    public void clearBahnhofVerbindungen() {
        synchronized (bahnhofVerbindungen) {
            this.bahnhofVerbindungen.clear();
        }

    }

    public SortedMap<Integer, Bahnsteig> getBahnsteigOrderMap() {
        SortedMap<Integer, Bahnsteig> bahnsteigMap = new TreeMap<>();
        synchronized (bahnsteige) {
            for (Bahnsteig bst : bahnsteige.values()) {
                bahnsteigMap.put(Integer.valueOf(bst.getOrderId()), bst);
            }
        }

        return bahnsteigMap;
    }

    public void clearBahnhofTeile() {
        synchronized (bahnhofTeile) {
            bahnhofTeile.clear();
        }

    }
}
