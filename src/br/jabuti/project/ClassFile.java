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


package br.jabuti.project;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import br.jabuti.criteria.AbstractCriterion;
import br.jabuti.criteria.Criterion;
import br.jabuti.lookup.RClassCode;

/**
 *
 * This class stores information about coverage for a single
 * class file that is composed by a set of methods. 
 *
 * It groups the information about a given class file
 * and stores the dinamic information to be presented
 * in the GUI.
 *
 * It is basically composed by a set of 
 * method objects.
 *
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 * @author: Tatiana Sugeta
 *
 */
public class ClassFile {
	
    /** The current project */
    private JabutiProject prj;
	
    /** The class file name */
    private String className;

    /** The class file name */
    private ClassSourceFile sourceFile;

    /**
     * The coverage w.r.t the entire class file... 
     * The sum of the coverage of each method
     */
    private Coverage[] classCoverage;

    // Used by toString method to ident the output
    String prefix = new String("");
	
    // All methods that belongs to this class file
    private HashMap methodsTable = null;

    // The correspondence between method number and
    // method name
    private HashMap codeNameTable = null;

    //private JavaClass java_class = null;
    private Method[] methods = null;
    private int classId;

    public ClassFile(JabutiProject p, String name, int cId) {

        prj = p;
        className = name;
        classId = cId;

        classCoverage = new Coverage[Criterion.NUM_CRITERIA];
        for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
            classCoverage[i] = new Coverage();
        }

        /* Creating all other objects */
        methodsTable = new HashMap();
        codeNameTable = new HashMap();

        RClassCode rcc = (RClassCode) prj.getProgram().get(className);

        JavaClass java_class = rcc.getTheClass();

        sourceFile = new ClassSourceFile(prj, rcc);
        
        ConstantPoolGen cp = new ConstantPoolGen(java_class.getConstantPool());
        ClassGen cg = new ClassGen(java_class);
        
        methods = java_class.getMethods();
        
        System.out.println("********** WORKING WITH " + className + " CLASS **********");
        
        for (int j = 0; j < methods.length; j++) {
            String methodName = methods[j].getName() + methods[j].getSignature();
            
            //System.out.println("\t+++ METHOD " + methodName + " +++");
            
            MethodGen mg = new MethodGen(methods[j], className, cp);
        
            ClassMethod meth = new ClassMethod(prj, this, mg, cp, cg, methodName, j);
        
            methodsTable.put(methodName, meth);
            codeNameTable.put(new Integer(j), methodName);
            // meth.getWeightByDominator( Criterion.ALL_NODES );
        }
        
        updateClassFileCoverage();
    }

    /***********************************************************/
    
    /* Get and Set Methods implementation                      */
    
    /***********************************************************/
	
    public JabutiProject getProject() {
        return prj;
    }

    /*private void setProject(JabutiProject p) {
        prj = p;
    }*/

    public String getClassName() {
        return className;
    }

    public HashMap getMethodsTable() {
        return methodsTable;
    }
	
    public String[] getAllMethodsNames() {
        if (methodsTable.size() > 0) {
            return (String[]) methodsTable.keySet().toArray(new String[0]);
        } else {
            return null;
        }
    }

    public Method[] getMethods() {
        return methods;
    }

    public JavaClass getJavaClass() {
		RClassCode rcc = (RClassCode) prj.getProgram().get(className);
		return rcc.getTheClass();
    }

    public ClassMethod getMethod(String name) {
        if (methodsTable.containsKey(name)) {
            return (ClassMethod) methodsTable.get(name);
        } else {
            return null;
        }
    }

    public ClassMethod getMethod(int id) {
        Integer mthId = new Integer(id);

        if (codeNameTable.containsKey(mthId)) {
            String mthName = (String) codeNameTable.get(mthId);

            return getMethod(mthName);
        } else {
            return null;
        }
    }

    public Coverage getClassFileCoverage(int c) {
        if ((c >= 0) && (c < Criterion.NUM_CRITERIA)) {
            return classCoverage[ c ];
        }
        return null;
    }

    public int getNumberOfMethods() {
        return methodsTable.size();
    }
	
    public void updateClassFileCoverage() {
        /** Restarting current coverage */
        for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
            classCoverage[i] = new Coverage();
        }
		
        Iterator it = methodsTable.values().iterator();

        while (it.hasNext()) {
            ClassMethod mt = (ClassMethod) it.next();

            mt.updateClassMethodCoverage();
			
            for (int i = 0; i < Criterion.NUM_CRITERIA; i++) {
                Coverage methodCoverage = mt.getClassMethodCoverage(i);
                int totalCov = classCoverage[i].getNumberOfCovered()
                        + methodCoverage.getNumberOfCovered();
				
                int totalReq = classCoverage[i].getNumberOfRequirements()
                        + methodCoverage.getNumberOfRequirements();

                classCoverage[i].setNumberOfCovered(totalCov);
                classCoverage[i].setNumberOfRequirements(totalReq);
            }
        }
    }
	
    public ClassSourceFile getSourceFile() {
        return sourceFile;
    }

    public BufferedReader getBytecode() {
        BufferedReader buf = new BufferedReader(new StringReader(getJavaClass().toString()));

        return buf;
    }

    /*
     public Hashtable getWeightByDominator(int c) {
     Hashtable weight = new Hashtable();
     
     Iterator it = methodsTable.values().iterator();

     while (it.hasNext()) {
     ClassMethod mt = (ClassMethod) it.next();
     Hashtable methodWeight = mt.getWeightByDominator( c );

     weight.putAll( methodWeight );
     }
     return weight;
     }*/

    /**
     * Returns a HashSet with the corresponding weigth for
     * the entire class considering the weight of each one
     * of its individual methods.
     * It uses the SuperBlock information to get the weight
     * of each CFG Node.
     */
    public HashSet getWeightByNode(int cId) {
        HashSet weight = new HashSet();
		
        Iterator it = methodsTable.values().iterator();

        while (it.hasNext()) {
            ClassMethod mt = (ClassMethod) it.next();
            HashSet methodWeight = mt.getWeightByNode(cId);

            weight.addAll(methodWeight);
        }
        return weight;
    }

    /**
     * Returns a HashSet with the corresponding weigth for
     * the entire class considering the weight of each one
     * of its individual methods.
     * It uses the SuperBlock information to get the weight
     * of each CFG Node.
     */
    public Hashtable getWeightByEdge(int cId) {
        Hashtable methodEdgesWeight = new Hashtable(getNumberOfMethods());
    	
        Iterator it = methodsTable.values().iterator();

        while (it.hasNext()) {
            ClassMethod mt = (ClassMethod) it.next();
            Hashtable edgesWeight = mt.getWeightByEdge(cId);

            methodEdgesWeight.put(new Integer(mt.getMethodId()), edgesWeight);
        }
        return methodEdgesWeight;
    }

    /**
     * Returns a HashSet with the corresponding weigth for
     * the entire class considering the weight of each one
     * of its individual methods.
     * It uses the SuperBlock information to get the weight
     * of each CFG Node.
     */
    public Hashtable getWeightByUse(int cId) {
        Hashtable methodUsesWeight = new Hashtable(getNumberOfMethods());
    	
        Iterator it = methodsTable.values().iterator();

        while (it.hasNext()) {
            ClassMethod mt = (ClassMethod) it.next();
            Hashtable usesWeight = mt.getWeightByUse(cId);

            methodUsesWeight.put(new Integer(mt.getMethodId()), usesWeight);
        }
        return methodUsesWeight;
    }

    /**
     * This method releases the labelNodeTable of
     * each method that composes this class.
     * This data structure is used by GUI purpose.
     * Since the current class has been changed, this table
     * is not necessary anymore.
     */
    public void releaseLabelNodeTable() {
        Iterator it = methodsTable.values().iterator();

        while (it.hasNext()) {
            ClassMethod mt = (ClassMethod) it.next();

            mt.releaseLabelNodeTable();
        }
        System.gc();
    }
	
    public int getClassId() {
        return classId;
    }
	
    public String toString(String p) {
        prefix = p;
        return toString();
    }

    public String toString() {
        String out = new String(prefix + "ClassFile: " + className + "\n");

        out = out + prefix + "\tCurrent Coverage:\n";
        out = out + coverage2TXT( "\t\t" );
        
        out = out + prefix + "\tMethods:\n";
        Iterator it = methodsTable.values().iterator();

        while (it.hasNext()) {
            ClassMethod cm = (ClassMethod) it.next();

            out = out + cm.toString("\t\t\t");
        }

        return out;
    }
    
    public String coverage2TXT( String prefix ) {
        String out = prefix + AbstractCriterion.getName( Criterion.PRIMARY_NODES ) +": "
                + getClassFileCoverage(Criterion.PRIMARY_NODES).toString() + "\n";
        out = out + prefix + AbstractCriterion.getName( Criterion.SECONDARY_NODES ) +": "
                + getClassFileCoverage(Criterion.SECONDARY_NODES).toString() + "\n";
		out = out + prefix + AbstractCriterion.getName( Criterion.PRIMARY_EDGES ) +": "
                + getClassFileCoverage(Criterion.PRIMARY_EDGES).toString() + "\n";		
		out = out + prefix + AbstractCriterion.getName( Criterion.SECONDARY_EDGES ) +": "
                + getClassFileCoverage(Criterion.SECONDARY_EDGES).toString() + "\n";		
        out = out + prefix + AbstractCriterion.getName( Criterion.PRIMARY_USES ) +": "
                + getClassFileCoverage(Criterion.PRIMARY_USES).toString() + "\n";
        out = out + prefix + AbstractCriterion.getName( Criterion.SECONDARY_USES ) +": "
                + getClassFileCoverage(Criterion.SECONDARY_USES).toString() + "\n";
        out = out + prefix + AbstractCriterion.getName( Criterion.PRIMARY_POT_USES ) +": "
                + getClassFileCoverage(Criterion.PRIMARY_POT_USES).toString() + "\n";
        out = out + prefix + AbstractCriterion.getName( Criterion.SECONDARY_POT_USES ) +": "
                + getClassFileCoverage(Criterion.SECONDARY_POT_USES).toString() + "\n";
        return out;
    }
}
