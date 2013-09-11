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



package br.jabuti.util;


import java.util.Vector;

import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.generic.ReturnInstruction;
import org.aspectj.apache.bcel.generic.Select;
import org.aspectj.apache.bcel.generic.UnconditionalBranch;


/** Implements some utility methods concerning the use of the
 JVM instructions */
public class InstructCtrl {
	
    // possible types of use/def for each instruction
	
    /** Computes the set of instructions that can be executed after a 
     given instruction.

     @param x The instruction for which is wanted the set of possible
     followers
     @return The set of instructions that can be executed after the instruction
     got as argument
     */
    static public InstructionHandle[] nextToExec(InstructionHandle x) {
        Vector v = new Vector();
        InstructionHandle pr = x.getNext();
        Instruction inst = x.getInstruction();

        if (pr != null) {
            if (!(inst instanceof UnconditionalBranch || inst instanceof Select
                    || inst instanceof ReturnInstruction || inst instanceof RET)) {
                v.add(pr);
            }
        }
        if (inst instanceof Select) {
            InstructionHandle[] targ = ((Select) inst).getTargets();

            for (int i = 0; i < targ.length; i++) {
                v.add(targ[i]);
            }
        }
        if (inst instanceof BranchInstruction) {
            v.add(((BranchInstruction) inst).getTarget());
        }
        return (InstructionHandle[]) v.toArray(new InstructionHandle[0]);
    }
	
    /** Gets the name of a local variable in a given point of the method.

     @param lvt The local variable table for the method
     @param var The number of the local variable
     @param pc The poit (position) in the method where the name is wanted. Note
     that the same local variable number may be used with different names
     in several differen placed 

     @return The name of the variable. If not defined, returns "???"
     */
    public static String getLocalVariableName(LocalVariableTable lvt, int var, int pc) {
        LocalVariable[] lv = lvt.getLocalVariableTable();

        for (int i = 0; i < lv.length; i++) {
            if (var == lv[i].getIndex() && pc >= lv[i].getStartPC()
                    && pc <= lv[i].getStartPC() + lv[i].getLength()) {
                return lv[i].getName();
            }
        }
        return null;
    }


	/** Given a MethodGen object and an integer representing an offset,
	* finds the InstructionHandle corresponding to that offset
	*
	* @param mg The MethodGen where to search the instruction
	* @param offset The offset of the instruction
	*/
	static public InstructionHandle findInstruction(MethodGen mg, int offset)
	{	
        InstructionList il = mg.getInstructionList();
        
        int[] v = il.getInstructionPositions();
       	
        return InstructionList.findHandle(
            	il.getInstructionHandles(), v ,
            	v.length, offset);
	}
}
