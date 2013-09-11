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


import org.aspectj.apache.bcel.generic.ConstantPoolGen;

import br.jabuti.verifier.InstructionNode;


public class CFGSuperNode extends CFGCallNode {

	/**
	 * Added to jdk1.5.0_04 compiler
	 */
	private static final long serialVersionUID = 9190824248421013922L;

	public CFGSuperNode(CFGNode x, InstructionNode ins, ConstantPoolGen cp) {
        super(x, ins, cp);
    }
	
}		

