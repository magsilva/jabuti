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

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import br.jabuti.graph.CFG;
import br.jabuti.graph.Graph;
import br.jabuti.graph.GraphNode;

/** 
 * Implementation of the all-nodes criterion.
 *
 *
 * @version: 1.0
 * @author: Marcio Delamaro  
 * @author: Auri Vincenzi  
 *
 **/
public class AllNodes extends AbstractCriterion {

    /** This constructor takes the nodes of the graph as the criterion
     * requirements. It uses the domination relation to discard some
     * nodes. If a node x dominates a node y, then x can be discarded
     *
     * @param g The graph from where the requirements are extracted
     * @param which Says wich nodes to use: PRIMARY - does not use nodes
     * reached only by secondary  edges 
     * SECONDARY - use only nodes reached only by secondary edges; 
     * ALL - both primary and
     * secondary.
     */ 
    public AllNodes(Graph g, int which) {
        super(g);

        GraphNode[] fdt = g.findDFT(which == ALL);

        for (int i = 0; i < fdt.length; i++) {

            required.put(new Node(fdt[i].getLabel()), new Integer(0));  // number of paths that cover it
        }
        
        if ( which == SECONDARY )
        {
        	AllNodes aux = new AllNodes(g, ALL);
        	aux.required.keySet().removeAll(required.keySet());
        	required = aux.required;
        }
    }	
	
	public AllNodes(Graph g)
	{
		this(g, ALL);
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
		//System.out.println("All-Nodes addPath...");
    	HashSet co = (HashSet) pathSet.get(label);
        if ( co == null) {
			co = new HashSet();
			pathSet.put(label, co);
        }
        int cont = 0;
        HashSet co2 = new HashSet();
        
        for (int i = 0; i < path.length; i++) {
            Node lab = null;
            if (path[i] != null)
              lab = new Node(path[i].toString()); 
            else
              lab = new Node("null" + i);

            if (lab.from != null && required.containsKey(lab)) {
                co2.add(lab);
            }
        }
		
        // Updating the coverage information for this path
        Iterator it = co2.iterator();

        while (it.hasNext()) {
            Node lab = (Node) it.next();

			if ( ! co.contains(lab) )
			{
				cont++;
				co.add(lab);
				
				//Integer ki = (Integer) required.get(lab);
				//required.put(lab, new Integer(ki.intValue() + 1));

				//System.out.println("Covered node: " + lab + " " 
				//+ new Integer(ki.intValue() + 1)
				//);
			}
        }

        return cont;
    }
	

    
    
    public static void main ( String args[] )
    		throws Exception
    {
        JavaClass java_class;
        java_class = new ClassParser ( args[0] ).parse (  );	// May throw IOException
        ConstantPool cp = java_class.getConstantPool();
        
        ClassGen cg = new ClassGen(java_class);
                    
        Method[] methods = java_class.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
            System.out.println ( "\n\n--------------------------" );
            System.out.println ( methods[i].getName (  ) );
            System.out.println ( "--------------------------" );
            ConstantPoolGen cpg = new ConstantPoolGen(cp);
            MethodGen mg = new MethodGen ( methods[i], java_class.getClassName (  ), cpg);

            CFG g = new CFG(mg, cg);

            AllNodes an = new AllNodes(g);

			System.out.println( "Number of Requirements: " + 
					an.getNumberOfRequirements() );

			System.out.println( "Number of Possible Requirements: " + 
					an.getNumberOfPossibleRequirements() );

			Object[] reqs = an.getRequirements();
            System.out.println("Requirements: ");
            
            for (int j = 0; j < reqs.length; j++) {
            	Requirement req = (Requirement) reqs[j];
                System.out.print( req );
                System.out.print( " active: " );
                if ( an.isActive( req ) ) {
                	System.out.print( "true" );
                }
                else {
                	System.out.print( "false" );
            	}
				
				System.out.print( " covered: " );				
                if ( an.isCovered( req ) ) {
                	System.out.print( "true" );
                }
                else {
                	System.out.print( "false" );
            	}

				System.out.print( " feasible: " );				
                if ( an.isFeasible( req ) ) {
                	System.out.print( "true" );
                }
                else {
                	System.out.print( "false" );
            	}
            	System.out.println();
            }
            System.out.println();
            
            an.addPath(new String[] {"0", "4"}, "path 1" );  
            an.addPath(new String[] {"0", "48", "207"}, "path 3" );  
            an.addPath(new String[] {"0", "76", "207"}, "path 2" );  
            
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
            for (int j = 0; j < cv.length; j++)
            {
            	System.out.print(cv[j] + "  " );
            }
            System.out.println();
            
			hs = an.getCoveredRequirements( "path 1" );
			System.out.println( "Covered requirements by Path 1" );
			Iterator it = hs.iterator();
			while ( it.hasNext() ) {
				System.out.print( it.next() + " ");
			}            
           

			// Marcando todos os requisitos como inativos e reativando-os
            for (int j = 0; j < reqs.length; j++) {
            	an.setInactive( (Requirement)reqs[j] );
            }
            hs = an.getInactiveRequirements();
			for (int j = 0; j < reqs.length; j++) {
            	an.setActive( (Requirement) reqs[j] );
            }

			// Marcando todos os requisitos como infeasible e reativando-os
            for (int j = 0; j < reqs.length; j++) {
            	an.setInfeasible( (Requirement) reqs[j] );
            }
            hs = an.getInfeasibleRequirements();
            for (int j = 0; j < reqs.length; j++) {
            	an.setFeasible( (Requirement) reqs[j] );
            }

			// Obtendo os requisitos por meio de seus r�tulos
           /* for (int j = 0; j < req.length; j++) {
            	Object o = an.getRequirementByLabel( req[j].toString() );
            }
            */
            
            // Removendo um caso de teste
            an.removePath( "path 2" );

            // Removendo todos os casos de teste
            an.removeAllPaths( );
        }
    }
}
