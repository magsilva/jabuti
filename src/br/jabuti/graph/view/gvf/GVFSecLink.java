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
 * This class defines a link (an arc or branch) between two
 * Nodes in a graph within the Graph Visualization Framework.
 * It can be used directly or subclassed to include application
 * specific behavior.
 * 
 * @author Plinio Vilela
 * @version 1.0 Aug 2001
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;


public class GVFSecLink extends GVFLink {

	private float[] dashPattern = { 4F, 4F };

    private GVFNode ini;
    private GVFNode fin;

    public GVFSecLink(GVFNode n1, GVFNode n2) {
        super(n1, n2);
    	
        ini = getSourceNode();
        fin = getDestinationNode();
    }

    public void selected(boolean s) {
        selected = s;
        if (s) {
            color = selectedColor;
        } else {
            color = unselectedColor;
        }
    }

    public Color getColor() {
    	if ( CFGFrame.showSecondary() )
        	return color;
        else
        	return CFGFrame.getBkColor();
    }



 /*   protected void drawDirectedLine(Graphics g, int x1, int y1, int x2, int y2) {
        int x3;
        int y3;
        int x4;
        int y4;
        double angle;
		
        // Drawing the line
        Graphics2D g2 = (Graphics2D) g;
        
        Stroke normalStroke = g2.getStroke();

        g2.setStroke(new BasicStroke(1.5F, 
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0F,
                dashPattern,
                0));

        g2.drawLine(x1, y1, x2, y2);
	
        // calculate points for arrowhead
        angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI;
	
        x3 = (int) (x2 + Math.cos(angle - theta) * size);
        y3 = (int) (y2 + Math.sin(angle - theta) * size);
	
        x4 = (int) (x2 + Math.cos(angle + theta) * size);
        y4 = (int) (y2 + Math.sin(angle + theta) * size);
	
        // draw arrowhead
        g2.drawLine(x2, y2, x3, y3);
        g2.drawLine(x2, y2, x4, y4);
        g2.drawLine(x3, y3, x4, y4);
        
        g2.setStroke(normalStroke);        
    }
    
    */
    
    protected void drawShape(Graphics g, Shape sh) {
		Graphics2D g2 = (Graphics2D) g;
        
        Stroke normalStroke = g2.getStroke();


        g2.setStroke(new BasicStroke(1.5F, 
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0F,
                dashPattern,
                0));
        
        g2.draw(sh);
        
        g2.setStroke(normalStroke);        
    }

}
