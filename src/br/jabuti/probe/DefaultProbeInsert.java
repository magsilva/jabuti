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


package br.jabuti.probe;


import br.jabuti.graph.*;
import br.jabuti.util.*;
import br.jabuti.verifier.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import java.util.*;

import br.jabuti.instrumenter.*;
import br.jabuti.lookup.*;

 
/** This class is designed to insert probes on each 
 * node of certain classes in a given program
 */ 
public class DefaultProbeInsert {
	static public final String 
	     JABUTI_DEFAULT_INSTR_ATTRIBUTE = "JaBUTi Default Instrumented";

    /** The program structure */
    Program prog;
    
    /** The list of classes to be instrumented */
    Collection instrumentList;
	
    /** The constructor.
     * @param p - The {@link br.jabuti.lookup.Program structure} that represents
     * the program to be instrumented
     * @param c - The list of classes to be instrumented. Each element
     * is a string with the complete name of the class
     */
    public DefaultProbeInsert(Program p, Collection c) {
        prog = p;
        instrumentList = c;
    }
	
    public String getProbeClass() {
        return "br.jabuti.probe.DefaultProber";
    }
	
    public Map instrument(int typeOfCFG)
            throws InvalidInstructionException,
            InvalidStackArgument {
        String[] userClass = null;
        HashSet inst = new HashSet();

        if (instrumentList != null) {
            inst.addAll(instrumentList);
        }
        
        // pega o nome de todas as classes instrumentaveis do programa
        userClass = prog.getCodeClasses();
        Hashtable hs = new Hashtable();

		
		// para cada classe:
        for (int i = 0; i < userClass.length; i++) {
        	
           	RClassCode r = (RClassCode) prog.get(userClass[i]);
        	JavaClass jv = r.getTheClass();
        	
        	// verifica se esta classe jah foi instrumentada por
        	// esse instrumentador
	        Attribute[] atr = jv.getAttributes();
	        boolean achou = false;
	        for (int j = 0;  j < atr.length && ! achou; j++)
	        {
	        	if ( atr[j] instanceof Unknown )
	        	{
	        		Unknown uatr = (Unknown) atr[j];
	        		byte[] b = uatr.getBytes();
	        		achou = Arrays.equals(b, 
	        		              JABUTI_DEFAULT_INSTR_ATTRIBUTE.getBytes());
	        	}
	        }
        	
        	// se estah em inst eh pra instrumentar a classe
            if ( (!achou) && inst.contains(userClass[i]) && ! jv.isInterface()) 
            {
            	// chama a rotina que faz a instrumentacao
		        
                try {
					jv = doDefaultInstrument(jv, userClass[i], typeOfCFG);
	        		hs.put(r.getName(), jv);
				} 
                catch (Exception e) { //Se der exce��o, coloca bytecode nao instrumentado em hs
                    hs.put(userClass[i], jv);
				}                
                inst.remove(userClass[i]);
            } else { // coloca o codigo sem instrumentar
                hs.put(userClass[i], jv);
            }
        }
        return hs;
    }
	
    private JavaClass doDefaultInstrument(JavaClass java_class, 
    									  String className,
    									  int typeOfCFG)
            throws Exception {

        ClassGen cg = new ClassGen(java_class);				 
        ConstantPoolGen cp = cg.getConstantPool();
  		
        Method[] methods = cg.getMethods();

//		System.out.println( "Instrumenting class: " + className + 
//		                    " cfg option: " + typeOfCFG );

        for (int i = 0; i < methods.length; i++) {
            try {
//				System.out.println( "\tCurrent method: " + methods[i].getGenericSignature() + " - "
//						+ methods[i].getSignature());                        
                MethodGen mg = new MethodGen(methods[i], 
                        cg.getClassName(),
                        cp);
			
                        
                // does not instrument static initializations or abstract methods
                if ( (methods[i].getName().equals("<clinit>")) || 
                     ( methods[i].isAbstract() ) ) {
                    continue;
                }
                        
                InstructionList il = mg.getInstructionList();
                InstructionHandle[] ihVec = il.getInstructionHandles();
                int[] ihOffset = il.getInstructionPositions();

                ASMInstrumenter gi = new ASMInstrumenter(mg, cg, cp);

				int nextLocal = mg.getMaxLocals() + 1;
                CFG gfc = new CFG(mg, cg, typeOfCFG);

//                gfc.releaseInstructionGraph(); // free some memory
			
                HashSet jahFoi = new HashSet(); // controla quas nos jah foram instrumentados
			
                // insert probes at the end of each node in a constructor
                if (methods[i].getName().equals("<init>")) {
                    CFGNode superNode = null;

                    // acha a chamada ao super
                    for (int m = 0; m < gfc.size(); m++) {
                        CFGNode ey = (CFGNode) gfc.elementAt(m);

                        if (ey instanceof CFGSuperNode) 
                        {
                                superNode = ey;
                                break;
                        }
                    }
  				
                    // acha os nos que veem antes da chamada ao super
                    //System.out.println( "Instrumentando classe: " + cg.getClassName() );
                    //System.out.println( gfc );
                    gfc.findIDFT(false, superNode);
					InstructionHandle ih = 
        				InstructionList.findHandle(ihVec, ihOffset, 
        				                           ihOffset.length,
        			       							superNode.getEnd());

                    gi.insertAfter(ih, 
                            "aload_0 " + // empilha o objeto
                            " ldc \"" + className + "\"" + // empilha o nome da classe
                            " ldc " + i + // empilha o numero do metodo
                            " lconst_0 " + // empilha aninhamento que para construtor
                            				// eh sempre 0
                            " ldc " + // empilha o numero do no
                            "\"" + superNode.getNumber() + "\" " + 
                            "invokestatic " + getProbeClass() + 
                            " probe \"(Ljava/lang/Object;" + 
                            "Ljava/lang/String;IJLjava/lang/Object;)V\"");
 				
                    jahFoi.add(ih);
				
                    // coloca a instrumentacao em cada no
                    for (int m = 0; m < gfc.size(); m++) {
                        CFGNode ey = (CFGNode) gfc.elementAt(m);

                        // if marked means it is befor the super
                        if (ey.getMark()) {
                            continue;
                        }

        				ih = InstructionList.findHandle(ihVec, ihOffset, 
        				                           ihOffset.length,
        			       							ey.getStart());
//                        System.out.println("No: " + ey);
//                        System.out.println("Inicio do noh: " + ih);
//                        System.out.println("Inicio do noh: " + ey.getStart());
                        if (!jahFoi.contains(ih)) {
                            jahFoi.add(ih);
                            gi.insertBefore(ih, 
                                    "aload_0 " + // empilha o objeto
                                    " ldc \"" + className + "\"" + // empilha o nome da classe
                                    " ldc " + i + // empilha o numero do metodo
		                            " lconst_0 " + // empilha aninhamento que para construtor
                            				// eh sempre 0
                                    " ldc " + // empilha o numero do no
                                    "\"" + ey.getNumber() + "\" " + 
                                    "invokestatic " + getProbeClass() + 
                                    	" probe \"(Ljava/lang/Object;" + 
                                    	"Ljava/lang/String;IJLjava/lang/Object;)V\"");
                        }
                    }
                } else {	// in this case the method is an ordinary method
                    String probStat = null, method = null;

                    if (mg.isStatic()) {
                        probStat = "";
                        method = "";
                    } else {
                        probStat = "aload_0";
                        method = "Ljava/lang/Object;";
                    }
                    for (int m = 0; m < gfc.size(); m++) {
                        CFGNode gn = (CFGNode) gfc.elementAt(m);
		                        
						InstructionHandle ih = 
	        				InstructionList.findHandle(ihVec, ihOffset, 
        				                           ihOffset.length,
        			       							gn.getStart());

                        if (gfc.isEntry(gn) )
                        {
                        	String s = "invokestatic " + getProbeClass() 
                                + " getNest \"()J\"" 
                            + " lstore " + nextLocal;
                            gi.addBefore(ih, s);
                        }

                        

                        String s = probStat + " ldc \"" + className + "\"" + // empilha o nome da classe
                                " ldc " + i + // empilha o numero do metodo
                                " lload " + nextLocal + // empilha nivel de aninhamento
                                " ldc " + // empilha o numero do no
                                "\"" + gn.getNumber() + "\" "
                                + "invokestatic " + getProbeClass()
                                + " probe \"(" + method
                                + "Ljava/lang/String;IJLjava/lang/Object;)V\"";

                        if (!jahFoi.contains(ih)) {
                            jahFoi.add(ih);
                            gi.insertBefore(ih, s);
                        }
                    }
                }
                int stackSize = mg.getMaxStack();
                // Tentativa de contornar os erros do BCEL que n�o altera corretamente o tamanho da pilha
                if ( stackSize < 6 ) {
                	mg.setMaxStack(stackSize + 6);
                }
                // Remove LVTT do c�digo. Tenta evitar geracao errada do BCEL 5.2
                ConstantPoolGen p = mg.getConstantPool();
                for (Attribute atr : mg.getCodeAttributes()) {
                    int k = atr.getNameIndex();
                    Constant c = p.getConstant(k);
                    String s = ((ConstantUtf8) c).getBytes();

                	if ( s.equals ("LocalVariableTypeTable") ) 
                	{
                      mg.removeCodeAttribute(atr);
                    }
                }

                methods[i] = mg.getMethod();
            } catch (Exception e) { 
            	System.out.println(className);
                System.err.println("\tParser error " + e.getMessage());
                throw e;
            }
        }
        int newIndex = cp.addUtf8(JABUTI_DEFAULT_INSTR_ATTRIBUTE);
        Attribute atr = new Unknown(newIndex, 
        	JABUTI_DEFAULT_INSTR_ATTRIBUTE.length(),
        	JABUTI_DEFAULT_INSTR_ATTRIBUTE.getBytes(),
        	cp.getConstantPool() );
        cg.setConstantPool(cp);
        cg.addAttribute(atr);
        cg.setMethods(methods);
        return (cg.getJavaClass());
    }
	
	
	/** This method wraps a given method code between two pieces of code:
	* one to be executed before the method and other after, as a finaly clause.
	* The instrumentation will use the next 2 free local variable, so the code
	* to be inserted should not use them.
	* @param jv - The JavaClass where to find the method
	* @param name - the method name
	* @param sig - method signature
	* @param cfg - the control flow graph of the method
	* @param before - the code to be inserted before the method code
	* @param after - the code to be inserted after the method
	*/
	static public JavaClass wrapMethod(JavaClass jv, 
							String name,
							String sig,
//							CFG cfg, 
							String before, 
							String after)
	{
        ClassGen cg = new ClassGen(jv);				 
        ConstantPoolGen cp = cg.getConstantPool();
  		
        Method[] methods = cg.getMethods();
        int i;
        for (i = 0; i < methods.length; i++)
        {
        	String n = methods[i].getName();
        	String s = methods[i].getSignature();
        	if ( s.equals(sig) && n.equals(name) )
        		break;
        }
        if ( i >= methods.length )
        	return jv;
	    MethodGen mg = new MethodGen(methods[i], 
                    cg.getClassName(),
                    cp);
        try
        {
	        int nextLocal = mg.getMaxLocals();
	        ASMInstrumenter gi = new ASMInstrumenter(mg, cg, cp);
	        
	        InstructionList iList = mg.getInstructionList();
	        
			// pega instrucao inicial
	        InstructionHandle first = iList.getStart();
//	        System.out.println("First: " + first);
//	        System.out.println(iList);
	        gi.insertBefore(first, " NOP " + (before == null ? "" : before) );
//	        System.out.println(iList);
	        
	        
//	        iList = mg.getInstructionList();
	        InstructionHandle preambulo = iList.getStart();
//	        System.out.println("Preambulo: " + preambulo);
	        
	        // ultima instrucao do metodo
	        InstructionHandle last = iList.getEnd();
//	        System.out.println("Last: " + last);
	
			// comeca a inserir o tratador de instrucoes
	        gi.insertAfter(last, " astore " + (nextLocal + 1) );
//	        iList = mg.getInstructionList();
	        InstructionHandle catcher = iList.getEnd();
	        gi.insertAfter(catcher, " jsr l1 aload " + (nextLocal+1) + 
	        						" athrow " + " l1: NOP ");
	
//	        System.out.println("catcher: " + catcher);
	
			// comeca a inserir o codigo do finally
//	        iList = mg.getInstructionList();        
	        InstructionHandle subroutine = iList.getEnd();
//	        System.out.println("subroutine: " + subroutine);
	        
	        String finali = " astore " + nextLocal;
	        finali += after;
	        finali += " ret " + nextLocal;
	        gi.insertAfter(subroutine, finali);
	        
//	        iList = mg.getInstructionList();
			
			InstructionHandle nx = first, curr;
			mg.getMethod();
			
			// para cada return no codigo faz uma chamada aa subrotina
			do 
			{
				curr = nx;
				BranchInstruction jsr = new JSR(subroutine);
				Instruction ins = curr.getInstruction();
				if ( ins instanceof ReturnInstruction )
				{
					gi.insertBefore(curr, jsr);
				} 
				nx = curr.getNext();
			} while ( curr != last );
			
			// and finally, includes the exception handler for the entire original source code
	        iList = mg.getInstructionList();
	        mg.addExceptionHandler(preambulo, last, catcher, (ObjectType) null);
        } 
        catch (ParseException e) 
        { 
                System.err.println("Parser error " + e.getMessage());
        }
 
 		mg.setMaxStack();
 		mg.setMaxLocals();       
        methods[i] = mg.getMethod();
        cg.setMethods(methods);
        return (cg.getJavaClass());
	}
}

