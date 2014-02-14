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


package br.jabuti.gui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;

import br.jabuti.criteria.DefUse;
import br.jabuti.graph.CFG;
import br.jabuti.graph.CFGNode;
import br.jabuti.graph.GraphNode;
import br.jabuti.project.ClassFile;
import br.jabuti.project.ClassMethod;
import br.jabuti.util.ToolConstants;


/**
 * This class is responsible to create the panel that
 * shows the Java Bytecode file in colors, depending on 
 * the weights of a given requirement
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
class BytecodePanel extends JPanel {

    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -1253019699935548931L;

	private JabutiGUI parent; // main window reference

    private JTextPane tp = null; 
    private JPanel buttonPanel = new JPanel();
        	
    private JScrollPane scrollPane = null;
        
    private CodeSyntesePanel ssp;
        
    public BytecodePanel() {
        super();
        
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());	
			
        setLayout(new BorderLayout());	

		// no wrap by overriding text pane methods				
        tp = new JTextPane() {
                    /**
			 * 
			 */
			private static final long serialVersionUID = 5788458051077401904L;

					public void setSize(Dimension d) {
                        if (d.width < getParent().getSize().width)					d.width = getParent().getSize().width;
                        super.setSize(d);
                    }

                    public boolean getScrollableTracksViewportWidth() {
                        return false;
                    }
                };
                
        tp.setEditable(false);

        tp.setBackground(ToolConstants.getColor(ToolConstants.COLOR_0));
            
        tp.addMouseListener(new MouseAdapter() {
                    public void mouseReleased(MouseEvent e) {
                        if (!SelectedPoint.isSelected() && 
                        	(! (JabutiGUI.isAllPrimaryNodes() || JabutiGUI.isAllSecondaryNodes() ) ) ) {
                            bytecodePanel_mouseClicked(e);
                        }
                    }
                }
                );            

        scrollPane = new JScrollPane(tp);
        scrollPane.setViewportView(tp);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
			
        MyScrollbarListener myScrollbarListener = new MyScrollbarListener(); 
	    	
        scrollBar.addAdjustmentListener(myScrollbarListener);
	    	
        add(scrollPane, BorderLayout.CENTER);
	
        ssp = new CodeSyntesePanel("", "", "", "", "");
        ssp.setVisible(false);
        add(ssp, BorderLayout.SOUTH);
    }
	
    class MyScrollbarListener implements AdjustmentListener {
		    
        // This method is called whenever the value of a scrollbar is changed,
        // either by the user or programmatically.
        public void adjustmentValueChanged(AdjustmentEvent evt) {
            JViewport jvp = scrollPane.getViewport();
            int pos = tp.viewToModel(jvp.getViewPosition());
	            
            if (pos > 0) {
                tp.setCaretPosition(pos);
            }
	            
            int row = tp.getStyledDocument().getDefaultRootElement().getElementIndex(pos);
            int end = tp.getStyledDocument().getDefaultRootElement().getElementIndex(tp.getDocument().getLength());
		
            if (ssp != null) {	    
                ssp.setLineLabel("Line: " + (row + 1) + " of " + (end + 1));
            }
        }
    }
	
    public BytecodePanel showBytecodePanel(int toOffset) {
		
        parent = JabutiGUI.mainWindow();
    	
        String className = JabutiGUI.getProject().getCurClassName();
	
        ClassFile cl = JabutiGUI.getProject().getClassFile(className);
	                
        // Empting the content of the text panel
        tp.setText("");
	
        // UPDATING THE SOURCE COLOR...
        tp.setBackground(ToolConstants.getColor(ToolConstants.COLOR_0));
            	
        Document doc = tp.getStyledDocument();
	
        SimpleAttributeSet attr = new SimpleAttributeSet();
	
        //StyleConstants.setFontFamily(attr, "Courier New");
        //StyleConstants.setFontSize(attr, ToolConstants.sourceFontSize);
        StyleConstants.setBackground(attr, ToolConstants.getColor(ToolConstants.COLOR_0));

        JavaClass javaClass = cl.getJavaClass();
        Method[] methods = cl.getMethods();
	
        // BUILDING THE COLOR PANEL...
        buttonPanel.setVisible(false);
        buttonPanel.removeAll();

        Hashtable colorButtonTable = WeightColor.getColorButtonTable();
	    
        buttonPanel.setLayout(new GridLayout(1, colorButtonTable.size()));
	     
        int[] labels = WeightColor.getColorBarLabels();
	     	
        for (int i = 0; i < labels.length; i++) {
            Integer label = new Integer(labels[i]);
            JButton button = new JButton(label.toString());
	     
            int color = ((Integer) colorButtonTable.get(label)).intValue();

            button.setBackground(ToolConstants.getColor(color));
	
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setEnabled(false);
	     
            buttonPanel.add(button);
        }			
        buttonPanel.setVisible(true);
        add(buttonPanel, BorderLayout.NORTH);
	
        try {
            // Printing the header of the class file
            BufferedReader strReader = new BufferedReader(new StringReader(javaClass.toString()));
            String nl = System.getProperty("line.separator");
                
            // Printing the class name
            doc.insertString(doc.getLength(), strReader.readLine() + nl, attr);
            // Printing the class file name
            doc.insertString(doc.getLength(), strReader.readLine() + nl, attr);
            // Printing the source file name
            doc.insertString(doc.getLength(), strReader.readLine() + nl, attr);                                
				
            for (int i = 0; i < methods.length; i++) {
                String methodName = methods[i].getName()
                        + methods[i].getSignature();

                // Reseting the Background Color...
                StyleConstants.setBackground(attr, ToolConstants.getColor(ToolConstants.COLOR_0));
	        		
                ClassMethod method = cl.getMethod(methodName);

                method.setBeginBytecodeOffset(doc.getEndPosition().getOffset());
					
                // Printing the name of the method
                doc.insertString(doc.getLength(), nl + nl + method.getMethodGen().getReturnType() + " " + methodName + "\n", attr);
	
                Code code = methods[i].getCode();
	
                Hashtable offsetLines = new Hashtable();
                Hashtable offsetColors = new Hashtable();
		            
                if (code != null) {
                    strReader = new BufferedReader(new StringReader(code.toString()));
                    String line = strReader.readLine();
	
                    while (line != null) {
                        try {
                            int index = line.indexOf(":");

                            if (index > 0) {
                                Integer offset = new Integer(line.substring(0, index));

                                offsetLines.put(offset, line);
                                offsetColors.put(offset, new Integer(0));
                            }
                        } catch (NumberFormatException nfe) {}
                        line = strReader.readLine();
                    }
                }

                Hashtable positionTable = null;
                Hashtable classVariables = WeightColor.getClassVariableTable();

                // ALL-USES: Painting only the offset where the
                // variable definition is located
                if ( JabutiGUI.isAllPrimaryUses() || 
                     JabutiGUI.isAllSecondaryUses() ||
                     JabutiGUI.isAllPrimaryPotUses() || 
                     JabutiGUI.isAllSecondaryPotUses() ) {
                    	
                    // Position table can be null if
                    // the method has no def-use requirement
                    Integer mId = new Integer(method.getMethodId());

                    // ERROR: remove this above if statement
                    // Consequences: if the method has no def-use association a
                    // null pointer exception is thrown.
                    if (classVariables != null
                            && classVariables.containsKey(mId)) { 
                        positionTable = (Hashtable) classVariables.get(mId);
	
                        if (!SelectedPoint.isSelected()) {
                            // For each defined variable
                            Iterator itPos = positionTable.keySet().iterator();
			
                            while (itPos.hasNext()) {
                                Integer pos = (Integer) itPos.next();
			                             	
                                Vector nodeVar = WeightColor.getWeightestVariableFromPosition(method.getMethodId(), pos);
	
                                if (nodeVar != null) {
                                    GraphNode gn = (GraphNode) nodeVar.elementAt(0);
                                    String varDef = (String) nodeVar.elementAt(1);
                                    Integer varDefOff = ((CFGNode) gn).getDefinitionOffset(varDef);
				                                
                                    Integer varDefWgt = WeightColor.getVariableDefinitionWeight(method.getMethodId(), gn, varDef);
                                    Integer varDefColor = new Integer(WeightColor.getColorByWeight(labels, varDefWgt));
	                                    
                                    // Setting the color of the definition offset
                                    if (offsetColors.containsKey(varDefOff)) {
                                        Integer curColor = (Integer) offsetColors.get(varDefOff);
	
                                        if (curColor.intValue()
                                                < varDefColor.intValue()) {
                                            offsetColors.put(varDefOff, varDefColor);
                                        }
                                    }
                                }
                            }
                        } else if (SelectedPoint.isSelected()
                                && SelectedPoint.getMethod()
                                == method.getMethodId()) {
	                                     
                            Integer defOffset = ((CFGNode) SelectedPoint.getNode()).getDefinitionOffset(SelectedPoint.getVariable());
                            Integer varDefColor = (Integer) SelectedPoint.recoverFromNode(ToolConstants.LABEL_COLOR);
	
                            // Setting the color of the definition offset
                            if (offsetColors.containsKey(defOffset)) {
                                offsetColors.put(defOffset, varDefColor);
                            }
                            
                            System.out.println("Selected method: " + SelectedPoint.getMethod() );
                            System.out.println("Selected node: " + SelectedPoint.getNode() );
                            System.out.println("Selected variable: " + SelectedPoint.getVariable() );
                            
										 
                            // Getting all uses of the selected definition
                            Hashtable defUseTable = (Hashtable) WeightColor.getClassWeights().get(new Integer(SelectedPoint.getMethod()));
                            Hashtable defTable = (Hashtable) defUseTable.get(SelectedPoint.getNode());
                            Hashtable useTable = (Hashtable) defTable.get(SelectedPoint.getVariable());
										 
                            Iterator itUse = useTable.keySet().iterator();
			
                            while (itUse.hasNext()) {
                                DefUse du = (DefUse) itUse.next();
                                
                                System.out.println( "DEF-USE: " + du );
                                
                                Integer useWgt = (Integer) useTable.get(du);
	                     	
	                     		System.out.println( "\tDU weight: " + useWgt );
	                     		
                                int useColor = WeightColor.getColorByWeight(labels, useWgt);
				                        	
                                // C-Use color....
                                GraphNode gnUse = method.getGraphNodeByLabel(du.getUseFrom());
                                Integer useOffset = ((CFGNode) gnUse).getUseOffset(du.getVar()); 

								System.out.println( "\tUse node: " + gnUse );
								System.out.println( "\tUse offset: " + useOffset );
								
                                if (offsetColors.containsKey(useOffset)) {
                                    Integer curColor = (Integer) offsetColors.get(useOffset);
	
                                    if (curColor.intValue() < useColor) {
                                        offsetColors.put(useOffset, new Integer(useColor));
                                    }				                        	
                                }

                                // If p-use, change the color of the entire second node
                                String useLabel = du.getUseTo();
                                 
                                if (useLabel != null) {
                                	System.out.println( "CHANGING THE COLOR OF NODE: " + useLabel );
                                    GraphNode gn = method.getGraphNodeByLabel(useLabel);
                                    int c = ((Integer) gn.getUserData(ToolConstants.LABEL_COLOR)).intValue();
                                 
                                 System.out.println( "Current color: " + c );
                                 
					        		InstructionHandle ih = 
										br.jabuti.util.InstructCtrl.findInstruction(
			         					method.getMethodGen(), 
										((CFGNode) gn).getStart()
										);
                                    String inst = ih.toString();
                                    int index = inst.indexOf(":");
                                    Integer offset = new Integer(inst.substring(0, index).trim());
                                 
                                    if (offsetColors.containsKey(offset)) {
                                        int cNumber = ((Integer) offsetColors.get(offset)).intValue();
                                 
                                        if (cNumber < c) {
                                            offsetColors.put(offset, new Integer(c));
                                        }
                                    }
                                 
                                    while (ih.getPosition() != ((CFGNode) gn).getEnd()) {
                                        ih = ih.getNext();
                                        inst = ih.toString();
                                        index = inst.indexOf(":");
                                        offset = new Integer(inst.substring(0, index).trim());
                                        if (offsetColors.containsKey(offset)) {
                                            int cNumber = ((Integer) offsetColors.get(offset)).intValue();
                                 
                                            if (cNumber < c) {
                                                offsetColors.put(offset, new Integer(c));
                                            }
                                        }		
                                    }
                                }
                            }
                        }
                    }
                } else {
                    CFG cfg = method.getCFG();
                    GraphNode[] fdt = cfg.findDFT(true);
	
                    int c = 0;
                    
                    /*
                     Hashtable defUseTable = null;

                     if (allUsesCriterion.isSelected()) {
                     defUseTable = (Hashtable) classWeights.get(new Integer(method.getMethodId()));
                     }
                     */
                    
                    for (int x = 0; x < fdt.length; x++) {
		            	
                        GraphNode gn = fdt[x];
	                	
                        // Getting the color associated with this node
                        Integer weightColor = (Integer) gn.getUserData(ToolConstants.LABEL_COLOR);

                        if (weightColor != null) {
                            c = weightColor.intValue();
                        } else {
                            c = 0;
                        }

                        // ALL-EDGES: Painting only the decision bytecode instruction 
                        // when a decision node is selected
                        if ((JabutiGUI.isAllPrimaryEdges()
                                || JabutiGUI.isAllSecondaryEdges())
                                && ((!SelectedPoint.isSelected())
                                || (SelectedPoint.isSelected()
                                && gn.getLabel().equals(SelectedPoint.getNodeLabel())))) {
			        		InstructionHandle ih = 
								br.jabuti.util.InstructCtrl.findInstruction(
	         					method.getMethodGen(), 
								((CFGNode) gn).getEnd()
								);
                            String inst = ih.toString();
                            int index = inst.indexOf(":");
                            Integer offset = new Integer(inst.substring(0, index).trim());
	
                            if (offsetColors.containsKey(offset)) {
                                int cNumber = ((Integer) offsetColors.get(offset)).intValue();
	
                                if (cNumber < c) {
                                    offsetColors.put(offset, new Integer(c));
                                }
                            }
                        } else {
			        		InstructionHandle ih = 
								br.jabuti.util.InstructCtrl.findInstruction(
	         					method.getMethodGen(), 
								((CFGNode) gn).getStart()
								);
                            String inst = ih.toString();
                            int index = inst.indexOf(":");
                            Integer offset = new Integer(inst.substring(0, index).trim());
	
                            if (offsetColors.containsKey(offset)) {
                                int cNumber = ((Integer) offsetColors.get(offset)).intValue();
	
                                if (cNumber < c) {
                                    offsetColors.put(offset, new Integer(c));
                                }
                            }
							
                            while (ih.getPosition() != ((CFGNode) gn).getEnd()) {
                                ih = ih.getNext();
                                inst = ih.toString();
                                index = inst.indexOf(":");
                                offset = new Integer(inst.substring(0, index).trim());
                                if (offsetColors.containsKey(offset)) {
                                    int cNumber = ((Integer) offsetColors.get(offset)).intValue();
	
                                    if (cNumber < c) {
                                        offsetColors.put(offset, new Integer(c));
                                    }
                                }		
                            }
                        }
                    }
                }

                // Printing the code, independently of the tsting
                // criterion                    
                    
                Object[] orderedInstr = offsetLines.keySet().toArray();
                    
                Arrays.sort(orderedInstr);
                for (int x = 0; x < orderedInstr.length; x++) {
                    Integer offset = (Integer) orderedInstr[x];
	                	
                    String line = (String) offsetLines.get(offset);
                        
                    int c;

                    if (offsetColors.containsKey(offset)) {
                        c = ((Integer) offsetColors.get(offset)).intValue();
                    } else {
                        c = 0;
                    }	
                    StyleConstants.setBackground(attr, ToolConstants.getColor(c));
                    doc.insertString(doc.getLength(), line + nl, attr);
                        
                    // To print the code in the standard output...
                    // System.out.println( line );
                }
                method.setEndBytecodeOffset(doc.getEndPosition().getOffset());
            }
        } catch (Exception e) {
            ToolConstants.reportException(e, ToolConstants.STDERR);
        }

        scrollPane.setViewportView(tp);
            
        setCaret(toOffset);
            
        int row = tp.getStyledDocument().getDefaultRootElement().getElementIndex(tp.getCaretPosition());
        int end = tp.getStyledDocument().getDefaultRootElement().getElementIndex(tp.getDocument().getLength());

        String specificTool = parent.getActiveToolName();

        String criterion = parent.getActiveCriterionName();

        ssp.setContent(ToolConstants.toolName + ": " + specificTool, 
                "File: " + className, "Line: " + (row + 1) + " of " + (end + 1), "Coverage: " + criterion, "Highlighting: All Priorized");
        ssp.setVisible(true);
				
        return this;
    }
	
    public void setCaret(int pos) {
		try {
        	tp.setCaretPosition(pos);
        }
        catch ( IllegalArgumentException iae ) {
        	ToolConstants.reportException( iae, ToolConstants.STDERR );
        } finally {
        	pos = 0;
        }
        
        if (pos != 0) {
				
            try {
                Rectangle rect = tp.modelToView(pos);

                if (rect != null) {
                    JViewport jvp = scrollPane.getViewport();

                    if (jvp != null) {
                        jvp.setViewPosition(rect.getLocation());
                    }
                }
            } catch (BadLocationException ble) {}
        }
    }
		
    public int getCaret() {
        if (tp != null) {
            return tp.getCaretPosition();
        }
        return 0;
    }
        
    private void bytecodePanel_mouseClicked(MouseEvent e) {
        int elIndex = 0;
        Element el = null;
        String inst = null;
			
        try {
            //if (e.isPopupTrigger()) {
        	if (e.getButton() == MouseEvent.BUTTON3) {
                int pos = tp.viewToModel(new Point(e.getX(), e.getY()));

                tp.setCaretPosition(pos);
            }
            elIndex = tp.getStyledDocument().getDefaultRootElement().getElementIndex(tp.getCaretPosition());
            el = tp.getStyledDocument().getDefaultRootElement().getElement(elIndex);
            inst = tp.getText(el.getStartOffset(), el.getEndOffset() - el.getStartOffset());
        } catch (BadLocationException ble) {
            return;
        }

        int offset = inst.indexOf(":");

        if (offset < 0) {
            return;
        }
                
        Integer off = null;

        try {
            off = new Integer(inst.substring(0, offset).trim());
        } catch (NumberFormatException nfe) {
            return;
        }

        offset = off.intValue();
	        
        // Finding the corresponding class file
        ClassFile cf = JabutiGUI.getProject().getClassFile(JabutiGUI.getProject().getCurClassName());
        Method[] methods = cf.getMethods();
        int methodId = -1;
        ClassMethod method = null;

        for (int i = 0; i < methods.length && methodId == -1; i++) {
            String methodName = methods[i].getName() + methods[i].getSignature();
	
            // System.out.println( "Method Name: " + methodName );
            method = cf.getMethod(methodName);
                
            // System.out.println( "\tBEGIN OFFSET: " + method.getBeginBytecodeOffset() );
            // System.out.println( "\tEND OFFSET: " + method.getEndBytecodeOffset() );
            // System.out.println( "\tCURRENT OFFSET: " +  el.getStartOffset() );
            if ((el.getStartOffset() >= method.getBeginBytecodeOffset())
                    && (el.getStartOffset() <= method.getEndBytecodeOffset())) {
                methodId = method.getMethodId();
            }
        }
        // Method found...
        if (methodId != -1) {
            GraphNode selectedGn = null;
            String selectedVar = null;
            	
            if (JabutiGUI.isAllPrimaryEdges()
                    || JabutiGUI.isAllSecondaryEdges()) {
                selectedGn = method.getDecisionNodeFromOffset(offset);

                if (selectedGn == null) {
                    return;
                }
				                	
                if (SelectedPoint.set(true, selectedGn, methodId, selectedVar)) {
                    parent.updatePane();
                }
            } else {
                //if (e.isPopupTrigger()) {
            	if (e.getButton() == MouseEvent.BUTTON3) {
                    HashSet varDef = WeightColor.getVariableSetFromPosition(methodId, new Integer(offset));
	
                    if (varDef != null) {
                        JPopupMenu varPopup = new JPopupMenu();
		             			
                        Iterator it = varDef.iterator();
	
                        while (it.hasNext()) {
                            Vector nodeVar = (Vector) it.next();
                            GraphNode node = (GraphNode) nodeVar.elementAt(0);
                            String varName = (String) nodeVar.elementAt(1);
                            Integer wgt = WeightColor.getVariableDefinitionWeight(methodId, node, varName);
	                            
                            String varSrcName = method.getVariableSourceName(varName, ((CFGNode) node).getDefinitionOffset(varName).intValue());
								         					
                            MyJMenuItem item = new MyJMenuItem(methodId, node, varName, varName + " (" + varSrcName + ")");
	
                            // item.setToolTipText( node.getLabel() );
                            item.setBackground(ToolConstants.getColor(WeightColor.getColorByWeight(WeightColor.getColorBarLabels(), wgt)));
                            item.addActionListener(new java.awt.event.ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                            MyJMenuItem item = (MyJMenuItem) e.getSource();
                                            int mId = item.getMethodId();
                                            String methodName = JabutiGUI.getProject().getClassFile( 
                                                            JabutiGUI.getProject().getCurClassName()).getMethod(mId).getMethodName();
                                            GraphNode gn = item.getGraphNode();
                                            String varName = item.getVarName();
											
                                            System.out.println("CURRENT VAR: " + varName);
                                            System.out.println("CURRENT METHOD: " + mId + " name: " + methodName);
                                            System.out.println("CURRENT NODE: " + gn.getLabel());
											
                                            if (gn == null) {
                                                return;
                                            }
										                	
                                            if (SelectedPoint.set(true, gn, mId, varName)) {
                                                parent.updatePane();
                                            }
                                        }
                                    }
                                    );
                            varPopup.add(item);
                        }
                        varPopup.show(e.getComponent(), e.getX(), e.getY());
                    }
                } else {
                    Vector nodeVar = WeightColor.getWeightestVariableFromPosition(methodId, new Integer(offset));
                        
                    if (nodeVar == null) {
                        return;
                    }

                    selectedGn = (GraphNode) nodeVar.elementAt(0);
                    selectedVar = (String) nodeVar.elementAt(1);
	
                    if (selectedGn == null) {
                        return;
                    }
					                	
                    if (SelectedPoint.set(true, selectedGn, methodId, selectedVar)) {
                        parent.updatePane();
                    }
                }
            }
        }
    }
}
