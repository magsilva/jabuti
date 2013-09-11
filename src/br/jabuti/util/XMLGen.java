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


package br.jabuti.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import br.jabuti.criteria.AbstractCriterion;
import br.jabuti.criteria.AllPotUses;
import br.jabuti.criteria.AllUses;
import br.jabuti.criteria.Criterion;
import br.jabuti.criteria.Requirement;
import br.jabuti.lookup.Program;
import br.jabuti.lookup.RClassCode;
import br.jabuti.project.ClassFile;
import br.jabuti.project.ClassMethod;
import br.jabuti.project.ClassSourceFile;
import br.jabuti.project.JabutiProject;
import br.jabuti.project.TestCase;
import br.jabuti.project.TestSet;

public class XMLGen {
	private static Document xmlDoc;
	
	private XMLGen() {
		try {
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
		        
	        xmlDoc = db.newDocument();
	    } catch (ParserConfigurationException pce) {
	    	ToolConstants.reportException( pce, ToolConstants.STDERR);
	    }
	}
	
	/**
	 * This method creates a new DOM document
	 */
	public static void restart() {
		xmlDoc = null;
		new XMLGen();
	}
	
	public static Document customReport( JabutiProject prj,
									   boolean prj2XML, // project info
									   boolean class2XML, // class info
									   boolean method2XML, // method info
									   boolean ts2XML, // test set info
									   boolean tc2XML // test case info
									  ) {
		
		Element body = XMLGen.getDocumentHead();
		
		Document out = null;
		
		if ( prj2XML )
			out = XMLGen.projectXMLReport( prj, body );

		if ( class2XML ) {
			// Traversing each class, collecting coverage information		
			String[] classList = prj.getAllClassFileNames();
			Element classes = xmlDoc.createElement("classes");
			body.appendChild(classes);
			
			for ( int i = 0; i < classList.length; i++ ) {
							
				ClassFile cf = prj.getClassFile( classList[i] );
				out = XMLGen.classFileXMLReport( prj, cf, classes );
				
				if ( method2XML ) {
//					 Traversing each class method, collecting coverage information		
					String[] methodList = cf.getAllMethodsNames();
					Element methods = xmlDoc.createElement("methods");
					body.appendChild(methods);
					
					if ( methodList != null ) {
						for( int j = 0; j < methodList.length; j++ ) {
							ClassMethod cm = cf.getMethod( methodList[j] );
							out = XMLGen.classMethodXMLReport( cf, cm, methods );
						}
					}
			 	}
			}
		}
		
		if ( ts2XML )
			out = testSetXMLReport( body, tc2XML );
		
		return out;
	}
	
	/**
	 * This method returns the BODY element of an XML document.
	 * It always restart the current XML document.
	 *
	 * @param title - an String representing the title of the
	 *                report.
	 * @return body XML element.
	 */
	public static Element getDocumentHead() {
		restart();
		
		//ProcessingInstruction pi = xmlDoc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\"");
		
        // Top Level ELEMENT
        Element root = xmlDoc.createElement("jbtreport");
        //xmlDoc.appendChild(pi);
        xmlDoc.appendChild( root );
        
        return root;
	}

	/**
	 * This method generates a summary report for a project
	 *
	 * @param ap - a {@link br.jabuti.project.ActiveProject} object
	 * @param body - the body Element of an XML documment.
	 *
	 * @return a DOM Document representing the generated XML file
	 */
    public static Document projectXMLReport( JabutiProject prj, Element body ) {
    	//project element and attributes
	    Element project = xmlDoc.createElement("project");

        project.setAttribute("name", prj.getProjectFileName());
        project.setAttribute("type", "research");
        project.setAttribute("mobility", new Boolean(prj.isMobility() ).toString());
        project.setAttribute("cfg_option",new Integer(prj.getCFGOption()).toString());
        
//		baseclass element and attributes
        Element base = xmlDoc.createElement("base_class");
        base.setAttribute("name", prj.getMain());
        project.appendChild(base);
        
//      classpath element and attributes
        Element classpath = xmlDoc.createElement("classpath");
        classpath.setAttribute("path", prj.getClasspath() );
        project.appendChild(classpath);
        
        body.appendChild(project);

//		avoided packages
        Element avpackages = xmlDoc.createElement("avoided_packages");
		Iterator it = prj.getAvoid().iterator();
		while( it.hasNext() ) {
			Element pkg = xmlDoc.createElement("package");
			pkg.setAttribute( "name", (String) it.next() );
			avpackages.appendChild(pkg);
		}
        
        project.appendChild(avpackages);


//		project coverage
        Element coverage = xmlDoc.createElement("coverage");
       
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {	        
	        Element criterion = xmlDoc.createElement(AbstractCriterion.getName(i));
	        criterion.setAttribute("covered", "" + prj.getProjectCoverage(i).getNumberOfCovered() );
	        criterion.setAttribute("required", "" + prj.getProjectCoverage(i).getNumberOfRequirements() );
	        criterion.setAttribute("percentage", "" + prj.getProjectCoverage(i).getPercentage() );
	        coverage.appendChild(criterion);
		}
		
		project.appendChild(coverage);
		
		return xmlDoc;
    }

/*
	*//**
	 * This method generates a summary report for a given class
	 *
	 * @param cf - a {@link br.jabuti.project.ClassFile} object
	 * @param body - the body Element of an XML documment.
	 *
	 * @return a DOM Document representing the generated XML file
	 */
    public static Document classFileXMLReport( JabutiProject prj, ClassFile cf, Element classes ) {
	    Element clazz = xmlDoc.createElement("class");
	    clazz.setAttribute("name", cf.getClassName() );

		// extended class
	    Program pg = prj.getProgram();
		RClassCode cc = (RClassCode) pg.get( cf.getClassName() );
		String superC = cc.getSuperClass();
		int level = pg.levelOf( cf.getClassName() );

		Element extendz = xmlDoc.createElement("extends");
		extendz.setAttribute("name", XMLGen.convertStr(superC) );
		extendz.setAttribute("level", new Integer( level ).toString() );
		
		clazz.appendChild(extendz);
		
		// implemented interfaces
		Element implementz = xmlDoc.createElement("implements");		

		String[] interfaces = cc.getInterfaces();				
		for ( int k = 0; k < interfaces.length; k++ ) {
			Element implement = xmlDoc.createElement("implement");
			implement.setAttribute("name", XMLGen.convertStr(interfaces[k]) );
			implementz.appendChild(implement);
		}	    
		clazz.appendChild(implementz);
		
		// source file
		ClassSourceFile cfs = cf.getSourceFile();
		String sn = new String( "" );
		if ( cfs.exists() )
			sn = cfs.getSourceName();

		Element src = xmlDoc.createElement("source");
		src.setAttribute("name", XMLGen.convertStr(sn) );
		
		clazz.appendChild(src);
	    
//		class coverage
        Element coverage = xmlDoc.createElement("coverage");
       
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {	        
	        Element criterion = xmlDoc.createElement(AbstractCriterion.getName(i));
	        criterion.setAttribute("covered", "" + cf.getClassFileCoverage(i).getNumberOfCovered() );
	        criterion.setAttribute("required", "" + cf.getClassFileCoverage(i).getNumberOfRequirements() );
	        criterion.setAttribute("percentage", "" + cf.getClassFileCoverage(i).getPercentage() );
	        coverage.appendChild(criterion);
		}
		
		clazz.appendChild(coverage);
		classes.appendChild(clazz);
		
		return xmlDoc;
    }


	/**
	 * This method generates a summary report for a given method
	 *
	 * @param cm - an {@link br.jabuti.project.ClassMethod} object
	 * @param body - the body Element of an XML documment.
	 *
	 * @return a DOM Document representing the generated XML file
	 */
    public static Document classMethodXMLReport( ClassFile cf, ClassMethod cm, Element methods ) {
	    Element method = xmlDoc.createElement("method");
	    method.setAttribute("class_name", XMLGen.convertStr(cf.getClassName()) );
	    method.setAttribute("name", XMLGen.convertStr(cm.getMethodName()) );
	    method.setAttribute("full_name", XMLGen.convertStr(cf.getClassName() + "." + cm.getMethodName()) );

//		class coverage
        Element coverage = xmlDoc.createElement("coverage");
       
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {	        
	        Element criterion = xmlDoc.createElement(AbstractCriterion.getName(i));
	        criterion.setAttribute("covered", "" + cm.getClassMethodCoverage(i).getNumberOfCovered() );
	        criterion.setAttribute("required", "" + cm.getClassMethodCoverage(i).getNumberOfRequirements() );
	        criterion.setAttribute("percentage", "" + cm.getClassMethodCoverage(i).getPercentage() );
	        coverage.appendChild(criterion);
		}
		
		method.appendChild(coverage);
		
		Element requirements = xmlDoc.createElement("requirements");
		method.appendChild(requirements);
		
		for( int k = 0; k < Criterion.NUM_CRITERIA; k++ ) {
			requirementsXMLReport( cm, requirements, k );
		}
		
		methods.appendChild(method);
		
		return xmlDoc;
    }


	/**
	 * This method generates a summary report for a given class
	 *
	 * @param cf - a {@link br.jabuti.project.ClassFile} object
	 * @param body - the body Element of an XML documment.
	 *
	 * @return a DOM Document representing the generated XML file
	 */
    public static Document testSetXMLReport( Element body, boolean tc2XML ) {
	    Element testSet = xmlDoc.createElement("test_set");
	    testSet.setAttribute("total", ""+ TestSet.getNumberOfTestCases());
	    body.appendChild(testSet);
			
//		class coverage
        Element coverage = xmlDoc.createElement("coverage");
       
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {	        
	        Element criterion = xmlDoc.createElement(AbstractCriterion.getName(i));
	        criterion.setAttribute("covered", "" + TestSet.getTestSetCoverage(i).getNumberOfCovered() );
	        criterion.setAttribute("required", "" + TestSet.getTestSetCoverage(i).getNumberOfRequirements() );
	        criterion.setAttribute("percentage", "" + TestSet.getTestSetCoverage(i).getPercentage() );
	        coverage.appendChild(criterion);
		}
	
		testSet.appendChild(coverage);
		
		if ( tc2XML ) {
			Element testCases = xmlDoc.createElement("test_cases");
			testSet.appendChild(testCases);
			// Report for each test case 
			Object[] testcases = TestSet.getTestCaseLabels();
			for ( int i = 0; i < testcases.length; i++ ) {
				TestCase tc = TestSet.getTestCase( testcases[i].toString() );

				testCaseXMLReport( tc, testCases );
			}
		}
	
		return xmlDoc;
    }


	/**
	 * This method generates a summary report for a test case
	 *
	 * @param tc - an {@link br.jabuti.project.TestCase} object
	 * @param body - the body Element of an XML documment.
	 *
	 * @return a DOM Document representing the generated XML file
	 */
    public static Document testCaseXMLReport( TestCase tc, Element testCases ) {
	    Element testCase = xmlDoc.createElement("test_case");
	    testCase.setAttribute("label", tc.getLabel());    	
	    testCase.setAttribute("host", tc.getHostName());

		// Checking if the test case is active						
		String ans = "N";
		if ( TestSet.isActive( tc.getLabel() ) ) 
			ans = "Y";
		testCase.setAttribute("active", ans);

// 		Checking if the test case is success. An U represents undefined
		ans = "U";
		if ( TestSet.isSuccess( tc.getLabel() ) ) 
			ans = "Y";
		testCase.setAttribute("success", ans);		

// 		Checking if the test case is fail. An U represents undefined
		ans = "U";
		if ( TestSet.isFail( tc.getLabel() ) ) 
			ans = "Y";
		testCase.setAttribute("fail", ans);	    

//		test cases coverage
        Element coverage = xmlDoc.createElement("coverage");
       
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {	        
	        Element criterion = xmlDoc.createElement(AbstractCriterion.getName(i));
	        criterion.setAttribute("covered", "" + TestSet.getTestSetCoverage(i).getNumberOfCovered() );
	        criterion.setAttribute("required", "" + TestSet.getTestSetCoverage(i).getNumberOfRequirements() );
	        criterion.setAttribute("percentage", "" + TestSet.getTestSetCoverage(i).getPercentage() );
	        coverage.appendChild(criterion);
		}
	
		testCase.appendChild(coverage);
		testCases.appendChild(testCase);
		
		return xmlDoc;
    }

	public static Document requirementsXMLReport( ClassMethod cm, Element requirements, int id ) {
	    Element criterion = xmlDoc.createElement(AbstractCriterion.getName(id));
        requirements.appendChild(criterion);
        
		Criterion theCriterion = cm.getCriterion( id );
		
		/*
		HashSet weights = new HashSet();
		if ((id == Criterion.PRIMARY_NODES) || (id == Criterion.SECONDARY_NODES) )
			weights = cm.getWeightByNode(id);
		else if ((id == Criterion.PRIMARY_EDGES) || (id == Criterion.SECONDARY_EDGES) )
			weights = cm.getWeightByEdge(id);
		else // data-flow criteria
			weights = cm.getWeightByUse(id);
		*/			
		XMLGen.possibleRequirementsXMLReport( criterion, theCriterion  );
		XMLGen.possibleCoveredXMLReport( criterion, theCriterion );
		XMLGen.infeasibleRequirementsXMLReport( criterion, theCriterion );
		XMLGen.inactiveRequirementsXMLReport( criterion, theCriterion );
		return xmlDoc;
	}

	/**
	 * This method generates a summary report containing the set
	 * of active, uncovered and feasible testing requirements, called
	 * possible requirements.
	 *
	 * @param criterion a {@link criteria.Criterion} object
	 *
	 * @return a DOM Document representing the generated XML file
	 */
    public static Document possibleRequirementsXMLReport( Element criterion, Criterion theCriterion ) {

	    Element uncovered = xmlDoc.createElement("uncovered");
		criterion.appendChild( uncovered );

		Requirement[] requirements = theCriterion.getPossibleRequirements();
		Arrays.sort( requirements );
		for ( int i = 0; i < requirements.length; i++ ) {
			Element requirement = xmlDoc.createElement( "requirement" );
			if ( !theCriterion.isCovered( requirements[i] ) ) {

				// Removing the < and > in case of associations								
				String req = requirements[i].toString();
				if ( ( theCriterion instanceof AllUses ) || 
				     ( theCriterion instanceof AllPotUses ) ) {
					req = convertStr( req );
				}

				requirement.appendChild( xmlDoc.createTextNode( req ) );
				requirement.setAttribute("weight", "0");
				
				uncovered.appendChild( requirement );
			}
		}
		
		return xmlDoc;
    }


	/**
	 * This method generates a summary report containing the set
	 * of covered testing requirements.
	 *
	 * @param criterion a {@link criteria.Criterion} object
	 *
	 * @return a DOM Document representing the generated XML file
	 */
    public static Document possibleCoveredXMLReport( Element criterion, Criterion theCriterion ) {
	    Element covered = xmlDoc.createElement("covered");
		criterion.appendChild( covered );

		HashSet set = theCriterion.getPossibleCoveredRequirements();

		if ( set.size() > 0 ) {
			Requirement[] requirements = new Requirement[ set.size() ];
			int i = 0;
			Iterator it = set.iterator();
			while( it.hasNext() ) {
				requirements[i++] = (Requirement) it.next();
			}
			
			Arrays.sort( requirements );
			
			for ( i = 0; i < requirements.length; i++ ) {
				Element requirement = xmlDoc.createElement( "requirement" );

				// Removing the < and > in case of associations				
				String req = requirements[i].toString();
				if ( ( theCriterion instanceof AllUses ) || 
				     ( theCriterion instanceof AllPotUses ) ) {
					req = convertStr( req );
				}
				requirement.appendChild( xmlDoc.createTextNode( req ) );
				requirement.setAttribute("effectivetcs", 
						theCriterion.getEffectiveTestCases( requirements[i] ));
				
				covered.appendChild( requirement );
			}
		}

		return xmlDoc;
    }


	/**
	 * This method generates a summary report containing the set
	 * of infeasible testing requirements.
	 *
	 * @param criterion a {@link criteria.Criterion} object
	 *
	 * @return a DOM Document representing the generated XML file
	 */
    public static Document infeasibleRequirementsXMLReport( Element criterion, Criterion theCriterion ) {
        Element infeasible = xmlDoc.createElement("infeasible");
		criterion.appendChild( infeasible );

		HashSet set = theCriterion.getInfeasibleRequirements();
		
		if ( set.size() > 0 ) {
			Object[] requirements = new Object[ set.size() ];
			int i = 0;
			Iterator it = set.iterator();
			while( it.hasNext() ) {
				requirements[i++] = it.next();
			}
			
			Arrays.sort( requirements );
			
			for ( i = 0; i < requirements.length; i++ ) {
				Element requirement = xmlDoc.createElement( "requirement" );

				// Removing the < and > in case of associations				
				String req = requirements[i].toString();
				if ( ( theCriterion instanceof AllUses ) || 
				     ( theCriterion instanceof AllPotUses ) ) {
					req = convertStr( req );
				}

				requirement.appendChild( xmlDoc.createTextNode( req ) );				
				infeasible.appendChild( requirement );
			}
		}

		return xmlDoc;
    }


	/**
	 * This method generates a summary report containing the set
	 * of infeasible testing requirements.
	 *
	 * @param criterion a {@link criteria.Criterion} object
	 *
	 * @return a DOM Document representing the generated XML file
	 */
    public static Document inactiveRequirementsXMLReport( Element criterion, Criterion theCriterion ) {
        Element inactive = xmlDoc.createElement("inactive");
		criterion.appendChild( inactive );

		HashSet set = theCriterion.getInactiveRequirements();
		
		if ( set.size() > 0 ) {
			Requirement[] requirements = new Requirement[ set.size() ];
			int i = 0;
			Iterator it = set.iterator();
			while( it.hasNext() ) {
				requirements[i++] = (Requirement) it.next();
			}
			
			Arrays.sort( requirements );
			
			for ( i = 0; i < requirements.length; i++ ) {
				Element requirement = xmlDoc.createElement( "requirement" );
			
				// Removing the < and > in case of associations				
				String req = requirements[i].toString();
				if ( ( theCriterion instanceof AllUses ) || 
				     ( theCriterion instanceof AllPotUses ) ) {
					req = convertStr( req );
				}
				requirement.appendChild( xmlDoc.createTextNode( req ) );
				requirement.setAttribute("effectivetcs", 
						theCriterion.getEffectiveTestCases( requirements[i] ));
				
				inactive.appendChild( requirement );
			}
		}
		
		return xmlDoc;
    }

	/**
	 * This method converts a JTable to a XML table.
	 *
	 * @param body - a given body XML Element
	 * @param caption - the table caption
	 * @param table   - a JTable object
	 *
	 * @return a DOM Document representing the generated XML file
	 *//*
    public static Document jtable2XML( Element body, String caption, JTable table ) {
	
		if ( caption != null ) {
		    Element h3 = xmlDoc.createElement("H3");
			h3.appendChild( xmlDoc.createTextNode( caption ) );
			body.appendChild( h3 );
		}

		// Recovering the column column's names
		int nCols = table.getColumnCount();
		String[] header = null;
		if ( nCols > 0 ) {
			header = new String[nCols];
			for ( int i = 0; i < nCols; i++ ) {
				header[i] = table.getColumnName( i );
			}
		}

		int nRows = table.getRowCount();
		String[][] rows = new String[nRows][nCols];
		for ( int i = 0; i < nRows; i++ ) {
			for( int j = 0; j < nCols; j++ ) {
				Object obj = table.getValueAt(i,j);
				String text = new String();
				if ( obj instanceof JButton ) {
					text = ((JButton) obj).getText();
				} else if ( obj instanceof JTextField ) {
					text = ((JTextField) obj).getText();
				} else if ( obj instanceof JProgressBar ) {
					text = ((JProgressBar) obj).getString();
				} else if ( obj instanceof JPanel ) {
					text = ((JProgressBar)((JPanel) obj).getComponent(0)).getString();
				} else {
					text = table.getValueAt(i,j).toString();
					try{
						double num = Double.parseDouble( text );
						DecimalFormat formatter = new DecimalFormat();
						formatter.setMaximumFractionDigits( 3 );
						//formatter.setDecimalSeparatorAlwaysShown(true);
						text = formatter.format(num);
					} catch (NumberFormatException nfe) {}
				}
				
				rows[i][j] = XMLGen.convertStr( text );
			}
		}		

		body.appendChild( XMLGen.createTable(xmlDoc, caption, header, rows, 1 ) );

		return xmlDoc;
    }


    *//**
     * This method is responsible to create a table in a XML format.
     *
     * @param header is the table header and is printed in bold;
     * @param rows is the table data. The first element of each row is printed in bold;
     * @param border is the border size.
     * @return a Element that represents the generated table.
     *//*
    private static Element createTable( Document xmlDoc, String caption, Object[] header, Object[][] rows, int border ) {
		Element table = xmlDoc.createElement("TABLE");
		
		table.setAttribute( "BORDER", new Integer( border ).toString() );
		
		if ( caption != null ) {
			Element cp = xmlDoc.createElement("CAPTION");
			table.appendChild( cp );
			
		    Element h4 = xmlDoc.createElement("H4");
	        cp.appendChild(h4);
	        h4.appendChild( xmlDoc.createTextNode( caption ) );
		}
		
		// Inserting the table header, if any
		if ( header != null ) {
			Element tr = xmlDoc.createElement("TR");
			table.appendChild( tr );
			
			for ( int i = 0; i < header.length; i++ ) {
				// All header elements in bold
				Element th = xmlDoc.createElement("TH");
				tr.appendChild( th );
				th.setAttribute( "ALIGN", "center" );
				th.setAttribute( "VALIGN", "center" );
				th.appendChild( xmlDoc.createTextNode(header[i].toString() ) );
			}
		}
		
		// Inserting the table data, if any
		if ( table != null ) {
			for ( int i = 0; i < rows.length; i++ ) {
				Element tr = xmlDoc.createElement("TR");
				table.appendChild( tr );
				
				Element th = xmlDoc.createElement("TH");
				tr.appendChild( th );
				th.setAttribute( "ALIGN", "left" );
				th.setAttribute( "VALIGN", "center" );
				
				// First element of each row in bold
				th.appendChild( xmlDoc.createTextNode( rows[i][0].toString() ) );
				
				for ( int j = 1; j < rows[i].length; j++ ) {
					Element td = xmlDoc.createElement("TD");
					tr.appendChild( td );
					td.setAttribute( "ALIGN", "right" );
					td.setAttribute( "VALIGN", "center" );
					
					td.appendChild( xmlDoc.createTextNode(rows[i][j].toString()));
				}
			}
		}
		
    	return table;
    }
    
     */
     public static String convertStr( String str ) {
     	String result = new String();
     	
     	for( int i = 0; i < str.length(); i++ ) {
     		char c = str.charAt( i );
     		if ( c == '<' ) 
     			result += "&lt;";
     		else if ( c == '>' ) 
     			result += "&gt;";
     		else
     			result += c;
     	}
     	return result;
     }
     
	/**
	 * This method generates the Requirements Summary Report.
	 * It is assumed that the current summary report presented by
	 * JaBUTi is the Testing Requirements Summary Report.
	 *//*
	public static Document requirements2XML( String title ) {
		JabutiProject prj = JabutiGUI.getProject();
		Element body = XMLGen.getBody( prj.getProjectFileName() ); 

	    Element h1 = xmlDoc.createElement("H1");
		h1.appendChild( xmlDoc.createTextNode( prj.getProjectFileName() ) );
		body.appendChild( h1 );
	    
	    
	    Element h3 = xmlDoc.createElement("H3");
		h3.appendChild( xmlDoc.createTextNode( title ) );
		body.appendChild( h3 );
		
		XMLGen.addSeparator( body, 4, 80 );
		
		// Name of the current class
	    Element h4 = xmlDoc.createElement("H4");
		h4.appendChild( xmlDoc.createTextNode( "Current Class: " + prj.getCurClassName() ) );
		body.appendChild( h4 );
		
		// Name of the current method
		h4 = xmlDoc.createElement("H4");
		h4.appendChild( xmlDoc.createTextNode( "Current Method: " + prj.getCurMethodName() ) );
		body.appendChild( h4 );
		
		body.appendChild( xmlDoc.createElement("BR") );
		
		XMLGen.jtable2XML( body, null, TableSorterPanel.getTable() );		
		
		XMLGen.addSeparator( body, 4, 80 );		
		
		ClassFile cf = prj.getClassFile( prj.getCurClassName() );
		ClassMethod cm = cf.getMethod( prj.getCurMethodName() );

		//String[] header = {"Criterion", "Number Of Covered Requirements", "Percentage"};
		
		// Finding the current criterion
		int c = JabutiGUI.mainWindow().getActiveCriterionId();
		
		// Method and Total Coverage
		String[][] tab = { 
			{"Method Coverage", 
			 cm.getClassMethodCoverage(c).toString(),
			 new Float( cm.getClassMethodCoverage(c).getPercentage() ).toString() },
			{"Total Coverage",
			 prj.getProjectCoverage(c).toString(),
			 new Float( prj.getProjectCoverage(c).getPercentage() ).toString()} };
		
		body.appendChild( XMLGen.createTable(xmlDoc, null, null, tab, 1 ) );
		
		return xmlDoc;
	}

	/**
	 * This method generates the Complexity Metrics Summary Report.
	 * It is assumed that the current summary report presented by
	 * JaBUTi is the Metrics Summary Report.
	 *//*
	public static Document metrics2XML( String title ) {
		JabutiProject prj = JabutiGUI.getProject();
		Element body = XMLGen.getBody( prj.getProjectFileName() ); 
	 
	    Element h1 = xmlDoc.createElement("H1");
		h1.appendChild( xmlDoc.createTextNode( prj.getProjectFileName() ) );
		body.appendChild( h1 );
	    
	    Element h3 = xmlDoc.createElement("H3");
		h3.appendChild( xmlDoc.createTextNode( title ) );
		body.appendChild( h3 );
		
		XMLGen.addSeparator( body, 4, 80 );
		
		XMLGen.jtable2XML( body, null, TableSorterPanel.getTable() );		
		
		XMLGen.addSeparator( body, 4, 80 );		
		
		// Finding the current criterion
		int c = JabutiGUI.mainWindow().getActiveCriterionId();
		
		// Method and Total Coverage
		String[][] tab = { 
			{"Number of Metrics", new Integer( Metrics.metrics.length ).toString() },
			{"Number of Classes", new Integer( prj.getAllClassFileNames().length ).toString()} };
		
		body.appendChild( XMLGen.createTable(xmlDoc, null, null, tab, 1 ) );
		
		return xmlDoc;
	}

	*//**
	 * This method generates the a Summary Report for all
	 * others reports provided by JaBUTi graphical interface.
	 *//*
	public static Document overallCoverage2XML( String title ) {
		JabutiProject prj = JabutiGUI.getProject();
		
		Element body = XMLGen.getBody( prj.getProjectFileName() );
		
	    Element h1 = xmlDoc.createElement("H1");
		h1.appendChild( xmlDoc.createTextNode( prj.getProjectFileName() ) );
		body.appendChild( h1 );
	    
	    Element h3 = xmlDoc.createElement("H3");
		h3.appendChild( xmlDoc.createTextNode( title ) );
		body.appendChild( h3 );
		
		XMLGen.addSeparator( body, 4, 80 );
		
		XMLGen.jtable2XML( body, null, TableSorterPanel.getTable() );		
		
		XMLGen.addSeparator( body, 4, 80 );		
		
		// Finding the current criterion
		int c = JabutiGUI.mainWindow().getActiveCriterionId();
		
		// Method and Total Coverage
		String[][] tab = { 
			{"Total Coverage",
			 prj.getProjectCoverage(c).toString(),
			 new Float( prj.getProjectCoverage(c).getPercentage() ).toString()} };
		
		body.appendChild( XMLGen.createTable(xmlDoc, null, null, tab, 1 ) );


		XMLGen.addSeparator( body, 4, 80 );		
		
		// Method and Total Coverage
		tab = new String[][] { 
			{"Tool", JabutiGUI.mainWindow().getActiveToolName() },
			{"Active Test Cases", 
				TestSet.getNumberOfActiveTestCases() + 
				" of " + 
				TestSet.getNumberOfTestCases() } };
		
		body.appendChild( XMLGen.createTable(xmlDoc, null, null, tab, 1 ) );
		
		return xmlDoc;
	}*/
    
    public static void main(String[] args) {
        // Setting the text field with the server configuration info
        String fileName = "relat.xml";
        
        if ( fileName.length() > 0 ) {
	        if ( !fileName.endsWith( ".xml" ) ) {
	        	fileName = fileName + ".xml";
	        }    	
        }
        
        Document htmlDoc = null;
    	try{
            JabutiProject prj = JabutiProject.reloadProj( args[0], true );

            htmlDoc = XMLGen.customReport(prj, true, true, true, true, true);
    		
			XMLPrettyPrinter.writeDocument( htmlDoc, 
											fileName );
    	} catch ( Exception pce ) {
    		ToolConstants.reportException( pce, ToolConstants.STDERR);
            JOptionPane.showMessageDialog(null,
                    "Error saving the file " + fileName + "!!!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
    	} finally {
			htmlDoc = null;
			XMLGen.restart();
    	}
    }
}