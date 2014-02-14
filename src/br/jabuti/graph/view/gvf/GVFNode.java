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
 * This class defines a Node in a graph within the Graph 
 * Visualization Framework.
 * It can be used directly or subclassed to include application
 * specific behavior.
 * 
 * @author Plinio Vilela
 * @version 1.0 Aug 2001
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import br.jabuti.graph.GraphNode;
import br.jabuti.project.ClassMethod;
import br.jabuti.util.ToolConstants;


public class GVFNode implements GVFDisplayable {

    protected GraphNode gn;
    protected ClassMethod cm;
    
    protected String id;

    protected int X = 0; 
    protected int Y = 0;
    protected int height = 36;
    protected int width = 36;
    protected Shape shape;
    protected boolean isSelected = false;

    private Color selectedColor = Color.red;
    private Color unselectedColor = Color.black;
    protected Color color;

    protected String mySource = new String("");

    public String myName = new String("");
    
    public GVFNode(GraphNode g, ClassMethod m) {
        gn = g;
        cm = m;
    	
        id = gn.getLabel();
        color = unselectedColor;
        shape = new Rectangle(X, Y, width, height);
    }

    public void setName(String name) {
        myName = name;
    }

    public String getName() {
        return myName;
    }

    public void setSource(String sc) {
        mySource = sc;
    }

    public String getSource() {
        if (mySource.equals("")) {
            if (myName.equals("")) {
                return id;
            } else {
                return myName;
            }
        }
        return mySource;
    }

    public void setShape(Shape s) {
        shape = s;
    }

    public Rectangle getBounding() {
        return shape.getBounds();
    }

    protected void updateLocation() {
        // Should have one "if" for each type of shape...
        if (shape instanceof Rectangle) {
            ((Rectangle) shape).setLocation(X - ((int) width / 2), Y - ((int) height / 2));
        }
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public int offset() {
        return (int) height / 2;
    }

    public GraphNode getGraphNode() {
        return gn;
    }

    public ClassMethod getClassMethod() {
        return cm;
    }
	
    public MethodGen getMethodGen() {
        return cm.getMethodGen();
    }

    public ConstantPoolGen getConstantPoolGen() {
        return cm.getConstantPoolGen();
    }
	
    // -------------
    // Methods to implement Displayable:

    public void moveTo(int x, int y) {
        X = x+ADJUST_X;
        Y = y+ADJUST_Y;
    }

    public void translate(int deltaX, int deltaY) {
        X += deltaX;
        Y += deltaY;
    }

    public boolean itsMe(int x, int y) {
        updateLocation();
        return (shape.contains((double) x, (double) y));
    }

    public boolean itsMe(Rectangle r) {
        // Tests if this element is inside the Rectangle
        updateLocation();
        return (r.contains(shape.getBounds2D()));
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
        
        Rectangle2D rect = shape.getBounds2D();

        circle.setFrame(rect);
        
        Graphics2D g2 = (Graphics2D) g;
        
        Stroke normalStroke = g2.getStroke();

        g2.setStroke(new BasicStroke(2.0F));
        
        g2.draw(circle);
        
        g2.setColor(ToolConstants.getColor(c));
        g2.fill(circle);        
        g2.setColor(color);

        g2.setStroke(normalStroke);
		
        drawLabel(g);
    }
    
    public void drawLabel(Graphics g) {
        Graphics2D g2 = (Graphics2D) g; // Initialized elsewhere
        Font f = new Font("Times New Roman", Font.BOLD, 14);
		
        String message = getSource();  // The text to measure and display
        Rectangle2D box = shape.getBounds2D(); // The display box: initialized elsewhere
		
        // Measure the font and the message
        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D bounds = f.getStringBounds(message, frc);
        LineMetrics metrics = f.getLineMetrics(message, frc);
        float width = (float) bounds.getWidth();     // The width of our text
        float lineheight = metrics.getHeight();      // Total line height
        float ascent = metrics.getAscent();          // Top of text to baseline
		
        // Now display the message centered horizontally and vertically in box
        float x0 = (float) (box.getX() + (box.getWidth() - width) / 2);
        float y0 = (float) (box.getY() + (box.getHeight() - lineheight) / 2
                + ascent);

        g2.setFont(f);
        g2.drawString(message, x0, y0);
    }

    public void selected(boolean s) {
        isSelected = s;
        if (s) {
            color = selectedColor;
        } else {
            color = unselectedColor;
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean contains(int x, int y) {
        return ((Shape) shape).contains(x, y);
    }

    public String getLabel() {
        return mySource;
    }
    
    public void updateShapeSize(Graphics g) {
    	
        Graphics2D g2 = (Graphics2D) g; // Initialized elsewhere
        Font f = new Font("Times New Roman", Font.BOLD, 14);
		
        String message = getSource();  // The text to measure and display
        // Measure the font and the message
        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D bounds = f.getStringBounds(message, frc);
        float width = (float) bounds.getWidth();     // The width of our text
    	
        if (width >= (this.width - (this.height / 3))) {
            this.width = (int) width + (this.height / 3);
            shape = new Rectangle(X, Y, this.width, height);
        }
    }
}

