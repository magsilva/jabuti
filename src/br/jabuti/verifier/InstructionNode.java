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


package br.jabuti.verifier;


import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BREAKPOINT;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.ConversionInstruction;
import org.apache.bcel.generic.DCMPG;
import org.apache.bcel.generic.DCMPL;
import org.apache.bcel.generic.DNEG;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.DUP2_X1;
import org.apache.bcel.generic.DUP2_X2;
import org.apache.bcel.generic.DUP_X1;
import org.apache.bcel.generic.DUP_X2;
import org.apache.bcel.generic.FCMPG;
import org.apache.bcel.generic.FCMPL;
import org.apache.bcel.generic.FNEG;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.IMPDEP1;
import org.apache.bcel.generic.IMPDEP2;
import org.apache.bcel.generic.INEG;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.IndexedInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.LCMP;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LNEG;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RET;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.SWAP;
import org.apache.bcel.generic.StackProducer;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.TypedInstruction;

import br.jabuti.graph.GraphNode;
import br.jabuti.graph.RRReqLocal;
import br.jabuti.util.Debug;


/** This class represents a node of a {@link InstructionGraph} that
 is a program graph where each node is a single JVM instruction.
 Besides the instruction itself, an object of this class contains
 several addition information like the possible types the stack
 and local variables can assume when the instruction is executed,
 the variables used/defined in that instruction, etc

 @version: 0.00001
 @author: Marcio Delamaro

 */
public class InstructionNode extends GraphNode {
	
    /**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -8329836653849738754L;

	/** The instruction in this node */
    public InstructionHandle ih;

    InstructionNode domEntry = null;

    /** The current stack, i.e., the stack before executing the instruction */
    VMStack theStack, 

            /** The next stack, i.e., the stack just after executing the instruction */
            nextStack; // current and next stack
        
    /** The current local variables, i.e., the locals before executing the instruction */
    VMLocal	localVars, 

            /** The next local variables, i.e., the locals just after executing the instruction */
            nextVars; // current and next variable

    /** This vector stores the set of stacks after the instruction execution.
     It is used whith {@link InstructionNode#calcAllStack}, its just a intermediary
     variable */
    Vector	vnextStack, 

            /** This vector stores the set of locals after the instruction execution.
             It is used whith {@link InstructionNode#calcAllStack}, its just a intermediary
             variable */
            vnextVars;  
		
    /** The string that indicates use/definition of a local variable */
    final static public String STR_LOCAL = "L@";
    final static public String STR_STATIC = "S@";
    final static public String DONT_CARE = "DC";
							 
    /** Indicates whether the stack/locals have changed */
    boolean changed;

    /** Indicates whether the instruction is a call to super */
    public boolean isSuper;

    /** if an error occurred when executing the instruction, the cause is
     stored here. For example, trying to access an array position from a 
     non array reference  {@link VMLocal} */
    Exception err;

    /** The set of all possible local variable configuration that reach this instruction */
    Vector   allLocals,

            /** The set of all possible stack configuration that reach this instruction.
             Each element is a {@link VMStack} */
            allStack;

    /** This indicates which local var is argument to the instruction */
    public int argLocal,

            /** This indicates the first position in the stack that is argumento to
             the instruction */
            argStackFrom,

            /** This indicates the last position in the stack that is argumento to
             the instruction */
            argStackTo;

    static private int  VECTOR_INIT = 1,
            VECTOR_INC = 10;

    /** This string is used to store a {@link BitSet} 
     object using the {@link GraphNode#setUserData} method. 
     Such BitSet represents which local variable are required
     to be kept track in this instruction. For example, if the instructions
     after this one depend on local variables 1, 3 and 5, only these three
     variables will be considered to computa the {@link InstructionNode#allLocals}
     set. For the stack this is not necessary because we know that all the
     elements up to the stack size at the instruction execution will be used. */
    public final static String reqLocalLabel = RRReqLocal.class.toString();
	
    /** Creates an instruction node */
    public InstructionNode(InstructionHandle hi) {
        super();
        ih = hi;
        theStack = null;
        localVars = null;
        changed = isSuper = false;
        err = null;
        allLocals = new Vector(VECTOR_INIT, VECTOR_INC);
        allStack = new Vector(VECTOR_INIT, VECTOR_INC);
        vnextStack = new Vector(VECTOR_INIT, VECTOR_INC);
        vnextVars = new Vector(VECTOR_INIT, VECTOR_INC);
        argLocal = -1;
        argStackFrom = -1;
        argStackTo = -1;
        setUserData(reqLocalLabel, new BitSet());
    }	

    /** Returns the stack expected when the instruction is 
     executed. It makes sense only if {@link InstructionNode#calcStack}
     has being called and {@link InstructionNode#calcAllStack} has not.
     In this case, the resulting stack is the "merge" of all the configurations
     of the stacks that reach this instruction.

     @return The expected stack when the execution reaches this instruction.
     @see InstructionNode#calcStack
     @see InstructionNode#merge 
     */
    public VMStack getStack() {
        return theStack;
    }
	
    /** Returns the local variables expected when the instruction is 
     executed. It makes sense only if {@link InstructionNode#calcStack}
     has being called and {@link InstructionNode#calcAllStack} has not.
     In this case, the resulting stack is the "merge" of all the configurations
     of the stacks that reach this instruction.

     @return The expected locaL variables when the execution reaches this instruction.
     @see InstructionNode#calcStack
     @see InstructionNode#merge 
     */
    public VMLocal getLocals() {
        return localVars;
    }
    
    /** Return the stack after the execution of this instruction. 
     It makes sense only if {@link InstructionNode#calcStack}
     has being called and {@link InstructionNode#calcAllStack} has not.
     Used only inside the methods, not supposed to be called externally */

    private VMStack getNextStack() {
        return nextStack;
    }
	
    /** Return the local variables after the execution of this instruction. 
     It makes sense only if {@link InstructionNode#calcStack}
     has being called and {@link InstructionNode#calcAllStack} has not.
     Used only inside the methods, not supposed to be called externally */
    private VMLocal getNextLocals() {
        return nextVars;
    }

    /** Return the k-th stack after the execution of this instruction. 
     It makes sense only inside {@link InstructionNode#calcAllStack}
     because in this case there may exist more than one possible 
     "next" stack.
     Used only inside the methods, not supposed to be called externally 

     @param k The number of the stack to be recovered.
     @return The k-th "next" stack 
     */
    private VMStack getNextStack(int k) {
        return k >= vnextStack.size() ? null : (VMStack) vnextStack.elementAt(k);
    }
	
    /** Return the k-th local variable configuration after the 
     execution of this instruction. 
     It makes sense only inside {@link InstructionNode#calcAllStack}
     because in this case there may exist more than one possible 
     "next" stack.
     Used only inside the methods, not supposed to be called externally 

     @param k The number of the local variable configuration to be recovered.
     @return The k-th "next" local variable configuration 
     */
    private VMLocal getNextLocals(int k) {
        return k >= vnextVars.size() ? null : (VMLocal) vnextVars.elementAt(k);
    }
    
    /** Throws away the computed set of "next" stacks and local variable
     configurations */
    public void removeNextStackLocal() {
        vnextStack = new Vector(VECTOR_INIT, VECTOR_INC);
        vnextVars = new Vector(VECTOR_INIT, VECTOR_INC);
        nextStack = null;
        nextVars = null;
    }    	

    /** Returns whether the "execution" of this instruction, changed the
     stack or local variable configuration. More precisely, returns whether 
     or not the last call to {@link InstructionNode#calcStack} has changed
     the "next" stack or local variable 

     @return true - If the last call to {@link InstructionNode#calcStack}
     changed the configuration of the "next" stack or "next" local
     variable.

     @see InstructionNode#merge
     @see InstructionNode#calcStack
     */
    public boolean changed() {
        return changed;
    }

    /** Set the value of the {@link InstructionNode#changed} field.

     @param x The value to be assigned to the field.
     */    
    public void setChanged(boolean x) {
        changed = x;
    }
    
    /** Returns whether this instruction is unreacheable code. The decision
     is based on the value of an internal variable 
     {@link InstructionNode#theStack} that should be not be null if the
     {@link InstructionNode#calcStack} or {@link InstructionNode#calcAllStack}
     are ever called to this instruction.

     @return true - If the value of {@link InstructionNode#theStack} is 
     null. It means that {@link InstructionNode#calcStack} or 
     {@link InstructionNode#calcAllStack} have never been called to this
     instruction and so it is unreacheable.
     */ 
    public boolean isUnreacheable() {
        return theStack == null;
    }

    /** Sets the value of the "required locals" set.

     @param x The new value to be assigned to the set
     */
    public void setReqLocal(BitSet x) {
        setUserData(reqLocalLabel, x);
    }
	
    /** Gets the value of the "required locals" set

     @return The required locals set
     */
    public BitSet getReqLocal() {
        return (BitSet) getUserData(reqLocalLabel);
    }
	
    /** Initialize the stack and local configuration for this instruction.
     This method should be called before calling {@link InstructionNode#calcStack}
     and only for the entry node and for the exception handlers (the first
     instruction of).

     @param stack The initial stack for this instruction. If it is the
     entry node, the initial stack is empty. If it is an exception handler
     it is a stack with one single element whith the type of the exception
     it handles. If the type is &lt;all exceptions&gt; the type of the element
     is {link@ java.lang.Exception}.
     @param locals The configuration of the local variables to this instruction.
     If it is the entry node, the locals should reflect the parameters of the
     method (with "this" in the local 0, etc). If the instruction is an exception
     handler all local variables are set unintialized, i.e., null.
     @param The {@link ConstantPool} object that represents the constant pool
     of this class (unfortunately, in some cases it is necessary...

     @return Nothing, but besides the stack and local variables, the 
     {@link InstructionNode#changed} flag is set.
     */

    public void initStack(VMStack stack, VMLocal locals, ConstantPoolGen cp)
            throws InvalidInstructionException, InvalidStackArgument {
        // Debug.D("initStack " + ih); 
		
        // initialize the "current" stack
        theStack = (VMStack) stack.clone();
        localVars = (VMLocal) locals.clone();

        // calcula a pilha apos a execucao
        nextStack = (VMStack) stack.clone();
        nextVars = (VMLocal) locals.clone();
		
        execInstruction(nextStack, nextVars, cp);
        err = null;
        changed = true;
    }
	
    /** Initialize the stack and local configuration for this instruction.
     This method should be called before calling {@link InstructionNode#calcAllStack}
     and only for the entry node.

     @param stack The initial stack for this instruction. If it is the
     entry node, the initial stack is empty. If it is an exception handler
     it is a stack with one single element whith the type of the exception
     it handles. If the type is &lt;all exceptions&gt; the type of the element
     is {link@ java.lang.Exception}.
     @param locals The configuration of the local variables to this instruction.
     If it is the entry node, the locals should reflect the parameters of the
     method (with "this" in the local 0, etc). If the instruction is an exception
     handler all local variables are set unintialized, i.e., null.
     @param The {@link ConstantPool} object that represents the constant pool
     of this class (unfortunately, in some cases it is necessary...

     @return Nothing, but besides the stack and local variables, the 
     {@link InstructionNode#changed} flag is set.
     */
    public void initAllStack(VMStack stack, VMLocal locals, ConstantPoolGen cp)
            throws InvalidInstructionException, InvalidStackArgument {
        // Debug.D("initAllStack " + ih); 
        // initialize the "current" stack
        addStack(theStack = (VMStack) stack.clone());
        addLocal(localVars = (VMLocal) locals.clone());

        // calcula a pilha apos a execucao
        nextStack = (VMStack) stack.clone();
        nextVars = (VMLocal) locals.clone();
		
        execInstruction(nextStack, nextVars, cp);
        err = null;
        vnextStack.add(nextStack);
        vnextVars.add(nextVars);
        changed = true;
    }

    /** Initialize the stack and local configuration for this instruction.
     This method should be called before calling {@link InstructionNode#calcAllStack}
     and only for the exception handlers.

     @param stack The initial stack for this instruction. If it is the
     entry node, the initial stack is empty. If it is an exception handler
     it is a stack with one single element whith the type of the exception
     it handles. If the type is &lt;all exceptions&gt; the type of the element
     is {link@ java.lang.Exception}.

     @return Nothing, but besides the stack and local variables, the 
     {@link InstructionNode#changed} flag is set.
     */
    public void initAllStack(VMStack stack) {
        // Debug.D("initAllStack " + ih); 
        // initialize the "current" stack
        addStack(theStack = (VMStack) stack.clone());
        changed = true;
    }

    /** <p>This method is used to compute the stack and the local variables
     after the execution of this instruction. It uses the "next" stack and
     local variables of a predecessor instruction, passed as argument.<BR><p>
     <p>In summary, the method:<BR>
     <UL>
     <LI> Takes the configuration from the previous instruction, i.e.,
     <code>vh.nextStack</code>
     <LI> "Merges" (see {@link InstructionNode#merge}) these configurations
     to the ones already computed up to this point. If no change in the
     stack is seen, the method ends. 
     <LI> Use these configurations and applies ("executes") the current
     instruction. Note tha this is totally dependent of the instruction
     semantics.
     <LI> Stores this new configurations in its own "next" variables.
     </UL>

     @param vh The previous instruction, from where the initial stack 
     is taken.
     @param cp The Constant Pool of the current class. It is used to analyse
     some instructions that refers to the CP

     @return Nothing, but changes <BR>
     <UL>
     <LI> the {@link InstructionNode#changed} flag
     <LI> the {@link InstructionNode#theStack} and {@link InstructionNode#localVars}
     that store the current stack and local configurations
     <LI> the {@link InstructionNode#nextStack} and {@link InstructionNode#nextVars}
     that store the stack and local configurations after executiong the instruction
     </UL>

     @throws InvalidInstructionException If this InstructionNode does not represents
     a valid JVM instruction
     @throws InvalidStackArgument If the stack got from the previous instruction does
     not have the same size of the current stack
     */
    public void calcStack(InstructionNode vh, ConstantPoolGen cp)
            throws InvalidInstructionException, InvalidStackArgument {
        // Debug.D("calcStack " + ih + "\n" + vh.ih); 
		
        // se a pilha nao foi inicializada, inicializa com
        // o "next" do predecessor
		
        VMLocal prev = vh.getNextLocals();
        VMStack prevs = (VMStack) vh.getNextStack();

        if (theStack == null) {
            theStack = (VMStack) prevs.clone();
            localVars = (VMLocal) prev.clone();
            changed = true;
        } else { // se jah existe a pilha faz o merge com o predecessor
            changed = merge(vh.getNextStack(), vh.getNextLocals());
			
        }
        // so calcula o next se hove mudanca na pilha "corrente"
		
        if (changed) {
            try {
                nextStack = (VMStack) theStack.clone();
                nextVars = (VMLocal) localVars.clone();
                execInstruction(nextStack, nextVars, cp);
                err = null;
            } catch (InvalidStackArgument e) {
                // se nao eh possivel executar, anula a pilha e marca
                // como erro
                Debug.D(e.getMessage());
                err = e;
                changed = false;
            }
        }
    }
	
    /** <p>This method is used to compute all the stack and the local variables
     configurations 
     after the execution of this instruction. It uses the set of "next" stack and
     local variables of a predecessor instruction, passed as argument.<BR><p>
     <p>In summary, the method:<BR>
     <UL>
     <LI> Takes the i-th configuration from the previous instruction, i.e.,
     <code>vh.getNextStack(i)</code>
     <LI> Removes from the local variables configuration the local variable
     that does not influence the subsequent instructions. 
     <LI> Checks whether the local variable configuration (after the 
     preveious step) is new, i.e., if it is not in the set of "known" 
     configurations
     <LI> Checks whether the stack configuration (after the 
     preveious step) is new, i.e., if it is not in the set of "known" 
     configurations
     <LI> If none of the above has changed, nothing os done. Otherwise
     <LI> Use these configurations and applies ("executes") the current
     instruction. Note that this is totally dependent of the instruction
     semantics.
     <LI> Stores this new configurations in its own "next" sets.
     </UL>

     @param vh The previous instruction, from where the initial stack 
     is taken.
     @param cp The Constant Pool of the current class. It is used to analyse
     some instructions that refers to the CP

     @return Nothing, but changes <BR>
     <UL>
     <LI> the {@link InstructionNode#changed} flag
     <LI> the {@link InstructionNode#vnextStack} and {@link InstructionNode#localVars}
     that store the current stack and local configurations
     <LI> the {@link InstructionNode#nextStack} and {@link InstructionNode#vnextVars}
     that store the stack and local configurations after executing the instruction
     </UL>



     @throws InvalidInstructionException If this InstructionNode does not represents
     a valid JVM instruction
     */
    public void calcAllStack(InstructionNode vh, ConstantPoolGen cp)
            throws InvalidInstructionException {
        // Debug.D("calcAllStack" + ih + "\n" + vh.ih); 
        boolean changedLocal = true, changedStack = true;
		
        // se a pilha nao foi inicializada, inicializa com
        // o "next" do predecessor
		
        VMLocal prev = localVars = vh.getNextLocals(0);
        VMStack prevs = theStack = (VMStack) vh.getNextStack(0);
        int contNext = 0;

        while (prev != null) {
            prev = (VMLocal) prev.clone();
            //VMLocal aux = prev;

            /* se uma determinada variavel nao estah no conjunto
             requerido, seta seu valor para null */
            for (int i = 0; i < prev.size(); i++) {
                if (!getReqLocal().get(i)) {
                    prev.add(null, i);
                }
            }
            changedStack = changedStack(prevs);
            changedLocal = changedLocal(prev); 
            changed = changedStack || changedLocal;
            // so calcula o next se hove mudanca na pilha "corrente"
		
            if (changed) {
                try {
                    nextStack = (VMStack) prevs.clone();
                    nextVars = (VMLocal) prev.clone();
                    execInstruction(nextStack, nextVars, cp);
                    if ((!vnextStack.contains(nextStack))
                            || (!vnextVars.contains(nextVars))) {
                        vnextStack.add(nextStack);
                        vnextVars.add(nextVars);
                    }
                    err = null;
                    if (changedLocal) {
                        addLocal((VMLocal) prev.clone());
                    }
                    if (changedStack) {
                        addStack((VMStack) prevs.clone());
                    }
                } catch (InvalidStackArgument e) {
                    // se nao eh possivel executar, nao inclui
                    // no next
                    Debug.D("Execution failed: " + contNext + " " + e.getMessage());
                    err = e;
                }
            }
            contNext++;
            prev = vh.getNextLocals(contNext);
            prevs = (VMStack) vh.getNextStack(contNext);
        }
        changed = (vnextVars.size() > 0);
    }

    /*
     <P>
     This method acts the same as {@link InstructionNode#calcStack}
     but it does not change the stack of the instruction. It is 
     used to deal with the first instruction of an exception 
     handler. For such instruction the stack is not computed
     froma previous instruction because throwing the exception 
     empties the stack. The stack for this instruction is computed
     at the begining of the process. <BR></p>

     The locals on the other hand should be computed from the previous
     instructions, i.e., from every instruction that in inside the
     handler range.

     @param vh The previous instruction, from where the initial stack 
     is taken.
     @param cp The Constant Pool of the current class. It is used to analyse
     some instructions that refers to the CP


     @throws InvalidInstructionException If this InstructionNode does not represents
     a valid JVM instruction
     @throws InvalidStackArgument If the stack got from the previous instruction does
     not have the same size of the current stack (it is not supposed to happen)
     */

    public void calcLocal(InstructionNode vh, ConstantPoolGen cp)
            throws InvalidInstructionException, InvalidStackArgument {
        // Debug.D("calcLocal " + ih + "\n " + vh.ih); 
        // supoe-se que a pilha jah estah inicializada
        // faz o merge com as locais do predecessor e o proprio
        // stack. assim changed e true se as locais mudaram
        VMStack aStack = (VMStack) theStack.clone();
        VMLocal prev = vh.getNextLocals();
		
        changed = merge(aStack, prev);
		
        // so calcula o next se hove mudanca na pilha "corrente"
		
        if (changed) {
            try {
                nextStack = aStack;
                nextVars = (VMLocal) localVars.clone();
                execInstruction(nextStack, nextVars, cp);
                err = null;
            } catch (InvalidStackArgument e) {
                // se nao eh possivel executar, anula a pilha e marca
                // como erro
                Debug.D(e.getMessage());
                err = e;
                changed = false;
            }
        }
    }

    /*
     <P>
     This method acts the same as {@link InstructionNode#calcStack}
     but it does not change the stack of the instruction. It is 
     used to deal with the first instruction of an exception 
     handler. For such instruction the stack is not computed
     froma previous instruction because throwing the exception 
     empties the stack. The stack for this instruction is computed
     at the begining of the process. <BR></p>

     The locals on the other hand should be computed from the previous
     instructions, i.e., from every instruction that in inside the
     handler range.

     @param vh The previous instruction, from where the initial stack 
     is taken.
     @param cp The Constant Pool of the current class. It is used to analyse
     some instructions that refers to the CP


     @throws InvalidInstructionException If this InstructionNode does not represents
     a valid JVM instruction
     */
    public void calcAllLocal(InstructionNode vh, ConstantPoolGen cp)
            throws InvalidInstructionException {
        // Debug.D("calcAllLocal " + ih + "\n " + vh.ih); 

        // supoe-se que a pilha jah estah inicializada

        VMStack aStack = (VMStack) theStack.clone();
        VMLocal prev = vh.getNextLocals(0);
        int contNext = 0;		

        while (prev != null) {
            prev = (VMLocal) prev.clone();
            localVars = prev;

            /* se uma determinada variavel nao estah no conjunto
             requerido, seta seu valor para null */
            for (int i = 0; i < prev.size(); i++) {
                if (!getReqLocal().get(i)) {
                    prev.add(null, i);
                }
            }
            changed = changedLocal(prev);
            // so calcula o next se hove mudanca na pilha "corrente"
		
            if (changed) {
                try {
                    nextStack = (VMStack) aStack.clone();
                    nextVars = (VMLocal) prev.clone();
                    execInstruction(nextStack, nextVars, cp);
                    err = null;
                    addLocal((VMLocal) prev.clone());
                    if (!vnextVars.contains(nextVars)) {
                        vnextStack.add(nextStack);
                        vnextVars.add(nextVars);
                    }
                } catch (InvalidStackArgument e) {
                    // se nao eh possivel executar, nao insere next
                    Debug.D("Execution failed: " + contNext + " " + e.getMessage());
                    err = e;
                }
            }
            contNext++;
            prev = vh.getNextLocals(contNext);
        }
        changed = (vnextVars.size() > 0);
    }

    /*
     <p>
     This method merges the current stack and local variable configuration 
     with another one got from a previous instruction. Here, merge means:<BR></p>

     <p><UL>
     <LI> if one spot in the stack (or local) is the type NULL or is
     empty (only for the locals) and in the new stack (local)
     is not, them fill the spot whith the new, and returns true (changed);
     <LI> if the spot is not null or empty but the new one is, 
     them keep the current spot and return false;
     <LI> if neither current or new spot are null or empty but are the same, return false;
     <LI> if neither are null or empty but are not the same, then return false
     </UL>
     </p>
     <p>
     According to the JVM specification, the last item above should be <BR></p>
     <p><UL>
     <LI> if neither are null or empty but are not the same, then find the
     closer commom ancestor of these two types and it will be placed in the spot
     in the current stack and the returnin value is true.
     </p>

     <p>It means that we should find the commom ancestor and use it. This is not
     implemented. Mainly because this method is used in the 
     {@link InstructionNode#calcStack} method and this method is supersued by 
     {@link InstructionNode#calcAllStack}.

     @param xt The new stack to be merged with the current one, in 
     {@link InstructionNode#theStack}
     @param vars The new local variable configuration, to be merges 
     with the curren one in {@link InstructionNode#localVars}

     @return true is the merge operation changes the current stack of
     local variable cnfigurations

     @throws InvalidStackArgument if the size of the new stack does not match
     the current. It means this instruction is reached by 2 different sizes
     of stack, what is illegal according to the JVM specification.

     */
    public boolean merge(VMStack xt, VMLocal vars)
            throws InvalidStackArgument {
        VMStack st = (VMStack) xt.clone();

        if (theStack.size() != st.size() || localVars.size() != vars.size()) {
            throw new InvalidStackArgument(
                    "Two paths reach " + ih + " with different stack depth");
        }
        boolean ret = false;

        if (st != theStack) {
            VMStack ns = new VMStack(theStack.length());

            while (!theStack.empty()) {
                VMStackElement  se = st.top(),
                        se2 = theStack.top();

                st.pop(1);
                theStack.pop(1);
                if (se2.type == Type.NULL && se.type != Type.NULL) {
                    ret = true;
                    ns.push(se);
                } else {
                    ns.push(se2);
                }
            }
            while (!ns.empty()) {
                theStack.push(ns.top());
                ns.pop();
            }
        }
        for (int i = 0; i < localVars.size(); i++) {
            if (localVars.get(i) != vars.get(i)
                    && (localVars.get(i) == null
                    || localVars.get(i) == Type.NULL)) {
                ret = true;
                localVars.add(vars.get(i), i);
            }
        }
        return ret;
    }
	
    /** Verifies if the local variable configuration passed as argument
     is a new one, i.e., if it is not yet in the collection of configurations
     store in this instruction. <BR>
     This method is called from {@link InstructionNode#calcAllStack}.

     @param x A local var configuration that will be checked to see if
     it already belongs to the set os configurations of this instruction

     @return true If the set of local configurations does not contains
     the argument.
     */
    private boolean changedLocal(VMLocal x) {
        return !allLocals.contains(x);
    }
	
    /** Verifies if the stack configuration passed as argument
     is a new one, i.e., if it is not yet in the collection of configurations
     store in this instruction. <BR>
     This method is called from {@link InstructionNode#calcAllStack}.

     @param x A stack configuration that will be checked to see if
     it already belongs to the set os configurations of this instruction

     @return true If the set of local configurations does not contains
     the argument.
     */
    private boolean changedStack(VMStack x) {
        return !allStack.contains(x);
    }

    /**
     Adds the argument in the set of stack configurations of this instruction.

     @param x A stack configuration that will added
     to the set os configurations of this instruction
     */
    private void addStack(VMStack x) {
        allStack.add(x);
    }
	
    /**
     Adds the argument in the set of local variable configurations of this instruction.

     @param x A local Variable configuration that will added
     to the set os configurations of this instruction
     */
    private void addLocal(VMLocal x) {
        allLocals.add(x);
    }
	
    /** <p>
     This is the method that calculates the efect of this instruction in a given
     configuration of the stack and local variables. It also inserts the uses and
     definitions in the sets use/def of this instruction </p>

     @param st The initial stack on which the instruction will be applied; it 
     may be changed by the instruction and hold the result of executiong it;
     @param st The initial local variables on which the instruction will be applied; it 
     may be changed by the instruction and hold the result of executing it;
     @param cp The constant pool of the class

     @throws InvalidInstructionExceptio If this object does not correspond to
     a valid JVM instruction. Should not occour.
     @param InvalidStackArgument If the stack or local variables have in a given
     spot a type that is not compatible with the instruction.

     */
    public void execInstruction(VMStack st, VMLocal locals, ConstantPoolGen cp)
            throws InvalidInstructionException, InvalidStackArgument { 
//        int delta = ih.getInstruction().produceStack(cp)
//                - ih.getInstruction().consumeStack(cp);
        Instruction ins = ih.getInstruction();
		
        if (ins instanceof ACONST_NULL) {
            st.push(new VMStackElement(Type.NULL, DONT_CARE, this));
        } else
        if (ins instanceof DNEG || ins instanceof FNEG || ins instanceof INEG
                || ins instanceof LNEG) {
            setStackArgs(st, 1);
            Type t1 = ((ArithmeticInstruction) ins).getType(cp);

            st.pop(1);
            VMStackElement vme = new VMStackElement(t1, DONT_CARE, this);

            st.push(vme);
        } else
        if (ins instanceof ArithmeticInstruction) {
            setStackArgs(st, 2);
            Type t1 = ((ArithmeticInstruction) ins).getType(cp);

            st.pop(2);
            VMStackElement vme = new VMStackElement(t1, DONT_CARE, this);

            st.push(vme);
        } else
        if (ins instanceof ArrayInstruction) {
 //           Type t = ((ArrayInstruction) ins).getType(cp);
            int k = (ins instanceof StackProducer) ? 0 : 1;
            // t1 = tipo do indice (inteiro)
//            Type t1 = st.top(k).type;
            
            // t2 � o tipo do array por exemplo int[]
            Type t3, t2 = st.top(k + 1).type;
			
			// t3 � o tipo do elemento a ser armazenado p.e. int se t2 = int[]
            if (t2 instanceof ArrayType) {
                t3 = ((ArrayType) t2).getElementType();
            } else {
                throw new InvalidStackArgument("Instruction " + ins + " requires an array reference");
            }
			
			// adiciona [] na indicacao da origem do array.
			// por exemplo L@1[]
            String defUse = st.top(k + 1).defuse + "[]";

            if (k == 0) // is a LOAD instruction
            {
                setStackArgs(st, 2);
                VMStackElement vme = 
                        new VMStackElement(t3, defUse, this);

                st.pop(2);
                st.push(vme);
                useArrayAdd(defUse);
            } else // is a STORE
            {
                setStackArgs(st, 2);
                st.pop(3);
                defArrayAdd(defUse);
            }	
        } else
        if (ins instanceof ARRAYLENGTH || ins instanceof INSTANCEOF) {
            setStackArgs(st, 1);
            VMStackElement vme = 
                    new VMStackElement(Type.INT, DONT_CARE, this);

            st.pop();
            st.push(vme);
        } else
        if (ins instanceof ATHROW) {
            setStackArgs(st, 1);
            VMStackElement vme = st.top();

            vme.producer = this;
            st.reset();
            st.push(vme);
        } else
        if (ins instanceof ConstantPushInstruction) {
            ConstantPushInstruction iins = (ConstantPushInstruction) ins;
            VMStackElement vme = 
                    new VMStackElement(iins.getType(cp), DONT_CARE, this);

            st.push(vme);
        } else
        if (ins instanceof JsrInstruction) {
            JsrInstruction iins = (JsrInstruction) ins;
//            InstructionHandle jr = iins.physicalSuccessor();
            VMStackElement vme = 
                    new VMStackElement(iins.getType(cp), DONT_CARE, this);

            st.push(vme);			
        } else
        if (ins instanceof BranchInstruction) {
            setStackArgs2(st, ins.consumeStack(cp));
            st.pop2(ins.consumeStack(cp));
        } else
        if (ins instanceof PUTFIELD) {
            setStackArgs(st, 2);
            st.pop();
            String defUse = st.top().defuse;

            st.pop();
            defFieldAdd(defUse + "." + ((PUTFIELD) ins).getFieldName(cp));
        } else
        if (ins instanceof PUTSTATIC) {
            setStackArgs(st, 1);
            st.pop(1);
            defFieldAdd(STR_STATIC + ((PUTSTATIC) ins).getClassName(cp) + "." + ((PUTSTATIC) ins).getFieldName(cp));
        } else
        if (ins instanceof ConversionInstruction) {
            setStackArgs(st, 1);
            ConversionInstruction iins = (ConversionInstruction) ins;
            VMStackElement vme = 
                    new VMStackElement(iins.getType(cp), DONT_CARE, this);

            st.pop();
            st.push(vme);			
        } else
        if (ins instanceof NEW) {
            TypedInstruction iins = (TypedInstruction) ins;
            VMStackElement vme = 
                    new VMStackElement(iins.getType(cp), DONT_CARE, this);

            st.push(vme);			
        } else
        if (ins instanceof MULTIANEWARRAY) {
            setStackArgs2(st, ins.consumeStack(cp));
            MULTIANEWARRAY iins = (MULTIANEWARRAY) ins;
            VMStackElement vme = 
                    new VMStackElement(iins.getType(cp), DONT_CARE, this);

            st.pop2(ins.consumeStack(cp));
            st.push(vme);			
        } else
        if (ins instanceof ANEWARRAY) {
            ANEWARRAY iins = (ANEWARRAY) ins; 
            VMStackElement vme = 
                    new VMStackElement(new ArrayType(iins.getType(cp), 1),
                    DONT_CARE, this);

            st.pop();
            st.push(vme);			
        } else
        if (ins instanceof NEWARRAY) {
            setStackArgs(st, 1);
            NEWARRAY iins = (NEWARRAY) ins;
            VMStackElement vme = 
                    new VMStackElement(iins.getType(), DONT_CARE, this);

            st.pop();
            st.push(vme);			
        } else
        if (ins instanceof LDC || ins instanceof LDC2_W) {
            TypedInstruction iins = (TypedInstruction) ins;
            VMStackElement vme = 
                    new VMStackElement(iins.getType(cp), DONT_CARE, this);

            st.push(vme);			
        } else
        if (ins instanceof DCMPG || ins instanceof DCMPL || ins instanceof FCMPG
                || ins instanceof FCMPL || ins instanceof LCMP) {
            setStackArgs(st, 2);
            VMStackElement vme = 
                    new VMStackElement(Type.INT, DONT_CARE, this);

            st.pop(2);
            st.push(vme);
        } else
        if (ins instanceof MONITORENTER || ins instanceof MONITOREXIT
                || ins instanceof POP || ins instanceof POP2) {
            setStackArgs(st, 1);
            st.pop();
        } else
        if (ins instanceof RETURN) {
            st.reset();
        } else
        if (ins instanceof ReturnInstruction) {
            setStackArgs(st, 1);
            st.reset();
        } else
        if (ins instanceof SWAP) {
            setStackArgs(st, 2);
            VMStackElement vme = new VMStackElement(st.top());

            st.pop();
            VMStackElement vme1 = new VMStackElement(st.top());

            st.pop();
            st.push(vme);			
            st.push(vme1);
        } else
        if (ins instanceof DUP) {
            setStackArgs(st, 1);
            VMStackElement vme = new VMStackElement(st.top());

            vme.defuse = DONT_CARE;
            st.push(vme);			
        } else
        if (ins instanceof DUP2) {
            setStackArgs2(st, 2);
            VMStackElement[] vme = new VMStackElement[2];
            int k = 0, sum = 0;

            while (sum < 2) {
                vme[k] = st.top();
                sum += vme[k++].type.getSize();
                st.pop();
            }
            for (int i = k - 1; i >= 0; i--) {
                VMStackElement vme1 = new VMStackElement(vme[i]);

                vme1.defuse = DONT_CARE;
                st.push(vme1);
            }
            for (int i = k - 1; i >= 0; i--) {
                st.push(vme[i]);
            }
        } else
        if (ins instanceof DUP_X1) {
            setStackArgs(st, 2);
            VMStackElement vme = st.top();

            st.pop();
            VMStackElement vme1 = new VMStackElement(vme);

            vme1.defuse = DONT_CARE;
            VMStackElement vme2 = st.top();

            st.pop();
            st.push(vme1);
            st.push(vme2);
            st.push(vme);			
        } else
        if (ins instanceof DUP2_X1) {
            setStackArgs2(st, 3);
            VMStackElement[] vme = new VMStackElement[2];
            int k = 0, sum = 0;

            while (sum < 2) {
                vme[k] = st.top();
                sum += vme[k++].type.getSize();
                st.pop();
            }
            VMStackElement vme1 = st.top();

            st.pop();
            for (int i = k - 1; i >= 0; i--) {
                VMStackElement vme2 = new VMStackElement(vme[i]);

                vme2.defuse = DONT_CARE;
                st.push(vme2);
            }
            st.push(vme1);
            for (int i = k - 1; i >= 0; i--) {
                st.push(vme[i]);
            }
        } else
        if (ins instanceof DUP_X2) {
            setStackArgs2(st, 3);
            VMStackElement[] vme = new VMStackElement[3];
            int k = 0, sum = 0;

            while (sum < 3) {
                vme[k] = st.top();

                sum += vme[k++].type.getSize();
                st.pop();
            }
            VMStackElement vme1 = new VMStackElement(vme[0]);

            vme1.defuse = DONT_CARE;
            st.push(vme1);
            for (int i = k - 1; i >= 0; i--) {
                st.push(vme[i]);
            }
        } else
        if (ins instanceof DUP2_X2) {
            setStackArgs2(st, 4);
            VMStackElement[] vme = new VMStackElement[2];
            int k = 0, j = 0, sum = 0;

            while (sum < 2) {
                vme[k] = st.top();
                sum += vme[k++].type.getSize();
                st.pop();
            }
            VMStackElement[] vme1 = new VMStackElement[2];

            while (sum < 4) {
                vme1[j] = st.top();
                sum += vme1[j++].type.getSize();
                st.pop();
            }
            for (int i = k - 1; i >= 0; i--) {
                VMStackElement vme2 = new VMStackElement(vme[i]);

                vme2.defuse = DONT_CARE;
                st.push(vme2);
            }
            for (int i = j - 1; i >= 0; i--) {
                st.push(vme1[i]);
            }
            for (int i = k - 1; i >= 0; i--) {
                st.push(vme[i]);
            }
        } else
        if (ins instanceof LoadInstruction) {
            LoadInstruction iins = (LoadInstruction) ins;
            int index = iins.getIndex();

            setLocalArg(locals, index);
            useLocalAdd(STR_LOCAL + index);
            if (locals.get(index) == null) {
                throw new InvalidStackArgument("Trying to load " + "uninitialized element");
            }
            VMStackElement vme = new VMStackElement(locals.get(index), STR_LOCAL + index, this);

            st.push(vme);
        } else
        if (ins instanceof StoreInstruction) {
            setStackArgs(st, 1);
            StoreInstruction iins = (StoreInstruction) ins;
            int index = iins.getIndex();

            defLocalAdd(STR_LOCAL + index);
            VMStackElement vme = st.top();

            st.pop();
            locals.add(vme.type, index);
        } else
        if (ins instanceof InvokeInstruction) {
        	// verifica se eh uma chamada de super.
        	// para isso verifica se o methodo chamado eh <init>
        	// e se o objeto sendo usado eh L@0
        	if ( ins instanceof INVOKESPECIAL)
        	{
        		INVOKESPECIAL iesp = (INVOKESPECIAL) ins;
        		if ( iesp.getName(cp).equals("<init>") )
        		{
        			Type[] t = iesp.getArgumentTypes(cp);
        			VMStackElement el = st.top(t.length);

        			if ( el.defuse.equals(STR_LOCAL + "0") )
        			{
        				isSuper = true;
        			}		
        		}
        	}
        	
            setStackArgs2(st, ins.consumeStack(cp));
            InvokeInstruction iins = (InvokeInstruction) ins;
            VMStackElement vme = 
                    new VMStackElement(iins.getReturnType(cp), DONT_CARE, this);

            st.pop2(ins.consumeStack(cp));
            st.push(vme);			
        } else
        if (ins instanceof GETSTATIC) {
            FieldInstruction iins = (FieldInstruction) ins;
            String defUse = STR_STATIC + iins.getClassName(cp) + "."
                    + iins.getFieldName(cp);
            VMStackElement vme = 
                    new VMStackElement(iins.getFieldType(cp), defUse, this);

            useFieldAdd(defUse);
            st.push(vme);			
        } else
        if (ins instanceof GETFIELD) {
            setStackArgs(st, 1);
            FieldInstruction iins = (FieldInstruction) ins;
            String defUse = st.top().defuse + "." + iins.getFieldName(cp);
            VMStackElement vme = 
                    new VMStackElement(iins.getFieldType(cp), defUse, this);

            useFieldAdd(defUse);
            st.pop(1);
            st.push(vme);			
        } else
        if (ins instanceof BREAKPOINT || ins instanceof IMPDEP1
                || ins instanceof IMPDEP2 || ins instanceof NOP) {;
        } else
        if (ins instanceof RET) {
            IndexedInstruction iins = (IndexedInstruction) ins;
            int index = iins.getIndex();

            setLocalArg(locals, index);
        } else
        if (ins instanceof CHECKCAST) {
            setStackArgs(st, 1);
        } else
        if (ins instanceof IINC) {
            String defUse = STR_LOCAL + ((IINC) ins).getIndex();

            useLocalAdd(defUse);
            defLocalAdd(defUse);
        } else {
            throw new InvalidInstructionException(ins.toString());
        }
    }
	
    /**
     Sets the variables {@link InstructionNode#argStackFrom} and 
     {@link InstructionNode#argStackTo} that indicates which spots in the 
     stack are used as arguments for this instrction 

     */
    private void setStackArgs(VMStack st, int k) {
        int l = st.size();

        if (l <= 0) {
            return;
        }
        if (argStackTo < 0) {
            argStackTo = l - 1;
        } else {
            if (argStackTo != l - 1) {
                Debug.D("Stack args changed (high): " + argStackFrom + " " + (l - 1));
                argStackTo = l - 1;
            }
        }
		
        for (int i = 0; i < k; i++) {
            VMStackElement v = st.top(i);

            l -= v.type.getSize();
        }
		
        if (argStackFrom < 0) {
            argStackFrom = l;
        } else {
            if (argStackFrom < l) {
                Debug.D("Stack args changed (low): " + argStackFrom + " " + l);
                argStackFrom = l;
            }
        }
    }
	
    /**
     Sets the variables {@link InstructionNode#argStackFrom} and 
     {@link InstructionNode#argStackTo} that indicates which spots in the 
     stack are used as arguments for this instrction 

     */
    private void setStackArgs2(VMStack st, int k) {
        int l = st.size();

        if (l <= 0) {
            return;
        }
        if (argStackTo < 0) {
            argStackTo = l - 1;
        } else {
            if (argStackTo != l - 1) {
                Debug.D("Stack args changed (high): " + argStackFrom + " " + (l - 1));
                argStackTo = l - 1;
            }
        }
		
        l -= k;
        if (argStackFrom < 0) {
            argStackFrom = l;
        } else {
            if (argStackFrom < l) {
                Debug.D("Stack args changed (low): " + argStackFrom + " " + l);
                argStackFrom = l;
            }
        }
    }

    /**
     Sets the variable {@link InstructionNode#argLocal} 
     that indicates which local variable 
     is used as arguments for this instrction 

     */
    private void setLocalArg(VMLocal st, int k) {
        if (argLocal < 0) {
            argLocal = k;
        } else
        if (argLocal != k) {
            Debug.D("Local args changed: " + argLocal + " " + k);
            argLocal = k;
        }
    }

    /**
     <p>Returns a string with several informations about this instruction
     like </p>

     <p>
     <UL>
     <LI> The instruction
     <LI> The set of "required locals"
     <LI> The number of stacks configurations computed
     <LI> The number of local variable configurations
     <LI> The variable {@link InstructionNode#argStackFrom}, 
     {@link InstructionNode#argStackTo} and
     {@link InstructionNode#argLocal}
     <LI> The definitions and uses
     <LI> etc
     </UL>
     </p>
     */
    public String toString() {
        String str = hashCode() + " " + ih;

        str += "\nRequired locals: " + getReqLocal();
        str += "\nStack lengths: " + allStack.size();
        str += "\nLocal lengths: " + allLocals.size();
        str += "\nStack args: " + argStackFrom + " to " + argStackTo;
        str += "\nLocal arg: " + argLocal;
        str += "\nArray defs: ";
        for (Iterator in = arrayDef.iterator(); in.hasNext();) {
            str += in.next() + " ";
        }
        str += "\nArray uses: ";
        for (Iterator in = arrayUse.iterator(); in.hasNext();) {
            str += in.next() + " ";
        }
        str += "\nField defs: ";
        for (Iterator in = fieldDef.iterator(); in.hasNext();) {
            str += in.next() + " ";
        }
        str += "\nField uses: ";
        for (Iterator in = fieldUse.iterator(); in.hasNext();) {
            str += in.next() + " ";
        }
        str += "\nLocal defs: ";
        for (Iterator in = localDef.iterator(); in.hasNext();) {
            str += in.next() + " ";
        }
        str += "\nLocal uses: ";
        for (Iterator in = localUse.iterator(); in.hasNext();) {
            str += in.next() + " ";
        }
        str += "\nSuccessors:\n";
        Vector nx = getPrimNext();

        if (nx.size() == 0) {
            str += "\tNONE\n";
        }
        for (int i = 0; i < nx.size(); i++) {
            InstructionNode suc = (InstructionNode) nx.elementAt(i);

            str += "\t" + suc.hashCode() + " " + suc.ih + "\n";
        }
        str += "Previous:\n";
        nx = getPrimArriving();
        if (nx.size() == 0) {
            str += "\tNONE\n";
        }
        for (int i = 0; i < nx.size(); i++) {
            InstructionNode suc = (InstructionNode) nx.elementAt(i);

            str += "\t" + suc.hashCode() + " " + suc.ih + "\n";
        }
        if (err != null) {
            str += "Invalid instruction:\n\t" + err.getMessage() + "\n";
        }
        if (isUnreacheable()) {
            return str += " is Unreacheable";
        }
        str += "STACK:\n" + theStack + // "\nNEXT:\n" + nextStack +
                "\nLOCALS:\n";
        for (int i = 0; i < localVars.size(); i++) {
            if (localVars.get(i) == null) {
                str += "\t<empty>\n";
            } else {		
                str += "\t" + localVars.get(i).getSignature() + "\n";
            }
        }
        return str;
    }
		
    /**
     <p>The method required by the {@link Comparator} interface. It returns</p>
     <p>
     <UL>
     <LI> < 0 if the position in the code of the first objet is lower than of
     the second
     <LI> == 0 if the position in the code of the first objet is equal to of
     the second
     <LI> > 0 if the position in the code of the first objet is higher than of
     the second
     </UL>
     </p>

     @param x1 The first {@link InstructionNode} object to be compared
     @param x2 The second {@link InstructionNode} object to be compared
     */
    public int compare(Object x1, Object x2) {
        InstructionNode y1 = (InstructionNode) x1,
                y2 = (InstructionNode) x2;

        return y1.ih.getPosition() - y2.ih.getPosition();
    }

    // sets of defs and uses

    /** This is the set of field uses for this instruction */	
    public HashSet  fieldUse = new HashSet(),

            /** This is the set of field definitions for this instruction */	
            fieldDef = new HashSet(),

            /** This is the set of array/object uses for this instruction */	
            arrayUse = new HashSet(),

            /** This is the set of array/object definitions for this instruction */	
            arrayDef = new HashSet(),

            /** This is the set of local uses for this instruction */	
            localUse = new HashSet(),

            /** This is the set of local definitions for this instruction */	
            localDef = new HashSet();
	
    /** Adds a definition to the field definition set <BR>
     A field is defined by instructions that directly changes a
     field  They are </p>
     <p>
     <UL>
     <LI> PUTSTATIC
     <LI> PUTFIELD
     </UL>
     </p>
     */
    private void defFieldAdd(String x) {
        fieldDef.add(x);
    }
	
    /** <p>Adds a use to the field use set <BR>
     A field is used by instructions that directly changes a
     field. They are:<BR></p>

     <p>
     <UL>
     <LI> GETSTATIC
     <LI> GETFIELD
     </UL>
     </p>

     */
    private void useFieldAdd(String x) {
        fieldUse.add(x);
    }
	
    /** <p>Adds a definition to the array definition set <BR>
     The instructions are the array store instructions: </p>

     <p>
     <UL>
     <LI> AASTORE, 
     <LI> BASTORE, 
     <LI> CASTORE, 
     <LI> DASTORE, 
     <LI> FASTORE, 
     <LI> IASTORE, 
     <LI> LASTORE, 
     <LI> SASTORE 
     </UL>
     </p>
     */
    private void defArrayAdd(String x) {
        arrayDef.add(x);
    }
	
    /** Adds a use to the array/object use set <BR>
     The instructions are the array load instructions: </p>

     <p>
     <UL>
     <LI> AALOAD,
     <LI> BALOAD, 
     <LI> CALOAD, 
     <LI> DALOAD,
     <LI> FALOAD, 
     <LI> IALOAD,
     <LI> LALOAD, 
     <LI> SALOAD
     </UL>
     </p>
     */
    private void useArrayAdd(String x) {
        arrayUse.add(x);
    }

    /**
     <p>Adds a definition to the local definition set 
     A local is defined by instructions that directly changes a
     local variable. They are </p>
     <p>
     <UL>
     <LI> IINC,
     <LI> ASTORE, 
     <LI> DSTORE, 
     <LI> FSTORE, 
     <LI> ISTORE, 
     <LI> LSTORE
     </UL>
     </p>
     */
    void defLocalAdd(String x) {
        localDef.add(x);
    }
	
    /** <p>Adds a use to the local use set 
     The instructions are </p>
     <p>
     <UL>
     <LI> ALOAD, 
     <LI> DLOAD, 
     <LI> FLOAD, 
     <LI> ILOAD, 
     <LI> LLOAD 
     </UL>
     </p>*/
    private void useLocalAdd(String x) {
        localUse.add(x);
    }
	
    /** <p> Gets all the types in a given position of the
     * satcks stored for this instruction. For example, calling
     * this method with argument 3 will return a list of types
     * that appear in position 3 of the stack in each of the
     * elements of variable {@link InstructionNode#allStack}.
     * The array may have repetitions.
     */
    public Type[] getStackAt(int k) {
        Type[] vt = new Type[allStack.size()];

        Debug.D(allStack.size() + " " + k + "\n" + this);
        for (int i = 0; i < allStack.size(); i++) {
            VMStack vms = (VMStack) allStack.elementAt(i);

            Debug.D(vms);
            Debug.D(vms.get(k));
            vt[i] = vms.get(k).type;
        }
        return vt;
    }

    public void setDomEntry(InstructionNode x) {
        domEntry = x;
    }
	
    public InstructionNode getDomEntry() {
        return domEntry;
    }
}
