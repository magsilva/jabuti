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


package br.jabuti.device;


import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Unknown;

import br.jabuti.lookup.Program;
import br.jabuti.probe.DefaultProbeInsert;
import br.jabuti.verifier.InvalidInstructionException;
import br.jabuti.verifier.InvalidStackArgument;

 
/** This class is designed to insert probes on each 
 * node of certain classes in a given program
 */ 
public class DeviceProbeInsert extends DefaultProbeInsert {
	static public final String 
	     JABUTI_J2ME_INSTR_ATTRIBUTE = "JaBUTi J2ME Instrumented";

	private String baseClass;
	private String server; // nome do servidor destino
	private String fileName; // nome do arquivo
	private String id;
	private long thr;

	private boolean keepAlive;
	
    /** The constructor.
     * @param p - The {@link br.jabuti.lookup.Program structure} that represents
     * the program to be instrumented
     * @param c - The list of classes to be instrumented. Each element
     * is a string with the complete name of the class
     * @param b - name of the midlet class
     * @param s - address of the destination server
     * @param f - name of the temporary file to use
     * @param memTreshold 
     * @param id - identification of the MIDLET
     */
    public DeviceProbeInsert(Program p, Collection c, String b,
    						String f, String s, long memTreshold, 
							boolean kA, String id) {
    	super(p,c);
    	baseClass = b;
    	server = s;
    	fileName = f;
    	this.id = id;
    	thr = memTreshold;
    	keepAlive = kA;
    }
	
    public String getProbeClass() {
        return "br.jabuti.device.j2me.DeviceProber";
    }
	
    public Map instrument(int typeOfCFG) throws InvalidInstructionException,
    											InvalidStackArgument 
    {
		
		Map mp = super.instrument(typeOfCFG);
		
		Hashtable hs = new Hashtable();
		
		
		Iterator it = mp.keySet().iterator();		
		// para cada classe:
		while (it.hasNext()) 
		{
			String className = (String) it.next();
			JavaClass jv = (JavaClass) mp.get(className);
			        	
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
		    				JABUTI_J2ME_INSTR_ATTRIBUTE.getBytes());
		    	}
		    }
		    
			// se estah em inst eh pra instrumentar a classe
		    if ( (! achou) && className.equals(baseClass)) 
		    {
		    	// chama a rotina que faz a instrumentacao
		        jv = doMethodInstrument(jv);
				hs.put(className, jv);
		    } else { // coloca o codigo sem instrumentar
		        hs.put(className, jv);
		    }
		}
		return hs;
	}

    protected JavaClass doMethodInstrument(JavaClass java_class)
    {
    	/* o trecho abaixo � inserido no inicio do metodo startApp 
    	   corresponde a:

    	 	HostProber.init(fileName, server, );
    	 */
    	 	
    	String before  = 
    		( fileName == null ? " aconst_null " : "ldc \"" + fileName + "\" ") +
    		( server == null ? " aconst_null " : "ldc \"" + server + "\" ") +
    		"ldc " + thr + " " +
			"ldc " + (keepAlive ? 1: 0) + 
    		"ldc \"" + id + "\" " +
	   		"invokestatic " + getProbeClass() + " init " +
					        "\"(Ljava/lang/String;" + 
					        "Ljava/lang/String;IZ" + 
					        "Ljava/lang/String;)V\"";
	   	String after =	"nop";
    	java_class = wrapMethod(java_class, "startApp", "()V", before, after);
    	
    	before  = "nop";
	   	after = 
	   		"invokestatic " + getProbeClass() + " finish " +
	        "\"()V\"";
    	java_class = wrapMethod(java_class, "pauseApp", "()V", before, after);
    	
    	before  = "nop";
	   	after = 
	   		"invokestatic " + getProbeClass() + " finish " +
	        "\"()V\"";
    	java_class = wrapMethod(java_class, "destroyApp", "(Z)V", before, after);

    	return java_class;
    }
    
}

