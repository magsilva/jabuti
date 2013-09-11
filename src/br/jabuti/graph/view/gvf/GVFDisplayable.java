/*  Copyright 2003  Auri Marcelo Rizzo Vicenzi, Marcio Eduardo Delamaro, 			    Jose Carlos Maldonado

    This file is part of Jabuti.

    Jabuti is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as 
    published by the Free Software Foundation, either version 3 of the      
    License, or (at your option) any later version.

    Jabuti is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Jabuti.  If not, see <http://www.gnu.org/licenses/>.
*/


package br.jabuti.graph.view.gvf;


/**
 * Interface to allow Objects to be visualized and manipulated on screen.
 * It should be implemented by any Object that will eventually be
 * visualized by the Graph Visualization Framework.
 *
 * @author  Plinio Vilela
 * @version 1.0
 */

import java.awt.Graphics;
import java.awt.Rectangle;


public interface GVFDisplayable {

	static public final int ADJUST_X = 50,
							ADJUST_Y = 50;
    /**
     * Move the Object to a particular (X,Y) position.
     */
    public void moveTo(int X, int Y);

    /**
     * Move the Object a certain distance from its original position.
     */
    public void translate(int deltaX, int deltaY);
 
    /**
     * Return TRUE if the point (X,Y) is within the boundaries of the Object.
     */
    public boolean itsMe(int X, int Y);

    /**
     * Return TRUE if the Object is within the boundaries of the Rectangle.
     */
    public boolean itsMe(Rectangle r);

    /**
     * Method the defines how the Object should be drawn.
     */
    public void draw(Graphics g);

    /**
     * Changes the selected state of the Object.
     */
    public void selected(boolean s);

    /**
     * Returns TRUE if the Object is currently selected, FALSE otherwise.
     */
    public boolean isSelected();

    /**
     * Returns TRUE if the Object contains the point (X,Y).
     */
    public boolean contains(int x, int y);

    /**
     * Returns a String with the Object's Label.
     */
    public String getLabel();
}
