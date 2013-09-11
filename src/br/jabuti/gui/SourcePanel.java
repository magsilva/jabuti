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


import java.awt.Adjustable;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
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

import org.aspectj.apache.bcel.classfile.LineNumberTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.generic.InstructionHandle;

import br.jabuti.criteria.DefUse;
import br.jabuti.graph.CFG;
import br.jabuti.graph.CFGNode;
import br.jabuti.graph.GraphNode;
import br.jabuti.project.ClassFile;
import br.jabuti.project.ClassMethod;
import br.jabuti.project.ClassSourceFile;
import br.jabuti.project.JabutiProject;
import br.jabuti.util.ToolConstants;


/**
 * This class is responsible to create the panel that
 * shows the Java Source Code file in colors, when the 
 * source code is available...
 * If not, the bytecode is shown instead...
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
class SourcePanel extends JPanel {
	
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 8216359341631580892L;

	JabutiGUI parent;
    
    JabutiProject prj;
		
    private JTextPane tp = null; 
    private JPanel buttonPanel = new JPanel();
        	
    private JScrollPane scrollPane = null;
	    
    CodeSyntesePanel ssp;
        
    Hashtable lineNodeTable = null;
	
    public SourcePanel() {
        super();
	
		prj = null;
	
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
			
        setLayout(new BorderLayout());	
				
		// no wrap by overriding text pane methods				
        tp = new JTextPane() {
                    /**
			 * Added to jdk1.5.0_04 compiler
			 */
			private static final long serialVersionUID = -7401130932936763115L;

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
                        if ((!SelectedPoint.isSelected()) && 
                        	(! (JabutiGUI.isAllPrimaryNodes() || JabutiGUI.isAllSecondaryNodes() ) ) ) {
                            sourcePanel_mouseClicked(e);
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
            Adjustable source = evt.getAdjustable();
	    
            // Determine which scrollbar fired the event
            int orient = source.getOrientation();
	
            if (orient == Adjustable.HORIZONTAL) {// Event from horizontal scrollbar
            } else {// Event from vertical scrollbar
            }
	    
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
	
    public SourcePanel showSourcePanel(int toOffset) {
		
        parent = JabutiGUI.mainWindow();
		
        prj = JabutiGUI.getProject();
        String className = prj.getCurClassName();
	
        ClassFile cl = prj.getClassFile(className);
        ClassSourceFile src = cl.getSourceFile();
	
        tp.setText("");
			
        BufferedReader buffer = null;
	
        if (src.exists()) {
            buffer = src.getSourceCode();
                
            lineNodeTable = new Hashtable();

            // UPDATING THE SOURCE COLOR...
            tp.setBackground(ToolConstants.getColor(ToolConstants.COLOR_0));
		
            Document doc = tp.getStyledDocument();
            String nl = System.getProperty("line.separator");
		
            SimpleAttributeSet attr = new SimpleAttributeSet();
		
            //StyleConstants.setFontFamily(attr, "Courier New");
            //StyleConstants.setFontSize(attr, ToolConstants.sourceFontSize);
            StyleConstants.setBackground(attr, ToolConstants.getColor(ToolConstants.COLOR_0));
	
            Method[] methods = cl.getMethods();
		
            // BUILDING THE COLOR PANEL...
            buttonPanel.setVisible(false);
            buttonPanel.removeAll();
		     
            Hashtable colorButtonTable = WeightColor.getColorButtonTable();
		    
            buttonPanel.setLayout(new GridLayout(1, colorButtonTable.size()));
		     
            Object[] keySet = colorButtonTable.keySet().toArray();
	
            Arrays.sort(keySet);
		     
            for (int i = 0; i < keySet.length; i++) {
                JButton button = new JButton(((Integer) keySet[i]).toString());
		     
                int color = ((Integer) colorButtonTable.get((Integer) keySet[i])).intValue();
	
                button.setBackground(ToolConstants.getColor(color));
		
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                button.setEnabled(false);
		     
                buttonPanel.add(button);
            }			
            buttonPanel.setVisible(true);
            add(buttonPanel, BorderLayout.NORTH);
				
            if (buffer != null) {
                try {
                    Vector sourceColor = new Vector();
						
                    // Inserting something at position 0
                    sourceColor.add(new Integer(ToolConstants.COLOR_0));
						
                    // The code starts at position 1
                    String line = buffer.readLine();

                    while (line != null) {
                        sourceColor.add(new Integer(ToolConstants.COLOR_0));
                        line = buffer.readLine();
                    }
                    
                    int[] labels = null;

                    if ( JabutiGUI.isAllPrimaryUses() ||
                         JabutiGUI.isAllSecondaryUses() ||
                         JabutiGUI.isAllPrimaryPotUses() ||
                         JabutiGUI.isAllSecondaryPotUses() ) {
                        labels = WeightColor.getColorBarLabels();
                    }

                    for (int i = 0; i < methods.length; i++) {
                        String methodName = methods[i].getName()
                                + methods[i].getSignature();
		
                        ClassMethod method = cl.getMethod(methodName);
		
                        // Line number table to map bytecode offset into
                        // source code line...
                        LineNumberTable lnTable = method.getMethodGen().getLineNumberTable(method.getConstantPoolGen());

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
		                    		
                                positionTable = (Hashtable) WeightColor.getClassVariableTable().get(mId);
                            	
                                if (!SelectedPoint.isSelected()) {
                                    // For each defined variable
                                    Iterator itPos = positionTable.keySet().iterator();
		
                                    while (itPos.hasNext()) {
                                        Integer pos = (Integer) itPos.next();
		                             	
                                        Vector nodeVar = WeightColor.getWeightestVariableFromPosition(method.getMethodId(), pos);

                                        if (nodeVar != null) {
                                            GraphNode gn = (GraphNode) nodeVar.elementAt(0);
                                            String varDef = (String) nodeVar.elementAt(1);
			                                
                                            Integer varDefWgt = WeightColor.getVariableDefinitionWeight(method.getMethodId(), gn, varDef);
                                            Integer varDefColor = new Integer(WeightColor.getColorByWeight(labels, varDefWgt));

											System.out.println( "MID " + mId + " name : " + method.getMethodName() );
											
											System.out.println( "Var Weight " + varDefWgt );
											System.out.println( "Var Color " + varDefColor );
											System.out.println( "Var Por " + pos );											
											
											// TO HANDLE ASPECT ORIENTED PROGRAMS
											if ( pos != null && pos.intValue() >= 0 )
                                            	sourceColor.setElementAt(varDefColor, pos.intValue());
                                        }
                                    }
                                } else if (SelectedPoint.isSelected()
                                        && SelectedPoint.getMethod()
                                        == method.getMethodId()) {
                                     
                                    Integer defOffset = ((CFGNode) SelectedPoint.getNode()).getDefinitionOffset(SelectedPoint.getVariable());
                                    int defSrcLine = method.bytecodeOffset2SourceLine(defOffset.intValue());
                          			 
                                    Integer varDefColor = (Integer) SelectedPoint.recoverFromNode(ToolConstants.LABEL_COLOR);

									// TO HANDLE ASPECT ORIENTED PROGRAMS
									if ( defSrcLine >= 0 )
	                                    // Painting the definition node
	                                    sourceColor.setElementAt(varDefColor, defSrcLine);
									 
                                    // Getting all uses of the selected definition
                                    Hashtable defUseTable = (Hashtable) WeightColor.getClassWeights().get(new Integer(SelectedPoint.getMethod()));
                                    Hashtable defTable = (Hashtable) defUseTable.get(SelectedPoint.getNode());
                                    Hashtable useTable = (Hashtable) defTable.get(SelectedPoint.getVariable());
									 
                                    Iterator itUse = useTable.keySet().iterator();
		
                                    while (itUse.hasNext()) {
                                        DefUse du = (DefUse) itUse.next();
                                        Integer useWgt = (Integer) useTable.get(du);
					                        	
                                        int useColor = WeightColor.getColorByWeight(labels, useWgt);
					                        	
                                        // C-Use color....
                                        GraphNode gnUse = method.getGraphNodeByLabel(du.getUseFrom());
                                        Integer useOffset = ((CFGNode) gnUse).getUseOffset(du.getVar());
                                        int useLine = method.bytecodeOffset2SourceLine(useOffset.intValue());
                                    	
                                    	// TO HANDLE ASPECT ORIENTED PROGRAMS
                                    	if ( useLine >= 0 ) {
	                                        Integer cor = (Integer) sourceColor.elementAt(useLine);
	
	                                        if (cor.intValue() < useColor) {
	                                            sourceColor.setElementAt(new Integer(useColor), useLine);
	                                        }
	                                        
	                                        // If p-use, change the color of the entire second node
	                                        String useLabel = du.getUseTo();
	
	                                        if (useLabel != null) {
	                                            GraphNode gn = method.getGraphNodeByLabel(useLabel);
	                                            int c = ((Integer) gn.getUserData(ToolConstants.LABEL_COLOR)).intValue();
	                                             
		
								        		InstructionHandle ih = 
        											br.jabuti.util.InstructCtrl.findInstruction(
        				         					method.getMethodGen(), 
        											((CFGNode) gn).getStart()
        											);
	                                            int srcLine = lnTable.getSourceLine(ih.getPosition());
	                                            
	                                            // TO HANDLE ASPECT ORIENTED PROGRAMS
	                                            if ( srcLine >= 0 ) {
		                                            cor = (Integer) sourceColor.elementAt(srcLine);
		                                             
		                                            if (cor.intValue() < c) {
		                                                sourceColor.setElementAt(new Integer(c), srcLine);
		                                            }
		                                             
		                                            while (ih.getPosition() != ((CFGNode) gn).getEnd()) {
		                                                ih = ih.getNext();
		                                                srcLine = lnTable.getSourceLine(ih.getPosition());
		                                                cor = (Integer) sourceColor.elementAt(srcLine);
		                                                if (cor.intValue() < c) {
		                                                    sourceColor.setElementAt(new Integer(c), srcLine);
		                                                }
		                                            }
		                                        }
	                                        }
	                                   }
                                    }
                                }
                            }
                        } else {
                            // Traversing the CFG looking for the color of each node...
                            CFG cfg = method.getCFG();
                            GraphNode[] fdt = cfg.findDFT(true);
			
                            int c = 0;	            
		
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
                                if ((!SelectedPoint.isSelected()
                                        && (JabutiGUI.isAllPrimaryEdges()
                                        || JabutiGUI.isAllSecondaryEdges()))
                                        || (SelectedPoint.isSelected()
                                        && gn.getLabel().equals(SelectedPoint.getNodeLabel()))) {
					        		InstructionHandle ih = 
										br.jabuti.util.InstructCtrl.findInstruction(
			         					method.getMethodGen(), 
										((CFGNode) gn).getEnd()
										);
                                    int srcLine = lnTable.getSourceLine(ih.getPosition());
                                    
                                    // TO HANDLE ASPECT ORIENTED PROGRAMS
                                    if ( srcLine >= 0 ) {
	                                    int cor = ((Integer) sourceColor.elementAt(srcLine)).intValue();
	
	                                    if (cor < c) {
	                                        Integer nc = new Integer(c);
	                                        Integer ln = new Integer(srcLine);                                    	
	
	                                        sourceColor.setElementAt(nc, srcLine);
	                                        lineNodeTable.put(ln, gn.getLabel());
	                                        System.out.println("LINE " + ln + " NODE: " + gn.getLabel());
	                                    }
	                                 }
                                    // ALL-NODES: Painting all offset of a given CFG Node
                                } else {
					        		InstructionHandle ih = 
										br.jabuti.util.InstructCtrl.findInstruction(
			         					method.getMethodGen(), 
										((CFGNode) gn).getStart()
										);
									//System.out.println( "METHOD ATUAL: " + method.getMethodName() );
									
									int srcLine = -1;
									try {
										srcLine = lnTable.getSourceLine(ih.getPosition());
	                                    int cor = ((Integer) sourceColor.elementAt(srcLine)).intValue();
	
	                                    if (cor < c) {
	                                        sourceColor.setElementAt(new Integer(c), srcLine);
	                                    }
									} catch (ArrayIndexOutOfBoundsException aobe) {
										//System.out.println("Exce��o gerada");									
									}
									//System.out.println( "\tBytecode position: " + ih.getPosition() );
									//System.out.println( "\tSource line position: " + srcLine );
                                    // TO HANDLE ASPECT ORIENTED PROGRAMS
                                    while (ih.getPosition() != ((CFGNode) gn).getEnd()) {
                                        ih = ih.getNext();
										try {                                        
	                                        srcLine = lnTable.getSourceLine(ih.getPosition());
	                                        int cor = ((Integer) sourceColor.elementAt(srcLine)).intValue();
	                                        if (cor < c) {
	                                            sourceColor.setElementAt(new Integer(c), srcLine);
	                                        }
										} catch (ArrayIndexOutOfBoundsException aobe) {
											//System.out.println("Exce��o gerada");									
										}
										//System.out.println( "\tBytecode position: " + ih.getPosition() );
										//System.out.println( "\tSource line position: " + srcLine );
                                    }
                                }
                            }
                        }
                    }
	                    
                    // Printing and painting the source code...
                    buffer = src.getSourceCode();
   	                    
                    line = buffer.readLine();
                    // The code starts at position 1
                    int curLine = 1;

                    while (line != null) {
                        int cor = ((Integer) sourceColor.elementAt(curLine)).intValue();

                        StyleConstants.setBackground(attr, ToolConstants.getColor(cor));
	                        
                        String lineNumber = new String("/* " + ToolConstants.getFourDigitNumber(curLine) + " */ ");

                        doc.insertString(doc.getLength(), lineNumber + line + nl, attr);

                        // To print the code in the standard output...
                        // System.out.println( lineNumber + line );
	                        
                        line = buffer.readLine();
                        curLine++;
                    }
                } catch (Exception e) {
                    ToolConstants.reportException(e, ToolConstants.STDERR);
                }
            }

            scrollPane.setViewportView(tp);
				
            setCaretByLine(toOffset);
            int row = tp.getStyledDocument().getDefaultRootElement().getElementIndex(tp.getCaretPosition());
            int end = tp.getStyledDocument().getDefaultRootElement().getElementIndex(tp.getDocument().getLength());

            String specificTool = parent.getActiveToolName();
	
            String criterion = parent.getActiveCriterionName();
	
            ssp.setContent(ToolConstants.toolName + ": " + specificTool, 
                    "File: " + className, "Line: " + (row + 1) + " of " + (end + 1), 
                    "Coverage: " + criterion, "Highlighting: All Priorized");
            ssp.setVisible(true);
        } else {
            // buffer = cl.getBytecode();
            JOptionPane.showMessageDialog(null,
                    "No source file available for the selected class file: " + className,
                    "Warning...",
                    JOptionPane.WARNING_MESSAGE);
        }
        return this;
    }
		
    public void setCaretByLine(int line) {
        System.out.println("Going to line" + line);
        if (tp != null && line >= 0) {
            	
            if (line > 4) {
                line -= 5;
            }
            	
            Element el = tp.getStyledDocument().getDefaultRootElement().getElement(line);

            if (el == null) {
                return;
            }
            int pos = el.getStartOffset();
				
            tp.setCaretPosition(pos);
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
            // tp.getCaret().setDot( pos );
            // tp.requestFocus();
        }
    }
		
    public int getCaretLine() {
        if (tp != null) {
            return tp.getStyledDocument().getDefaultRootElement().getElementIndex(tp.getCaretPosition());
        }
        return 0;
    }
        
    private void sourcePanel_mouseClicked(MouseEvent e) {
        int line = 0;
        //if (e.isPopupTrigger()) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            int pos = tp.viewToModel(new Point(e.getX(), e.getY()));

            tp.setCaretPosition(pos);
        }
        	
        line = tp.getStyledDocument().getDefaultRootElement().getElementIndex(tp.getCaretPosition());
        // TP line number starts at 0, bytecode starts at 1.
        line++;
        System.out.println("LineNr: " + line);

        // Finding the corresponding class file
        
        ClassFile cf = prj.getClassFile(prj.getCurClassName());
        Method[] methods = cf.getMethods();
        int methodId = -1;
        ClassMethod method = null;

        for (int i = 0; i < methods.length && methodId == -1; i++) {
            String methodName = methods[i].getName() + methods[i].getSignature();
	
            method = cf.getMethod(methodName);
            if ((line >= method.getBeginSourceLine())
                    && (line <= method.getEndSourceLine())) {
                methodId = method.getMethodId();
            }
        }

        // Method found...
        if (methodId != -1) {
            Integer mId = new Integer(methodId);

            if (!WeightColor.getClassWeights().containsKey(mId)) { 
                return;
            }

            Integer ln = new Integer(line);
	            
            if (JabutiGUI.isAllPrimaryEdges() || JabutiGUI.isAllSecondaryEdges()) {            	
                if (!lineNodeTable.containsKey(ln)) {
                    return;
                }
	            	
                // Hashtable fromTable = (Hashtable) classWeights.get( mId );
	            	
                String label = (String) lineNodeTable.get(ln);
	
                System.out.println("Showing definition of NODE: " + label);
	
                GraphNode gn = method.getGraphNodeByLabel(label);
	            	            	
                if (gn == null) {
                    return;
                }
	
                if (SelectedPoint.set(true, gn, methodId, null)) {
                    parent.updatePane();
                }
            } else {
                //if (e.isPopupTrigger()) {
            	if (e.getButton() == MouseEvent.BUTTON3) {
                    HashSet varDef = WeightColor.getVariableSetFromPosition(methodId, ln);

                    if (varDef != null) {
                        JPopupMenu varPopup = new JPopupMenu();
	             			
                        Iterator it = varDef.iterator();

                        while (it.hasNext()) {
                            Vector nodeVar = (Vector) it.next();
                            GraphNode node = (GraphNode) nodeVar.elementAt(0);
                            String varName = (String) nodeVar.elementAt(1);
                            Integer wgt = WeightColor.getVariableDefinitionWeight(methodId, node, varName);
                            
                            String varSrcName = method.getVariableSourceName(varName, ((CFGNode) node).getDefinitionOffset(varName).intValue());
							         					
                            MyJMenuItem item = new MyJMenuItem(methodId, node, varName, varSrcName + " (" + varName + ")");

                            // item.setToolTipText( node.getLabel() );
                            item.setBackground(ToolConstants.getColor(WeightColor.getColorByWeight(WeightColor.getColorBarLabels(), wgt)));
                            item.addActionListener(new java.awt.event.ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                            MyJMenuItem item = (MyJMenuItem) e.getSource();
                                            int mId = item.getMethodId();
                                            String methodName = prj.getClassFile( 
                                                            prj.getCurClassName()).getMethod(mId).getMethodName();
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
                    Vector nodeVar = WeightColor.getWeightestVariableFromPosition(methodId, ln);

                    if (nodeVar == null) {
                        return;
                    }
                        	
                    GraphNode selectedGn = (GraphNode) nodeVar.elementAt(0);
                    String selectedVar = (String) nodeVar.elementAt(1);
	
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
