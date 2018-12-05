package com.gleisbelegung.lib.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import de.heidelbach_net.util.XML;


/**
 * @author interpret
 * @license LGPLv2
 */
public class Trainlist implements Iterable<Zug> {

	/**
	 * Creates a new trainlist by parsing given XML describing a trainlist in
	 * XML notation.
	 *
	 * @param handler
	 *            instance of caller
	 * @param xml
	 *            the trainlist to parse
	 * @return parsed trainlist
	 */
	public static Trainlist parse(final XML xml) {
		if (!xml.getKey().equals("zugliste")) {
			throw new IllegalArgumentException();
		}
		final List<XML> trainsXML = xml.getInternXML();
		final Trainlist list = new Trainlist();
		for (final XML trainXML : trainsXML) {
			final Zug train = Zug.parseXml(trainXML);
			list.idMap.put(train.getZugId(), train);
		}

		return list;
	}

	private final Map<Integer,Zug> idMap = new HashMap<>();
	
	/**
	 * Contains the idMap of last update() run to prevent re-creating trains for which the sim 
	 * already sent a notification that the train does not exist.
	 */
	private final Map<Integer, Zug> history = new HashMap<>();
	
	/**
	 * Contains all trains with no name
	 */
	private final Map<Integer, Zug> dummyIdMap = new HashMap<>();
	private final Set<TrainListListener> listeners = new HashSet<>();

	private Trainlist() {
	}

	public synchronized Zug get(final Integer id) {
		final Zug t = this.idMap.get(id);
		if (t != null)
			return t;
		return this.dummyIdMap.get(id);
	}

	public Zug get(final String id) {
		return get(Integer.valueOf(id));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Zug> iterator() {
		return this.idMap.values().iterator();
	}

	synchronized void purge(final Integer id) {
		remove(id);
		this.history.remove(id);
	}
	
	public synchronized void remove(final Integer id) {
		final Zug old = this.idMap.remove(id);

		
		if (old != null) {
			Consumer<TrainListListener> notifier = new Consumer<TrainListListener>() {

				@Override
				public void accept(TrainListListener t) {
					t.notifyRemoved(old);
				}
			};
			listeners.forEach(notifier);
		}
		this.history.put(id, old);
	}

	public void remove(final Zug train) {
		remove(train.getZugId());
	}

	public synchronized Collection<Zug> getZuege() {
		return Collections.unmodifiableCollection(new HashSet<>(this.idMap.values()));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.idMap.values().toString();
	}

	public final synchronized void update(final XML trainlistXML) {
		final List<XML> trainsXML = trainlistXML.getInternXML();
		final Map<Integer, Zug> oldMap = this.idMap;
		final Map<Integer, Zug> newMap = new HashMap<>();
		final ArrayDeque<Zug> toRemove = new ArrayDeque<>();
		this.dummyIdMap.clear();
		final Map<Integer, Zug> historyOld = new HashMap<>(this.history);
		this.history.clear();
		for (final XML trainXML : trainsXML) {
			final Integer id = Integer.valueOf(trainXML.get("zid"));
			final Zug train;
			if (oldMap.containsKey(id)) {
				train = oldMap.remove(id);
			} else if (historyOld.containsKey(id)) {
				// removed by event, but update of trainlist still shows this
				// train
				this.history.put(id, historyOld.remove(id));
				continue;
			} else {
				train = Zug.parseXml(trainXML);
			}
			newMap.put(id, train);
		}
		for (final Integer id : oldMap.keySet()) {
			final Zug old = oldMap.get(id);
			toRemove.add(old);
		}
		for (final Zug t : toRemove) {
			final Integer id = t.getZugId();
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

	public synchronized Map<Integer, Zug> toMap() {
		return new HashMap<>(this.idMap);
	}

}
