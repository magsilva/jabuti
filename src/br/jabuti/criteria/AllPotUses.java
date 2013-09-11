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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import br.jabuti.graph.CFG;
import br.jabuti.graph.CFGNode;
import br.jabuti.graph.GraphNode;
import br.jabuti.graph.RRLiveDefs;


 
/** 
 * This class implements the All Potential Uses criterion. 
 * No otimizations has been
 * done *YET*
 *
 *
 * @version: 1.0
 * @author: Marcio Delamaro  
 * @author: Auri Vincenzi  
 *
 **/
public class AllPotUses extends AbstractCriterion {
							
    /** This constructor takes the def/use associations of the graph as the criterion
     * requirements. It does not use otimizations yet
     *
     * @param g The graph from where the requirements are extracted
     * @param useSecond Says wich edges to use: false - only primary 
     * edges; true - both primary and
     * secondary.
     */ 
    public AllPotUses(CFG graph, boolean useSecond) {
        super(graph);
        
        Hashtable required2 = new Hashtable();
		
//        graph.computeDefUse();
        RRLiveDefs rral = new RRLiveDefs(useSecond? RRLiveDefs.ALL: RRLiveDefs.PRIMARY);

        graph.roundRobinAlgorithm(rral, true);
		AllNodes secNodes = new AllNodes(graph, SECONDARY);
		
        for (int i = 0; i < graph.size(); i++) {
        	
        	// Pra cada noh pega o conjunto de variaveis vivas
        	// e o no onde foi definida
            CFGNode g = (CFGNode) graph.elementAt(i);
            boolean isSec = secNodes.required.containsKey(new Node(g.getLabel()));
            
            HashSet h1 = (HashSet) g.getUserData(RRLiveDefs.defaultLabel);

            if (h1 == null) { // no eh nao alcancavel
                continue;
            }

			// pega o numero de arestas primarias saindo
            Vector nextG = graph.getNext(g, false);
			int contNext = nextG.size();
			
			// se for uso predicativo, inclui as definicoes no
			// proprio no
			if (contNext > 1 )
			{
				h1 = new HashSet(h1);
				Iterator it = g.definitions.keySet().iterator();
				while (it.hasNext())
				{
					String defName = (String) it.next();
					Vector pair = new Vector(2);
					pair.add(defName);
					pair.add(g);
					h1.add(pair);
				}
			}

			Iterator defs = h1.iterator();

            while (defs.hasNext()) 
            {
            	
	            Vector pair = (Vector) defs.next(); 
	            String defVarName = (String) pair.elementAt(0);
				
	            GraphNode defNode = (GraphNode) pair.elementAt(1);
        		boolean isSecDef = 
	        		         secNodes.required.containsKey(defNode.getLabel());
	        		
					
				// se numero de sucesores <= 1 entao existe uma 
				// associa��o defini��o / uso no noh (computacional)
	            if ( contNext <= 1 )
	            {
	            	if (! defNode.getLabel().equals(g.getLabel()) ) // despreza uso local
	            	{
	                	DefUse assoc = new DefUse(defVarName, 
	                    	    defNode.getLabel(),
	                        	g.getLabel(), null
	                        	);
	
	                	if ( ! isSec && ! isSecDef ) 
	                		required.put(assoc, new Integer(0));
	                	else
	                		required2.put(assoc, new Integer(0));
	                }
	                continue;
	            }
				// senao, associa a def-uso a cada aresta primaria saindo
	            Vector v = g.getPrimNext();
	            for (int j = 0; j < v.size(); j++) {
	                DefUse assoc = new DefUse(defVarName, 
	                        defNode.getLabel(),
	                        g.getLabel(), 
	                        ((GraphNode) v.elementAt(j)).getLabel()
	                     );
	            	if ( ! isSec && ! isSecDef ) 
	            		required.put(assoc, new Integer(0));
	            	else
	            		required2.put(assoc, new Integer(0));
	            }
				
            }	
        }
        
        if (useSecond)
        {
        	required = required2;
        }
    }	
	
    /** Add a path to the path set. To each path a label is associate so
     * informaton about the path can be obtained and the path can be 
     * removed.
     *
     * @param path The path to be added. The object in the array can be:
     * a string representing the label of a
     * graph node; an {@link Integer} representing the number of a graph 
     * node. They can also be mixed in the array.
     * @param label A label to be assigned to this path. If the label already
     * exists is is replaced by the path used in the call
     */
    public int addPath(Object[] path, String label) {
		//System.out.println("All-Pot-Uses addPath...");    	
		HashSet co = (HashSet) pathSet.get(label);
		if ( co == null ) {
			co = new HashSet();
			pathSet.put(label, co);
		}

		int cont = 0;
		HashSet co2 = new HashSet();

        Object[] req = getRequirements(); 
		
        nextReq:
        for (int i = 0; i < req.length; i++) {
        	// percorre cada um dos requisitos
            DefUse defuse = (DefUse) req[i];
			
            String var = defuse.var;
            String def = defuse.def;
            String use1 = defuse.useFrom;
            String use2 = defuse.useTo;
			
            nextDefNode:
            for (int j = 0; j < path.length - 1; j++) {
            	// percorre a partir do inicio do caminho, procurando
            	// primeiro o no onde ocorre a defini��o
                String from = null;

                if (path[j] instanceof Integer) {
                    from = ((Integer) path[j]).toString();
                } else if (path[j] != null) {
                    from = (String) path[j];
                } else {
                    from = "null" + j;
                }
                if (!from.equals(def)) {
                    continue;
                }
                
                // j � o no onde tem a defini��o
                
                // se uso predicativo, testa se eh <x, j, (j, j+1)>
                if ( use2 != null)
                {
	                String oneAf = null;
	                if (path[j+1] instanceof Integer) {
	                    oneAf = ((Integer) path[j+1]).toString();
	                } else {
	                    oneAf = (String) path[j+1];
	                }
					//ERROR: adding covered requirement to co instead of co2
	                if ( use1.equals(from) && use2.equals(oneAf) )
	                { // satisfez o requisito
	                	//co.add(defuse);
						co2.add(defuse);
	                	continue nextReq;
	                }
            	}
                
					
                for (int k = j + 1; k < path.length; k++) 
                {  // procura se o no com o uso tbem aparece no caminho
                   // a partir do proximo no, ou seja, j + 1
                    String to1 = null, to2 = "";

                    if (path[k] instanceof Integer) {
                        to1 = ((Integer) path[k]).toString();
                    } else {
                        to1 = (String) path[k];
                    }
					
                    // se o no k contem uma definicao da mesma variavel,
                    // continua a busca usando esse no como no do uso
                    DefUse alt = new DefUse(var, to1, use1, use2);

                    if (required.containsKey(alt)) {
                        j = k - 1;
                        continue nextDefNode;
                    }
					
                    if (use2 != null && k < path.length -1 ) 
                    {
                        if (path[k + 1] instanceof Integer) {
                            to2 = ((Integer) path[k + 1]).toString();
                        } else {
                            to2 = (String) path[k + 1];
                        }
                    }
					//ERROR: adding covered requirement to co instead of co2
                    if (to1.equals(use1) && (to2.equals("") || to2.equals(use2))) {
                        //co.add(defuse);
						co2.add(defuse);
                        continue nextReq;
                    }
                }
            }
        }
		
        // Updating the coverage information for this path
        Iterator it = co2.iterator();

        while (it.hasNext()) {
            DefUse defuse = (DefUse) it.next();

			if ( ! co.contains(defuse))
			{
				co.add(defuse);
				cont++;
				
				//Integer ki = (Integer) required.get(defuse);
				//required.put(defuse, new Integer(ki.intValue() + 1));
				
				//System.out.println("Covered pot-use: " + defuse + " " 
				// + new Integer(ki.intValue() + 1)
				//);
			}
        }
        return cont;
    }
	

/*    public static void main(String args[])
            throws Exception {
        JavaClass java_class;

        java_class = new ClassParser(args[0]).parse();	// May throw IOException
        ConstantPoolGen cp =
                new ConstantPoolGen(java_class.getConstantPool());
        ClassGen cg = new ClassGen(java_class);
        Method[] methods = java_class.getMethods();

        for (int i = 0; i < methods.length; i++) {
            System.out.println("\n\n--------------------------");
            System.out.println(methods[i].getName());
            System.out.println("--------------------------");
            MethodGen mg =
                    new MethodGen(methods[i], java_class.getClassName(), cp);
            CFG g = new CFG(mg, cg);

            AllUses an = new AllUses(g, false);

//            g.print(System.out);

            Object[] req = an.getRequirements();

            System.out.println("Requirements PRIMARY: ");
            for (int j = 0; j < req.length; j++) {
                DefUse gn = (DefUse) req[j];

                System.out.println(gn);
            }

            an = new AllUses(g, true);

//            g.print(System.out);

            req = an.getRequirements();

            System.out.println("Requirements SECONDARY: ");
            for (int j = 0; j < req.length; j++) {
                DefUse gn = (DefUse) req[j];

                System.out.println(gn);
            }

            an.addPath(an.changePath(g, 
                    new String[] {"0", "1", "3"}
                    ), "path 1");  
            an.addPath(an.changePath(g, 
                    new String[] {"0", "2", "3"}
                    ), "path 3");  
            
            int[] cv = an.getCoverage();

            System.out.println();
            System.out.println("Covered: ");
            for (int j = 0; j < cv.length; j++) {
                System.out.println(req[j] + " covered " + cv[j] + "  times");
            }
        }
    }*/
}
