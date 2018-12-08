package com.gleisbelegung.lib.data;

import de.heidelbach_net.util.XML;

import java.util.*;
import java.util.function.Consumer;


/**
 * @author interpret
 * @license LGPLv2
 */
public class Trainlist implements Iterable<Train> {

    private final Map<Integer, Train> idMap = new HashMap<>();
    /**
     * Contains the idMap of last updateIntervall() run to prevent re-creating trains for which the sim
     * already sent a notification that the train does not exist.
     */
    private final Map<Integer, Train> history = new HashMap<>();
    /**
     * Contains all trains with no name
     */
    private final Map<Integer, Train> dummyIdMap = new HashMap<>();
    private final Set<TrainListListener> listeners = new HashSet<>();

    private Trainlist() {
    }

    /**
     * Creates a new trainlist by parsing given XML describing a trainlist in
     * XML notation.
     *
     * @param handler instance of caller
     * @param xml     the trainlist to parse
     * @return parsed trainlist
     */
    public static Trainlist parse(final XML xml) {
        if (!xml.getKey().equals("zugliste")) {
            throw new IllegalArgumentException();
        }
        final List<XML> trainsXML = xml.getInternXML();
        final Trainlist list = new Trainlist();
        for (final XML trainXML : trainsXML) {
            final Train train = Train.parseXml(trainXML);
            list.idMap.put(train.getTrainId(), train);
        }

        return list;
    }

    public synchronized Train get(final Integer id) {
        final Train t = this.idMap.get(id);
        if (t != null)
            return t;
        return this.dummyIdMap.get(id);
    }

    public Train get(final String id) {
        return get(Integer.valueOf(id));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Iterable#iterator()
     */
    @Override public Iterator<Train> iterator() {
        return this.idMap.values().iterator();
    }

    synchronized void purge(final Integer id) {
        remove(id);
        this.history.remove(id);
    }

    public synchronized void remove(final Integer id) {
        final Train old = this.idMap.remove(id);


        if (old != null) {
            Consumer<TrainListListener> notifier =
                    new Consumer<TrainListListener>() {

                        @Override public void accept(TrainListListener t) {
                            t.notifyRemoved(old);
                        }
                    };
            listeners.forEach(notifier);
        }
        this.history.put(id, old);
    }

    public void remove(final Train train) {
        remove(train.getTrainId());
    }

    public synchronized Collection<Train> getZuege() {
        return Collections
                .unmodifiableCollection(new HashSet<>(this.idMap.values()));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        return this.idMap.values().toString();
    }

    public final synchronized void update(final XML trainlistXML) {
        final List<XML> trainsXML = trainlistXML.getInternXML();
        final Map<Integer, Train> oldMap = this.idMap;
        final Map<Integer, Train> newMap = new HashMap<>();
        final ArrayDeque<Train> toRemove = new ArrayDeque<>();
        this.dummyIdMap.clear();
        final Map<Integer, Train> historyOld = new HashMap<>(this.history);
        this.history.clear();
        for (final XML trainXML : trainsXML) {
            final Integer id = Integer.valueOf(trainXML.get("zid"));
            final Train train;
            if (oldMap.containsKey(id)) {
                train = oldMap.remove(id);
            } else if (historyOld.containsKey(id)) {
                // removed by event, but updateIntervall of trainlist still shows this
                // train
                this.history.put(id, historyOld.remove(id));
                continue;
            } else {
                train = Train.parseXml(trainXML);
            }
            newMap.put(id, train);
        }
        for (final Integer id : oldMap.keySet()) {
            final Train old = oldMap.get(id);
            toRemove.add(old);
        }
        for (final Train t : toRemove) {
            final Integer id = t.getTrainId();
            if (id.intValue() > 0) {
                remove(id);
            } else {
                purge(id);
            }
        }
        assert oldMap.isEmpty();
        this.idMap.clear();
        this.idMap.putAll(newMap);
    }

    public boolean isEmpty() {
        return this.idMap.isEmpty();
    }

    public void addListener(TrainListListener listener) {
        // TODO Auto-generated method stub

    }

    public void removeListener(TrainListListener listener) {
        // TODO Auto-generated method stub

    }

    public synchronized Map<Integer, Train> toMap() {
        return new HashMap<>(this.idMap);
    }

}
