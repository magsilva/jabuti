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
 * This class provides basic functionality to display and interact
 * with GVFDisplayable Objects.
 *
 * @author  Plinio Vilela
 * @version 1.0
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.aspectj.apache.bcel.classfile.LineNumberTable;
import org.aspectj.apache.bcel.generic.InstructionHandle;

import br.jabuti.graph.CFGNode;
import br.jabuti.graph.GraphNode;
import br.jabuti.gui.JabutiGUI;
import br.jabuti.gui.MyJMenuItem;
import br.jabuti.gui.SelectedPoint;
import br.jabuti.gui.WeightColor;
import br.jabuti.util.ToolConstants;


class GVFDrawPanel extends JPanel implements MouseMotionListener {

    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 4514926373615887066L;
	Vector vNodes;
    Vector vLinks;

    GVFDisplayable nodeToken, oldNodeToken;
    GVFDisplayable arcToken;

    int oldX;
    int oldY;
    int selectX = 0, oX = 0;
    int selectY = 0, oY = 0;

    Vector vSelected;
    Rectangle selectedRectangle = null;
    
    private String tip = null;
    private int tipX = 0;
    private int tipY = 0;

    public GVFDrawPanel( int width, int height, Vector vN, Vector vL) {
    	
        // vNodes and vLinks are Vectors of Displayable objects.
        vNodes = vN;
        vLinks = vL;
	
        // setDoubleBuffered(true);
        setPreferredSize(new Dimension(width, height));

        addMouseListener(new MouseAdapter() {
                    public void mouseReleased(MouseEvent evt) {
                        if (selectedRectangle != null) {
                            // paintComponent(getGraphics());
                            repaint();
                            vSelected = new Vector();
                            for (Enumeration e = vLinks.elements(); e.hasMoreElements();) {
                                GVFDisplayable n = (GVFDisplayable) e.nextElement();

                                if (n.itsMe(selectedRectangle)) {
                                    vSelected.addElement(n);
                                }
                            }
                            for (Enumeration e = vNodes.elements(); e.hasMoreElements();) {
                                GVFDisplayable n = (GVFDisplayable) e.nextElement();

                                if (n.itsMe(selectedRectangle)) {
                                    vSelected.addElement(n);
                                }
                            }
                            if (vSelected != null) {
                                for (Enumeration e = vSelected.elements(); e.hasMoreElements();) {
                                    GVFDisplayable n = (GVFDisplayable) e.nextElement();

                                    n.selected(true);
                                    n.draw(getGraphics());
                                }
                                if (vSelected.size() == 0) {
                                    vSelected = null;
                                }
                            }
                            selectedRectangle = null;
                        } else {
                            if ( (JabutiGUI.isAllPrimaryUses() || 
                                  JabutiGUI.isAllSecondaryUses() ||
                                  JabutiGUI.isAllPrimaryPotUses() || 
                                  JabutiGUI.isAllSecondaryPotUses() ) && 
                                  ! SelectedPoint.isSelected() ) {
                                //if (evt.isPopupTrigger()) {
                            	if (evt.getButton() == MouseEvent.BUTTON3) {
                                    if (nodeToken != null) {
                                        JPopupMenu menu = new JPopupMenu();	                            		
                                        GraphNode gn = ((GVFNode) nodeToken).getGraphNode();
                                        int methodId = ((GVFNode) nodeToken).getClassMethod().getMethodId();
										
                                        Set varSet = WeightColor.getVariableSetFromGraphNode(methodId, gn);

                                        if (varSet != null) {
                                            Iterator it = varSet.iterator();
                                            MyJMenuItem item;
                                            int[] labels = WeightColor.getColorBarLabels();
											
                                            while (it.hasNext()) {
                                                String varName = (String) it.next();
                                                Integer wgt = WeightColor.getVariableDefinitionWeight(methodId, gn, varName);
                                                
                                                String varSrcName = ((GVFNode) nodeToken).getClassMethod().getVariableSourceName(varName, ((CFGNode) gn).getDefinitionOffset(varName).intValue());
	                            				
                                                if (JabutiGUI.isSourcePanel()) {
                                                    item = new MyJMenuItem(methodId, gn, varName, varSrcName + " (" + varName + ")");
                                                } else {
                                                    item = new MyJMenuItem(methodId, gn, varName, varName + " (" + varSrcName + ")");
                                                }
	                            				
                                                item.setBackground(ToolConstants.getColor(WeightColor.getColorByWeight(labels, wgt)));
                                                item.addActionListener(new java.awt.event.ActionListener() {
                                                            public void actionPerformed(ActionEvent e) {
                                                                MyJMenuItem item = (MyJMenuItem) e.getSource();
                                                                int mId = item.getMethodId();
                                                                String methodName = ((GVFNode) nodeToken).getClassMethod().getMethodName();
                                                                GraphNode gn = item.getGraphNode();
                                                                String varName = item.getVarName();
															
                                                                System.out.println("CURRENT VAR: " + varName);
                                                                System.out.println("CURRENT METHOD: " + mId + " name: " + methodName);
                                                                System.out.println("CURRENT NODE: " + gn.getLabel());
															
                                                                if (gn == null) {
                                                                    return;
                                                                }
														    
                                                                if ( SelectedPoint.set(true,
                                                                                gn,
                                                                                mId, 
                                                                                varName)) {
                                                                    (JabutiGUI.mainWindow()).updatePane();
                                                                    repaint();
                                                                }
                                                            }
                                                        }
                                                        );
	                            				
                                                menu.add(item);
                                            }
                                            menu.show(evt.getComponent(), evt.getX(), evt.getY());
                                        }
                                    }
                                } else {
                                    if (nodeToken != null) {
		                                // Updating the colors when one decision node is
		                                // clicked once
		                                System.out.println("CLICKED NODE: " + ((GVFNode) nodeToken).getGraphNode().getLabel());
		                                if ( ! SelectedPoint.isSelected() ) {
		                                    if ( JabutiGUI.isAllPrimaryEdges() || JabutiGUI.isAllSecondaryEdges() ) {
		                                        if ( SelectedPoint.set(true, 
		                                                        ((GVFNode) nodeToken).getGraphNode(),
		                                                        ((GVFNode) nodeToken).getClassMethod().getMethodId(), null)) {
		                                            (JabutiGUI.mainWindow()).updatePane();
		                                            repaint();
		                                        }
		                                    } else if ( JabutiGUI.isAllPrimaryUses() || 
		                                                JabutiGUI.isAllSecondaryUses() || 
		                                                JabutiGUI.isAllPrimaryPotUses() || 
		                                                JabutiGUI.isAllSecondaryPotUses() ) {
		                                        GraphNode gn = ((GVFNode) nodeToken).getGraphNode();
		                                        int methodId = ((GVFNode) nodeToken).getClassMethod().getMethodId();
			                                	
		                                        if ( SelectedPoint.set(true, 
		                                                        gn,
		                                                        methodId, 
		                                                        WeightColor.getWeightestVariableFromGraphNode(methodId, gn))) {
		                                            (JabutiGUI.mainWindow()).updatePane();
		                                            repaint();
		                                        }
		                                    }
		                                }
		                           	}
                                }
                            }
                        }
                    }

                    public void mousePressed(MouseEvent evt) {
                        selectedRectangle = null;
                        oldX = evt.getX();
                        oldY = evt.getY();

                        if (vSelected != null) {
                            nodeToken = null;
                            oldNodeToken = null;
                        } else {
                            oldNodeToken = nodeToken;
                            nodeToken = null;
                            for (Enumeration e = vNodes.elements(); (e.hasMoreElements() && nodeToken == null);) {
                                GVFDisplayable d = (GVFDisplayable) e.nextElement();

                                if (d.contains(oldX, oldY)) {
                                    nodeToken = d;
                                }
                            }
                        }
                        if (nodeToken == null) {		    
                            // When I click on the open space...
                            selectX = evt.getX();
                            selectY = evt.getY();
                            if (oldNodeToken != null) {
                                // Unselect all nodes.
                                oldNodeToken.selected(false);
                            }
                        } else {}
                    }

                    public void mouseClicked(MouseEvent evt) {
                    	
                        if (evt.getClickCount() == 2) {
                            if (nodeToken != null) {
                                if (JabutiGUI.isSourcePanel()) {
                                    (JabutiGUI.mainWindow()).updatePane(JabutiGUI.SOURCE_PANEL, 
                                            ((GVFNode) nodeToken).getClassMethod().getBeginSourceLine());
                                } else {
                                    (JabutiGUI.mainWindow()).updatePane(JabutiGUI.BYTECODE_PANEL, 
                                            ((GVFNode) nodeToken).getClassMethod().getBeginBytecodeOffset());
                                }
                            }
                        } else {
                            if (nodeToken != null) {
		                        // Updating the colors when one decision node is
		                        // clicked once
		                        System.out.println("CLICKED NODE: " + ((GVFNode) nodeToken).getGraphNode().getLabel());
		                        if ( ! SelectedPoint.isSelected() ) {
		                        if ( JabutiGUI.isAllPrimaryEdges() || JabutiGUI.isAllSecondaryEdges() ) {
	                            	if ( SelectedPoint.set(true, 
		                            	((GVFNode) nodeToken).getGraphNode(),
		                                ((GVFNode) nodeToken).getClassMethod().getMethodId(), null)) {
		                                	(JabutiGUI.mainWindow()).updatePane();
		                                	repaint();
		                            	}
		                            }
		                        }                            	
                            	
                                if (evt.isShiftDown()) {} else {
                                    if (evt.paramString().indexOf("mods=" + InputEvent.BUTTON3_MASK) > 0) {} else {
                                        if (nodeToken != null) {
                                            if (oldNodeToken != null) {
                                                if (!oldNodeToken.equals(nodeToken)) {
                                                    oldNodeToken.selected(false);
                                                    oldNodeToken.draw(getGraphics());
                                                }
                                            }
                                            nodeToken.draw(getGraphics());
                                        }
                                    }
                                }
                            } else {
                                // Click on empty space.
                                if (vSelected != null) {
                                    for (Enumeration e = vSelected.elements(); e.hasMoreElements();) {
                                        GVFDisplayable n = (GVFDisplayable) e.nextElement();
	
                                        n.selected(false);
                                    }
                                    vSelected = null;
                                }
                                // paintComponent(getGraphics());
                                repaint();
                            }
                        }
                    }
                }
                );
        addMouseMotionListener(this);
    }
    
    class pAction extends AbstractAction {
        /**
		 * Added to jdk1.5.0_04 compiler
		 */
		private static final long serialVersionUID = -5616747438750716828L;

		public pAction() {
            super("Properties");
        }

        public void actionPerformed(ActionEvent e) {// popup.setVisible(false);
            /*
             JFrame pFrame = new PropertiesFrame(nodeToken);
             pFrame.show();
             */}
    }

    public void mouseMoved(MouseEvent evt) {
        tip = null;
        boolean found = false;

        for (Enumeration e = vNodes.elements(); e.hasMoreElements();) {
            GVFNode newNode = (GVFNode) e.nextElement();

            if (newNode.itsMe(evt.getX(), evt.getY())) {
                tip = newNode.getGraphNode().toString();
	        	
                // Adding the corresponding source line numbers to
                // the GraphNode information...
	        	
                LineNumberTable lnTable = newNode.getMethodGen().getLineNumberTable(newNode.getConstantPoolGen());

                tip += "Corresponding Source Lines:\n";
		
        		InstructionHandle ih = 
        			br.jabuti.util.InstructCtrl.findInstruction(
        				         newNode.getMethodGen(), 
        						((CFGNode) newNode.getGraphNode()).getStart()
        						);
                int srcLine = lnTable.getSourceLine(ih.getPosition());
                HashSet srcLines = new HashSet();

                srcLines.add(new Integer(srcLine));
                while (ih.getPosition() != ((CFGNode) newNode.getGraphNode()).getEnd()) {
                    ih = ih.getNext();
                    srcLine = lnTable.getSourceLine(ih.getPosition());
                    srcLines.add(new Integer(srcLine));
                }
                Object[] lines = srcLines.toArray();

                Arrays.sort(lines);
				
                tip += "\t" + lines[0];
                for (int i = 1; i < lines.length; i++) {
                    tip += ", " + lines[i];
                }
                tip += "\n";

                tipX = newNode.getX() + 30;
                tipY = newNode.getY();
                reDraw();
                found = true;
            }
        }
        if (!found) {
            reDraw();
        }
    }

    public void mouseDragged(MouseEvent evt) {}
    
    public void xmouseDragged(MouseEvent evt) {
        int x = evt.getX();
        int y = evt.getY();

        if (vSelected != null) {
            for (int i = 0; i < vSelected.size(); i++) {
                ((GVFDisplayable) vSelected.elementAt(i)).translate(x - oldX, y - oldY);
            }
            oldX = x;
            oldY = y;
            // paintComponent(getGraphics());
            repaint();
            nodeToken = null;
        } else {
            if (nodeToken != null) {
                nodeToken.translate(x - oldX, y - oldY);
                oldX = x;
                oldY = y;
                // paintComponent(getGraphics());
                repaint();
            } else {
                if (evt.getX() < selectX) {
                    oX = evt.getX();
                } else {
                    oX = selectX;
                }
                if (evt.getY() < selectY) {
                    oY = evt.getY();
                } else {
                    oY = selectY;
                }
                // paintComponent(getGraphics());
                repaint();
                getGraphics().drawRect(oX, oY, Math.abs(evt.getX() - selectX), Math.abs(evt.getY() - selectY));
                selectedRectangle = new Rectangle(oX, oY, Math.abs(evt.getX() - selectX), Math.abs(evt.getY() - selectY));
            }
        }
    }
    
    public void setNodesLinks(Vector vN, Vector vL) {
        vNodes = vN;
        vLinks = vL;

        // paintComponent(getGraphics());
        repaint();
    }

    public void reDraw() {
        // paintComponent(getGraphics());
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Enumeration e = vLinks.elements(); e.hasMoreElements();) {
            ((GVFDisplayable) e.nextElement()).draw(g);
        }
        for (Enumeration e = vNodes.elements(); e.hasMoreElements();) {
            ((GVFDisplayable) e.nextElement()).draw(g);
        }
        if (tip != null && CFGFrame.showNodeTips()) {
            showTip(g);
        }
        // Enable/Disable the showButton according 
        // whether a decision/definition node is selected
        // or not.
        CFGFrame.updateColorButtonPanel();
        CFGFrame.showButtonEnabled( SelectedPoint.isSelected() );
    }
    
    private void showTip(Graphics g) {
        if (tip != null) {
            Graphics2D g2 = (Graphics2D) g;
            Font tipFont = new Font("Courier New", Font.PLAIN, 14);
            Rectangle2D tipBox;                  // The display box: initialized elsewhere
            float tx = tipX, ty = tipY, w = 0, h = 0;
			
            g2.setFont(tipFont);

            StringTokenizer st = new StringTokenizer(tip, "\n");
            float yInc = 0.0f;

            while (st.hasMoreTokens()) {
                String message = st.nextToken().trim(); 
    			
                // Measure the font and the message
                FontRenderContext frc = g2.getFontRenderContext();
                Rectangle2D bounds = tipFont.getStringBounds(message, frc);
                LineMetrics metrics = tipFont.getLineMetrics(message, frc);
                float width = (float) bounds.getWidth();     // The width of our text
				
                if (width > w) {
                    w = width;
                }
    			
                yInc = metrics.getHeight();
                h += yInc;
            }
            h += yInc;
     		
            tipBox = new Rectangle2D.Float(tx, ty, w, h);

            g2.setColor(new Color(255, 255, 205)); // Very Light Yellow
     		
            g2.draw(tipBox);
            g2.fill(tipBox);

            g2.setColor(Color.black);
			
            st = new StringTokenizer(tip, "\n");
            int y = tipY;    		

            while (st.hasMoreTokens()) {
                String message = st.nextToken().trim(); 
    			
                FontRenderContext frc = g2.getFontRenderContext();
                LineMetrics metrics = tipFont.getLineMetrics(message, frc);
    			
                yInc = metrics.getHeight();    			
                g2.drawString(message, tipX, y += yInc);     		
            }
        }
    }
}
