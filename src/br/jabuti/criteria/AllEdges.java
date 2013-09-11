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


package br.jabuti.criteria;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import br.jabuti.graph.Graph;
import br.jabuti.graph.GraphNode;


/** This class implements the All Edges criterion. 
 * It really require all edges, i.e., no otimizations has been
 * done *YET*
 *
 * @version: 1.0
 * @author: Marcio Delamaro
 * @author: Auri Vincenzi 
 *
 **/
public class AllEdges extends AbstractCriterion {
							
    /** <par>This constructor takes the edges of the graph as the criterion
     * requirements. It does not use otimizations yet. We consider the
     * following criteria to distinguishe primary and secondary edges
     * requeriments: </par><br>
     * <par>Secondary edges are secondary requirements</par><br>
     * <par>Primary edges leaving secondary edges are secondary 
     * requirements</par><br>
     * <par>Primary edges leaving primary edges are primary
     * requirements</par><br>
     *
     * @param g The graph from where the requirements are extracted
     * @param which Says wich edges to use: PRIMARY - only primary 
     * edges; SECONDARY - only secondary; ALL - both primary and
     * secondary.
     */ 
    public AllEdges(Graph g, int which) {
        super(g);

		AllNodes secNodes = new AllNodes(g, SECONDARY);
		 
        GraphNode[] fdt = g.findDFT(true);
        Vector prim = new Vector(), sec = new Vector();
        
        
        for (int i = 0; i < fdt.length; i++) 
        {
            GraphNode gn = fdt[i];
            String nodeLabel = gn.getLabel();

            Vector v = gn.getSecNext();

            for (int j = 0; j < v.size(); j++) 
            {
                Edge ed = new Edge(nodeLabel, 
                        ((GraphNode) v.elementAt(j)).getLabel());

                sec.add(ed);
            }
			
            v = gn.getPrimNext();

            for (int j = 0; j < v.size(); j++) 
            {
                Edge ed = new Edge(nodeLabel, 
                        ((GraphNode) v.elementAt(j)).getLabel());

				if ( secNodes.required.containsKey(nodeLabel) )
				{
                	sec.add(ed);
                }
                else
                {
                	prim.add(ed);
                }
            }
        }	
        
        if ((which & PRIMARY) == PRIMARY) 
        {
        	for (int i = 0; i < prim.size(); i++)
        	{
        		required.put(prim.elementAt(i), new Integer(0));
        	}
        }

        if ((which & SECONDARY) == SECONDARY) 
        {
        	for (int i = 0; i < sec.size(); i++)
        	{
        		required.put(sec.elementAt(i), new Integer(0));
        	}
        }		
    }	
	
    /** Add a path to the path set. To each path a label is associate so
     * informaton about the path can be obtained and the path can be 
     * removed.
     *
     * @param path The path to be added. The object in the array can be:
     * an string representing the label of a
     * graph node; an {@link Integer} representing the number of a graph 
     * node. They can also be mixed in the array.
     * @param label A label to be assigned to this path. If the label already
     * exists is is replaced by the path used in the call
     */
    public int addPath(Object[] path, String label) {
		//System.out.println("All-Edges addPath...");
		HashSet co = (HashSet) pathSet.get(label);
        if ( co == null ) {
			co = new HashSet();
			pathSet.put(label, co);
        }

        int cont = 0;
        HashSet co2 = new HashSet();

        for (int i = 0; i < path.length - 1; i++) {
			
            String lab1 = null, lab2 = null;

            if (path[i] != null)
              lab1 = path[i].toString(); 
            else
              lab1 = "null" + i;

			
            int k = i + 1;

            if (path[k] != null)
              lab2 = path[k].toString(); 
            else
              lab2 = "null" + k;
			
            Edge ed = null;

            if (lab1 != null && lab2 != null) {
                ed = new Edge(lab1, lab2);
            }

			//ERROR: adding covered requirement to co instead of co2
            if (ed != null && required.containsKey(ed)) {
                //co.add(ed);
                co2.add(ed);
            }
        }
		
        // Updating the coverage information for this path
        Iterator it = co2.iterator();

        while (it.hasNext()) {
            Edge ed = (Edge) it.next();

			if ( ! co.contains(ed))
			{
				co.add(ed);
				cont++;

				//Integer ki = (Integer) required.get(ed);
				//required.put(ed, new Integer(ki.intValue() + 1));
				
				//System.out.println("Covered edge: " + ed + " " 
				//+ new Integer(ki.intValue() + 1)
				//);
			}
        }
        return cont;
    }
	


	/*
    public static void main(String args[])
            throws Exception {
        JavaClass java_class;

        java_class = new ClassParser(args[0]).parse();	// May throw IOException
        ConstantPoolGen cp =
                new ConstantPoolGen(java_class.getConstantPool());
        Method[] methods = java_class.getMethods();
        ClassGen cg = new ClassGen(java_class);

        for (int i = 0; i < methods.length; i++) {
            System.out.println("\n\n--------------------------");
            System.out.println(methods[i].getName());
            System.out.println("--------------------------");
            MethodGen mg =
                    new MethodGen(methods[i], java_class.getClassName(), cp);
            CFG g = new CFG(mg, cg);

            g.print(System.out);
            AllEdges an = new AllEdges(g, AllEdges.ALL);

			System.out.println( "Number of Requirements: " + 
					an.getNumberOfRequirements() );

			System.out.println( "Number of Possible Requirements: " + 
					an.getNumberOfPossibleRequirements() );

            Object[] req = an.getRequirements();

            System.out.println("Requirements: ");
            for (int j = 0; j < req.length; j++) {
                Edge gn = (Edge) req[j];

                System.out.print(gn);
                
                System.out.print( " active: " );
                if ( an.isActive( req[j] ) ) {
                	System.out.print( "true" );
                }
                else {
                	System.out.print( "false" );
            	}
				
				System.out.print( " covered: " );				
                if ( an.isCovered( req[j] ) ) {
                	System.out.print( "true" );
                }
                else {
                	System.out.print( "false" );
            	}

				System.out.print( " feasible: " );				
                if ( an.isFeasible( req[j] ) ) {
                	System.out.print( "true" );
                }
                else {
                	System.out.print( "false" );
            	}
            	System.out.println();
            }
            an.addPath(an.changePath(g, 
                    new String[] {"0", "2", "4", "9", "12"}
                    ), "path 1");  
            an.addPath(an.changePath(g, 
                    new String[] {"0", "1", "3", "4", "11"}
                    ), "path 3");  
            an.addPath(an.changePath(g,
                    new String[] {"0", "2", "7", "9", "10"}
                    ), "path 2");  
            
            int[] cv = an.getCoverage();
            System.out.println();

			System.out.println( "Number of Possible Covered Requirements: " + 
					an.getNumberOfPossibleCovered() );

            HashSet hs = an.getCoveredRequirements();
            if ( hs.isEmpty() )
            	System.out.println( "No covered requirement." );
            else
            	System.out.println( "There are covered requirements." );

            hs = an.getPossibleCoveredRequirements();
            if ( hs.isEmpty() )
            	System.out.println( "No possible covered requirement." );
            else
            	System.out.println( "There are possible covered requirements." );
            
            System.out.println("Covered: ");
            for (int j = 0; j < cv.length; j++) {
                System.out.print(cv[j] + "  ");
            }

            System.out.println();
            
			hs = an.getCoveredRequirementsByPath( "path 1" );
			System.out.println( "Covered requirements by Path 1" );
			Iterator it = hs.iterator();
			while ( it.hasNext() ) {
				System.out.print( it.next() + " ");
			}            
           

			// Marcando todos os requisitos como inativos e reativando-os
            for (int j = 0; j < req.length; j++) {
            	an.setInactive( req[j] );
            }
            hs = an.getInactiveRequirements();
			for (int j = 0; j < req.length; j++) {
            	an.setActive( req[j] );
            }

			// Marcando todos os requisitos como infeasible e reativando-os
            for (int j = 0; j < req.length; j++) {
            	an.setInfeasible( req[j] );
            }
            hs = an.getInfeasibleRequirements();
            for (int j = 0; j < req.length; j++) {
            	an.setFeasible( req[j] );
            }

			// Obtendo os requisitos por meio de seus r�tulos
            for (int j = 0; j < req.length; j++) {
            	Object o = an.getRequirementByLabel( req[j].toString() );
            }
            
            // Removendo um caso de teste
            an.removePath( "path 2" );

            // Removendo todos os casos de teste
            an.removeAllPaths( );
        }
    }*/
}
