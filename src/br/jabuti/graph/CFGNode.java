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

import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.Instruction;

import br.jabuti.util.Debug;
import br.jabuti.verifier.InstructionGraph;
import br.jabuti.verifier.InstructionNode;

/**
 * This class represents a node of the {@link CFG} graph.
 * It extends the {@link GraphNode} class and stores a 
 * sequence of JVM instructions. <BR>
 * 
 * More precisely each such object stores a set of 
 * {@link InstructionNode} objects that reprtesents a JVM
 * instruction and some other static information about
 * those instructions.
 *
 * 
 * @version <0.00001>
 * @author Marcio Delamaro
 *
 * @see verifier.InstructionNode
 * @see GraphNode
 * @see CFG
 *
 */
public class CFGNode extends GraphNode
{


	/**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -4015954192443614666L;

	/** This field controls the "global" number to be assigned to
     * a node. It allows several {@link CFG} graphs to be sequentialy
     * numbered 
     */
    static int nextNumber = 0;

    /** The set of instructions that are in the node */
    transient protected Vector instructions = new Vector();

    /** The global number assigned to the node */
    int globalNumber = -1;

    /** Definitions in this node */
    public Hashtable definitions,

    /** Uses in this node */
    uses,

    /** Local uses in this node */
    nonGlobalUses;

    CFGNode entryDom = null;

    /** The first instruction of the node */
    private int first,

    /** The last instruction of the node */
    last;

    String prefix = null;

    /** Creates an empty node */
    public CFGNode()
    {
        super();
        definitions = new Hashtable(0, 10);
        uses = new Hashtable(0, 10);
        nonGlobalUses = new Hashtable(0, 10);
    }

    /** Creates from existing node */
    public CFGNode(CFGNode x)
    {
        super(x);
        instructions = x.instructions;
        globalNumber = x.globalNumber;
        definitions = x.definitions;
        uses = x.uses;
        nonGlobalUses = x.nonGlobalUses;
    }

    /** Adds an instruction to the node 
     * 
     * @param x The {@link InstructionNode} to be added to the  node.
     * 		  Instructions are always added to the end of the 
     * 		  {@link CFGNode#instructions} vector and in general represent
     * 		  the order they appear in the JVM code.
     * 		  
     * 
     */
    public void add(InstructionNode x)
    {
        instructions.add(x);
    }

    /** Gets the first instruction of this node
     * 
     * @return The first {@link InstructionHandle} in the {@link CFGNode#instructions}
     *     vector
     */
    public int getStart()
    {
        if (instructions == null)
        {
            return first;
        }
        return ((InstructionNode) instructions.firstElement()).ih.getPosition();
    }

    /** Gets the last instruction of this node
     * 
     * @return The last {@link InstructionHandle} in the {@link CFGNode#instructions}
     *     vector
     */
    public int getEnd()
    {
        if (instructions == null)
        {
            return last;
        }
        return ((InstructionNode) instructions.lastElement()).ih.getPosition();
    }

    /** Returns a string with some important data: <BR>
     * <UL>
     * <LI> The number assigned to this node
     * <LI> The first and last instructions
     * <LI> The locals and fields defined/used in this node 
     * <LI> The next nodes according to primary edges
     * <LI> The next nodes according to secondary (exceptions) edges
     * </UL>
     * 
     * @return The string with those data
     */

    public String toString()
    {
        String str = "node " + getLabel();

        str += "\n Start PC: " + getStart();
        str += "\n End PC: " + getEnd();

        if (uses.size() > 0)
        {
            str += "\nVariable uses ";
            Enumeration it = uses.keys();

            while (it.hasMoreElements())
            {
                String s = (String) it.nextElement();

                str += s + " " + " PC: " + uses.get(s) + " ";
            }
        }
        if (nonGlobalUses.size() > 0)
        {
            str += "\nLocal uses ";
            Enumeration it = nonGlobalUses.keys();

            while (it.hasMoreElements())
            {
                String s = (String) it.nextElement();

                str += s + " " + " PC: " + nonGlobalUses.get(s) + " ";
            }
        }
        if (definitions.size() > 0)
        {
            str += "\nVariable definitions ";
            Enumeration it = definitions.keys();

            while (it.hasMoreElements())
            {
                String s = (String) it.nextElement();

                str += s + " " + " PC: " + definitions.get(s) + " ";
            }
        }

        if (next.size() > 0)
        {
            str += "\n Children ";
            for (int i = 0; i < next.size(); i++)
            {
                CFGNode n = (CFGNode) next.elementAt(i);

                str += n.getLabel() + " ";
            }
        }
        if (secNext.size() > 0)
        {
            str += "\n Exception Children ";
            for (int i = 0; i < secNext.size(); i++)
            {
                CFGNode n = (CFGNode) secNext.elementAt(i);

                str += n.getLabel() + " ";
            }
        }
        str += "\n";
        CFGNode edom = getEntryDom();

        str += "JSR Instruction: "
            + (edom == null ? null : ("" + getEntryDom().getLabel()));
        str += "\n";

        return str;
    }

    /** Assign a number to this node. Sets also its global number with
     * the next sequential number taken from {@link CFGNode#globalNumber}
     * 
     * @param x The number to be assigned to the node. No check is done
     * 		 to verify whether or not another node already has this number.
     * 
     * @see GraphNode#setNumber
     * @see GraphNode#getNumber
     */
    public void setNumber(int x)
    {
        super.setNumber(x);
        globalNumber = nextNumber++;
    }

    /** Gets the global number assigned to this node.
     * 
     * @return The sequential number assigned to this node 
     * using (@link CFGNode#setNumber}
     * 
     */
    public int getGlobalNumber()
    {
        return globalNumber;
    }

    /** Implements {@link Comparator#compare}. Parameters must be
     * {@link CFGNode} objects otherwise a cast exception is thrown.
     * The object which first instruction has the lower PC is considered
     * smaller. In this way two nodes are ordered according to their
     * position in the JVM code.
     * 
     * @param x1 The first object to be compared
     * @param x2 The second object to be compared
     * 
     * @return < 0 if <code>x1</code> starts in a position before <code>x2</code>
     * <BR>    > 0 if <code>x1</code> starts in a position after <code>x2</code>
     * <BR>	0 if <code>x1</code> starts in the same position of <code>x2</code>
     * 	
     */
    public int compare(Object x1, Object x2)
    {
        CFGNode y1 = (CFGNode) x1, y2 = (CFGNode) x2;

        return y1.getStart() - y2.getStart();
    }

    /** This method gets rid of the {@link InstructionGraph} that
     * has been used to build this object. Once all the useful information
     * has been collected and abstracted to the block level in this
     * object, the original instruction graph can be disposed. This 
     * may be necessary because that graph is very demanding in terms
     * of memory consumption.
     * 
     */

    public void releaseInstructions()
    {
        first =
            ((InstructionNode) instructions.firstElement()).ih.getPosition();
        last = ((InstructionNode) instructions.lastElement()).ih.getPosition();
        instructions = null;
    }

    /** This metod computes the definitions and uses for each graph
     * node
     */
    public void computeDefUse()
    {
        for (int i = 0; i < instructions.size(); i++)
        {
            InstructionNode in = (InstructionNode) instructions.elementAt(i);
            Instruction inst = in.ih.getInstruction();

            if (inst instanceof IINC)
            {
                computeUso(in);
                computeDef(in);
            } else
            {
                computeDef(in);
                computeUso(in);
            }
        }
    }

    private void computeDef(InstructionNode in)
    {

        Integer pc = new Integer(in.ih.getPosition());

        Iterator it = in.localDef.iterator();
        while (it.hasNext())
        {
            String s = (String) it.next();

            // Uma definicao de local tem che comecar com L@
            if (!s.startsWith(InstructionNode.STR_LOCAL))
            {
                Debug.D("Invalid definition on " + in);
                return;
            }
            if (!definitions.containsKey(s))
            {
                definitions.put(s, pc);
            }
        }

        it = in.fieldDef.iterator();
        while (it.hasNext())
        {
            String s = (String) it.next();

            if (s.startsWith(InstructionNode.DONT_CARE))
            {
                continue;
            }

            if (!definitions.containsKey(s))
            {
                definitions.put(s, pc);
            }
        }

        it = in.arrayDef.iterator();
        while (it.hasNext())
        {
            String s = (String) it.next();

            if (s.startsWith(InstructionNode.DONT_CARE))
            {
                continue;
            }

            if (!definitions.containsKey(s))
            {
                definitions.put(s, pc);
            }
        }
    }

    private void computeUso(InstructionNode in)
    {
        Integer pc = new Integer(in.ih.getPosition());
        Iterator it = in.localUse.iterator();
        while (it.hasNext())
        {
            String s = (String) it.next();

            // Uma definicao de local tem che comecar com L@
            if (!s.startsWith(InstructionNode.STR_LOCAL))
            {
                Debug.D("Invalid definition on " + in);
                return;
            }
            if (definitions.containsKey(s))
            { // se a var jah foi definida no bloco eh um uso local
                if (!nonGlobalUses.containsKey(s)) // 
                {
                    nonGlobalUses.put(s, pc);
                }
            } else
                if (!uses.containsKey(s))
                {
                    uses.put(s, pc);
                }
        }

        it = in.fieldUse.iterator();
        while (it.hasNext())
        {
            String s = (String) it.next();

            if (s.startsWith(InstructionNode.DONT_CARE))
            {
                continue;
            }

            if (definitions.containsKey(s))
            { // se a var jah foi definida no bloco eh um uso local
                if (!nonGlobalUses.containsKey(s)) // 
                {
                    nonGlobalUses.put(s, pc);
                }
            } else
                if (!uses.containsKey(s))
                {
                    uses.put(s, pc);
                }
        }

        it = in.arrayUse.iterator();
        while (it.hasNext())
        {
            String s = (String) it.next();

            if (s.startsWith(InstructionNode.DONT_CARE))
            {
                continue;
            }

            if (definitions.containsKey(s))
            { // se a var jah foi definida no bloco eh um uso local
                if (!nonGlobalUses.containsKey(s)) // 
                {
                    nonGlobalUses.put(s, pc);
                }
            } else
                if (!uses.containsKey(s))
                {
                    uses.put(s, pc);
                }
        }

    }

    void setEntryDom(CFGNode x)
    {
        entryDom = x;
    }

    CFGNode getEntryDom()
    {
        return entryDom;
    }

    /**
     Returns the label assigned to this node
    
     @return The label of this node
     */
    public String getLabel()
    {
        String str = "";

        if (prefix != null)
        {
            str = prefix + ".";
        }
        return str + getNumber();
    }

    /**
     *	Gets the set of defined variables in this node
     */
    public Hashtable getDefinitions()
    {
        return definitions;
    }

    /**
     *	Get definition offset
     */
    public Integer getDefinitionOffset(String var)
    {
        if (definitions.containsKey(var))
        {
            return (Integer) definitions.get(var);
        }
        // Case the definition is not found it is because
        // it is assigned with the first CFGNode
        // Ou faca g.getEntry().getStart();
        return new Integer(0);
    }

    /**
     *	Gets the set of used variables in this node
     */
    public Hashtable getUses()
    {
        return uses;
    }

    /**
     *	Get definition offset
     */
    public Integer getUseOffset(String var)
    {
        if (uses.containsKey(var))
        {
            return (Integer) uses.get(var);
        }
        /* Some generated def-use associations are
         * generated but the use in the current node
         * does not exists. It is the case of association
         * <L@0.g, 4, (4,27)> and <L@0.g, 4, (4,37)>, of
         * init() method in the Factorial.class.
         * 
         * In this case, there is no bytecode offset assigned
         * with such a use, so, the first bytecode instruction
         * of the node is returned.
         */
        return new Integer(getStart());
        //return null;
    }
}
