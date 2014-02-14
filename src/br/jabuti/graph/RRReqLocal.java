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


import java.util.BitSet;
import java.util.Vector;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.StoreInstruction;

import br.jabuti.verifier.InstructionNode;


public class RRReqLocal implements RoundRobinExecutor {
    private String label;
	
    public RRReqLocal(String x) {
        label = x;
    }
	
    public void init(GraphNode x) { // uses the classname as the key
        x.setUserData(label, new BitSet());
    }
				
    // Just to satisfy all the implementations 
    // required by RoundRobinExecutor
    public void init(GraphNode x, Vector primary, Vector secondary) {
        this.init(x);
    }
				
    public Object calcNewSet(GraphNode theNode, 
            Vector primary, 
            Vector secondary) {
        BitSet req = new BitSet(); // the new set
        Vector nx = primary;
        InstructionNode theInstNode = (InstructionNode) theNode;

        for (int i = 0; i < nx.size(); i++) {
            InstructionNode in = (InstructionNode) nx.elementAt(i);
            BitSet bs = (BitSet) in.getUserData(label);

            req.or(bs);
        }
        nx = secondary;
        for (int i = 0; i < nx.size(); i++) {
            InstructionNode in = (InstructionNode) nx.elementAt(i);
            BitSet bs = (BitSet) in.getUserData(label);

            req.or(bs);
        }
        Instruction ins = theInstNode.ih.getInstruction();

        if (ins instanceof LoadInstruction) {
            LoadInstruction li = (LoadInstruction) ins;
            int index = li.getIndex();

            req.set(index);
        } else
        if (ins instanceof StoreInstruction) {
            StoreInstruction si = (StoreInstruction) ins;
            int index = si.getIndex();

            req.clear(index);
        }
        return req;
    }
				
    public boolean compareEQ(GraphNode theNode, Object theNewSet) {
        BitSet r1 = (BitSet) theNode.getUserData(label);

        if (theNewSet == null) {
            return r1 == null;
        }
        return theNewSet.equals(r1);
    }

    public void setNewSet(GraphNode theNode, Object theNewSet) {
        theNode.setUserData(label, theNewSet);
    }	
}
