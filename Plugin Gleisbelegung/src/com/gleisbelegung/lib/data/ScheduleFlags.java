package com.gleisbelegung.lib.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.heidelbach_net.util.XML;


public class ScheduleFlags {

	public static final ScheduleFlags EMPTY = new ScheduleFlags();

	public static ScheduleFlags parse(final XML xml, final Zug Zug,
			final Map<Integer, Zug> Zugs) {
		if (!xml.getKey().equals("gleis")) {
			throw new IllegalArgumentException();
		}
		final ScheduleFlags flags = new ScheduleFlags(xml.get("flags"), Zug,
				Zugs, xml.get("plan"), Integer.parseInt(xml.get("ab")));
		return flags;
	}

	private boolean r;

	private boolean d;
	private boolean a;
	private boolean w, l; // either or semantics, not both may be active

	private Zug k;
	private Zug k_reverse; // helper Flag of k, train with this flag is target of k
	private Zug e;
	private Zug f;
	private Integer eId;
	private Integer fId;
	private Integer kId;

	private ScheduleFlags() {
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (this.d) {
			sb.append("D");
		}
		if (this.r) {
			sb.append("R");
		}
		if (this.a) {
			sb.append("A");
		}
		if (this.l) {
			sb.append("L");
		}
		if (this.w) {
			sb.append("W");
		}
		if (this.e != null) {
			sb.append("E");
		}
		if (this.k != null) {
			sb.append("K");
		}
		if (this.k_reverse != null) {
			sb.append("G");
		}
		if (this.f != null) {
			sb.append("F");
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param init
	 * @param Zug
	 * @param zugs
	 * @param string
	 * @param arrival
	 */
	public ScheduleFlags(final String init, final Zug Zug,
			final Map<Integer, Zug> zugs, final String string, long arrival) {
		for (int i = 0; i < init.length(); ++i) {
			switch (init.charAt(i)) {
			case 'A':
				this.a = true;
				break;
			case 'D':
				this.d = true;
				break;
			case 'R':
				this.r = true;
				while(i + 1< init.length() && init.charAt(i + 1) >= '0' && init.charAt(i + 1) <= '9') {
					i++;
				}
				break;
				
			case 'W':
			case 'L':
				switch (init.charAt(i)) {
				case 'W':
					this.w = true;
					final int startW1Pos = init.indexOf('[', ++i);
					final int startW2Pos = init.indexOf('[', startW1Pos + 1);
					final int endWPos = init.indexOf(']', startW2Pos + 1);
					i = endWPos;
					break;
				case 'L':
					this.l = true;
					break;
				default:
				}

				break;

			case 'F':
				final int startFPos = init.indexOf('(', ++i) + 1;
				final int endFPos = init.indexOf(')', startFPos);
				final String f = init.substring(startFPos, endFPos);
				this.fId = Integer.valueOf(f);
				this.f = zugs.get(this.fId);
				if (this.f == null) {
				} else {
					this.f.setNachfolger(Zug);
				}
				i = endFPos;
				continue;

			case 'K':
				final int startKPos = init.indexOf('(', ++i) + 1;
				final int endKPos = init.indexOf(')', startKPos);
				final String k = init.substring(startKPos, endKPos);
				this.kId = Integer.valueOf(k);
				this.k = zugs.get(this.kId);
				if (this.k == null) {
				} else {
					Zug.setNachfolger(this.k);
					final List<FahrplanHalt> kSchedule = this.k.getFahrplan();
					FahrplanHalt lastMatch = null;
					if (kSchedule == null) {
					} else {
						for (final Iterator<FahrplanHalt> iter =
								kSchedule.iterator(); iter.hasNext();) {
							final FahrplanHalt kse = iter.next();
							if (kse.getPlanBahnsteig().equals(string)) {
								final long kDep = kse.getAbfahrt();
								if (kDep < arrival) {
									break;
								}
								lastMatch = kse;
							}
						}
					}
					if (lastMatch != null) {
						lastMatch.getFlags().k_reverse = Zug;
					}
				}
				i = endKPos;
				continue;

			case 'E':
				final int startEPos = init.indexOf('(', ++i) + 1;
				final int endEPos = init.indexOf(')', startEPos);
				final String e = init.substring(startEPos, endEPos);
				this.eId = Integer.valueOf(e);
				this.e = zugs.get(this.eId);
				if (this.e == null) {
				} else {
					Zug.setNachfolger(this.e);
					this.e.setNachfolger(Zug);
				}
				i = endEPos;
				continue;

			case 'B':
				// no relevance, multi-flag
				while (i + 1 < init.length()) {
					final char next = init.charAt(i + 1);
					if ((next >= '0') && (next <= '9')) {
						++i;
					} else {
						break;
					}
				}
				continue;

			case 'P':
				// no relevance, Train setup on game start on this plattform
				if (init.length() > i + 1) {
					if (init.charAt(i + 1) == '[') {
						i = init.indexOf(']', i);
					}
				}
				continue;
				
			case '[':
				if (init.length() > i + 1) {
					i = init.indexOf(']', i);
				}
				continue;
				
			default:
				System.err.println("unparsed flag " + init.charAt(i));
			}
		}
	}

	/**
	 * Namenaenderung
	 */
	public Zug getE() {
		return this.e;
	}

	/**
	 * Fluegelung
	 */
	public Zug getF() {
		return this.f;
	}

	/**
	 * Kupplung
	 */
	public Zug getK() {
		return this.k;
	}

	/**
	 * Lokwechsel
	 */
	public boolean getL() {
		return this.l;
	}
	
	public boolean getR()
	{
		return this.r;
	}

	/**
	 * Lokumsetzung
	 */
	public boolean getW() {
		return this.w;
	}

	public Integer getsEID() {
		return this.eId;
	}

	/**
	 * Durchfahrt
	 */
	public boolean getD() {
		return this.d;
	}

	public void invalidateK(final Zug Zug) {
		if (this.k_reverse == Zug) {
			this.k_reverse = null;
			if (Zug.getZugId() < 0) {
				this.l = this.w = false;
			}
		}
		
	}
}
