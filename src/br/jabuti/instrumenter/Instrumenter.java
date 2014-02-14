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


package br.jabuti.instrumenter;


import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;


/** This is the class that implements the functionality of a
 JVM code instrumenter. Using such object it is possible
 to insert JVM code in a given JVM method.

 @version: 0.00001
 @author: Marcio Delamaro

 */
public class Instrumenter {
	
    /** The list of instructions of the method */
    InstructionList il;

    /** The method to be instrumented */
    MethodGen meth;
	
    /** The constant pool associated to the method */
    ConstantPoolGen cg;

    /** Creates the instrumenter for a given method 

     @param mg The method to be changed
     */
    public Instrumenter(MethodGen mg, ConstantPoolGen c) {
        meth = mg;
        cg = c;
        il = mg.getInstructionList();
    }
	
    /** Inserts one single instruction before a given instruction
     in the specified method. To insert means that the new instruction
     will take the place of the old one that will be shifted up.
     This means that instructions that had the old instruction as target
     (jumps for example) will target the new instruction.

     @param ih The instruction before which the code will be inserted
     @param x The instruction to be inserted


     */ 	
    public void insertBefore(InstructionHandle ih, Instruction x) {
        InstructionHandle newih = il.insert(ih, x);

        recalTables(ih, newih);
    }


    /** Inserts one single instruction before a given instruction
     in the specified method. To insert means that the new instruction
     will take the place of the old one that will be shifted up.
     This means that instructions that had the old instruction as target
     (jumps for example) will target the new instruction.

     @param ih The instruction before which the code will be inserted
     @param x The instruction to be inserted


     */ 	
    public void insertBefore(InstructionHandle ih, BranchInstruction x) {
        InstructionHandle newih = il.insert(ih, x);

        recalTables(ih, newih);
    }

    /** Add one single instruction before a given instruction
     in the specified method. To add means that the new instruction
     will not take the place of the old one that will be shifted up.
     This means that instructions that had the old instruction as target
     (jumps for example) will not target the new instruction.

     @param ih The instruction before which the code will be inserted
     @param x The instruction to be added


     */ 	
    public void addBefore(InstructionHandle ih, Instruction x) {
        il.insert(ih, x);
    }
	
    /** Redo all the tables for the method after an instruction has been
     inserted. The programer does not need to call it explicitly because
     {@link Instrumenter#insertBefore(InstructionHandle ih, Instruction x)}
     and
     {@link Instrumenter#insertBefore(InstructionHandle ih, InstructionList x)}
     call this method.

     @param ih The original instruction, before which the new instruction
     has been inserted
     @param newih The new instruction, that took the place of the old one
     */

    private void recalTables(InstructionHandle ih, InstructionHandle newih) {
        InstructionTargeter[] it = ih.getTargeters();

        for (int i = 0; it != null && i < it.length; i++) {
            it[i].updateTarget(ih, newih);
        }
        // O codigo acima deve fazer o mesmo que o codigo abaixo.
        /*
         CodeExceptionGen[] ceg = meth.getExceptionHandlers();
         for (int i = 0; i < ceg.length; i++)
         {
         if (ceg[i].getStartPC() == ih)
         ceg[i].setStartPC(newih);
         }
         LocalVariableGen[] lvg = meth.getLocalVariables();
         for (int i = 0; i < lvg.length; i++)
         {
         if (lvg[i].getStart() == ih)
         lvg[i].setStart(newih);
         }
         LineNumberGen[] lng = meth.getLineNumbers();
         for (int i = 0; i < lng.length; i++)
         {
         if (lng[i].getInstruction() == ih)
         lng[i].setInstruction(newih);
         }
         il.redirectBranches(ih, newih);
         */
    }
	
    /** The same of {@link Instrumenter#insertBefore(InstructionHandle ih, Instruction x)}
     but a list of instructions is inserted.

     @param ih The instruction before which the code will be inserted
     @param x The list of instructions to be inserted

     */ 	

    public void insertBefore(InstructionHandle ih, InstructionList x) {	
        InstructionHandle newih = il.insert(ih, x);

        if (newih == null) {
            return;
        }
        recalTables(ih, newih);
    }
			

    /** The same of {@link Instrumenter#addBefore(InstructionHandle ih, Instruction x)}
     but a list of instructions is added.

     @param ih The instruction before which the code will be inserted
     @param x The list of instructions to be inserted

     */ 	
    public void addBefore(InstructionHandle ih, InstructionList x) {			
        il.insert(ih, x);
    }

    /** Inserts one single instruction after a given instruction
     in the specified method. To insert means that the new instruction
     will take the place of the next one that will be shifted down.

     @param ih The instruction before which the code will be inserted
     @param x The instruction to be inserted


     */ 	
    public void insertAfter(InstructionHandle ih, Instruction x) {
        InstructionHandle nextih = ih.getNext();
//        InstructionHandle newih = null;

        if (nextih != null) {		
//            newih = 
            	il.insert(nextih, x);
        } else {
//            newih = 
            	il.append(x);
        }
        // recalTables(ih, newih);
    }

    /** The same of {@link Instrumenter#insertAfter(InstructionHandle ih, Instruction x)}
     but a list of instructions is inserted.

     @param ih The instruction before which the code will be inserted
     @param x The list of instructions to be inserted

     */ 	

    public void insertAfter(InstructionHandle ih, InstructionList x) {			
        InstructionHandle nextih = ih.getNext();
//        InstructionHandle newih = null;

        if (nextih != null) {		
//            newih = 
            	il.insert(nextih, x);
        } else {
//            newih = 
        		il.append(x);
        }
        // if ( newih == null)
        // return;
        // recalTables(ih, newih);
    }
			
    /** Reserves one or more extra local variable.
     *
     * @param n The number of local variables to be reserved
     */
    public void addLocalVar(int n) {
        meth.setMaxLocals(meth.getMaxLocals() + n);
    }

}
