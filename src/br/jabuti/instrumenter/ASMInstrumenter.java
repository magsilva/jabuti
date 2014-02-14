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


import java.io.ByteArrayInputStream;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;


/** This class allows one to insert code in a JVM program
 * (a method). The code to be inserted is specified with 
 * JVM instructions, defined as a string. It extends the class
 * {@link Instrumenter} that is a "raw" instrumenter where the
 * instructions to be inserted are created "by hand", i.e., 
 * hardcoded.
 * 
 * @version: 0.00001
 * @author: Marcio Delamaro
 * 
 * @see Instrumenter
 */
public class ASMInstrumenter extends Instrumenter {

    /** The class that is being instrumented */
    ClassGen classGen;

    /** Creates the instrumenter, specifying which method will
     * be changed.
     * 
     * @param mg The method that will be instrumented
     * 
     */
    public ASMInstrumenter(MethodGen mg, ClassGen cg, ConstantPoolGen cp) {
        super(mg, cp);
        classGen = cg;
    }
	
    /** Inserts a peace of code before a given instruction
     * in the specified method.
     * 
     * @param ih The instruction before which the code will be inserted
     * @param x A sequence of JVM instructions to be inserted
     * 
     * @throws ParseException If the string <code>x</code> is not a valid
     * sequence of instructions. To the complete definition of what is a 
     * valid sequence see <a href="asm.txt"> the Javacc grammar</a>
     * 
     */ 	
    public void insertBefore(InstructionHandle ih, String x) throws ParseException {
        // try once with fake method and class
        // if no exception, do it again
        ClassGen cg2 = new ClassGen("DUMMY", "java/lang/Object", "DUMMY.class",
                0, null);
        MethodGen m2 = meth.copy(meth.getClassName(), new ConstantPoolGen());
        ASMParse asmp = new ASMParse(new ByteArrayInputStream(x.getBytes()), m2, cg2);
        InstructionList inedir = asmp.ASMProg();
		
        // do a second time with the real method and class
        asmp = new ASMParse(new ByteArrayInputStream(x.getBytes()), meth, classGen);
        inedir = asmp.ASMProg();
        // System.out.println(meth.getConstantPool());
        // System.out.println(meth.getConstantPool().getFinalConstantPool());
        insertBefore(ih, inedir);
        meth.setMaxLocals();
        meth.setMaxStack();
    }
	
    /** Adds a peace of code before a given instruction
     * in the specified method.
     * 
     * @param ih The instruction before which the code will be inserted
     * @param x A sequence of JVM instructions to be added
     * 
     * @throws ParseException If the string <code>x</code> is not a valid
     * sequence of instructions. To the complete definition of what is a 
     * valid sequence see <a href="asm.txt"> the Javacc grammar</a>
     * 
     */ 	
    public void addBefore(InstructionHandle ih, String x) throws ParseException {
        // try once with fake method and class
        // if no exception, do it again
        ClassGen cg2 = new ClassGen("DUMMY", "java/lang/Object", "DUMMY.class",
                0, null);
        MethodGen m2 = meth.copy(meth.getClassName(), new ConstantPoolGen());
        ASMParse asmp = new ASMParse(new ByteArrayInputStream(x.getBytes()), m2, cg2);
        InstructionList inedir = asmp.ASMProg();
		
        // do a second time with the real method and class
        asmp = new ASMParse(new ByteArrayInputStream(x.getBytes()), meth, classGen);
        inedir = asmp.ASMProg();
        // System.out.println(meth.getConstantPool());
        // System.out.println(meth.getConstantPool().getFinalConstantPool());
        addBefore(ih, inedir);
        meth.setMaxLocals();
        meth.setMaxStack();
    }

    /** Inserts a peace of code after a given instruction
     * in the specified method.
     * 
     * @param ih The instruction before which the code will be inserted
     * @param x A sequence of JVM instructions to be inserted
     * 
     * @throws ParseException If the string <code>x</code> is not a valid
     * sequence of instructions. To the complete definition of what is a 
     * valid sequence see <a href="asm.txt"> the Javacc grammar</a>
     * 
     */ 	
    public void insertAfter(InstructionHandle ih, String x) throws ParseException {
        // try once with fake method and class
        // if no exception, do it again
        ClassGen cg2 = new ClassGen("DUMMY", "java/lang/Object", "DUMMY.class",
                0, null);
        MethodGen m2 = meth.copy(meth.getClassName(), new ConstantPoolGen());
        ASMParse asmp = new ASMParse(new ByteArrayInputStream(x.getBytes()), m2, cg2);
        InstructionList inedir = asmp.ASMProg();
		
        // do a second time with the real method and class
        asmp = new ASMParse(new ByteArrayInputStream(x.getBytes()), meth, classGen);
        inedir = asmp.ASMProg();
        // System.out.println(meth.getConstantPool());
        // System.out.println(meth.getConstantPool().getFinalConstantPool());
        insertAfter(ih, inedir);
        meth.setMaxLocals();
        meth.setMaxStack();
    }

    /**
     * This is a test driver. It takes a class name on args[0], inserts
     * some instructions in several points of each method in the class
     * and then dump the instrumented class to "new_"<original_name> 
     * file
     */

    public static void main(String args[]) throws Exception {
        // o melhor eh chamar com java ... ASMInstrumenter samples\arquivo.class
        // assim ele vai criar um novo arquivo new_samples\arquivo.class que
        // se pode testar
		
        JavaClass java_class;

        if ((java_class = Repository.lookupClass(args[0])) == null) {
            java_class = new ClassParser(args[0]).parse();
        } // May throw IOException

        ClassGen cg = new ClassGen(java_class);				 
        ConstantPoolGen cp = cg.getConstantPool();
        Method[] methods = cg.getMethods();

        for (int i = 0; i < methods.length; i++) {
            try {
                System.out.println("\n\n--------------------------");						 
                System.out.println(methods[i].getName());
                System.out.println("--------------------------");		
                MethodGen mg = new MethodGen(methods[i], 
                        cg.getClassName(),
                        cp);
                ASMInstrumenter gi = new ASMInstrumenter(mg, cg, cp);
                int nvars = mg.getMaxLocals() + 10;
  			
                String s = "GETSTATIC java.lang.System out \"Ljava/io/PrintStream;\"  "
                        + "astore " + nvars + " ";
                String s2 = "aload " + nvars + " "; 
                String s3 = "LDC \"Entrando no metodo " + mg.getName() + "\" ";
                String s4 = "LDC \"Saindo do metodo " + mg.getName() + "\\n \" ";
                String s5 = "invokevirtual java.io.PrintStream println "
                        + "\"(Ljava/lang/Object;)V\" ";

                gi.insertBefore(mg.getInstructionList().getStart(), s + s2 + s3 + s5);
                gi.insertBefore(mg.getInstructionList().getEnd(), s2 + s4 + s5);
                methods[i] = mg.getMethod();
            } catch (ParseException e) {
                System.err.println("Parser error " + e.getMessage());
            }
        }
        cg.setMethods(methods);
        java_class = cg.getJavaClass();
        java_class.dump("new_" + args[0]);
    }
}

