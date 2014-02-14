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
import java.util.Iterator;
import java.util.Vector;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;


/**
 This is a class used to construct a pre- or pos-dominator
 tree from a CFG. 

 @version: 0.00001
 @author: Marcio Delamaro

 */
public class DominatorTree extends ReduceGraph {
	
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 8555812394502893222L;

	/** Creates a pre- or a pos-dominator tree for a
     * {@link Graph}. The set of dominator must have been creted
     * before calling this constructor.
     * @param g - the {@link Graph} to be analyzed
     * @param label - the label to access the dominator set of each
     * node in the original graph */
    public DominatorTree(Graph g, String label) {
        super();
        for (int i = 0; i < g.size(); i++) {
            GraphNode gn = (GraphNode) g.elementAt(i);
            DominatorTreeNode rn = new DominatorTreeNode(gn);

            add(rn); // inclui no no grafo
            if (g.isEntry(gn)) {
                setEntry(rn);
            }
        }
        for (int i = 0; i < g.size(); i++) {
            GraphNode gn = (GraphNode) g.elementAt(i);
            ReduceNode rn = getReduceNodeOf(gn);
            HashSet domHS = (HashSet) gn.getUserData(label);
            Iterator it = domHS.iterator();

            while (it.hasNext()) {
                GraphNode donmGN = (GraphNode) it.next();

                if (donmGN != gn) {
                    addPrimEdge(getReduceNodeOf(donmGN), rn);
                }
            }
        }
        removeComposite(false);
        computeExit(true);
    }
 	 	
    DominatorTree() {
        super();
    }

    public void merge(DominatorTree g) {
        for (int j = 0; j < size(); j++) {
            DominatorTreeNode rn = (DominatorTreeNode) elementAt(j);
            GraphNode gn = rn.getOriginalNode();
            ReduceNode rnOther = g.getReduceNodeOf(gn);
            Vector next = rnOther.getPrimNext();

            for (int i = 0; i < next.size(); i++) {
                DominatorTreeNode rnNextOther = (DominatorTreeNode) next.elementAt(i);
                GraphNode gnOther = rnNextOther.getOriginalNode();
                ReduceNode rnNext = getReduceNodeOf(gnOther);

                addPrimEdge(rn, rnNext); 
            }
        }
        removeExits();
    }
	
    public void markCovered(GraphNode x) {
        DominatorTreeNode rn = (DominatorTreeNode) getReduceNodeOf(x);

        if (rn == null) {
            return;
        }
        rn.setCovered(true);
    }

    public int getWeigth(DominatorTreeNode rn) {
        return getWeigth(rn, new HashSet());
    }
	
    private int getWeigth(DominatorTreeNode rn, HashSet hs) {
        if (rn == null) {
            return 0;
        }
        if (rn.getCovered()) {
            return 0;
        }
        if (hs.contains(rn)) {
            return 0;
        }
        int k = rn.getOriginalNodes().length;

        hs.add(rn);
        Vector ar = rn.getPrimArriving();

        for (int i = 0; i < ar.size(); i++) {
            DominatorTreeNode dtn = (DominatorTreeNode) ar.elementAt(i);

            k += getWeigth(dtn, hs);
        }
        return k;
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
        DominatorTree rd = new DominatorTree();

        for (int i = 0; i < hs.length; i++) {
            DominatorTreeNode rn = new DominatorTreeNode(hs[i]);

            rd.add(rn);
        }
        // rd.setDefaultNumbering();
        for (int i = 0; i < rd.size(); i++) {
            DominatorTreeNode rn = (DominatorTreeNode) rd.elementAt(i);
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

    public static void main(String args[])
            throws Exception {
    	
        JavaClass java_class;

        java_class = new ClassParser(args[0]).parse();	// May throw IOException
        ConstantPoolGen cp =
                new ConstantPoolGen(java_class.getConstantPool());
        ClassGen cg = new ClassGen(java_class);
        Method[] methods = java_class.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (args.length > 1 && !args[1].equals(methods[i].getName())) {
                continue;
            }
            System.out.println("\n\n--------------------------");
            System.out.println(methods[i].getName());
            System.out.println("--------------------------");
            MethodGen mg =
                    new MethodGen(methods[i], java_class.getClassName(), cp);
            CFG g = new CFG(mg, cg);

//            g.computeDefUse();
            
            g.print(System.out);
            RRDominator rrd = new RRDominator("Dominator");

            g.roundRobinAlgorithm(rrd, true);

            rrd = new RRDominator("IDominator");
            g.roundIRobinAlgorithm(rrd, true);

            RRLiveDefs rral = new RRLiveDefs("Alive definitions", RRLiveDefs.ALL);

            g.roundRobinAlgorithm(rral, true);
            
            System.out.println("\n\nPre dominator TREE ***************");
            DominatorTree dtDom = new DominatorTree(g, "Dominator");

            dtDom.setDefaultNumbering();
            dtDom.print(System.out);

            System.out.println("\n\nPos dominator TREE ***************");
            DominatorTree dtIDom = new DominatorTree(g, "IDominator");

            dtIDom.setDefaultNumbering();
            dtIDom.print(System.out);

            dtDom.merge(dtIDom);
            System.out.println("\n\nMerged dominator TREE ***************");
            dtDom.print(System.out);

            DominatorTree bbDom = (DominatorTree) reduceSCC(dtDom, false);

            bbDom.setEntry(bbDom.getReduceNodeOf(dtDom.getEntry()));
            bbDom.setDefaultNumbering();
            System.out.println("\n\nBasic Block Dominator TREE ***************");
            bbDom.print(System.out);
            
            System.out.println("\n\nFinal Basic Block Dominator TREE ***************");
            bbDom.removeComposite(false);
            bbDom.print(System.out);
            
            System.out.println("\n\nPesos dos nos ***************");
            for (int z1 = 0; z1 < bbDom.size(); z1++) {
                DominatorTreeNode dtn = (DominatorTreeNode) bbDom.elementAt(z1);

                System.out.println(dtn);
                System.out.println("\nPeso: " + bbDom.getWeigth(dtn));
            }
        }
    }

}

