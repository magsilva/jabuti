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


package br.jabuti.graph;


import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;


/**
 * This class represents a node of a reduced graph.
 * It extends the {@link GraphNode} class and stores  
 * a set of other GraphNodes <BR>
 * 
 * More precisely each such object stores a set of 
 * objects that reprtesents nodes of other Graph. So
 * it can be used to reduce a graph according to
 * certain rulles.
 * 
 * @version <0.00001>
 * @author Marcio Delamaro
 *
 * @see GraphNode
 *
 */
public class ReduceNode extends GraphNode {
	
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -1457961592326481684L;
	/** This field holds the set of GraphNodes in the node
     */ 
    HashSet setOfNodes;

    /** Creates an empty node */
    public ReduceNode() {
        super();
        setOfNodes = new HashSet();
    }
	
    /** Creates from existing node */
    public ReduceNode(GraphNode x) {
        this();
        setOfNodes.add(x);
    }
	
    /** Creates from an existing set of nodes */
    public ReduceNode(Collection x) {
        this();
        setOfNodes.addAll(x);
    }

    /** Adds a (@link GraphNode} to the node 
     * 
     * @param x The {@link GraphNode} to be added to the  node.
     */
    protected void add(GraphNode x) {
        setOfNodes.add(x);
    }
	
    /** Gets the nodes in this node
     * 
     * @return The first {@link InstructionHandle} in the {@link CFGNode#instructions}
     *     vector
     */
    public GraphNode[] getOriginalNodes() {
        return (GraphNode[]) setOfNodes.toArray(new GraphNode[0]);
    }	

    public GraphNode getOriginalNode() {
        GraphNode x[] = getOriginalNodes();

        if (x == null || x.length == 0) {
            return null;
        }
        return x[0];
    }

    /** Implements {@link Comparator#compare}. Parameters must be
     * {@link ReduceNode} objects otherwise a cast exception is thrown.
     * So far there is no use to such method so we compare by the
     * number of elements in the node.
     *
     * @param x1 The first object to be compared
     * @param x2 The second object to be compared
     * 
     * @return < 0 if <code>x1</code> has fewer elements than <code>x2</code>
     * <BR>    > 0 if <code>x1</code> has more elements than <code>x2</code>
     * <BR>	0 if <code>x1</code> has the same number of elements of <code>x2</code>
     * 	
     */
    public int compare(Object x1, Object x2) {
        ReduceNode y1 = (ReduceNode) x1,
                y2 = (ReduceNode) x2;

        return y1.setOfNodes.size() - y2.setOfNodes.size();
    }
	
    /** Returns the toString() of the set of nodes it holds. 
     * Has to be changed... */
    public String toString() {
        String str = super.toString();

        str += "\nOriginal Nodes: ";
        GraphNode orig[] = getOriginalNodes();

        for (int i = 0; i < orig.length; i++) {
            str += " " + orig[i].getLabel();
        }
        return str;
    }
	
}

