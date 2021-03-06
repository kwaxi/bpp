/*
 * Bpp - A Bin Packer in Java
 *
 * Copyright (C) 2014  Daniel Wagner
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
 */
package name.wagners.bpp.selection;

import name.wagners.bpp.Dna;

/**
 * Interface for selection algorithms.
 */
public interface Selection {

	/**
	 * Selection of new population out of an existing one.
	 *
	 * @param pop
	 *            The input population.
	 * @return The resulting population.
	 */
	Dna[] select(final Dna[] pop);

}
