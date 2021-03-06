/*
 * Bpp - A Bin Packer in Java
 *
 * Copyright (C) 2012  Daniel Wagner <daniel@wagners.name>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 */
package name.wagners.bpp;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * The Dna class.
 */
@Slf4j
public class Dna extends Object implements Cloneable {

	private static final double PROPABILITY = 0.5;

	public static Dna recombineVariantA(Dna a, Dna b) {
		Dna c;
		int[] n = new int[Bpp.n];
		int[] as = a.toArray(), bs = b.toArray();

		int x = Bpp.rand.nextInt(Bpp.n);
		int y = Bpp.rand.nextInt(Bpp.n);
		int i;

		if (x > y) {
			int h = x;
			x = y;
			y = h;
		}

		// Chromosome vor dem 1. Crossover-Punkt von a nach c �bernehmen
		for (i = 0; i < x; ++i) {
			n[i] = as[i];
		}

		// Im Crossover-Bereich von b nach c �bernehmen
		for (; i < y; ++i) {
			n[i] = bs[i];
		}

		// Restliche Chromosome aus a nach c �bernehmen
		for (; i < Bpp.n; ++i) {
			n[i] = as[i];
		}

		// Die so erzeugte L�sung wird jetzt wieder in eine Dna verpackt
		c = new Dna(n);

		// Repair the solution.
		c.repair();

		return (c);
	}

	public static Dna recombineVariantB(Dna a, Dna b) {
		Dna c = new Dna(false);

		int i, as, bs;

		as = a.dna.size();
		bs = b.dna.size();

		// Decide from which Dna each Chromosome is chosen.
		i = 0;
		while ((i < as) && (i < bs)) {
			try {
				if (Bpp.rand.nextDouble() < PROPABILITY) {
					c.dna.add((Chromosome) a.dna.get(i).clone());
				} else {
					c.dna.add((Chromosome) b.dna.get(i).clone());
				}

			} catch (CloneNotSupportedException e) {
				log.error("Could not clone an object.", e);
			}
		}

		// Repair the solution.
		c.repair();

		return (c);
	}

	public List<Chromosome> dna;

	public Dna(final boolean init) {
		super();

		dna = new ArrayList<Chromosome>();

		if (init) {
			initDna();
		}
	}

	public Dna(final int[] a) {
		super();

		Chromosome c = new Chromosome();
		dna = new ArrayList<Chromosome>();

		for (int j = 0; j < Bpp.n; j++) {
			if (c.weight() + Bpp.instance.data[a[j]] > Bpp.wmax) {
				dna.add(c);
				c = new Chromosome();
			}

			c.add(a[j]);
		}

		dna.add(c);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		return super.clone();

		/**
		 * old version Chromosome c = new Chromosome();
		 *
		 * Dna d = new Dna(false);
		 *
		 * for (int j = 0; j < dna.size(); ++j) { d.dna = (Vector<Chromosome>)
		 * dna.clone(); }
		 *
		 * return (d);
		 */
	}

	/**
	 * Computes the fitness.
	 *
	 * @return Objective value.
	 */
	public double fitness() {
		double g = 0.0;

		for (int j = 0; j < dna.size(); j++) {
			g += Math.pow(dna.get(j).weight() / Bpp.wmax, 2);
		}

		g = g / dna.size();

		return (g);
	}

	private void initDna() {
		Pool pool = new Pool(Bpp.n);
		Chromosome c = new Chromosome();

		dna.clear();

		for (int j = 0; j < Bpp.n; j++) {
			try {
				int i = pool.nextInt();

				if (c.weight() + Bpp.instance.data[i] > Bpp.wmax) {
					dna.add(c);
					c = new Chromosome();
				}

				c.add(i);
			} catch (Exception e) {
				log.error("Could not initialize Dna.", e);
			}
		}

		dna.add(c);
	}

	public void mutate() {
		/**
		 * Zuf�llig zwei Chromosome ausw�hlen, von jedem Gen zuf�llig ein
		 * Chromosm ausw�hlen falls die ausgew�hlten Gene im anderen Chromosome
		 * noch Platz haben werden diese vertauscht
		 */

		Chromosome ca = dna.get(Bpp.rand.nextInt(dna.size()));
		Chromosome cb = dna.get(Bpp.rand.nextInt(dna.size()));

		if (ca != cb) {
			// Nur bei verschiedenen Chromosomen mutieren

			// Jetzt zwei zuf�llige Gene ausw�hlen
			int ga = ca.get(Bpp.rand.nextInt(ca.size()));
			int gb = cb.get(Bpp.rand.nextInt(cb.size()));

			if ((Bpp.instance.data[ga] + cb.weight() - Bpp.instance.data[gb] <= Bpp.wmax)
					&& (Bpp.instance.data[gb] + ca.weight()
							- Bpp.instance.data[ga] <= Bpp.wmax)) {
				ca.remove(ga);
				ca.add(gb);

				cb.remove(gb);
				cb.add(ga);
			}
		}
	}

	/**
	 * This method repairs a infeasible solution.
	 */
	public void repair() {
		Chromosome c;
		boolean[] f = new boolean[Bpp.n];
		int i, j, l;

		// doppelte entfernen
		for (i = 0; i < dna.size(); ++i) {
			c = dna.get(i);

			j = 0;
			while (j < c.size()) {
				l = c.get(j);

				if (f[l]) {
					c.remove(l);
				} else {
					j++;
				}

				f[l] = true;
			}
		}

		// leere chromosome entfernen
		i = 0;
		while (i < dna.size()) {
			c = dna.get(i);

			if (c.size() == 0) {
				dna.remove(c);
			} else {
				i++;
			}
		}

		// fehlende wieder einfügen
		for (i = 0; i < Bpp.n; ++i) {
			if (!f[i]) {
				c = null;

				for (j = 0; j < dna.size(); ++j) {
					c = dna.get(j);

					if (c.weight() + Bpp.instance.data[i] <= Bpp.wmax) {
						break;
					}
				}

				if ((c == null)
						|| (c.weight() + Bpp.instance.data[i] > Bpp.wmax)) {
					c = new Chromosome();
					dna.add(c);
				}

				c.add(i);
			}
		}
	}

	public int[] toArray() {
		int[] a = new int[Bpp.n];
		int[] b;
		int l = 0;

		for (int j = 0; j < dna.size(); ++j) {
			b = dna.get(j).toArray();

			for (int i = 0; i < b.length; ++i) {
				a[l] = b[i];
				++l;
			}
		}

		return a;
	}

	@Override
	public String toString() {
		String out;
		Chromosome c;

		out = "Fitness: " + fitness() + ", Size: " + dna.size() + ", Dna: ";

		for (int j = 0; j < dna.size(); ++j) {
			c = dna.get(j);

			if (j > 0) {
				out += " | " + c.toString();
			} else {
				out += c.toString();
			}
		}

		return (out);
	}

	public double weight() {
		double g = 0.0;

		for (int j = 0; j < dna.size(); j++) {
			g += (dna.get(j)).weight();
		}

		return (g);
	}
}
