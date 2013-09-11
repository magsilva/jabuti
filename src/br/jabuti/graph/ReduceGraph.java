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


import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;


/**
 This is a class used to construct a pre- or pos-dominator
 tree from a CFG. 

 @version: 0.00001
 @author: Marcio Delamaro

 */
public class ReduceGraph extends Graph {

    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -8295291795947168930L;
	Hashtable hs;
	
    protected ReduceGraph() {
        super();
        hs = new Hashtable();
    }
 	 	
    /** Returns the {@link ReduceNode} in this Graph that
     *   contains the given node in the original graph 
     *
     * @param gn - The {@link GraphNode} to be searched
     * @return - The node in this object where the node passed
     * as argument is. null if not found
     */
    public ReduceNode getReduceNodeOf(GraphNode gn) {
        return (ReduceNode) hs.get(gn);
    }
 
    /** Adds the node in the graph. 
     */
    public void add(ReduceNode rn) {
        super.add(rn);
        GraphNode gns[] = rn.getOriginalNodes();

        for (int i = 0; i < gns.length; i++) {
            hs.put(gns[i], rn);
        }
    }

    /** Inserts a node in a reduced node, part of this Graph
     */
    public void put(ReduceNode rn, GraphNode orig) {
        rn.add(orig);
        hs.put(orig, rn);
    }
	
    /** This method creates a new Graph, where each node is a 
     * strongly connected component of a given graph
     *
     * @param g - the graph to be reduced
     * @param sec - if secondary edges should be used 
     * @return the reduced graph
     **/

    static public ReduceGraph reduceSCC(Graph g, boolean sec) {
        HashSet v[] = g.computeSCC(sec);

        return reduce(v, g, sec);
    }

    /** Given an array of sets of {@link GraphNode}'s, creates 
     * a new {@link ReduceGraph} where each set correspond to 
     * a node.
     *
     * @param v - the array of sets. Each set corresponds to a 
     * node in the new graph.
     * @param g - the graph to be reduced
     * @param sec - if secondary edges should be considered
     * @return the reduced graph.
     */
    static public ReduceGraph reduce(HashSet hs[], Graph g, boolean sec) {
        ReduceGraph rd = new ReduceGraph();

        for (int i = 0; i < hs.length; i++) {
            ReduceNode rn = new ReduceNode(hs[i]);

            rd.add(rn);
        }
        // rd.setDefaultNumbering();
        for (int i = 0; i < rd.size(); i++) {
            ReduceNode rn = (ReduceNode) rd.elementAt(i);
            GraphNode inNodes[] = rn.getOriginalNodes();

            for (int j = 0; j < inNodes.length; j++) {
                Vector v = g.getNext(inNodes[j], sec);

                for (int k = 0; k < v.size(); k++) {
                    GraphNode gnex = (GraphNode) v.elementAt(k);
                    ReduceNode rdNex = rd.getReduceNodeOf(gnex);

                    if (rdNex != rn) {
                        rd.addPrimEdge(rn, rdNex);
                    }
                }
            }
        }
        return rd;
    }

}

