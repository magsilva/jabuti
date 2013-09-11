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


 
/** This class implements the All Uses criterion. 
 * No otimizations has been
 * done *YET*
 *
 *
 * @version: 1.0
 * @author: Marcio Delamaro  
 * @author: Auri Vincenzi  
 *
 **/
public class AllUses extends AbstractCriterion {
							
    /** This constructor takes the def/use associations of the graph as the criterion
     * requirements. It does not use otimizations yet
     *
     * @param g The graph from where the requirements are extracted
     * @param useSecond Says wich edges to use: false - only primary 
     * edges; true - both primary and
     * secondary.
     */ 
    public AllUses(CFG graph, boolean useSecond) {
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
            boolean isSec = secNodes.required.keySet().contains(new Node(g.getLabel()));
            
            HashSet h1 = (HashSet) g.getUserData(RRLiveDefs.defaultLabel);

            if (h1 == null) { // no eh nao alcancavel
            	System.out.println( "##############INALCANCAVEL##############" );
                continue;
            }

			// pega o numero de arestas primarias saindo
			// se for <= 1, nao considera os usos locais
            Vector nextG = graph.getNext(g, false);
			int contNext = nextG.size();

			// primeiro trata dos usos locais
			Iterator useIt = g.nonGlobalUses.keySet().iterator();
            while (contNext > 1 && useIt.hasNext()) 
            {
            	String useVarName = (String) useIt.next();
				
				Vector v = g.getPrimNext();
	            for (int j = 0; j < v.size(); j++) {
	                    DefUse assoc = new DefUse(useVarName, 
	                            g.getLabel(),
	                            g.getLabel(), 
	                            ((GraphNode) v.elementAt(j)).getLabel()
	                         );
	                    if ( ! isSec )
	                    	required.put(assoc, new Integer(0));
	                    else
	                    	required2.put(assoc, new Integer(0));
	            }
				
				// o trecho abaixo associa o uso a uma aresta secundaria
				// acho que isso nao deve ser feito, por isso comentei
/*	            if ( useSecond ) {
	                 v = g.getSecNext();
	                 for (int j = 0; j < v.size(); j++) {
	                         DefUse assoc = new DefUse(useVarName, 
	                                 g.getLabel(),
	                                 g.getLabel(), 
	                                 ((GraphNode) v.elementAt(j)).getLabel()
	                                 );
	                         required2.put(assoc, new Integer(0));
	                   }
	            }*/
			}

			// agora trata usos globais
			useIt = g.uses.keySet().iterator();

            while (useIt.hasNext()) 
            {
            	
            	String useVarName = (String) useIt.next();
				String defVarName = null;
				
				HashSet h2 = new HashSet();
				
				// h1 � o conjunto de definicoes vivas
	            Iterator defIt = h1.iterator();
	            while (defIt.hasNext())
	            {
	                Vector pair = (Vector) defIt.next(); 
	                defVarName = (String) pair.elementAt(0);
	                if ( defVarName.equals(useVarName) )
	                {
	                	h2.add(pair);
	                }
				}	
				
				// se nenuma definicao foi encontrada, coloca o primeiro
				// no como definicao			
				if (h2.size() == 0)
				{
					Vector pair = new Vector(2);
					pair.add(useVarName);
					pair.add(graph.getEntry());
					h2.add(pair);
				}
				
				// h2 tem o conjunto de def e os nos para um dado uso
	            defIt = h2.iterator();
	            while (defIt.hasNext())
	            {
	                Vector pair = (Vector) defIt.next(); 
	                defVarName = (String) pair.elementAt(0);
	                /*if ( ! defVarName.equals(useVarName) )
	                {
	                	continue;
	                }*/
	                GraphNode defNode = (GraphNode) pair.elementAt(1);
            		boolean isSecDef = 
            		         secNodes.required.containsKey(defNode.getLabel());
            		
	                Vector v = graph.getNext(g, false);
					
					// se numero de sucesores <= 1 entao existe uma 
					// associa��o defini��o / uso no noh (computacional)
	                if (v.size() <= 1 )
	                {
	                	if (! defNode.getLabel().equals(g.getLabel()) ) // despreza uso local
	                	{
	                    	DefUse assoc = new DefUse(useVarName, 
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
	
	                v = g.getPrimNext();
	                for (int j = 0; j < v.size(); j++) {
	                    DefUse assoc = new DefUse(useVarName, 
	                            defNode.getLabel(),
	                            g.getLabel(), 
	                            ((GraphNode) v.elementAt(j)).getLabel()
	                         );
	                	if ( ! isSec && ! isSecDef ) 
	                		required.put(assoc, new Integer(0));
	                	else
	                		required2.put(assoc, new Integer(0));
	                }
				
				// o trecho abaixo associa o uso a uma aresta secundaria
				// acho que isso nao deve ser feito, por isso comentei
/*	                if ( useSecond ) {
	                    v = g.getSecNext();
	                        for (int j = 0; j < v.size(); j++) {
	                            DefUse assoc = new DefUse(useVarName, 
	                                    defNode.getLabel(),
	                                    g.getLabel(), 
	                                    ((GraphNode) v.elementAt(j)).getLabel()
	                                    );
	
	                            required2.put(assoc, new Integer(0));
	                        }
	                }*/
	            }
            }	
        }
        
        if (useSecond)
        {
        	required = required2;
        }
        simplify();
    }	
	
    /**
	 *  This method apply a few simplifications to the criterion
	 */
	private void simplify() {
		Object[] req = getRequirements(); 

		for (int i = 0; i < req.length; i++) {
			// percorre cada um dos requisitos
			DefUse defuse = (DefUse) req[i];
			
			String var = defuse.var;
			String def = defuse.def;
			String use1 = defuse.useFrom;
			String use2 = defuse.useTo;
			// se existem requisitos (x, No1, (No2,No3) ) e (x, No2, (No2, No3)) 
			// entao elimina o segundo
			// verifica se eh uso predicativo e se nao eh local
			if ( use2 == null || def.equals(use1) )
				continue;
			DefUse alt = new DefUse(var, use1, use1, use2);

			if (required.containsKey(alt)) {
				required.remove(alt);
			}
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
    	//System.out.println("All-Uses addPath...");
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
					// ERROR: adding covered requirement to co instead of co2
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
				
				//System.out.println("Covered use: " + defuse + " " 
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
