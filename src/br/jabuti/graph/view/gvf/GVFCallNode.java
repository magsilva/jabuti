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
 * This class represents the CFGCallNode (square and circle). 
 * It defines a CallNode in a graph within the Graph Visualization 
 * Framework.
 * It can be used directly or subclassed to include application
 * specific behavior.
 * 
 * @author Plinio Vilela
 * @version 1.0 Aug 2001
 */

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import br.jabuti.graph.GraphNode;
import br.jabuti.project.ClassMethod;
import br.jabuti.util.ToolConstants;


public class GVFCallNode extends GVFNode {

    public GVFCallNode(GraphNode gn, ClassMethod m) {
        super(gn, m);
    }

    public void draw(Graphics g) {
        int c = 0;

        // Calculate the new size of the node
        // w.r.t. its label.
        updateShapeSize(g);
    	
        updateLocation();
        g.clearRect(X - ((int) width / 2), Y - ((int) height / 2), width, height);

        g.setColor(color);

        Integer colorNumber = (Integer) gn.getUserData(ToolConstants.LABEL_COLOR);

        if (colorNumber != null) {
            c = colorNumber.intValue();
        }

        // Changing a Rectangle by a Circle (Ellipse)
        Ellipse2D.Float circle = new Ellipse2D.Float();
        Ellipse2D.Float circle2 = new Ellipse2D.Float();
        
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setStroke(new BasicStroke(2.0F));

        Rectangle2D rect = shape.getBounds2D();

        circle.setFrame(rect);
                
        g2.draw(circle);
        
        g2.setColor(ToolConstants.getColor(c));
        g2.fill(circle);        
        g2.setColor(color);
		
        circle2.setFrameFromCenter(rect.getCenterX(), rect.getCenterY(), rect.getX() + 3, rect.getY() + 3);
        g2.draw(circle2);
		
        g2.setColor(ToolConstants.getColor(c));
        g2.fill(circle2);        
        g2.setColor(color);
		
        drawLabel(g);
    }

}

