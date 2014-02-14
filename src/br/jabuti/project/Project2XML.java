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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.bcel.classfile.JavaClass;

import br.jabuti.criteria.AbstractCriterion;
import br.jabuti.criteria.Criterion;
import br.jabuti.criteria.DefUse;
import br.jabuti.criteria.Edge;
import br.jabuti.criteria.Requirement;
import br.jabuti.lookup.Program;
import br.jabuti.lookup.RClassCode;
import br.jabuti.util.HTMLGen;
import br.jabuti.util.ToolConstants;

/**
 * This class is responsible to persist a project in a
 * XML file. It traverse the project, its classes and
 * methods storing all the information necessary to
 * recover the state of a given project.
 *
 * @version: 1.0
 * @author: Auri Vincenzi
 */
public class Project2XML {
    static private Writer  out;
    static private String indentString = "    "; // Amount to indent
    static private int indentLevel = 0;
	
    public static boolean project2XML( JabutiProject prj, File xmlFile ) {
        try {
            // Set up output stream
            out = new OutputStreamWriter( new 
            	FileOutputStream( xmlFile ), "UTF8");
        } catch (Throwable t) {
            ToolConstants.reportException(t, ToolConstants.STDERR );
            return false;
        }

        StringBuffer xmlOut;
    	
        // Saving the high level element: JABUTI
    	emit( "<" + XML2Project.JABUTI + ">" );

        // Saving the PROJECT and its attributes
        indentLevel++;
        xmlOut = new StringBuffer( "<" + XML2Project.PROJECT );
    	xmlOut.append( " name=\"" + prj.getProjectFileName() + "\"" );
    	xmlOut.append( " type=\"research\"" );
    	xmlOut.append( " mobility=\"" + (prj.isMobility()? "Y":"N") + "\"" );
    	xmlOut.append( " CFGOption=\"" + new Integer( prj.getCFGOption() ).toString() + "\">" );
    	nl();
    	emit( xmlOut.toString() );


        // Saving the BASE_CLASS and its attributes
        indentLevel++;
        xmlOut = new StringBuffer( "<" + XML2Project.BASE_CLASS );
    	xmlOut.append( " name=\"" + prj.getMain() + "\"/>" );
    	nl();
    	emit( xmlOut.toString() );

        // Saving the CLASSPATH and its attributes
        xmlOut = new StringBuffer( "<" + XML2Project.CLASSPATH );
        
        // Converting the classpath in a system independent way (more less)
        StringTokenizer st = new StringTokenizer(prj.getClasspath(), File.pathSeparator);
        StringBuffer sb = new StringBuffer();
        while (st.hasMoreTokens()) {
        	String s = st.nextToken();
        	if (File.pathSeparatorChar == ';') // If windows
        		s = s.replace(':', '?'); // replacing driver letter d: by d? for instance.
        	
			sb.append(s + " ");
        }
        
    	xmlOut.append( " path=\"" + sb.toString().trim()  + "\"/>" );
    	nl();
    	emit( xmlOut.toString() );

        // Saving the JUNIT_SRC_DIR and its attributes
        xmlOut = new StringBuffer( "<" + XML2Project.JUNIT_SRC_DIR );
    	xmlOut.append( " dir=\"" + prj.getJunitSrcDir()  + "\"/>" );
    	nl();
    	emit( xmlOut.toString() );

        // Saving the JUNIT_BIN_DIR and its attributes
        xmlOut = new StringBuffer( "<" + XML2Project.JUNIT_BIN_DIR );
    	xmlOut.append( " dir=\"" + prj.getJunitBinDir()  + "\"/>" );
    	nl();
    	emit( xmlOut.toString() );

        // Saving the JUNIT_TEST_SET and its attributes
        xmlOut = new StringBuffer( "<" + XML2Project.JUNIT_TEST_SET );
    	xmlOut.append( " name=\"" + prj.getJunitTestSet()  + "\"/>" );
    	nl();
    	emit( xmlOut.toString() );

        // Saving the JABUTI_BIN and its attributes
        xmlOut = new StringBuffer( "<" + XML2Project.JUNIT_JAR );
    	xmlOut.append( " name=\"" + prj.getJUnitJar()  + "\"/>" );
    	nl();
    	emit( xmlOut.toString() );
    	
        // Saving the AVOIDED_PACKAGES and its attributes
    	nl();
    	emit( "<" + XML2Project.AVOIDED_PACKAGES + ">" );

			// Saving the individual PACKAGE and its attributes
	        indentLevel++;
	
			Iterator it = prj.getAvoid().iterator();
			while( it.hasNext() ) {
				xmlOut = new StringBuffer( "<" + XML2Project.PACKAGE );
				xmlOut.append( " name=\"" + (String) it.next() + "\"/>" );
	    		nl();
		    	emit( xmlOut.toString() );
			}
	        indentLevel--;
    	nl();
    	emit( "</" + XML2Project.AVOIDED_PACKAGES + ">" );

		// Saving CLASS and INST_CLASS tags
		Program pg = prj.getProgram();
		String[] cnames = pg.getCodeClasses();
		for ( int i = 0; i < cnames.length; i++ ) {
			ClassFile cf = prj.getClassFile( cnames[i] );
		
			xmlOut = new StringBuffer( "<" ); 

			String tag = null;
			String size = "0000";
			String check = "00000000";
			
			if ( cf != null ) { // INST_CLASS tag
				tag = XML2Project.INST_CLASS;
				JavaClass theClass = cf.getJavaClass();
				size = new Integer( 
						theClass.getBytes().length ).toString();
				check = theClass.getMajor() + "-" + 
						theClass.getMinor();
				
			}
	        else { // CLASS tag
				tag = XML2Project.CLASS;
			}

			// CLASS commom attributes
			xmlOut.append( tag + " " );			
			xmlOut.append( " name=\"" + cnames[i] + "\"" );			
			xmlOut.append( " size=\"" + size + "\"" );
			xmlOut.append( " checksum=\"" + check + "\">" );

			nl();
			emit( xmlOut.toString() );
			
			indentLevel++;

			// EXTEND attributes
			RClassCode cc = (RClassCode) pg.get( cnames[i] );
			String superC = cc.getSuperClass();
			int level = pg.levelOf( cnames[i] );

			xmlOut = new StringBuffer( "<" + XML2Project.EXTEND );
			xmlOut.append( " name=\"" + superC + "\"" );
			xmlOut.append( " level=\"" + new Integer( level ).toString() + "\"/>" );
			nl();
			emit( xmlOut.toString() );

			// IMPLEMENT
			String[] interfaces = cc.getInterfaces();				
			for ( int k = 0; k < interfaces.length; k++ ) {
				xmlOut = new StringBuffer( "<" + XML2Project.IMPLEMENT );
				xmlOut.append( " name=\"" + interfaces[k] + "\"/>" );
				nl();
				emit( xmlOut.toString() );
			}
			
			if ( cf != null )
				Project2XML.classFile2XML( prj, cf );

			// Closing the correct tag			
			indentLevel--;
			nl();
			emit( "</" + tag + ">" );
		}

		// The TEST_SET element and its attributes
	
	    Project2XML.testSet2XML();

		indentLevel--;
		// Closing PROJECT
		nl();
		emit( "</"+ XML2Project.PROJECT + ">" );

		
		indentLevel--;
		// Closing JABUTI
		nl();
		emit( "</"+ XML2Project.JABUTI + ">" );

		try {
			out.flush();
		} catch (IOException ioe) {
			ToolConstants.reportException(ioe, ToolConstants.STDERR );
			return false;
		}

		try {
			out.close();
		} catch (IOException ioe) {
			ToolConstants.reportException(ioe, ToolConstants.STDERR );
			return false;
		}
				
		return true;
    }
    
    private static void classFile2XML( JabutiProject prj, ClassFile cf ) {
		// SOURCE tag and attribute
		ClassSourceFile cfs = cf.getSourceFile();
		String sn = new String( "" );
		if ( cfs.exists() )
			sn = cfs.getSourceName();
			
		StringBuffer xmlOut = new StringBuffer( "<" + XML2Project.SOURCE );
		xmlOut.append( " name=\"" + sn + "\"/>" );
		nl();
		emit( xmlOut.toString() );
		
		// Calling the createXMLDoc of each method
		String[] mnames = cf.getAllMethodsNames();
		if ( mnames != null ) {
			for ( int i = 0; i < mnames.length; i++ ) {
				ClassMethod cm = cf.getMethod( mnames[i] );
				Project2XML.classMethod2XML( cm );
			}
		}else {
			System.out.println("Class " + cf.getClassName() + " has no method" );
		}
		
    }
    
    private static void classMethod2XML( ClassMethod cm ) {
		// The METHOD tag and attributes
		StringBuffer xmlOut = new StringBuffer( "<" + XML2Project.METHOD );
		xmlOut.append( " id=\"" + 
						new Integer( cm.getMethodId() ).toString() + 
						"\"" );		
		xmlOut.append( " name=\"" + 
						HTMLGen.convertStr( cm.getMethodName() ) + 
						"\">" );
		nl();
		emit( xmlOut.toString() );

		indentLevel++;		
		// Getting the testig requirements information for each testing criterion
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {

			switch(i) {
				case Criterion.PRIMARY_NODES:
					Project2XML.allNodes2XML( cm, Criterion.PRIMARY_NODES );
					break;
				case Criterion.SECONDARY_NODES:
					Project2XML.allNodes2XML( cm, Criterion.SECONDARY_NODES );
					break;
				case Criterion.PRIMARY_EDGES:
					Project2XML.allEdges2XML( cm, Criterion.PRIMARY_EDGES );
					break;
				case Criterion.SECONDARY_EDGES: 
					Project2XML.allEdges2XML( cm, Criterion.SECONDARY_EDGES );
					break;
				case Criterion.PRIMARY_USES: 
					Project2XML.allUses2XML( cm, Criterion.PRIMARY_USES );
					break;
				case Criterion.SECONDARY_USES:
					Project2XML.allUses2XML( cm, Criterion.SECONDARY_USES );
					break;
				case Criterion.PRIMARY_POT_USES: 
					Project2XML.allUses2XML( cm, Criterion.PRIMARY_POT_USES );
					break;
				case Criterion.SECONDARY_POT_USES:
					Project2XML.allUses2XML( cm, Criterion.SECONDARY_POT_USES );
					break;
			}
		}
		indentLevel--;
		nl();
		emit( "</" + XML2Project.METHOD + ">" );
    }
    
    private static void allNodes2XML(ClassMethod cm, int c ) {
    							 	 	
    	String tag = AbstractCriterion.getName( c );

		if ( tag == null )
			return;
    	
		Criterion criterion = cm.getCriterion( c );
		
		StringBuffer xmlOut = new StringBuffer( "<" + tag );
		xmlOut.append( " id=\"" + 
						new Integer( c ).toString() + 
						"\"" );		
		xmlOut.append( " totreq=\"" + 
						new Integer( criterion.getNumberOfRequirements() ).toString() + 
						"\"" );		
		xmlOut.append( " act=\"" + 
						new Integer( criterion.getNumberOfPossibleRequirements() ).toString() + 
						"\"" );		
		xmlOut.append( " inf=\"" + 
						new Integer( criterion.getInfeasibleRequirements().size() ).toString() + 
						"\">" );		
		nl();
		emit( xmlOut.toString() );

		indentLevel++;
		Object[] requirements = criterion.getRequirements();
		for ( int k = 0; k < requirements.length; k++ ) {
			Requirement req = (Requirement) requirements[k];
			
			// Adding one more requirement and their attributes
			xmlOut = new StringBuffer( "<" + XML2Project.NODE );
			xmlOut.append( " id=\"" + 
							new Integer( k ).toString() + 
							"\"" );		
			xmlOut.append( " label=\"" + 
							req.toString() + 
							"\"" );
							
			// Checking if the requirement is active						
			String ans = "N";
			if ( criterion.isActive( req ) ) 
				ans = "Y";
			xmlOut.append( " active=\"" + ans + "\"" );		

			// Checking if the requirement is covered
			ans = "N";
			if ( criterion.isCovered( req ) ) 
				ans = "Y";
			xmlOut.append( " covered=\"" + ans + "\"" );

			// Checking if the requirement is infeasible
			ans = "Y";
			if ( criterion.isFeasible( req ) ) 
				ans = "N";
			xmlOut.append( " infeasible=\"" + ans + "\"" );
			
			//Printing the test case labels
			xmlOut.append( " effectivetcs=\"" 
				+ criterion.getEffectiveTestCases( req ) 
				+ "\"/>" );
			
			nl();
			emit( xmlOut.toString() );
		}
		indentLevel--;
		nl();
		emit( "</" + tag + ">" );
    }
    
    private static void allEdges2XML( ClassMethod cm, int c ) {
    	String tag = AbstractCriterion.getName( c );

		if ( tag == null )
			return;

		Criterion criterion = cm.getCriterion( c );
		
		StringBuffer xmlOut = new StringBuffer( "<" + tag );
		xmlOut.append( " id=\"" + 
						new Integer( c ).toString() + 
						"\"" );		
		xmlOut.append( " totreq=\"" + 
						new Integer( criterion.getNumberOfRequirements() ).toString() + 
						"\"" );		
		xmlOut.append( " act=\"" + 
						new Integer( criterion.getNumberOfPossibleRequirements() ).toString() + 
						"\"" );		
		xmlOut.append( " inf=\"" + 
						new Integer( criterion.getInfeasibleRequirements().size() ).toString() + 
						"\">" );		
		nl();
		emit( xmlOut.toString() );

		indentLevel++;
		Object[] requirements = criterion.getRequirements();
		for ( int k = 0; k < requirements.length; k++ ) {
			Requirement req = (Requirement) requirements[k];
			// Adding one more requirement and their attributes
			xmlOut = new StringBuffer( "<" + XML2Project.EDGE );
			xmlOut.append( " id=\"" + 
							new Integer( k ).toString() + 
							"\"" );		
			xmlOut.append( " from=\"" + 
							((Edge) req).getFrom() + 
							"\"" );
							
			xmlOut.append( " to=\"" + 
							((Edge) req).getTo() + 
							"\"" );
							
			// Checking if the requirement is active						
			String ans = "N";
			if ( criterion.isActive( req ) ) 
				ans = "Y";
			xmlOut.append( " active=\"" + ans + "\"" );		

			// Checking if the requirement is covered
			ans = "N";
			if ( criterion.isCovered( req ) ) 
				ans = "Y";
			xmlOut.append( " covered=\"" + ans + "\"" );

			// Checking if the requirement is infeasible
			ans = "Y";
			if ( criterion.isFeasible( req ) ) 
				ans = "N";
			xmlOut.append( " infeasible=\"" + ans + "\"" );
			
			//Printing the test case labels
			xmlOut.append( " effectivetcs=\"" 
				+ criterion.getEffectiveTestCases( req ) 
				+ "\"/>" );

			nl();
			emit( xmlOut.toString() );
		}
		indentLevel--;
		nl();
		emit( "</" + tag + ">" );
    }
    
    private static void allUses2XML( ClassMethod cm, int c ) {
    	String tag = AbstractCriterion.getName( c );

		if ( tag == null )
			return;

		Criterion criterion = cm.getCriterion( c );
		StringBuffer xmlOut = new StringBuffer( "<" + tag );
		xmlOut.append( " id=\"" + 
						new Integer( c ).toString() + 
						"\"" );		
		xmlOut.append( " totreq=\"" + 
						new Integer( criterion.getNumberOfRequirements() ).toString() + 
						"\"" );		
		xmlOut.append( " act=\"" + 
						new Integer( criterion.getNumberOfPossibleRequirements() ).toString() + 
						"\"" );		
		xmlOut.append( " inf=\"" + 
						new Integer( criterion.getInfeasibleRequirements().size() ).toString() + 
						"\">" );		
		nl();
		emit( xmlOut.toString() );

		indentLevel++;
		Object[] requirements = criterion.getRequirements();
		for ( int k = 0; k < requirements.length; k++ ) {
			Requirement req = (Requirement) requirements[k]; 
			DefUse du = (DefUse) req;
			
			xmlOut = new StringBuffer( "<" );

			if ( du.getUseTo() == null ) {
				xmlOut.append( XML2Project.C_USE );
				
				xmlOut.append( " id=\"" + 
							new Integer( k ).toString() + 
							"\"" );		
				xmlOut.append( " var=\"" + 
							du.getVar() + 
							"\"" );		
				xmlOut.append( " def=\"" + 
							du.getDef() + 
							"\"" );		
				xmlOut.append( " use=\"" + 
							du.getUseFrom() + 
							"\"" );		
			} else {
				xmlOut.append( XML2Project.P_USE );
				
				xmlOut.append( " id=\"" + 
							new Integer( k ).toString() + 
							"\"" );		
				xmlOut.append( " var=\"" + 
							du.getVar() + 
							"\"" );		
				xmlOut.append( " def=\"" + 
							du.getDef() + 
							"\"" );		
				xmlOut.append( " from=\"" + 
							du.getUseFrom() + 
							"\"" );		
				xmlOut.append( " to=\"" + 
							du.getUseTo() + 
							"\"" );		
			}

			// Checking if the requirement is active						
			String ans = "N";
			if ( criterion.isActive( req ) ) 
				ans = "Y";
			xmlOut.append( " active=\"" + ans + "\"" );		

			// Checking if the requirement is covered
			ans = "N";
			if ( criterion.isCovered( req ) ) 
				ans = "Y";
			xmlOut.append( " covered=\"" + ans + "\"" );

			// Checking if the requirement is infeasible
			ans = "Y";
			if ( criterion.isFeasible( req ) ) 
				ans = "N";
			xmlOut.append( " infeasible=\"" + ans + "\"" );
			
			//Printing the test case labels
			xmlOut.append( " effectivetcs=\"" 
				+ criterion.getEffectiveTestCases( req ) 
				+ "\"/>" );

			nl();
			emit( xmlOut.toString() );
		}
		indentLevel--;
		nl();
		emit( "</" + tag + ">" );
    }    

	/*
	 * This method is responsible to persiste the information of the 
	 * complete test set. Observe that the test cases marked to be 
	 * removed are not saved.
	 */    
    private static void testSet2XML( ) {
    	// Currently saving all test cases.
    	// Later it is necessary to check if some test cases
    	// need to be remove
    	
		// The TEST_SET element and its attributes
		StringBuffer xmlOut = new StringBuffer( "<" + XML2Project.TEST_SET );
		
		/*
		xmlOut.append( " last_id=\"0\">" );
		nl();
		emit( xmlOut.toString() );
		*/
		xmlOut.append( " last_id=\"" + 
			new Integer( TestSet.getTestCaseId() ).toString() + "\">" );
		nl();
		emit( xmlOut.toString() );
    	
    	indentLevel++;
    	String[] tcs = TestSet.getTestCaseLabels();
    	for( int i = 0; i < tcs.length; i++ ) {
    		// Checking if is to remove the test case
    		if ( !TestSet.isDeleted( tcs[i] ) ) {
    			TestCase tc = TestSet.getTestCase( tcs[i] );
    			if ( tc != null ) {
			        Project2XML.testCase2XML( tc );
			    }
		    }
    	}
    	indentLevel--;
    	
		// Closing TEST_SET
		nl();
		emit( "</"+ XML2Project.TEST_SET + ">" );
    }

    private static void testCase2XML( TestCase tc ) {
		// The TEST_SET element and its attributes
		StringBuffer xmlOut = new StringBuffer( "<" + XML2Project.TEST_CASE );
		xmlOut.append( " label=\"" + tc.getLabel() + "\"" );
		xmlOut.append( " alias=\"" + tc.getAlias() + "\"" );    	
		xmlOut.append( " host=\"" + tc.getHostName() + "\"" );
		
		// Checking if the test case is active						
		String ans = "N";
		if ( TestSet.isActive( tc.getLabel() ) ) 
			ans = "Y";
		xmlOut.append( " active=\"" + ans + "\"" );
    	
		// Checking if the test case is success. An U represents undefined
		ans = "U";
		if ( TestSet.isSuccess( tc.getLabel() ) ) 
			ans = "Y";
		xmlOut.append( " success=\"" + ans + "\"" );		

		// Checking if the test case is fail. An U represents undefined
		ans = "U";
		if ( TestSet.isFail( tc.getLabel() ) ) 
			ans = "Y";
		xmlOut.append( " fail=\"" + ans + "\">" );		

		nl();
		emit( xmlOut.toString() );

		//indentLevel--;
		//nl();
		emit( "</" + XML2Project.TEST_CASE + ">"  );
	}

    //===========================================================
    // Utility Methods ...
    //===========================================================

    // Wrap I/O exceptions in SAX exceptions, to
    // suit handler signature requirements
    static private void emit(String s)
    {
        try {
            out.write(s);
            //out.flush();
        } catch (IOException e) {
            ToolConstants.reportException( e, ToolConstants.STDERR );
        }
    }

    // Start a new line
    // and indent the next line appropriately
    static private void nl()
    {
        String lineEnd =  System.getProperty("line.separator");
        try {
            out.write(lineEnd);
            for (int i=0; i < indentLevel; i++) out.write(indentString);
        } catch (IOException e) {
        	ToolConstants.reportException( e, ToolConstants.STDERR );
        }
    }
}
