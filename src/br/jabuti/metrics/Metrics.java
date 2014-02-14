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


package br.jabuti.metrics;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.MethodGen;

import br.jabuti.graph.CFG;
import br.jabuti.graph.CFGCallNode;
import br.jabuti.graph.CFGNode;
import br.jabuti.lookup.Program;
import br.jabuti.lookup.RClass;
import br.jabuti.lookup.RClassCode;
import br.jabuti.util.Debug;

/**
* This class implements a set o OO metrics, calculated 
* on a Program structure. Each class represented in 
* the structure by a {@link br.jabuti.lookup.RClassCode} will have
* its set of metrics. 
*
* If desired, a subset of the {@link br.jabuti.lookup.RClassCode} can also
* be used as the set of classes where the metrics will be applied,
* for example, only the class files that are under testing.
*
* @version: 1.0
* @author: Marcio Delamaro
* @author: Auri Vincenzi
*
*/
public class Metrics
{
	Hashtable classTable, graphTable;
	Program prog;
	
	public static final String metrics[][] = 
	   { 
	   		{"anpm",	"Average Number of Parameters per Method (ANPM)"},
	   		{"amz_locm","Average Method Size (AMZ_LOCM)"},
	   		{"amz_size","Average Method Size (AMZ_SIZE) - Number of Bytecode Instructions"},
	   		{"cbo",		"Coupling Between Object (CBO)"},
	   		{"cc_avg",	"Cyclomatic Complexity Metric (CC) - CC_AVR"},
	   		{"cc_max",	"Cyclomatic Complexity Metric (CC) - CC_MAX"},
	   		{"dit",		"Depth of Inheritance Tree (DIT)"},
	   		{"lcom",	"Lack of Cohesion in Methods (LCOM)"},
	   		{"lcom_2",	"Lack of Cohesion in Methods (LCOM): LCOM_2 - only static methods"},
	   		{"lcom_3",	"Lack of Cohesion in Methods (LCOM): LCOM_3 - static or instance methods"},
	   		{"mnpm",	"Maximum Number of Parameters per Method (MNPM)"},
	   		{"ncm",		"Number of Class Methods in a class (NCM)"},
	   		{"ncm_2",	"Number of public Class Methods in a class (NCM_2)"},
	   		{"ncv",		"Number of Class Variables in a class (NCV)"},
	   		{"nii",		"Number of Interfaces Implemented (NII)"},
	   		{"niv",		"Number of Instance Variables in a class (NIV)"},
	   		{"nmas",	"Number of Methods Added by a Subclass (NMAS)"},
	   		{"nmis",	"Number of Methods Inherited by a Subclass (NMIS)"},
	   		{"nmos",	"Number of Methods Overridden by a Subclass (NMOS)"},
	   		{"noc", 	"Number of Children (NOC)"},
	   		{"npim",	"Number of Public Instance Methods in a class (NPIM)"},
	   		{"rfc",		"Response for a Class (RFC)"},
	   		{"si",		"Specialization Index (SI)"},
	   		{"wmc_1", 	"Weighted Methods per Class (WMC): WMC_1 - metric 1"},
	   		{"wmc_cc", 	"Weighted Methods per Class (WMC): WMC_CC - metric CC"},
	   		{"wmc_locm","Weighted Methods per Class (WMC): WMC_LOCM - metric LOCM"},
	   		{"wmc_size","Weighted Methods per Class (WMC): WMC_SIZE - metric SIZE (number of instructions)"}
	   } ;

    private static final Class param[] = { String.class }; 	   
	private static final java.lang.reflect.Method methods[] = 
	           new java.lang.reflect.Method[metrics.length];
	
	static { 
	    for (int i = 0; i < metrics.length; i++)
	    {
			try
			{
	    		methods[i] = Metrics.class.getMethod(metrics[i][0], param);
	    	}
	    	catch (Exception e)
	    	{
	    		Debug.D("Not found " + i + " " + e);
	    	}
	 	}
	}
	
	/**
	 * This method calculates the metrics w.r.t. all
	 * user defined classes in a given project.
	 */
	public Metrics(Program prog)
	{
		String[] classes = (this.prog = prog).getCodeClasses();
		classTable = new Hashtable();
		for (int i = 0; i < classes.length; i++)
		{
			graphTable = new Hashtable();
			classTable.put(classes[i], computeMetrics(classes[i]));
		}
	}

	/**
	 * This method calculates the metrics only w.r.t. 
	 * the classes that were chosen to be instrumented.
	 */
	public Metrics(Program prog, String[] classes )
	{
		this.prog = prog;
		//String[] classes = (this.prog = prog).getCodeClasses();
		classTable = new Hashtable();
		for (int i = 0; i < classes.length; i++)
		{
			graphTable = new Hashtable();
			classTable.put(classes[i], computeMetrics(classes[i]));
		}
	}
	
	public Hashtable computeMetrics(String className)
	{
		Hashtable hs = new Hashtable();
		for (int i = 0; i < metrics.length; i++)
		{
			java.lang.reflect.Method mt = methods[i];
			Double fs = null; // calcula a metrica
			
			try 
			{
				fs = (Double) mt.invoke(this, new Object[] {className});
			}
			catch (java.lang.reflect.InvocationTargetException ee) 
			{ 
			    Throwable e = ee.getTargetException();
				System.out.println("Not found2 " + i + " " + e);
				System.out.println("Not found2 " + mt + " " + e.getMessage());
				e.printStackTrace();
			}
			catch (IllegalAccessException ee) 
			{ 
				System.out.println("Not found2 " + i + " " + ee);
				System.out.println("Not found2 " + mt + " " + ee.getMessage());
			}
			hs.put(metrics[i][0], fs);
		}
		return hs;
	}




/////////////////////////////////////////////////////////////
////////// METRICS CK
	
	/** <p>Implements the DIT - depth in tree <p><br>
*	<p> Considers only the part of the program inside the inrterest limits. 
*	It means that classes "avoided" in the program structure are not counted.
*   The longest path is considered in the computation. It means, if the class 
*   implements an interface, the path through the interface is also 
*   considered.
*	@param className - The name of the class
*	@return the distance from the border of the program or -1.0 if the
*  	class is not in the program. 
	*/ 
	public double dit(String className)
	{
		RClass tc = prog.get(className);
		if ( tc == null)
			return -1.0;
		if (! (tc instanceof RClassCode ) )
			return 0.0;
		RClassCode theClass = (RClassCode) tc; 
		String inter[] = theClass.getInterfaces();
		double theValue = 0;
		for (int i = 0; inter != null && i < inter.length; i++)
		{
			int k = prog.levelOf(inter[i]);
			theValue = k > theValue? k : theValue;
		}
		double d = (double) prog.levelOf(className);
		return d > theValue ? d : theValue;
	}
	
	/** <p>Implements the NOC - number of children <p><br>
*	@param className - The name of the class 
*	@return the distance from the border of the program or -1.0 if the
*  	class is not in the program. 
	*/ 
	public double noc(String className)
	{
		RClass rc = prog.get(className);
		if ( rc == null)
			return -1.0;
		return (double) (rc.getSubClasses().length);
	}


/**<p>Falta de Coes�o entre os m�todos </p>
<p>M�trica calculada atrav�s da contagem do n�mero de pares de
m�todos na classe que n�o compartilham vari�veis de inst�ncia
menos o n�mero de pares de m�todos que compartilham vari�veis de
inst�ncia. Quando o resultado � negativo, a m�trica recebe o valor
zero. Os m�todos est�ticos n�o s�o considerados na contagem, uma
vez que s� as vari�veis de inst�ncia s�o tomadas. Construtores 
s�o considerados</p>
 */
    public double lcom(String className)
    {
    	return lcom_0(className, false);
    }


/**<p>Falta de Coes�o entre os m�todos </p>
<p>O mesmo que LCOM por�m s� os metodos est�ticos s�o 
considerados </p>
*/
    public double lcom_2(String className)
    {
    	return lcom_0(className, true);
    }

/**<p>Falta de Coes�o entre os m�todos </p>
<p> lcom + lcom_2</p>
*/
    public double lcom_3(String className)
    {
    	return lcom(className) + lcom_2(className);
    }

    private double lcom_0(String className, boolean isStatic)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    		Vector dmethod = new Vector();
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        ClassGen cg = new ClassGen(theClazz);
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
                
            // static methods are not considered
            if (mg.isStatic() != isStatic)
        		continue;
            CFG g = null;
            try 
            {
            	g = getCFG(mg, cg);
            }
            catch (Exception e)
            {
            	return -1.0;
            }
            Collection defUses = findDefUse(g);
            // keeps only the non  static accesses
            Iterator it = null; 
            for ( it = defUses.iterator(); it.hasNext(); )
            {
            	String s = (String) it.next();
            	if ( ! s.startsWith("L@0.") )
            		it.remove();
            }
            dmethod.add(defUses);
        }
        int doShare = 0, notShare = 0;
        // now compare the methods
        for (int i = 0; i < dmethod.size()-1; i++)
        {
        	Collection v = (Collection) dmethod.elementAt(i);
        	r1:
        	for (int j = i+1; j < dmethod.size(); j++)
        	{
        		Collection v1 = (Collection) dmethod.elementAt(j);
        		for (Iterator it1 = v.iterator();  it1.hasNext(); )
        		{
        			String s = (String) it1.next();
        			if ( v1.contains(s) )
        			{
        				Debug.D("Methods " + i + " and " + j + " share " + s);
        				doShare++;
        				continue r1;
        			}
        		}
        		Debug.D("Methods " + i + " and " + j + " do not share ");
        		notShare++;
        	}
        }
        double lcom = (double) notShare - doShare;
        return lcom >= 0? lcom: 0.0;
    }
    
    private double CC(MethodGen mg, ClassGen cg)
    {
     	int nos = 0;
    	int arcos = 0;
	   	try
    	{
    		CFG g = getCFG(mg, cg);
    		nos = g.size();
    		arcos = 0;
    		for (int i = 0; i < g.size(); i++)
    		{
    			CFGNode gn = (CFGNode) g.elementAt(i);
    			arcos += gn.getPrimNext().size();
    		}
    		arcos += g.getExits().length;
    	}
    	catch (Exception e)
    	{
    		return -1.0;
    	}
    	return (double) (arcos - nos + 1);
    }

    private double LOCM(MethodGen mg)
    {
    	LineNumberGen lng[] = mg.getLineNumbers();
    	if (lng == null)
    		return 0.0;
    	return (double) lng.length;
    }

    private double SIZE(MethodGen mg)
    {
    	InstructionList il = mg.getInstructionList();
    	if (il == null)
    		return 0.0;
    	return (double) il.getLength();
    }
    
/**<p>N�mero de m�todos ponderados por classe </p>
<p>M�trica calculada atrav�s da soma da complexidade de cada m�todo.
Usa CC (McCabe) como m�trica de complexidade por metodo 
Construtores s�o considerados. </p>
*/
    public double wmc_cc(String className)
    {
    	double theValue = 0.0;
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        ClassGen cg = new ClassGen(theClazz);
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
            double d = CC(mg, cg);
            
            if ( d < 0.0)
            	return -1;
            theValue += d;
        }
        return theValue;
    }

/**<p>N�mero de m�todos ponderados por classe </p>
<p>M�trica calculada atrav�s da soma da complexidade de cada m�todo.
Usa 1 como m�trica de complexidade por metodo </p>
*/
    public double wmc_1(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        Method[] methods = theClazz.getMethods (  );
        int theValue = 0;
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
        	theValue++;
        }
        return (double) theValue;
    }

/**<p>N�mero de m�todos ponderados por classe </p>
<p>M�trica calculada atrav�s da soma da complexidade de cada m�todo.
Usa LOCM (linhas de codigo) como m�trica de complexidade por metodo </p>
*/
    public double wmc_locm(String className)
    {
    	double theValue = 0.0;
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
            double d = LOCM(mg);
            theValue += d;
        }
        return theValue;
    }

/**<p>N�mero de m�todos ponderados por classe </p>
<p>M�trica calculada atrav�s da soma da complexidade de cada m�todo.
Usa size (n�mero de instru��es) como m�trica de complexidade por metodo </p>
*/
    public double wmc_size(String className)
    {
    	double theValue = 0.0;
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
            double d = SIZE(mg);
            theValue += d;
        }
        return theValue;
    }

    private Collection findDefUse(CFG gfc)
    {
    	HashSet v = new HashSet();
    	for (int i = 0; i < gfc.size(); i++)
    	{
    		CFGNode gn = (CFGNode) gfc.elementAt(i);
    		v.addAll(gn.definitions.keySet());
    		v.addAll(gn.uses.keySet());
    	}
    	return v;
    }
	
/**<p>Complexidade Ciclom�tica de McCabe </p>
<p>Calculado como o valor maximo da CC para os metodos 
Construtores s�o considerados. </p>
*/
    public double cc_max(String className)
    {
    	double theValue = 0.0;
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        ClassGen cg = new ClassGen(theClazz);
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
            double d = CC(mg, cg);
            
            if ( d < 0.0)
            	return -1;
            theValue = ( d > theValue ) ? d : theValue;
        }
        return theValue;
    }
	

/**<p>Complexidade Ciclom�tica de McCabe </p>
<p>Calculado como o valor m�dio da CC para os metodos 
Construtores s�o considerados. </p>
*/
    public double cc_avg(String className)
    {
    	double theValue = 0.0;
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        ClassGen cg = new ClassGen(theClazz);
        Method[] methods = theClazz.getMethods (  );
        int k=0;
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
            double d = CC(mg, cg);
            
            if ( d < 0.0)
            	return -1;
            theValue += d;
            k++;
        }
        return k > 0? theValue / k: 0.0;
    }
    
/**<p>Resposta para uma classe </p>
<p>M�trica calculada atrav�s da soma do n�mero de m�todos da classe
mais os m�todos que s�o invocados diretamente por eles. � o n�mero
de m�todos que podem ser potencialmente executados em resposta a
uma mensagem recebida por um objeto de uma classe ou por algum
m�todo da classe. Quando um m�todo polim�rfico � chamado para
diferentes classes, cada diferente chamada � contada uma vez. 
Construtores s�o considerados. </p>
*/
    public double rfc(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        ClassGen cg = new ClassGen(theClazz);
        Method[] methods = theClazz.getMethods (  );
        int theValue = 0;
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        	{
        		continue;
        	}
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
                
            CFG g = null;
            try 
            {
            	g = getCFG(mg, cg);
            }
            catch (Exception e)
            {
            	return -1.0;
            }

	        HashSet hs = new HashSet();
            for (int j = 0; j < g.size(); j++ )
            {
            	CFGNode gn= (CFGNode) g.elementAt(j);
            	if (! ( gn instanceof CFGCallNode ) )
            		continue; 
            	CFGCallNode gcn = (CFGCallNode) gn;
            	for (int h = 0; h < gcn.getClasse().length; h++)
            	{
            		hs.add(gcn.getClasse()[h] + gcn.getName() );
            	}
            }
            theValue += hs.size();
            theValue++; // counts also the method current
        }
        return (double) theValue ;
    }

/**<p>Acoplamento entre objetos </p>
<p>H� acoplamento entre duas classes quando uma classe usa m�todos
e/ou vari�veis de inst�ncia de outra classe. M�trica calculada
atrav�s da contagem do n�mero de classes �s quais uma classe est�
acoplada de alguma forma, o que exclui o acoplamento baseado em
heran�a. Assim, o valor CBO de uma classe A � o n�mero de classes
das quais a classe A utiliza algum m�todo e/ou vari�vel de
inst�ncia. </p>
*/
    public double cbo( String className )
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPool cp = theClazz.getConstantPool (  );
        Constant[]ct = cp.getConstantPool (  );
        HashSet hs = new HashSet();
        for ( int i = 0; i < ct.length; i++ )
        {
            if ( ct[i] instanceof ConstantCP )
            {
                ConstantCP cc = ( ConstantCP ) ct[i];
                if ( ! className.equals( cc.getClass(cp) ) )
                {
                  	hs.add(cc.getClass(cp));
                }
            }
        }
        return ( hs.size() );
    }
	
////////////////////////////////////////////////////////////////////////////
///////// METRICS LK

/** <p>N�mero de m�todos de inst�ncia p�blicos na classe</p>
<p>M�trica calculada atrav�s da contagem do n�mero de m�todos de
inst�ncia p�blicas na classe, incluindo os construtores.</p>
*/
    public double npim(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
        	if (methods[i].isStatic() )
        		continue;
        	if (! methods[i].isPublic() )
        		continue;
        	cont++;
		}
		return (double) cont;
	}

/**<p>N�mero de m�todos de classe na classe</p> 
<p>M�trica calculada atrav�s da contagem do n�mero de m�todos
static na classe.</p> 
*/
    public double ncm(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
        	if (! methods[i].isStatic() )
        		continue;
//        	if (! methods[i].isPublic() )
//        		continue;
        	cont++;
		}
		return (double) cont;
	}

/** M�trica calculada atrav�s da contagem do n�mero de m�todos
static na classe, por�m somente os m�todos p�blicos s�o considerados. 
*/

    public double ncm_2(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
        	if (! methods[i].isStatic() )
        		continue;
        	if (! methods[i].isPublic() )
        		continue;
        	cont++;
		}
		return (double) cont;
	}

/**<p>N�mero de vari�veis de inst�ncia na classe </p>
<p>M�trica calculada atrav�s da contagem do n�mero de vari�veis de
inst�ncia na classe, o que inclui as vari�veis public,
private e protected dispon�veis para as
inst�ncias.</p>    */
	public double niv(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        Field[] fields = theClazz.getFields (  );
        for ( int i = 0; i < fields.length; i++ )
        {
        	if (fields[i].isStatic() )
        		continue;
        	cont++;
		}
		return (double) cont;
	}

/** <p>N�mero de vari�veis de classe na classe </p> 
<p>M�trica calculada atrav�s da contagem do n�mero de vari�veis
static na classe.    </p>
*/
	public double ncv(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        Field[] fields = theClazz.getFields (  );
        for ( int i = 0; i < fields.length; i++ )
        {
        	if (! fields[i].isStatic() )
        		continue;
        	cont++;
		}
		return (double) cont;
	}
/**<p>N�mero m�dio de par�metros por m�todo </p>
<p>M�trica calculada atrav�s da divis�o entre o somat�rio do n�mero
de par�metros de cada m�todo da classe pelo n�mero total de
m�todos da classe. Construtores s�o considerados. </p>
*/
    public double anpm(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0, contPar = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
        	String dummy[] = mg.getArgumentNames();
        	cont++;
        	contPar += dummy.length;
		}
		if (cont == 0 )
			return -1.0;
		return (double) contPar / cont;
	}


/**<p>N�mero m�ximo de par�metros em um m�todo da classe</p>
*/
    public double mnpm(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int max = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
        	String dummy[] = mg.getArgumentNames();
        	max = dummy.length > max ? dummy.length : max;
		}
		return (double) max;
	}

/**<p>Tamanho m�dio do m�todo </p>
<p>M�trica calculada atrav�s da divis�o entre a soma do n�mero de
linhas de c�digo dos m�todos da classe pelo n�mero de m�todos na
classe (soma dos m�todos inst�ncia e classe). Construtores inclusive </p>
*/
    public double amz_locm(String className)
    {
    	double theValue = 0.0;
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
            double d = LOCM(mg);
            theValue += d;
			cont++;
        }
        if (cont == 0 )
        	return -1.0;
        return theValue / cont;
    }

/**<p>Tamanho m�dio do m�todo </p>
<p>M�trica calculada atrav�s da divis�o entre a soma do numero de
instrucoes dos m�todos da classe pelo n�mero de m�todos na
classe (soma dos m�todos inst�ncia e classe). Construtores inclusive </p>
*/
    public double amz_size(String className)
    {
    	double theValue = 0.0;
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        ConstantPoolGen cp =
            new ConstantPoolGen ( theClazz.getConstantPool (  ) );
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
            MethodGen mg =
                new MethodGen ( methods[i], theClazz.getClassName (  ), cp );
            double d = SIZE(mg);
            theValue += d;
			cont++;
        }
        if (cont == 0 )
        	return -1.0;
        return theValue / cont;
    }

/**<p>n�mero de interfaces implementadas pela classe </p>
*/
    public double nii(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0; 
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
    	String[] dummy = theClazz.getInterfaceNames();
    	if ( dummy == null)
    		return 0.0;
    	return (double) dummy.length;
    }
    
 
/**<p>N�mero de m�todos sobrescritos na subclasse </p>
<p>M�trica calculada atrav�s da contagem do n�mero de m�todos
definidos na subclasse com a mesma assinatura de um m�todo na sua
superclasse. Construtores e inicializadores est�ticos 
N�O s�o considerados. </p>
*/
    public double nmos(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        rc = prog.get(rcc.getSuperClass());
        if ( ! (rc instanceof RClassCode) )
          	return 0.0;
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].getName().endsWith("init>") )
        		continue;
        	if (methods[i].isAbstract())
        		continue;
        	if (findMethInClass(methods[i], rcc.getSuperClass(), true) )
        		cont++; 
        }
        return (double) cont;
    }

/**<p> N�mero de m�todos adicionados pela subclasse </p>
<p>M�trica calculada atrav�s da contagem do n�mero de novos m�todos
adicionados pela classe. Construtores e inicializadores est�ticos 
N�O s�o considerados.</p>
*/
    public double nmas(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        rc = prog.get(rcc.getSuperClass());
        if ( ! (rc instanceof RClassCode) )
          	return 0.0;
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].getName().endsWith("init>") )
        		continue;
        	if (methods[i].isAbstract())
        		continue;
        	if ( ! findMethInClass(methods[i], rcc.getSuperClass(), true) )
        		cont++; 
        }
        return (double) cont;
    }

/**<p>N�mero de m�todos herdados pela subclasse </p>
<p>M�trica calculada atrav�s da contagem do n�mero de m�todos
herdados pela subclasse de suas superclasses. Construtores e 
inicializadores est�ticos 
N�O s�o considerados.</p>
*/
    public double nmis(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		rc = prog.get(rcc.getSuperClass());
        if ( ! (rc instanceof RClassCode) )
          	return 0.0;
        JavaClass supertheClazz = rcc.getTheClass();
        Method[] methods = supertheClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].getName().endsWith("init>") )
        		continue;
        	if (methods[i].isAbstract())
        		continue;
        	if ( ! findMethInClass(methods[i], className, false) )
        	{
        		cont++; 
        	}
        }
        return (double) cont + nmis(rcc.getSuperClass());
    }


/**<p>�ndice de Especializa��o </p>
<p>M�trica calculada atrav�s da divis�o entre o resultado da
multiplica��o de NMOS e DIT (m�trica de CK) pelo n�mero total de m�todos. </p>
*/
    public double si(String className)
    {
    	RClass rc = prog.get(className);
    	if ( ! (rc instanceof RClassCode) )
    		return -1.0;
    	int cont = 0;
    	RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
        Method[] methods = theClazz.getMethods (  );
        for ( int i = 0; i < methods.length; i++ )
        {
        	if (methods[i].isAbstract())
        		continue;
       		cont++; 
        }
        if (cont == 0 )
        	return -1;
        return nmos(className) * dit(className) / cont;
    }


///////////////////////////////////////////////////////////////

	public void list()
	{
		Enumeration en = classTable.keys();
		while (en.hasMoreElements() )
		{
			String className = (String) en.nextElement();
			Hashtable hs = (Hashtable) classTable.get(className);
			System.out.println();
			System.out.println("============================================");
			System.out.println(className);
			Enumeration en2 = hs.keys();
			while (en2.hasMoreElements() )
			{
				String metricName = (String) en2.nextElement();
				Double mtValue = (Double) hs.get(metricName);
				System.out.println("\t" + metricName + ": " + mtValue);
			}
		}
	}


	private CFG getCFG(MethodGen mg, ClassGen cg) 
		throws br.jabuti.verifier.InvalidInstructionException,
				br.jabuti.verifier.InvalidStackArgument
	{
		String s = mg.getClassName() + "." + mg.getName() + mg.getSignature();
		Object osg = graphTable.get(s);
		if ( osg == null )
		{
	       	CFG g = new CFG (mg, cg);
	       	String sg = br.jabuti.util.Persistency.add(g);
	       	if ( sg != null)
	       		graphTable.put(s, sg);
	       	else
	       		graphTable.put(s, g);
	       	return g;
	    }
	    
	    if ( osg instanceof String )
	    {
	    	String sg = (String) osg;
	    	CFG g = null;
	    	try
	    	{
	    		g = (CFG) br.jabuti.util.Persistency.get(sg);
	    	}
	    	catch (Exception e)
	    	{ ;}
	        return g;
	    }
	    return (CFG) osg;
	}


	private boolean findMethInClass(Method m, String className, boolean rec)
	{
		RClass rc = prog.get(className);
		if (rc == null || ! (rc instanceof RClassCode) )
		{
			return false;
		}
		RClassCode rcc = (RClassCode) rc;
		JavaClass theClazz = rcc.getTheClass();
		Method[] methods = theClazz.getMethods();
		int i;
		for (i = 0; i < methods.length; i++)
		{
        	if ( methods[i].getName().equals(m.getName()) &&
        	     methods[i].getSignature().equals(m.getSignature()) )
        	{
        		return true;
        	}
		}
		if ( rec )
		{
			return findMethInClass(m, rcc.getSuperClass(), rec);
		}
		return false;
	}
	

	/**
	* This method returns the set of computed metrics for a given 
	* class.
	*/
    public Object[] getClassMetrics(String cName) {
        Object[] values = null;

        if (classTable.containsKey(cName)) {
            Hashtable hs = (Hashtable) classTable.get(cName);

            values = new Object[Metrics.metrics.length];
            for (int i = 0; i < Metrics.metrics.length; i++) {
                String metricName = (String) Metrics.metrics[i][0];

                values[i] = hs.get(metricName);
            }
        }
        return values;
    }
	
	
	static public void main ( String args[] ) throws Exception
    {
        Program p = null;
        p = new Program ( args[0], true, null, args[1] );
        String[] classes = {args[0]};
        Metrics mt = new Metrics(p, classes);
        mt.list();
    }
}