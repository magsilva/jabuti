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


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.MethodGen;

import br.jabuti.util.Debug;
import br.jabuti.util.ToolConstants;
import br.jabuti.verifier.InvalidInstructionException;
import br.jabuti.verifier.InvalidStackArgument;


/** This is the class that implements the functionality of a
 JVM code instrumenter. Using such object it is possible
 to insert JVM code in a given JVM method.

 @version: 0.00001
 @author: Auri Marcelo Rizzo Vincenzi
 M�rcio Eduardo Delamaro

 */

public class ClassSummary {
    static boolean detailed = false;
    static boolean defuse = false;
    static boolean alive = false;		
    static boolean dominators = false;
    static boolean childrens = false;
    static boolean cfg = false;
    static boolean callNodes = false;
	
    public static void usage() {
        System.out.println("Java Bytecode ClassSummary");
        System.out.println("\nUSAGE:");
        System.out.println("java graph.ClassSummary [options] -i <class files> | -jar <compressed file>\n");
        System.out.println("      -i <class files>        A list of classes to be instrumented these classes");
        System.out.println("                              should be reachable from the <main_class>.\n");
        System.out.println("      -jar <compressed file>  A compressed .jar or .zip file.\n");
        System.out.println("      [options] could be: [-h | -v | -du | -li | -do | -hi | -cfg | -all ]");
        System.out.println("      -h:                     This help.");
        System.out.println("      -v:                     ClassSummary version number.");
        System.out.println("      -call:                  Creates the CFG with call nodes.");        
        System.out.println("      -du:                    Shows Variables Definitions and Uses of each Node, if any.");
        System.out.println("      -li:                    Shows Alive Variables of each Node, if any.");		
        System.out.println("      -do:                    Shows Dominators and Inverse Dominators of each Node, if any.");		
        System.out.println("      -ch:                    Shows Primary and Secundary Children of each Node, if any.");				
        System.out.println("      -cfg:                   Generates a text file representation (dot format) with the CFG of each method.");
        System.out.println("      -all:                   Enables all options except -h and -v.");
        System.out.println(" If no option is specified a very simple summary w.r.t. ");
        System.out.println(" the given class files is printed out. ");
        System.out.println("\nCopyright (c) 2002\n");
    }

    public static void main(String args[]) throws Throwable {
        JavaClass jc = null;
    	
        HashSet toInstrumenter = null;
        String fileName; // A given class or compressed file name
	
        ZipFile zippedFile = null; // To handle compressed class files
        if (args.length > 0) {

            int i = 0;
			
            if (args.length == 1) {
                if (("-v".equals(args[i])) || ("-h".equals(args[i]))) {
                    if ("-v".equals(args[i])) { 
                        System.out.println("Bytecode ClassSummaty v0.0001");
                    } else if ("-h".equals(args[i])) { 
                        usage();
                    }
                    System.exit(0);
                }
            }
			
            while (i < args.length && args[i].startsWith("-")) {
                // -i: Classes to be instrumented
                if (("-i".equals(args[i])) && (i < args.length - 1)) {
                    if (zippedFile == null) {
                        if (toInstrumenter == null) {
                            toInstrumenter = new HashSet();
                        }
                        i++;
                        fileName = args[i];
                        toInstrumenter.add(getRealName(fileName, ".class"));
                        i++;
                    } else {
                        System.out.println("Options -i and -jar can not appear toghether. ");
                        System.out.println("try java instrumenter.ClassSummary -h for help.");
                        System.exit(0);
                    }
                } // Compressed file...
                else if (("-jar".equals(args[i])) && (i < args.length - 1)) {
                    if (toInstrumenter == null) {
                        i++;
                        fileName = args[i];

                        if (fileName.endsWith(".jar")) {
                            zippedFile = new JarFile(getRealName(fileName, ".jar"));
                        } else if (fileName.endsWith(".zip")) {
                            zippedFile = new ZipFile(getRealName(fileName, ".zip"));
                        } else {
                            System.out.println("ERROR: after a -jar should be specified a .jar or .zip file!!!");
                            System.exit(0);
                        }
                        i++;
                    } else {
                        System.out.println("Options -i and -jar can not appear toghether. ");
                        System.out.println("try java instrumenter.ClassSummary -h for help.");
                        System.exit(0);
                    }
                } else if (("-call".equals(args[i]))) {
                    i++;
                    callNodes = true;
                } else if (("-du".equals(args[i]))) {
                    i++;
                    detailed = true;
                    defuse = true;
                } else if (("-li".equals(args[i]))) {
                    i++;
                    detailed = true;
                    alive = true;
                } else if (("-do".equals(args[i]))) {
                    i++;
                    detailed = true;
                    dominators = true;
                } else if (("-ch".equals(args[i]))) {
                    i++;
                    detailed = true;
                    childrens = true;
                } else if (("-cfg".equals(args[i]))) {
                    i++;
                    cfg = true;
                } else if (("-all".equals(args[i]))) {
                    i++;
                    detailed = true;
                    defuse = true;
                    dominators = true;
                    childrens = true;
                    cfg = true;
                    alive = true;
                } else {
                    System.out.println("Unrecognized option: " + args[i]);
                    System.out.println("try java instrumenter.ClassSummary -h for help.");
                    System.exit(0);
                }
            }

            if (zippedFile != null) {
                Enumeration en = zippedFile.entries();
                ZipEntry zippedEntry = null;

                while (en.hasMoreElements()) {
                    zippedEntry = (ZipEntry) en.nextElement();
                    String className = zippedEntry.getName();

                    if (!className.endsWith(".class")) {
                        continue;
                    }
            		
                    jc = new ClassParser(zippedFile.getInputStream(zippedEntry), className).parse();	// May throw IOException
                    getClassSummary(jc);
                }
            }
	   		
            if (toInstrumenter != null) {
                Iterator it = toInstrumenter.iterator();

                while (it.hasNext()) {
                    jc = new ClassParser((String) it.next()).parse();
                    getClassSummary(jc);
                }
            }
        } else {
            usage();
        }
        System.exit(0);
    }
 	
    /**
     Capturar e imprimir as informa��es sobre defini��es e usuos de vari�veis.
     Gerar o CFG por metodo de cada uma das classes.
     */
 	
    private static void getClassSummary(JavaClass java_class) {
        ClassGen cg = new ClassGen(java_class);
        ConstantPoolGen cp = cg.getConstantPool();
        Method[] methods = cg.getMethods();
  		
        System.out.println("Class File: " + cg.getClassName());
  		
        for (int i = 0; i < methods.length; i++) {
            try {
                MethodGen mg = new MethodGen(methods[i], 
                        cg.getClassName(),
                        cp);
                
                CFG g;
                if (callNodes)
	                g = new CFG(mg, cg, CFG.NONE );
	            else
	            	g = new CFG(mg, cg, CFG.NO_CALL_NODE );
            	
                // For collecting data w.r.t Dominators, Inverse Dominator and Live Variables
                if (detailed) {
//                    g.computeDefUse();
                    RRDominator rrd = new RRDominator("Dominator");

                    g.roundRobinAlgorithm(rrd, true);

                    rrd = new RRDominator("IDominator");
                    g.roundIRobinAlgorithm(rrd, true);

                    RRLiveDefs rral = new RRLiveDefs("Alive definitions", RRLiveDefs.ALL);

                    g.roundRobinAlgorithm(rral, true);
                }

                // Gerarating the CFG file (dot format)
                if (cfg) {
                    createCFGDotFile(g, cg.getClassName(), methods[i].getName());
                    createAllTreeDotFiles(g, cg.getClassName(), methods[i].getName());
                }
            	
                System.out.println("\t\tNumber of Blocks: " + g.size());
                if (detailed) {
                    System.out.println("\t\t\tBlock Details");
                }
            		
                int decisions = 0;
                Vector decisionBlocks = new Vector(5, 5);
                CFGNode pred;

                for (int j = 0; j < g.size(); j++) {
                    pred = (CFGNode) g.elementAt(j);
                    if (detailed) {
                        System.out.println("\t\t\t\t" + pred.getLabel() + " PC: " + pred.getStart() + " to " + pred.getEnd());

                        // Variable Definitions and Usages
                        // Usages...
                        if (defuse) {
                            getUsages(pred);
						
                            // Definitions
                            getDefinitions(pred);
                        }

                        // Children
                        if (childrens) {
                            // Primary children
                            getPrimChildren(pred);
						
                            // Secundary children (Exceptions)
                            getSecChildren(pred);
                        }
	
                        if (dominators) {
                            // Dominators of a given node
                            getDominators(pred);
						
                            // Inverse Dominators
                            getInverseDominators(pred);
                        }
						
                        // Set of live definitions
                        if (alive) {
                            getAliveDefinitions(pred);
                        }
                    }
                    if (pred.getPrimNext().size() > 1) {
                        decisions++;
                        decisionBlocks.add(pred);
                    }
                }
                System.out.println("\t\tNumber of Decisions: " + decisions);
                if (detailed) {
                    System.out.println("\t\t\tDecision Details");
                    while (!(decisionBlocks.isEmpty())) {
                        pred = (CFGNode) decisionBlocks.firstElement();
                        System.out.println("\t\t\t\t" + pred.getLabel() + " Start: " + pred.getStart() + " End: " + pred.getEnd());
                        decisionBlocks.remove(pred);
                    }
                }
            } catch (InvalidInstructionException ii) {
                ToolConstants.reportException( ii, ToolConstants.STDERR );
            } catch (InvalidStackArgument ia) {
                ToolConstants.reportException( ia, ToolConstants.STDERR );
            }
            
        }

    }
 	
    public static String getRealName(String name, String ext) {
        String tmp = new String(name);
		
        if (tmp.indexOf(".class") >= 0) {
            tmp = tmp.substring(0, tmp.length() - 6);
        } else if ((tmp.indexOf(".jar") >= 0) || (tmp.indexOf(".zip") >= 0)) {
            tmp = tmp.substring(0, tmp.length() - 4);
        }
		
        return tmp.replace('.', '/') + ext;
    }
	
    public static String getRealNameNoExtention(String name) {
        String tmp = new String(name);
		
        if (tmp.indexOf(".class") >= 0) {
            tmp = tmp.substring(0, tmp.length() - 6);
        } else if ((tmp.indexOf(".jar") >= 0) || (tmp.indexOf(".zip") >= 0)) {
            tmp = tmp.substring(0, tmp.length() - 4);
        }
		
        return tmp.replace('.', '/');
    }

    public static void createCFGDotFile(CFG gfc, String className, String methodName) {
        String dotFileName;

        if (methodName.equals("<init>")) {
            methodName = "init";
        }
        dotFileName = getRealNameNoExtention(className) + "_" + methodName
                + ".dot";			
		
		System.out.println( "DOT FILE: " + dotFileName );
		
        try {
            PrintWriter dotFile = new PrintWriter(new FileOutputStream(dotFileName));
				
            dotFile.println("digraph " + methodName);
            dotFile.println("{");
            dotFile.println("\tsize=\"7.5,10\"; ");
            dotFile.println("\tratio=auto;");
            // dotFile.println("\tnode [shape=circle, fixedsize=true]; ");
            dotFile.println("\tnodesep=0.1;");

            if (gfc != null) {
                GraphNode[] fdt = gfc.findDFT(true);

                for (int i = 0; i < fdt.length; i++) {
                    CFGNode current = (CFGNode) fdt[i];

                    // if (i == 0) {
                    // dotFile.println("\t" + current.getLabel() + " [style=bold];");
                    // } else
					
                    if (current instanceof CFGCallNode) {
                        dotFile.println("\t" + current.getLabel() + " [shape=doublecircle];");
                    } else
                    if ( ( current.getPrimNext().size() == 0 ) &&
                         ( current.getSecNext().size() == 0 ) ) {
                        // Termination node
                        dotFile.println("\t" + current.getLabel() + " [style=bold];");
                    }

		
                    // Normal edges
                    Vector children = current.getPrimNext();			   			

                    if (children.size() > 0) {
                        String str = " -> { ";

                        for (int j = 0; j < children.size(); j++) {
                            str += ((CFGNode) children.elementAt(j)).getLabel()
                                    + "; ";
                        }
                        str += "};";
                        dotFile.println("\t" + current.getLabel() + str);
                    } 
                    
                    // Exception edges
                    children = current.getSecNext();			   			
                    if (children.size() > 0) {
                        String str = " -> { ";

                        for (int j = 0; j < children.size(); j++) {
                            str += ((CFGNode) children.elementAt(j)).getLabel()
                                    + "; ";
                        }
                        str += "}";
                        dotFile.println("\t" + current.getLabel() + str + "[style=dashed];");
                    } 
                }
            }
            dotFile.println("}");
            dotFile.close();
        } catch (FileNotFoundException e) {
        	ToolConstants.reportException( e, ToolConstants.STDERR );
        }
    }
	
    public static void createAllTreeDotFiles(CFG gfc, String className, String methodName) {

        if (methodName.equals("<init>")) {
            methodName = "init";
        }

        try {
            RRDominator rd = new RRDominator(ToolConstants.LABEL_DOMINATOR);

            gfc.roundRobinAlgorithm(rd, true);
			
            rd = new RRDominator(ToolConstants.LABEL_IDOMINATOR);
            gfc.roundIRobinAlgorithm(rd, true);

            // Calculating the dominator tree...
            DominatorTree dtDom = new DominatorTree(gfc, ToolConstants.LABEL_DOMINATOR);

            dtDom.setDefaultNumbering();
            
            // Printing the current tree
            String dotFileName = getRealNameNoExtention(className) + "_"
                    + methodName;
            
            createTreeDotFile( dtDom, dotFileName + "_Dominator.dot" );        	            
            
            // Calculating the inverse dominator tree...
            DominatorTree dtIDom = new DominatorTree(gfc, ToolConstants.LABEL_IDOMINATOR);

            dtIDom.setDefaultNumbering();

            // Printing the current tree
            createTreeDotFile( dtIDom, dotFileName + "_IDominator.dot" );


            // Merging both trees
            dtDom.merge(dtIDom);

            // Printing the current tree
            createTreeDotFile( dtDom, dotFileName + "_MergedTree.dot" );

            // Calculating the Basic Block Dominator TREE
            DominatorTree bbDom = (DominatorTree) DominatorTree.reduceSCC(dtDom, false);

            if (dtDom.getEntry() != null) {
                bbDom.setEntry(bbDom.getReduceNodeOf(dtDom.getEntry()));
                bbDom.setDefaultNumbering();
            
                // Calculating the Final Basic Block Dominator TREE
                bbDom.removeComposite(false);
            }
            
            if (methodName.equals("<init>")) {
                methodName = "init";
            }
        	
            createSuperBlockFile(bbDom, dotFileName + "_SuperBlock.dot");            
        } catch (Exception e) {
            ToolConstants.reportException(e, ToolConstants.STDERR);
            return;
        }
    } 
	
    public static void createTreeDotFile(DominatorTree bbDom, String dotFileName) {
        try {
            PrintWriter dotFile = new PrintWriter(new FileOutputStream(dotFileName));
				
            dotFile.println("digraph tree");
            dotFile.println("{");
            dotFile.println("\tsize=\"7.5,10\"; ");
            dotFile.println("\tratio=auto;");
            dotFile.println("\tnode [fixedsize=false]; ");
            dotFile.println("\tnodesep=0.1;");

            if (bbDom != null) {
                for (int z1 = 0; z1 < bbDom.size(); z1++) {
                    DominatorTreeNode dtn = (DominatorTreeNode) bbDom.elementAt(z1);
	                
                    dotFile.println("\t" + dtn.getLabel() + " [label=\"" + dtn.getOriginalNode().getLabel() + "\"];");

                    // Normal edges
                    Vector children = dtn.getPrimNext();

                    if (children.size() > 0) {
                        String str = " -> { ";

                        for (int j = 0; j < children.size(); j++) {
                            DominatorTreeNode dtnChild = (DominatorTreeNode) children.elementAt(j);

                            dotFile.println("\t" + dtnChild.getLabel() + " [label=\"" + dtnChild.getOriginalNode().getLabel() + "\"];");
                            str += dtnChild.getLabel() + "; ";
                        }
                        str += "};";
                        dotFile.println("\t" + dtn.getLabel() + str);
                    } 
                }
            }
            dotFile.println("}");
            dotFile.close();
        } catch (FileNotFoundException e) {
        	ToolConstants.reportException( e, ToolConstants.STDERR );
            System.err.println("File " + dotFileName + " not created!!!");
        }
    }

	
    public static void createSuperBlockFile(DominatorTree bbDom, String dotFileName) {
        try {
            PrintWriter dotFile = new PrintWriter(new FileOutputStream(dotFileName));
				
            dotFile.println("digraph tree");
            dotFile.println("{");
            dotFile.println("\tsize=\"7.5,10\"; ");
            dotFile.println("\tratio=auto;");
            dotFile.println("\tnode [fixedsize=false]; ");
            dotFile.println("\tnodesep=0.1;");

            if (bbDom != null) {
                for (int z1 = 0; z1 < bbDom.size(); z1++) {
                    DominatorTreeNode dtn = (DominatorTreeNode) bbDom.elementAt(z1);
	                
                    dotFile.println("\t" + dtn.getLabel() + " [label=\"" + getOriginalNodeNames(dtn) + "\"];");

                    // Normal edges
                    Vector children = dtn.getPrimNext();

                    if (children.size() > 0) {
                        String str = " -> { ";

                        for (int j = 0; j < children.size(); j++) {
                            DominatorTreeNode dtnChild = (DominatorTreeNode) children.elementAt(j);

                            dotFile.println("\t" + dtnChild.getLabel() + " [label=\"" + getOriginalNodeNames(dtnChild) + "\"];");
                            str += dtnChild.getLabel() + "; ";
                        }
                        str += "};";
                        dotFile.println("\t" + dtn.getLabel() + str);
                    } 
                }
            }
            dotFile.println("}");
            dotFile.close();
        } catch (FileNotFoundException e) {
        	ToolConstants.reportException( e, ToolConstants.STDERR );
            System.err.println("File " + dotFileName + " not created!!!");
        }
    }
    
    private static String getOriginalNodeNames(DominatorTreeNode dtn) {
        GraphNode[] nodes = dtn.getOriginalNodes();

        String label = new String();
		
        String[] labels = new String[nodes.length];
    	
        for (int z = 0; z < nodes.length; z++) {
            GraphNode curNode = ((ReduceNode) nodes[z]).getOriginalNode();

            labels[z] = new String(curNode.getLabel());
        }
		
        Arrays.sort(labels);
        for (int z = 0; z < labels.length; z++) {
            if (z == 0) 
                label += labels[z];
            else
                label += ", " + labels[z];
        }
        return label;
    }
	
    static void getUsages(CFGNode pred) {
        if (pred.uses.size() > 0) {
            System.out.print("\t\t\t\t\tVariable uses:");
            Enumeration it = pred.uses.keys();

            while (it.hasMoreElements()) {
                String s = (String) it.nextElement();

                System.out.print("  " + s + " " + "PC: " + pred.uses.get(s));
            }
            System.out.println();
        }
    }
	
    static void getDefinitions(CFGNode pred) {
        if (pred.definitions.size() > 0) {
            System.out.print("\t\t\t\t\tVariable definitions:");
            Enumeration it = pred.definitions.keys();

            while (it.hasMoreElements()) {
                String s = (String) it.nextElement();

                System.out.print("  " + s + " " + "PC: " + pred.definitions.get(s));
            }
            System.out.println();		   					
        }
    }
	
    static void getPrimChildren(CFGNode pred) {	
        Vector next = pred.getPrimNext();
						
        if (next.size() > 0) { 
            System.out.print("\t\t\t\t\tChildren:");
            for (int k = 0; k < next.size(); k++) {
                CFGNode n = (CFGNode) next.elementAt(k);

                System.out.print(" " + n.getLabel());
            }
            System.out.println();		   					
        }
    }

    static void getSecChildren(CFGNode pred) {	
        Vector secNext = pred.getSecNext();

        if (secNext.size() > 0) {
            System.out.print("\t\t\t\t\tException Children:");
            for (int k = 0; k < secNext.size(); k++) {
                CFGNode n = (CFGNode) secNext.elementAt(k);

                System.out.print(" " + n.getLabel());
            }
            System.out.println();
        }
    }
	
    static void getDominators(CFGNode pred) {
        HashSet h1 = (HashSet) pred.getUserData("Dominator");
        Iterator it = h1.iterator();

        System.out.print("\t\t\t\t\tDominators: ");
        while (it.hasNext()) {
            CFGNode gdom = (CFGNode) it.next();

            System.out.print(gdom.getLabel() + " ");
        }
        System.out.println();
    }
	
    static void getInverseDominators(CFGNode pred) {
        HashSet h1 = (HashSet) pred.getUserData("IDominator");
        Iterator it = h1.iterator();

        System.out.print("\t\t\t\t\tInverse Dominators: ");
        while (it.hasNext()) {
            CFGNode gdom = (CFGNode) it.next();

            System.out.print(gdom.getLabel() + " ");
        }
        System.out.println();
    }
	
    static void getAliveDefinitions(CFGNode pred) {
        HashSet h1 = (HashSet) pred.getUserData("Alive definitions");
        Iterator it = h1.iterator();

        System.out.print("\t\t\t\t\tAlive definitions: ");
        while (it.hasNext()) {
            Object p = it.next();

            Debug.D("TYPE: " + p.getClass());
            Vector pair = (Vector) p;
            String def = (String) pair.elementAt(0);
            CFGNode gfcn = (CFGNode) pair.elementAt(1);

            System.out.print("  " + def + " BK: " + gfcn.getLabel());
        }
        System.out.println();
    }
}
