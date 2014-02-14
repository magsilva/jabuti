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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.bcel.classfile.Method;

import br.jabuti.criteria.Criterion;
import br.jabuti.criteria.DefUse;
import br.jabuti.criteria.Node;
import br.jabuti.graph.CFG;
import br.jabuti.graph.CFGNode;
import br.jabuti.graph.GraphNode;
import br.jabuti.project.ClassFile;
import br.jabuti.project.ClassMethod;
import br.jabuti.project.JabutiProject;
import br.jabuti.project.TestSet;
import br.jabuti.util.ToolConstants;

/**
 * This is resonsable to calculate the weights and
 * the colors of a given class, considering the
 * different testing criteria.
 *
 * Since a sigle class can be enabled at one time,
 * variables and methods of this class are static.
 *
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
public class WeightColor {

    // This variable controls the color bottons associated
    // to be displayed
    static private Hashtable colorButtonTable = new Hashtable(); 
    
    // This hashtable store decions or definition relations
    // for each method and is used to present the information
    // in the GUI
    static private Hashtable classWeights;

    // This hashtable stores variables defined in a given CFG Node.
    // Since more than one variable can be located at the same
    // bytecode offset or the same source line, this hash table
    // is use to present the set of variables located at a
    // given point. It is used by the BytecodePanel and/or SourcePanel
    // when the all-uses criterion is selected
    static private Hashtable classVariables;    

	/** This method update the color attributes based on the 
	 * weights of the curret class file, identified from 
	 * {@link JabutiProject#getCurClassName}
	 */
    static public void updateColorAttributes( ) {
    	String className = JabutiGUI.getProject().getCurClassName();
    	
        ClassFile cf = JabutiGUI.getProject().getClassFile(className);
	                
        Method[] methods = cf.getMethods();

        HashSet weightSet = new HashSet();
        
        Vector failSets = new Vector();
        Vector successSets = new Vector();
        int colors[] = new int[] {0};
        int inc = 0;
		
        if (JabutiGUI.isCoverage()) {
            if (JabutiGUI.isAllPriorized()) {
                if ( JabutiGUI.isAllPrimaryNodes() || JabutiGUI.isAllSecondaryNodes() ) {
        
                    weightSet = cf.getWeightByNode(JabutiGUI.mainWindow().getActiveCriterionId());
                    classWeights = null;
                } else {
                    if ( JabutiGUI.isAllPrimaryEdges() || JabutiGUI.isAllSecondaryEdges() ) {
                        classWeights = cf.getWeightByEdge(JabutiGUI.mainWindow().getActiveCriterionId());
                        weightSet = WeightColor.weightSetForEdges(cf, classWeights);
                    } else if ( JabutiGUI.isAllPrimaryUses() || 
                                JabutiGUI.isAllSecondaryUses() ||
                                JabutiGUI.isAllPrimaryPotUses() || 
                                JabutiGUI.isAllSecondaryPotUses() ) {
                        classWeights = cf.getWeightByUse(JabutiGUI.mainWindow().getActiveCriterionId());

                        // Updating the weight of each CFG node, based on
                        // the weight of each variable definition
                        weightSet = WeightColor.weightSetForUses(cf, classWeights);
                    }
                }
                WeightColor.colorButtonTable = computeColors( weightSet );
            }
        } else if ( JabutiGUI.isSlice() ) {
            WeightColor.colorButtonTable = computeColorsFailSuccess(TestSet.getFailSet(), TestSet.getSuccessSet());
            if ((TestSet.getFailSet().size() < 1)
                    || (TestSet.getFailSet().size() > 2)) {
                JOptionPane.showMessageDialog(null,
                        "One or two failed test case and at most two success test cases must be selected! ",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                if ((TestSet.getFailSet().size() == 1)
                        && (TestSet.getSuccessSet().size() == 0)) {
                    inc = 1;
                    colors = new int[] {ToolConstants.COLOR_1, ToolConstants.COLOR_9};
                } else if ((TestSet.getFailSet().size() == 1)
                        && (TestSet.getSuccessSet().size() == 1)) {
                    inc = 2;
                    colors = new int[] {ToolConstants.COLOR_1, ToolConstants.COLOR_6, ToolConstants.COLOR_9};
                } else if ((TestSet.getFailSet().size() == 1)
                        && (TestSet.getSuccessSet().size() == 2)) {
                    inc = 3;
                    colors = new int[] {ToolConstants.COLOR_1, ToolConstants.COLOR_3, ToolConstants.COLOR_6, ToolConstants.COLOR_9};
                }
                if ((TestSet.getFailSet().size() == 2)
                        && (TestSet.getSuccessSet().size() == 0)) {
                    inc = 1;
                    colors = new int[] {ToolConstants.COLOR_1, ToolConstants.COLOR_6, ToolConstants.COLOR_9};
                } else if ((TestSet.getFailSet().size() == 2)
                        && (TestSet.getSuccessSet().size() == 1)) {
                    inc = 2;
                    colors = new int[] {ToolConstants.COLOR_1, ToolConstants.COLOR_3, ToolConstants.COLOR_4, ToolConstants.COLOR_6, ToolConstants.COLOR_9};
                } else if ((TestSet.getFailSet().size() == 2)
                        && (TestSet.getSuccessSet().size() == 2)) {
                    inc = 3;
                    colors = new int[] {ToolConstants.COLOR_1, ToolConstants.COLOR_3, ToolConstants.COLOR_4, ToolConstants.COLOR_5, ToolConstants.COLOR_6, ToolConstants.COLOR_7, ToolConstants.COLOR_9};
                }
            }
        } else {
            colorButtonTable = new Hashtable();
        }

        // Building the labes of the color bar valid for all methods...
        int[] labels = getColorBarLabels();
	    
        // Setting the CFG node color for all methods...
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName() + methods[i].getSignature();

            ClassMethod method = cf.getMethod(methodName);
	
            CFG cfg = method.getCFG();
            GraphNode[] fdt = cfg.findDFT(true);
	
            int c = ToolConstants.COLOR_0;

            if ((JabutiGUI.isSlice()) && (colorButtonTable.size() > 0)) {
                
                failSets = new Vector();
                successSets = new Vector();

                if ((TestSet.getFailSet().size() >= 1)
                        && (TestSet.getFailSet().size() <= 2)) {
                    Iterator itFail = TestSet.getFailSet().iterator();
                    
                    HashSet allFail = new HashSet();
                    
                    while (itFail.hasNext()) {
                        String tcLabel = (String) itFail.next();
                    	
                        // Getting the complete set of covered nodes
						// PRIMARY
                        HashSet currentSet = method.getCoveredRequirementsByTestCase(
                                Criterion.PRIMARY_NODES,
								tcLabel);
                        // and SECONDARY
                        currentSet.addAll( method.getCoveredRequirementsByTestCase(
                                Criterion.SECONDARY_NODES,
								tcLabel) );

                        failSets.add(currentSet);
                        allFail.addAll(currentSet);
                    }
                    
                    Iterator itSucc = TestSet.getSuccessSet().iterator();

                    while (itSucc.hasNext()) {
                        String tcLabel = (String) itSucc.next();
                    	
                        // Getting the complete set of covered nodes
						// PRIMARY
                        HashSet currentSet = method.getCoveredRequirementsByTestCase(
                                Criterion.PRIMARY_NODES,
                                tcLabel);
                        // and SECONDARY
						currentSet.addAll( method.getCoveredRequirementsByTestCase(
                                Criterion.SECONDARY_NODES,
								tcLabel) );

                        currentSet.retainAll(allFail);
                        successSets.add(currentSet);
                    }
                }
            }

            for (int x = 0; x < fdt.length; x++) {
                GraphNode gn = fdt[x];

                if ((JabutiGUI.isCoverage()) && (colorButtonTable.size() > 0)) {
               	
                    Integer wgt = ((Integer) gn.getUserData(ToolConstants.LABEL_WEIGHT));
                    
                    c = getColorByWeight(labels, wgt);
	
                } else if ((JabutiGUI.isSlice())
                        && (colorButtonTable.size() > 0)) {
                    
                    int weight = 0;
                    Node tmpNode = new Node(gn.getLabel());
                    
                    for (int k = 0; k < failSets.size(); k++) {
                        if (((HashSet) failSets.elementAt(k)).contains(tmpNode)) {
                            weight += inc;
                        }
                    }

                    for (int k = 0; k < successSets.size(); k++) {
                        if (((HashSet) successSets.elementAt(k)).contains(tmpNode)) {
                            weight -= 1;
                        }
                    }
                    
                    c = colors[weight];
                }

                // Setting the new node color	     		
                gn.setUserData(ToolConstants.LABEL_COLOR, new Integer(c));                
                
                // In case of a selected Node...
                if ( SelectedPoint.isSelected() && 
                     SelectedPoint.getMethod() == method.getMethodId() ) {
                    if ( gn.getLabel().equals( SelectedPoint.getNodeLabel() ) ) {
                        // Setting the new node color
                        SelectedPoint.assignToNode( ToolConstants.LABEL_COLOR, 
                                                   new Integer(ToolConstants.SELECTED) );
                    }
                }
            }
        }
    }
    
    
    // This method update the weight of each CFGNode based on the weight
    // of each edge requirement.
    // It returns the set of weights of each CFGNode.
    static private HashSet weightSetForEdges(ClassFile cf, Hashtable mtdWeights) {
		
        HashSet wgtSet = new HashSet();
		
        Method[] mtds = cf.getMethods();
		
        // Calculating the weightSet...
        for (int i = 0; i < mtds.length; i++) {
            String methodName = mtds[i].getName() + mtds[i].getSignature();
			
            ClassMethod method = cf.getMethod(methodName);
				
            Hashtable fromHash = (Hashtable) mtdWeights.get(new Integer(method.getMethodId()));

            try {
                CFG cfg = method.getCFG();
							
                GraphNode[] fdt = cfg.findDFT(true);
                for (int j = 0; j < fdt.length; j++) {
                    GraphNode gn = fdt[j];
                    int fromWeight = -1;

                    if (fromHash.containsKey(gn)) {
                        Hashtable toHash = (Hashtable) fromHash.get(gn);
					        
                        Iterator itTo = toHash.keySet().iterator();

                        while (itTo.hasNext()) {
                            GraphNode theNode = (GraphNode) itTo.next();
                            Integer wgt = (Integer) toHash.get(theNode);
						                		
                            // Adding this weight to the weight set
                            wgtSet.add(wgt);
					                		
                            // Updating the weight of the from Node
                            // considering the weight of each child
                            int curWgt = wgt.intValue();

                            if (fromWeight < curWgt) {
                                fromWeight = curWgt;
                            }
                        }
                    }
					                
                    if (fromWeight == -1 || SelectedPoint.isSelected() ) {
                        gn.removeUserData(ToolConstants.LABEL_WEIGHT);
                    } else {
                        gn.setUserData(ToolConstants.LABEL_WEIGHT, new Integer(fromWeight));
                    }
                }

                if ( SelectedPoint.isSelected() && 
                     (SelectedPoint.getMethod() == method.getMethodId() ) ) {
                    if (fromHash.containsKey( SelectedPoint.getNode() )) {
                        Hashtable toHash = (Hashtable) fromHash.get( SelectedPoint.getNode() );

                        for (int j = 0; j < fdt.length; j++) {
                            GraphNode gn = fdt[j];
								
                            if ( (method.getMethodId() == SelectedPoint.getMethod() )
                                    && (toHash.containsKey(gn))) {
                                Integer wgt = (Integer) toHash.get(gn);

                                gn.setUserData(ToolConstants.LABEL_WEIGHT, new Integer(wgt.intValue()));
                            } else {
                                gn.removeUserData(ToolConstants.LABEL_WEIGHT);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                ToolConstants.reportException(e, ToolConstants.STDERR);
                return null;
            }
        }
        return wgtSet;
    }

    // This method update the weight of each CFGNode based on the weight
    // of def-use requirement.
    // It returns the set of weights of each CFGNode.	
    static private HashSet weightSetForUses(ClassFile cf, Hashtable mtdWeights) {
		
        HashSet wgtSet = new HashSet();
		
        Method[] mtds = cf.getMethods();
		
        // Calculating the weightSet...
        for (int i = 0; i < mtds.length; i++) {
            String methodName = mtds[i].getName() + mtds[i].getSignature();
			
            ClassMethod method = cf.getMethod(methodName);

            Hashtable defUseTable = (Hashtable) mtdWeights.get(new Integer(method.getMethodId()));
			
            CFG cfg = null;
			
            try {
                cfg = method.getCFG();
            } catch (Exception e) {
                ToolConstants.reportException(e, ToolConstants.STDERR);
                return null;
            }

            if (cfg == null) {
                return null;
            }
				
            GraphNode[] fdt = cfg.findDFT(true);

            // Giving color for each definition node
            // based on the weight of its defined variables.				
            for (int j = 0; j < fdt.length; j++) {
                GraphNode gn = fdt[j];
                int defWeight = -1;

                if (defUseTable.containsKey(gn)) {
                    Hashtable defTable = (Hashtable) defUseTable.get(gn);
					        
                    Iterator defs = defTable.keySet().iterator();
                    // Locating the variable with the highest weight
                    while (defs.hasNext()) {
                        String defVar = (String) defs.next();
						
                        Integer wgt = getVariableDefinitionWeight(method.getMethodId(), gn, defVar);

                        if (wgt != null) {
                            // Finding how many variables are located
                            // at the same offset/line
                            buildVariablePositionTable(method.getMethodId(), gn, defVar);
							
                            // Adding this weight to the weight set
                            wgtSet.add(wgt);
					                		
                            // Updating the weight of the from Node
                            // considering the weight of each child
                            int curWgt = wgt.intValue();

                            // Each node can have more than one definition
                            // the weightestDefinition stores the offset of the
                            // definition with the highest weight
                            if (defWeight < curWgt) {
                                defWeight = curWgt;
                                if ( SelectedPoint.isSelected()
                                        && ( SelectedPoint.getMethod()
                                        == method.getMethodId() )
                                        && ( gn.getLabel().equals( SelectedPoint.getNodeLabel() ) ) ) {
                                }
                            }
                        }
                    }
                }
                // Clean the weight label when the node has no variable definition 
                if (defWeight == -1 || SelectedPoint.isSelected() ) {
                    gn.removeUserData(ToolConstants.LABEL_WEIGHT);
                } else { // Setting the color with the highest weight
                    gn.setUserData(ToolConstants.LABEL_WEIGHT, new Integer(defWeight));
                }
            }

            // When a definition node is selected, its color change to
            // ligth blue and only the uses w.r.t the highest weight definition
            // is painted considering the corresponding use weight.
            // The highestSelectedVar variable stores which one is the variable
            // with the highest weight in the selected node.
            if ( SelectedPoint.isSelected() ) {
                if ( SelectedPoint.getMethod() == method.getMethodId() ) {
                    if (defUseTable.containsKey( SelectedPoint.getNode() ) ) {              	
                        Hashtable defTable = (Hashtable) defUseTable.get( SelectedPoint.getNode() );
	                	
                        // Getting the uses of the selected definition
                        // Hashtable useTable = (Hashtable) defTable.get( highestSelectedVar );
                        Hashtable useTable = (Hashtable) defTable.get( SelectedPoint.getVariable() );
	                    
                        Iterator itDu = useTable.keySet().iterator();
						
						System.out.print( "\tAssociations: ");
                        while (itDu.hasNext()) {
                            DefUse du = (DefUse) itDu.next();
                            
                            Integer wgt = (Integer) useTable.get(du);
	                    	
                            // Changing the color of the first node (c-use)
                            String useLabel = du.getUseFrom();
                            GraphNode gn = method.getGraphNodeByLabel(useLabel);
	                    	
                            Integer curWgt = (Integer) gn.getUserData(ToolConstants.LABEL_WEIGHT);
	                    	
                            if ((curWgt == null)
                                    || (curWgt.intValue() < wgt.intValue())) {
                                gn.setUserData(ToolConstants.LABEL_WEIGHT, new Integer(wgt.intValue()));
                            }
                            
                            // Changing the color of the second node (p-use), if any
                            useLabel = du.getUseTo();
                            if ( useLabel != null ) {
	                            gn = method.getGraphNodeByLabel(useLabel);
		                    	
	                            curWgt = (Integer) gn.getUserData(ToolConstants.LABEL_WEIGHT);
		                    	
	                            if ((curWgt == null)
	                                    || (curWgt.intValue() < wgt.intValue())) {
	                                gn.setUserData(ToolConstants.LABEL_WEIGHT, new Integer(wgt.intValue()));
	                            }
	                        }
                        }
                    }
                }
            }
        }
        return wgtSet;
    }

	/**
	 * This method is responsible to convert weights in colors
	 * when the coverage tool is selected.
	 */
    static private Hashtable computeColors(HashSet weight) {
        Object[] values = weight.toArray();
	
        Arrays.sort(values);

        Hashtable color = new Hashtable();
        
        if ( values.length > 0 ) {
	        int mostWeight = ((Integer) values[values.length - 1]).intValue();
	        
	        if ( values.length < 9 ) {
	        	for (int i = values.length - 1, 
	        	         j = ToolConstants.COLOR_9; 
	        	         i >= 0; i--, j-- ) {
	        	    Integer l = new Integer(((Integer)values[i]).intValue());
	        		color.put( l, new Integer(j));
	        	}
                color.put(new Integer(0), new Integer(ToolConstants.COLOR_1));
	        } else {
	            int delta = mostWeight / 6;
					
	            color.put(new Integer(0), new Integer(ToolConstants.COLOR_1));
	            color.put(new Integer(1), new Integer(ToolConstants.COLOR_2));
					
	            for (int i = 1 + delta, j = 3; (i < mostWeight)
	                    && (j < ToolConstants.COLOR_9); i += delta, j++) {
	                color.put(new Integer(i), new Integer(j));
	            }
					
	            color.put(new Integer(mostWeight), new Integer(ToolConstants.COLOR_9));
	        }
	    }
        return color;
    }

	/**
	 * This method is responsible to convert weights in colors
	 * when the slice tool is selected.
	 */
    static private Hashtable computeColorsFailSuccess(HashSet failSet, HashSet successSet) {
		
        Hashtable color = new Hashtable();
		
        if (failSet.size() == 1) {
            if (successSet.size() == 0) {
                color.put(new Integer(0), new Integer(1)); // white - non-executed
                color.put(new Integer(1), new Integer(9)); // red - one failed
            } else if (successSet.size() == 1) {
                color.put(new Integer(0), new Integer(1)); // white - non-executed
                color.put(new Integer(1), new Integer(6)); // green - one fail one success
                color.put(new Integer(2), new Integer(9)); // red - only failed
            } else if (successSet.size() == 2) {
                color.put(new Integer(0), new Integer(1)); // white - non-executed
                color.put(new Integer(1), new Integer(3)); // dark cyan - one fail one success
                color.put(new Integer(2), new Integer(6)); // yellow - one fail two success
                color.put(new Integer(3), new Integer(9)); // red - only failed
            }
        } else if (failSet.size() == 2) {
            if (successSet.size() == 0) {
                color.put(new Integer(0), new Integer(1)); // white - non-executed
                color.put(new Integer(1), new Integer(6)); // green - one failed
                color.put(new Integer(2), new Integer(9)); // red - two failed
            } else if (successSet.size() == 1) {
                color.put(new Integer(0), new Integer(1)); // white - non-executed
                color.put(new Integer(1), new Integer(3)); // dark cyan - one failed one success
                color.put(new Integer(2), new Integer(4)); // green - one failed
                color.put(new Integer(3), new Integer(6)); // yellow - two failed one sucess
                color.put(new Integer(4), new Integer(9)); // red - two failed
            } else if (successSet.size() == 2) {
                color.put(new Integer(0), new Integer(1)); // white - non-executed
                color.put(new Integer(1), new Integer(3)); // dark cyan - one failed two success
                color.put(new Integer(2), new Integer(4)); // green - one failed one success
                color.put(new Integer(3), new Integer(5)); // olive - one failed
                color.put(new Integer(4), new Integer(6)); // yellow - two failed two success
                color.put(new Integer(5), new Integer(7)); // dark yellow - two failed one success
                color.put(new Integer(6), new Integer(9)); // red - two failed
            }
        } else {
            color.put(new Integer(0), new Integer(1));
        }
        return color;
    }
    
 
    /** 
     * This method returns a vector with the labels of the
     * current color bar. It uses the colorButtonTable
     * data structure to identify such labels.
     */
    static public int[] getColorBarLabels() {
        // Building the labes of the color bar valid for all methods...
        Object[] keySet = colorButtonTable.keySet().toArray();

        Arrays.sort(keySet);
        int[] labels = new int[colorButtonTable.size()];

        for (int i = 0; i < keySet.length; i++) {
            labels[i] = ((Integer) keySet[i]).intValue();
        }
		
        return labels;
    }
 
    /**
     * This method returns a vector with the labels of the
     * current color bar. It uses the globalButtonTable
     * datastructure to do this.
     */
    static public int getColorValue(Integer label) {
        return ((Integer) colorButtonTable.get(label)).intValue();
    }

    /**
     * This method returns the int value that represents the
     * given color from a given weight. Labels is a set of
     * integer that represents the color bar values.
     */
    static public int getColorByWeight(int[] labels, Integer wgt) {
        int weight = 0;
        int c;

        if (wgt != null) {
            weight = wgt.intValue();

            if (weight > 0) {
                int j = 0;
		
                while (labels[j] < weight) {
                    j++;
                }
						     
                int k = weight - labels[j - 1];
                int l = labels[j] - weight;
						     			
                // Computing the color w.r.t. this weight
                int index = 0;
		
                c = 0;
                if (k > l) {
                    index = j;
                } else {
                    index = j - 1;
                }
				     			
                Integer wc = ((Integer) colorButtonTable.get(new Integer(labels[index])));
				     			
                if (wc != null) {
                    c = wc.intValue();
                }
            } else {
                c = ToolConstants.COLOR_1;
            }
        } else {
            c = ToolConstants.COLOR_0;
        }
        return c;
    }


    /**
     * This method builds the hashtable (classVariables) responsible
     * to store the set of variables avaliable at a given bytecode offset or
     * source line.
     */
    static private void buildVariablePositionTable(int methodId, GraphNode gn, String defVar) {
        ClassFile cf = JabutiGUI.getProject().getClassFile( JabutiGUI.getProject().getCurClassName() );
		
        if (classVariables == null) {
            classVariables = new Hashtable();
        }

        Integer defVarOffset = ((CFGNode) gn).getDefinitionOffset( defVar );
        Integer position = defVarOffset;

        // If the source panel is active, use the source line as position
        if (JabutiGUI.isSourcePanel()) {
            ClassMethod cm = cf.getMethod(methodId);
            position = new Integer(cm.bytecodeOffset2SourceLine(defVarOffset.intValue()));
        }

        Integer mId = new Integer(methodId);
		
        Hashtable positionTable = null;
		
        if (classVariables.containsKey(mId)) {
            positionTable = (Hashtable) classVariables.get(mId);
        } else {
            positionTable = new Hashtable();
            classVariables.put(mId, positionTable);
        }
			
        HashSet varSet = null;
		
        if (positionTable.containsKey(position)) {
            varSet = (HashSet) positionTable.get(position);
        } else {
            varSet = new HashSet(3);
            positionTable.put(position, varSet);
        }
		
        Vector nodeVar = new Vector(2);

        nodeVar.add(gn);
        nodeVar.add(defVar);
				
        varSet.add(nodeVar);
    }

    /** 
     * This method returns the set of defined variables in a given
     * position. The position can be a bytecode offset or the
     * source line number, depends on with panel is active.
     */
    static HashSet getVariableSetFromPosition(int methodId, Integer position) {
        Integer mId = new Integer(methodId);
		
        Hashtable positionTable = null;
        HashSet varSet = null;

		//System.out.println("*****************************************");
		//WeightColor.printClassVariableTable( mId );
		//System.out.println("*****************************************");
				
        if (classVariables != null && classVariables.containsKey(mId)) {
            positionTable = (Hashtable) classVariables.get(mId);
        } else {
            return null;
        }

        if (positionTable.containsKey(position)) {
            varSet = (HashSet) positionTable.get(position);
        } else {
            return null;
        }
        return varSet;
    }


	public static void printClassVariableTable( Integer mId ){
        Hashtable positionTable = null;
        HashSet varSet = null;

        if (classVariables != null && classVariables.containsKey(mId)) {
            positionTable = (Hashtable) classVariables.get(mId);
        } else {
            return;
        }
		
    	Iterator it = positionTable.keySet().iterator();
    	while ( it.hasNext() ) {
    		Integer pos = (Integer) it.next();
    		    		
    		varSet = (HashSet) positionTable.get( pos  );
    		Iterator it2 = varSet.iterator();
         	while ( it2.hasNext() ) {
         		Vector vet = (Vector) it2.next();
         		GraphNode gn = (GraphNode) vet.elementAt(0);
         		String defVar = (String) vet.elementAt(1);
         	}
        }         
	}

    /**
     * This method returns defined variable with the highest weight
     * from a set of defined variables in a given method position.
     */
    static Vector getWeightestVariableFromPosition(int methodId, Integer position) {
        Vector theVar = null;		
        HashSet varSet = getVariableSetFromPosition(methodId, position);
        int weight = -1;
		
        if (varSet != null) {
            Iterator it = varSet.iterator();

            while (it.hasNext()) {
                Vector nodeVar = (Vector) it.next();
                Integer curWgt = getVariableDefinitionWeight(methodId, 
                        (GraphNode) nodeVar.elementAt(0),
                        (String) nodeVar.elementAt(1));

                if ((theVar == null) || (weight < curWgt.intValue())) {
                    weight = curWgt.intValue();
                    theVar = nodeVar;
                }
            }
        }
        return theVar;
    }

    /**
     * This method returns the set of defined variables in a given
     * position. The position can be a bytecode offset or the
     * source line number, depends on with panel is active.
     */
    static public Set getVariableSetFromGraphNode(int methodId, GraphNode gn) {
        Hashtable defUseTable = (Hashtable) classWeights.get(new Integer(methodId));

        if (defUseTable == null) {
            return null;
        }
    		
        Hashtable defTable = (Hashtable) defUseTable.get(gn);

        if (defTable == null) {
            return null;
        }

        return defTable.keySet();
    }

    /**
     * This method returns defined variable with the highest weight
     * from a set of defined variables in a given method position.
     */
    static public String getWeightestVariableFromGraphNode(int methodId, GraphNode gn) {
        Set varSet = getVariableSetFromGraphNode(methodId, gn);
		
        String theVar = null;
        int weight = -1;

        if (varSet != null) {		
            Iterator itDef = varSet.iterator();

            while (itDef.hasNext()) {
                String varName = (String) itDef.next();
                Integer varWgt = getVariableDefinitionWeight(methodId, gn, varName);

                if (weight < varWgt.intValue()) {
                    // ERROR: remove the line below...
                    // Consequente: always returns the last variable,
                    // not necessarily the one with the 
                    // highest weight
                    weight = varWgt.intValue();
                    theVar = varName;
                }
            }
        }
        return theVar;
    }

    /**
     * This method returns the weight of a given defined variable in a 
     * given node, cosidering the weight of each use requirement.
     */
    static public Integer getVariableDefinitionWeight(int methodId, GraphNode gn, String defVar) {

        Integer defWgt = null;
		
        Hashtable defUseTable = (Hashtable) classWeights.get(new Integer(methodId));

        if (defUseTable == null) {
            return null;
        }
    		
        Hashtable defTable = (Hashtable) defUseTable.get(gn);

        if (defTable == null) {
            return null;
        }
			
        // Getting the uses of the selected definition
        Hashtable useTable = (Hashtable) defTable.get(defVar);

        if (useTable == null) {
            return null;
        }
	                    
        Iterator itDu = useTable.keySet().iterator();

        while (itDu.hasNext()) {
            DefUse du = (DefUse) itDu.next();
	                    	
            Integer wgt = (Integer) useTable.get(du);
	                    	
            if ((defWgt == null) || (defWgt.intValue() < wgt.intValue())) {
                defWgt = wgt;
            }
        }
        return defWgt;
    }
 
    static public void clearClassVariablesTable() {
        if (classVariables != null) {
            classVariables.clear();
            classVariables = null;
            System.gc();
        }
    }
    
	/*
	* This method returns the hashtable that store the
	* values of the colors to paint each code/bytecode line
	*/
	static public Hashtable getColorButtonTable() {
		return colorButtonTable;
	}

	/*
	* This method returns the hashtable that store the
	* set of variables found in the current class file.
	*/
	static public Hashtable getClassVariableTable() {
		return classVariables;
	}

	/*
	* This method returns the hashtable that store the
	* corresponding weights for the current class
	*/
	static public Hashtable getClassWeights() {
		return classWeights;
	}
}