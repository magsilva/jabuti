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


import java.util.Vector;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;

import br.jabuti.verifier.InstructionNode;


public class CFGCallNode extends CFGNode {

	
	/**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = -600189833884301571L;

	static public int NOTHING_SPECIAL = 0,
					  ASPECT_METHOD = 1;
	static private Vector aspectVector = new Vector();
	
	static {
		aspectVector.add("ajc$afterReturning$"); 
		aspectVector.add("ajc$after$"); 
		aspectVector.add("ajc$before$"); 
		aspectVector.add("ajc$around$"); 
	}

    /** The class where the called method is */
    private String classe[],
            
            /** The method name */
            name,
            
            /** The parameter types */
            param[];	
            
    /** indicates whether the method is something special, like aspects */
    private int specialMethod;
	
    public CFGCallNode(CFGNode x, InstructionNode ins, ConstantPool cp) {
        super(x);
        InvokeInstruction y = (InvokeInstruction) ins.ih.getInstruction();
        ConstantPoolGen cpg = new ConstantPoolGen(cp);
        name = y.getMethodName(cpg);
        Type[] vt = y.getArgumentTypes(cpg);

        param = new String[vt.length];
        for (int i = 0; i < vt.length; i++) {
            param[i] = vt[i].getSignature();
        }
		
        if (y instanceof InvokeInstruction) {
            classe = new String[1];
            classe[0] = y.getClassName(cpg);
        } else {
            Vector vtype = new Vector();

            vt = ins.getStackAt(ins.argStackFrom);
            for (int i = 0; i < vt.length; i++) {
                String s = vt[i].getSignature();

                if (!vtype.contains(s)) {
                    vtype.add(s);
                }
            }
            classe = (String[]) vtype.toArray(new String[0]);
        }
        
        specialMethod = NOTHING_SPECIAL;
        for (int i = 0; i < aspectVector.size(); i++)
        {
        	String s = (String) aspectVector.elementAt(i);
			if ( name.startsWith(s) )
			{
				specialMethod = ASPECT_METHOD;
				break;
			}
       }
     }
	
	public String[] getClasse() {
		return classe;
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getParam() {
		return param;
	}
	
	public int getSpecialMethod() {
		return specialMethod;
	}
	
    public String toString() {
        String str = super.toString();

        for (int h = 0; h < classe.length; h++) {
            str += "Call to " + classe[h] + "." + name + "(";
            int i;

            for (i = 0; i < param.length - 1; i++) {
                str += param[i] + ", ";
            }
            if (i < param.length) {
                str += param[i];
            }
            str += ")\n";
        }
        return str;
    }
}		

