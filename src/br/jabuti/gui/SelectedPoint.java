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

import java.util.Hashtable;

import br.jabuti.graph.GraphNode;

/**
 * This class store the global about a given point
 * in a given representation that was selected.
 * It reffers to a CFG node and it is used by 
 * different testing criteair to indicate, for example,
 * the set of variables defined in that point.
 *
 * Only one point can be selected at a time, so all the
 * variables and methods of this class are static.
 * 
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 */
public class SelectedPoint {

    // This variables controls 
    // when a given node (decision or definition node) is selected
    static private boolean nodeIsSelected = false;
    static private GraphNode selectedNode = null;
    static private int selectedMethod = -1;
    static private String selectedVariable = null;
	
 
    /**
     * This method is used to indicate that a given CFG node
     * has been selected.
     * 
     * Returns true if the selected node is a valid decision or
     * definition node.
     */
    static public boolean set(boolean selected, GraphNode gn, int methodId, String var) {
        if (selected) {
            Hashtable fromTable = (Hashtable) WeightColor.getClassWeights().get(new Integer(methodId));

            if (fromTable.containsKey(gn)) {
                nodeIsSelected = true;
                selectedNode = gn;
                selectedMethod = methodId;
                selectedVariable = var;
                return true;
            }
        }
        
        SelectedPoint.reset();

        return false;
    }
 
    // Close the project by seting all variables with null
    public static void reset() {
        nodeIsSelected = false;
        selectedNode = null;
        selectedMethod = -1;
        selectedVariable = null;
    }
    
    // Checks if there is a valid point selected
    public static boolean isSelected() {
    	return ( nodeIsSelected );
    }

    // Return an instance to the current selected CFG Node
    public static GraphNode getNode() {
    	return selectedNode;
    }

    // Returns the name (Label) of the current selected CFG Node
    public static String getNodeLabel() {
    	return ( selectedNode != null )? selectedNode.getLabel() : null ;
    }

    // Returns the name (Label) of the current selected CFG Node
    public static void assignToNode( String key, Object value ) {
    	if ( selectedNode != null )
    		selectedNode.setUserData( key, value );
    }

    // Returns the value assigned to the selected node
    public static Object recoverFromNode( String key ) {
    	if ( selectedNode != null )
    		return selectedNode.getUserData( key );
    	return null;
    }

    // Return the number of the current selected method
    public static int getMethod() {
    	return selectedMethod;
    }

    // Return the name of the current selected variable
    public static String getVariable() {
    	return selectedVariable;
    }
}