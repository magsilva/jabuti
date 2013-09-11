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


import java.io.PrintStream;
import java.util.HashSet;
import java.util.Vector;


/**
 This is an abstract class that represents a program graph. 
 The basic operations like including/removing a node or an
 edge are implemented here. The graph may have 2 types of
 edges, a "primary" and a "secondary". The most imediate use
 of them is to use primery edge to indicate ordinary flow
 transfer and secondary to indicate exception transfer.

 @version: 0.00001
 @author: Marcio Delamaro

 
 */
abstract public class Graph extends Vector {
    private Vector entry;
    private Vector exit;	
	
    /** Creates an empty program graph */
    public Graph() {
        super();
        entry = new Vector();
        exit = new Vector();
    }

    public void setDefaultNumbering() {
        for (int i = size() - 1; i >= 0; i--) {
            GraphNode gfcn = (GraphNode) elementAt(i);

            gfcn.setNumber(i);
        }
    }
 	 	
    /**
     Prints information about each graph node. Uses the method
     {@link GraphNode#toString} to obtain the information to
     be displaied

     @param f Where to print

     */
    public void print(PrintStream f) {
        for (int i = 0; i < size(); i++) {
            GraphNode gfcn = (GraphNode) elementAt(i);

            f.println("" + gfcn.toString());
        }
    }
	
    /**
     Marks a node as an entry node. The graph can have several
     entry nodes. The method does not add the node to the graph,
     it should be added before calling this method

     @param x The node that will be an entry.

     */


    public void setEntry(GraphNode x) {
        entry.add(x);
    }
	
    /**
     Marks a node as an exit node. The graph can have several
     exit nodes. The method does not add the node to the graph,
     it should be added before calling this method

     @param x The node that will be the exit.

     */
    public void setExit(GraphNode x) {
        exit.add(x);
    }
	
    /** Removes an node as entry.

     @param x - The node to be removed from the  entry set
     */
    public void removeEntry(GraphNode x) {
        entry.removeElement(x);
    }
	
    /** Removes all entries.

     */
    public void removeEntries() {
        entry = new Vector();
    }

    /** Removes an node as exit.

     @param x - The node to be removed from the  exit set
     */
    public void removeExit(GraphNode x) {
        exit.removeElement(x);
    }

    /** Removes all exit nodes.
     */
    public void removeExits() {
        exit = new Vector();
    }

    /**
     Returns the first entry node of the graph. The one that was first
     set using {@link #setEntry}.

     @return The first entry node of this graph. <code>null</code> if
     none has been set.

     */
    public GraphNode getEntry() {
        if (entry.size() == 0) {
            return null;
        }
        return (GraphNode) entry.elementAt(0);
    }
	
    /**
     Returns the complete set of entry nodes for this graph.
     
     @return An array with each of the entry nodes.

     */

    public GraphNode[] getEntries() {
        return (GraphNode[]) entry.toArray(new GraphNode[0]);
    }

    /**
     Returns the first exit node of the graph. The one that was 
     first set using {@link #setExit}.

     @return The exit node of this graph. <code>null</code> if
     none has been set.

     */
    public GraphNode getExit() {
        if (exit.size() == 0) {
            return null;
        }
        return (GraphNode) exit.elementAt(0);
    }
	
    /**
     Returns the complete set of exit nodes for this graph.
     
     @return An array with each of the entry nodes.

     */

    public GraphNode[] getExits() {
        return (GraphNode[]) exit.toArray(new GraphNode[0]);
    }
	
    /**
     Checks whether a node is an exit node.
     
     @param n the noce to be checked
     @return true if the node is an exit node

     */
    public boolean isExit(GraphNode n) {
        return exit.contains(n);
    }

    /**
     Checks whether a node is an entry node.
     
     @param n the noce to be checked
     @return true if the node is an entry node

     */
    public boolean isEntry(GraphNode n) {
        return entry.contains(n);
    }

    /** Finds the set of nodes without successors and then 
     set them as exit nodes.

     @param ex - Indicates whether secondary edges should be used
     to find exit nodes.
     */

    public void computeExit(boolean ex) {
        for (int i = 0; i < size(); i++) {
            GraphNode gn = (GraphNode) elementAt(i);
            Vector v = getNext(gn, ex);

            if (v.size() == 0) {
                setExit(gn);
            }
        }
    }
	
    /**
     Finds the largest set of connected nodes begining at a given
     node. 

     @param gn The node from which to calculate the subgraph.
     @param subg Is the place where to place the nodes of the subgraph
     @param ex Indicates whether to use secondary edges to calculate
     the subgraph

     */
    void getSubgraph(GraphNode gn, Vector subg, boolean ex) {
        Vector next = getNext(gn, ex);

        if (!subg.contains(gn)) {
            subg.add(gn);
        }
        for (int i = 0; i < next.size(); i++) {
            GraphNode gfcn = (GraphNode) next.elementAt(i);

            getSubgraph(gfcn, subg, ex);
        }
    }			   
	
    /**
     Adds an directed edge between two nodes

     @param x The source node
     @para y The destination node

     */
    public void addPrimEdge(GraphNode x, GraphNode y) {
        x.addPrimNext(y);
    }
		
    /**
     Adds several directed edges between two nodes

     @param x The source node
     @para y The set of destination nodes

     */
    public void addPrimEdge(GraphNode x, Vector y) {
        x.addPrimNext(y);
    }

    /**
     Adds an directed secondary edge between two nodes

     @param x The source node
     @para y The destination node

     */
    public void addSecEdge(GraphNode x, GraphNode y) {
        x.addSecNext(y);
    }
		
    /**
     Adds several directed secondary edges between two nodes

     @param x The source node
     @para y The set of destination nodes

     */
    public void addSecEdge(GraphNode x, Vector y) {
        x.addSecNext(y);
    }

    /**
     Remove an directed edge between two nodes

     @param x The source node
     @para y The destination node

     */
    public void deletePrimEdge(GraphNode x, GraphNode y) {
        x.deletePrimNext(y);
    }
	
    /**
     Adds an directed secondary edge between two nodes

     @param x The source node
     @para y The destination node

     */
    public void deleteSecEdge(GraphNode x, GraphNode y) {
        x.deleteSecNext(y);
    }

    /**
     Returns the set of nodes for wich there exist edges from a
     given node.

     @param x The source node.
     @param us Indicates whether or no to consider secondary edges two.
     @return The set of nodes for wich the node of interest has an edge
     (or a secondary edge).

     */
    public Vector getNext(GraphNode x, boolean us) {
        Vector y = (Vector) getPrimNext(x).clone();

        if (us) {
            y.addAll(getSecNext(x));
        }
        return y;
    }
	
    /**
     Returns the set of primary nodes for wich there exist edges from a
     given node.

     @param x The source node.

     @return The set of primary nodes for wich the node of interest has an edge.

     */
    public Vector getPrimNext(GraphNode x) {
        return x.getPrimNext();
    }
	
    /**
     Returns the set of primary nodes for wich there exist edges from a
     given node.

     @param x The source node.

     @return The set of secondary nodes for wich the node of interest has an edge.

     */
    public Vector getSecNext(GraphNode x) {
        return x.getSecNext();
    }

    /**
     Returns the set of nodes for wich there exist edges to a
     given node.

     @param x The destinatio node.
     @param us Indicates whether or no to consider secondary edges two.
     @return The set of nodes from wich the node of interest has an edge
     (or a secondary edge).

     */
    public Vector getArriving(GraphNode x, boolean us) {
        Vector y = (Vector) getPrimArriving(x).clone();

        if (us) {
            y.addAll(getSecArriving(x));
        }
        return y;
    }

    /**
     Returns the set of primary nodes for wich there exist edges to a
     given node.

     @param x The destinatio node.
     @return The set of nodes from wich the node of interest has a
     primary edge.

     */
    public Vector getPrimArriving(GraphNode x) {
        return x.getPrimArriving();
    }

    /**
     Returns the set of secondary nodes for wich there exist edges to a
     given node.

     @param x The destination node.
     @return The set of nodes from wich the node of interest has a
     secondary edge.

     */
    public Vector getSecArriving(GraphNode x) {
        return x.getSecArriving();
    }

    /**
     Removes a node from the graph. Deals with all the details
     of such operation as removing edges entering and exiting 
     the node, removing from the set of entry nodes and unseting
     the exit node (if those are the cases).

     @param x The node to be removed

     */
    public void removeNode(GraphNode x) {
        // elimina arestas saindo
        Vector v = getNext(x, true);

        for (int i = v.size() - 1; i >= 0; i--) {
            GraphNode y = (GraphNode) v.elementAt(i);

            x.deletePrimNext(y);
            x.deleteSecNext(y);
        }
        // elimina arestas chegando
        v = getArriving(x, true);
        for (int i = v.size() - 1; i >= 0; i--) {
            GraphNode y = (GraphNode) v.elementAt(i);

            y.deletePrimNext(x);
            y.deleteSecNext(x);
        }
		
        // remove as entry and exit node
        exit.remove(x);
        entry.remove(x);
		
        // remove do grafo
        remove(x);
    }
	
    /** This method removes a node but makes the links from
     * its previuos and next nodes.
     *
     * @param x The node to remove
     * @param ex If it should consider also the secondary links
     */
    public void jumpOver(GraphNode x, boolean ex) {
        if ( isEntry(x) )
        {
        	// nao mexer se for no inicial pois podem existir
        	// variaveis definidas no noh...
        	return;
        }
        Vector nx = getPrimNext(x),
                ar = getPrimArriving(x);

        for (int i = 0; i < ar.size(); i++) {
            GraphNode f = (GraphNode) ar.elementAt(i);

            for (int j = 0; j < nx.size(); j++) {
                GraphNode t = (GraphNode) nx.elementAt(j);

                addPrimEdge(f, t);
            }
        }
	    
        nx = getPrimNext(x); // sic eh realmente PRIMARY
        ar = getSecArriving(x);
        for (int i = 0; i < ar.size(); i++) {
            GraphNode f = (GraphNode) ar.elementAt(i);

            for (int j = 0; j < nx.size(); j++) {
                GraphNode t = (GraphNode) nx.elementAt(j);

                addSecEdge(f, t);
            }
        }
        removeNode(x); 
    }
	
    private GraphNode[] fdtf;
    private int ctdtf;

    /**
     * Construct a Depth First Tree sequence of the nodes.
     * 
     * @param ex Whether or not to use secondary edges when calculating
     * "next" nodes.
     *
     */
    public GraphNode[] findDFT(boolean ex) {
        fdtf = new GraphNode[size()];
        for (int i = 0; i < size(); i++) {
            GraphNode x = (GraphNode) elementAt(i);

            x.setMark(false);
        }
        ctdtf = 0;
        GraphNode[] entr = getEntries();

        for (int i = 0; i < entr.length; i++) {
            DFS(entr[i], ex);
        }
        GraphNode[] ret = new GraphNode[ctdtf];

        System.arraycopy(fdtf, 0, ret, 0, ctdtf);
        return ret;
    }
	
    private void DFS(GraphNode x, boolean ex) {
        do {
            if (x.getMark()) {
                return;
            }
            Vector next = getNext(x, ex);
            int k = next.size();

            x.setMark(true);
            fdtf[ctdtf++] = x;
            if (k <= 0) {
                return;
            }
				
            for (int i = 0; i < k - 1; i++) {
                GraphNode nexti = (GraphNode) next.elementAt(i);

                DFS(nexti, ex);
            }
            x = (GraphNode) next.lastElement();
        } while (true);
    }

    /** This method implements the framework for the "round robin" 
     * algorithm. 
     * @param x This object will take care of computing the new set
     * for a given node
     * @param reverse If true, the depth first tree is used in the 
     * opposite order, i.e., from 0 up (the normal order is from
     * the top elemen down).
     */

    public void roundRobinAlgorithm(RoundRobinExecutor x, 
            boolean reverse) {
        Vector nx, nx2;
        GraphNode[] dft = findDFT(true);
        int cont = 0, nchange;
        int inc = -1, init = dft.length - 1, end = -1;

        if (reverse) {
            end = init + 1;
            init = 0;
            inc = 1;
        }
        for (int i = init; i != end; i += inc) {
            GraphNode in = dft[i];

            if (reverse) // pega anterior
            {
                nx = in.getPrimArriving();
                nx2 = in.getSecArriving();
            } else {
                nx = in.getPrimNext();
                nx2 = in.getSecNext();
            }
            x.init(in, nx, nx2);
        }
        do {
            cont++;
            nchange = 0;
			
            for (int i = init; i != end; i += inc) {
                GraphNode in = dft[i];

                if (reverse) // pega anterior
                {
                    nx = in.getPrimArriving();
                    nx2 = in.getSecArriving();
                } else {
                    nx = in.getPrimNext();
                    nx2 = in.getSecNext();
                }
                Object bs = x.calcNewSet(in, nx, nx2);

                if (!x.compareEQ(in, bs)) // compare current and last
                {
                    nchange++;
                    x.setNewSet(in, bs);
                }
            }
			
        } while (nchange > 0);
    }
	
    /**
     * Construct an Inverse Depth First Tree sequence of the nodes.
     * 
     * @param ex Whether or not to use secondary edges when calculating
     * "next" nodes.
     *
     */
    public GraphNode[] findIDFT(boolean ex) {
        fdtf = new GraphNode[size()];
        for (int i = 0; i < size(); i++) {
            GraphNode x = (GraphNode) elementAt(i);

            x.setMark(false);
        }
        GraphNode[] ext = getExits();

        ctdtf = 0;
        for (int i = 0; i < ext.length; i++) {
            IDFS(ext[i], ex);
        }
        GraphNode[] ret = new GraphNode[ctdtf];

        System.arraycopy(fdtf, 0, ret, 0, ctdtf);
        return ret;
    }
	
    /**
     * Construct an Inverse Depth First Tree sequence of the nodes,
     * from a given node
     * @param ex Whether or not to use secondary edges when calculating
     * "next" nodes.
     * @param node The node from where to start
     *
     */
    public GraphNode[] findIDFT(boolean ex, GraphNode node) {
        fdtf = new GraphNode[size()];
        for (int i = 0; i < size(); i++) {
            GraphNode x = (GraphNode) elementAt(i);

            x.setMark(false);
        }
        GraphNode ext = node;

        ctdtf = 0;
        IDFS(ext, ex);
        GraphNode[] ret = new GraphNode[ctdtf];

        System.arraycopy(fdtf, 0, ret, 0, ctdtf);
        return ret;
    }

    private void IDFS(GraphNode x, boolean ex) {
        do {
            if (x.getMark()) {
                return;
            }
            Vector next = getArriving(x, ex);
            int k = next.size();

            x.setMark(true);
            fdtf[ctdtf++] = x;
            if (k <= 0) {
                return;
            }
				
            for (int i = 0; i < k - 1; i++) {
                GraphNode nexti = (GraphNode) next.elementAt(i);

                IDFS(nexti, ex);
            }
            x = (GraphNode) next.lastElement();
        } while (true);
    }

    /** This method implements the framework for the "round robin" 
     * algorithm. 
     * @param x This object will take care of computing the new set
     * for a given node
     * @param reverse If true, the depth first tree is used in the 
     * opposite order, i.e., from 0 up (the normal order is from
     * the top elemen down).
     */

    public void roundIRobinAlgorithm(RoundRobinExecutor x, 
            boolean reverse) {
		// System.out.println("Entrou RR");
        Vector nx, nx2;
        GraphNode[] dft = findIDFT(true);
        // System.out.println("Tamanho dft " + dft.length);

		// se dft.lnegth == 0 isso significa que metodo nao tem nos
		// de saida. assim, deve fazer apenas a inicializacao.
		if ( dft.length == 0 )
		{
			dft = findDFT(true);
		
	        for (int i = 0; i < dft.length; i++) {
	            GraphNode in = dft[i];
	
	            x.init(in, null, null );
	        }
	        return;
	 	}
	 	
        int cont = 0, nchange;
        int inc = -1, init = dft.length - 1, end = -1;

        if (reverse) {
            end = init + 1;
            init = 0;
            inc = 1;
        }
        for (int i = init; i != end; i += inc) {
            GraphNode in = dft[i];

            if (!reverse) // pega anterior
            {
                nx = in.getPrimArriving();
                nx2 = in.getSecArriving();
            } else {
                nx = in.getPrimNext();
                nx2 = in.getSecNext();
            }
            //System.out.println("Chamando init para no: " + in.getLabel());
            x.init(in, nx, nx2);
        }
        do {
            cont++;
            nchange = 0;
			
            for (int i = init; i != end; i += inc) {
                GraphNode in = dft[i];

                if (!reverse) // pega anterior
                {
                    nx = in.getPrimArriving();
                    nx2 = in.getSecArriving();
                } else {
                    nx = in.getPrimNext();
                    nx2 = in.getSecNext();
                }
                Object bs = x.calcNewSet(in, nx, nx2);

                if (!x.compareEQ(in, bs)) // compare current and last
                {
                    nchange++;
                    x.setNewSet(in, bs);
                }
            }
			
        } while (nchange > 0);
    }
	
    /** Computes a simple paths from one node to another
     * @param orig - the source node
     * @param dest - the destination node
     * @param sec - indecates whether secondary edges can be used in 
     * the paths
     *
     * @return - an array of {@link GraphNode}'s. If one of
     * the nodes is not part of this graph, returns null. If theres is 
     * no such path, returns an array of size 0.
     */
    public GraphNode[] computeAPath(GraphNode orig,
            GraphNode dest,
            boolean sec) {
        if (!contains(orig) || !contains(dest)) {
            return null;
        }
        Vector v = compAP(new HashSet(), orig, dest, sec);

        if (v == null) {
            return null;
        }
        GraphNode gnRet[] = (GraphNode[]) v.toArray(new GraphNode[0]);

        return gnRet;
    }
	
    private Vector compAP(HashSet forb, GraphNode f, GraphNode t, boolean sec) {
        if (forb.contains(f)) {
            // System.out.println("JA PASSOU: " + f);
            return null;
        }

        // System.out.println("\n\nPROCURANDO DE : " + f + "\nTO" + t);
        forb.add(f);
        if (f == t) {
            Vector v = new Vector();

            v.add(f);
            return v; 
        }
        Vector ret = null;
        Vector nx = getNext(f, sec);

        for (int i = 0; ret == null && i < nx.size(); i++) {
            GraphNode ngn = (GraphNode) nx.elementAt(i);

            // System.out.println("TENTANDO : " + i + " / " +nx.size());
            ret = compAP(forb, ngn, t, sec);
            if (ret != null) {
                ret.add(0, f);
            } else {
                forb.add(ngn);
            }
        }
        return ret;
    }

    /** Compute the set of Strongly Connected Components of this 
     * graph.
     * @return An array where each element corresponds ta a 
     * SC component
     * @param sec - indicates if secondary edges should be considered
     */
    public HashSet[] computeSCC(boolean sec) {
        Vector v = (Vector) clone();
        Vector ret = new Vector();

        while (v.size() > 0) {
            HashSet hs = new HashSet();
            GraphNode prim = (GraphNode) v.firstElement();

            for (int i = 0; i < v.size(); i++) {
                GraphNode gn = (GraphNode) v.elementAt(i);
                GraphNode vem[] = computeAPath(gn, prim, sec);

                if (vem == null || vem.length == 0) {
                    continue;
                } // no paths from this nodes
                GraphNode vai[] = computeAPath(prim, gn, sec);

                if (vai == null || vai.length == 0) {
                    continue;
                } // no paths from this nodes
                for (int j = 0; j < vai.length; j++) {
                    hs.add(vai[j]);
                }
                for (int j = 0; j < vem.length; j++) {
                    hs.add(vem[j]);
                }
            }
            v.removeAll(hs);
            ret.add(hs); 
        }
        return (HashSet[]) ret.toArray(new HashSet[0]);
    }

    public void removeComposite(boolean sec) {
        // System.out.println("Remove composite");
        for (int j = 0; j < size(); j++) {
            GraphNode gn = (GraphNode) elementAt(j);
            Vector next = getNext(gn, sec);

            nextNode:
            // System.out.println("Remove composite " + gn);
            for (int i = next.size() - 1; i >= 0; i--) {
                GraphNode nx = (GraphNode) next.elementAt(i);

                if (gn == nx) {
                    deletePrimEdge(gn, nx);
                    break;
                }
                // System.out.println(i + " Next: " + nx);
                for (int k = 0; k < next.size(); k++) {
                    if (k == i) {
                        continue;
                    }
                    GraphNode nx2 = (GraphNode) next.elementAt(k);
                    // System.out.println(" Testando: " + k + " / " + next.size() + "\n" + nx2);
					
                    GraphNode path[] = computeAPath(nx2, nx, false);

                    // System.out.println(" Caminho achado: " + (path != null));
                    if (path != null) {
                        deletePrimEdge(gn, nx);
                        break;
                    }
                }
            }
        }
		
    }
	
}

