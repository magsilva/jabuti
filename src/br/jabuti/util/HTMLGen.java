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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import br.jabuti.gui.JabutiGUI;
import br.jabuti.gui.TableSorterPanel;
import br.jabuti.metrics.Metrics;
import br.jabuti.project.ClassFile;
import br.jabuti.project.ClassMethod;
import br.jabuti.project.JabutiProject;
import br.jabuti.project.TestCase;
import br.jabuti.project.TestSet;

public class HTMLGen {
	private static Document htmlDoc;
	
	private HTMLGen() {
		try {
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
		        
	        htmlDoc = db.newDocument();
	    } catch (ParserConfigurationException pce) {
	    	ToolConstants.reportException( pce, ToolConstants.STDERR);
	    }
	}
	
	/**
	 * This method creates a new DOM document
	 */
	public static void restart() {
		htmlDoc = null;
		new HTMLGen();
	}
	
	public static Document customReport( JabutiProject prj,
									   boolean prj2HTML, // project info
									   boolean class2HTML, // class info
									   boolean method2HTML, // method info
									   boolean ts2HTML, // test set info
									   boolean tc2HTML, // test case info
									   boolean tcp2HTML // test case paths info
									  ) {
		
		Element body = HTMLGen.getBody( "Custom Project Report" );
		
		Document out = null;
		
		if ( prj2HTML )
			out = HTMLGen.projectHTMLReport( prj, body );

		if ( class2HTML ) {
			// Traversing each class, collecting coverage information		
			String[] classes = prj.getAllClassFileNames();
			for ( int i = 0; i < classes.length; i++ ) {
	
				HTMLGen.addSeparator( body, 8, 80 );
							
				ClassFile cf = prj.getClassFile( classes[i] );
				out = HTMLGen.classFileHTMLReport( cf, body );
				
				if ( method2HTML ) {
					// Traversing each class method, collecting coverage information
					String[] methods = cf.getAllMethodsNames();
					if ( methods != null ) {
						for( int j = 0; j < methods.length; j++ ) {
			
							HTMLGen.addSeparator( body, 4, 80 );
							
							ClassMethod cm = cf.getMethod( methods[j] );
							out = HTMLGen.classMethodHTMLReport( cf, cm, body );
							
							for( int k = 0; k < Criterion.NUM_CRITERIA; k++ ) {
								out = requirementsHTMLReport( cm, body, k );
							}
						}
					}
			 	}
			}
			HTMLGen.addSeparator( body, 8, 80 );
		}
		
		if ( ts2HTML )
			out = testSetHTMLReport( body );

		if ( tc2HTML ) {
			// Report for each test case 
			Object[] testcases = TestSet.getTestCaseLabels();
			for ( int i = 0; i < testcases.length; i++ ) {
				TestCase tc = TestSet.getTestCase( testcases[i].toString() );
				
				HTMLGen.addSeparator( body, 4, 80 );
				out = testCaseHTMLReport( tc, body );
				
				//if ( tcp2HTML ) 		
				//	out = testCasePathsHTMLReport( tc, body );
			}
		}
		
		return out;
	}
	
	/**
	 * This method returns the BODY element of an HTML document.
	 * It always restart the current HTML document.
	 *
	 * @param title - an String representing the title of the
	 *                report.
	 * @return body HTML element.
	 */
	public static Element getBody( String title ) {
		restart();
		
        // Top Level ELEMENT
        Element html = htmlDoc.createElement("HTML");
        htmlDoc.appendChild( html );

        Element tt = htmlDoc.createElement("TITLE");
        html.appendChild( tt );
        tt.appendChild( htmlDoc.createTextNode( title ) );
        
        Element body = htmlDoc.createElement("BODY");
        html.appendChild( body );
        
        return body;
	}

	/**
	 * This method generates a summary report for a project
	 *
	 * @param ap - a {@link br.jabuti.project.ActiveProject} object
	 * @param body - the body Element of an HTML documment.
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document projectHTMLReport( JabutiProject prj, Element body ) {

	    Element h1 = htmlDoc.createElement("H1");
        body.appendChild(h1);
        h1.appendChild( htmlDoc.createTextNode("Project Report: " + "Complete" ) );
        
		// The avoided packages
		String pkgs = new String();
		Iterator it = prj.getAvoid().iterator();
		while( it.hasNext() ) {
			pkgs += it.next() + " ";
		}


		String[][] tab = { {"Name", prj.getProjectFileName() }, 
						   {"Type", "Research" },
						   {"Mobility", new Boolean(prj.isMobility() ).toString() },
						   {"CFG Option", new Integer(prj.getCFGOption()).toString() },
						   {"Base Class", prj.getMain() },
						   {"Avoided Packages", pkgs },						   
						 };
		
		body.appendChild( HTMLGen.createTable(htmlDoc, "General Info", null, tab, 1 ) );

		body.appendChild( htmlDoc.createElement( "P" ) );

		String[] header = {"Criterion", "Number Of Covered Requirements", "Percentage"};

		// Project coverage
		tab = new String[Criterion.NUM_CRITERIA][3];
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {
			tab[i][0] = AbstractCriterion.getName(i);
			tab[i][1] = prj.getProjectCoverage(i).toString();
			tab[i][2] = new Float( prj.getProjectCoverage(i).getPercentage() ).toString();
		}
			  
		body.appendChild( HTMLGen.createTable(htmlDoc, "Project Coverage", header, tab, 1 ) );			  

		// Creating a list of all classes and methods that 
		// compose the project
		Element h3 = htmlDoc.createElement( "H3" );
		h3.appendChild( htmlDoc.createTextNode( "Set of Instrumented Classes and Methods" ) );
		body.appendChild( h3 );
		
		Element cul = htmlDoc.createElement( "UL" );
		body.appendChild( cul );
		
		// Traversing each class, collecting coverage information		
		String[] classes = prj.getAllClassFileNames();
		for ( int i = 0; i < classes.length; i++ ) {

			Element cit = htmlDoc.createElement( "LI" );
			Element cl = htmlDoc.createElement( "A" );
			cl.setAttribute( "HREF", "#" + classes[i] );
			cl.appendChild( htmlDoc.createTextNode( classes[i] ) );
			cit.appendChild( cl );
			cul.appendChild( cit );
			
			Element mul = htmlDoc.createElement( "UL" );
			cul.appendChild( mul );

			ClassFile cf = prj.getClassFile( classes[i] );
			
			// Traversing each class method, collecting coverage information
			String[] methods = cf.getAllMethodsNames();
			if ( methods != null ) {
				for( int j = 0; j < methods.length; j++ ) {
					
					Element mit = htmlDoc.createElement( "LI" );
					Element ml = htmlDoc.createElement( "A" );
					ml.setAttribute( "HREF", "#" + methods[j] );
					ml.appendChild( htmlDoc.createTextNode( methods[j] ) );
					mit.appendChild( ml );
					mul.appendChild( mit );
				}
			}
		}
		
		return htmlDoc;
    }


	/**
	 * This method generates a summary report for a given class
	 *
	 * @param cf - a {@link br.jabuti.project.ClassFile} object
	 * @param body - the body Element of an HTML documment.
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document classFileHTMLReport( ClassFile cf, Element body ) {

	    Element h1 = htmlDoc.createElement("H1");
		h1.appendChild( htmlDoc.createTextNode("Class: " + cf.getClassName() ) );
		
		Element ca = htmlDoc.createElement( "A" );
		ca.setAttribute( "NAME", cf.getClassName() );
		ca.appendChild( h1 );
		
		body.appendChild( ca );
                
		String[] header = {"Criterion", "Number Of Covered Requirements", "Percentage"};

		// Project coverage
		String[][] tab = new String[Criterion.NUM_CRITERIA][3];
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {
			tab[i][0] = AbstractCriterion.getName(i);
			tab[i][1] = cf.getClassFileCoverage(i).toString();
			tab[i][2] = new Float( cf.getClassFileCoverage(i).getPercentage() ).toString();
		}
			  
		body.appendChild( HTMLGen.createTable(htmlDoc, "Class Coverage", header, tab, 1 ) );			  
		
		return htmlDoc;
    }


	/**
	 * This method generates a summary report for a given method
	 *
	 * @param cm - an {@link br.jabuti.project.ClassMethod} object
	 * @param body - the body Element of an HTML documment.
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document classMethodHTMLReport( ClassFile cf, ClassMethod cm, Element body ) {

	    Element h1 = htmlDoc.createElement("H2");

        h1.appendChild( htmlDoc.createTextNode("Method: " + cf.getClassName() + "." + cm.getMethodName() ) );

		Element ma = htmlDoc.createElement( "A" );
		ma.setAttribute( "NAME", cm.getMethodName() );
		ma.appendChild( h1 );
		
		body.appendChild( ma );

        
		String[] header = {"Criterion", "Number Of Covered Requirements", "Percentage"};

		// Class Method coverage
		String[][] tab = new String[Criterion.NUM_CRITERIA][3];
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {
			tab[i][0] = AbstractCriterion.getName(i);
			tab[i][1] = cm.getClassMethodCoverage(i).toString();
			tab[i][2] = new Float( cm.getClassMethodCoverage(i).getPercentage() ).toString();
		}
			  
		body.appendChild( HTMLGen.createTable(htmlDoc, "Class Coverage", header, tab, 1 ) );			  
		
		return htmlDoc;
    }


	/**
	 * This method generates a summary report for a given class
	 *
	 * @param cf - a {@link br.jabuti.project.ClassFile} object
	 * @param body - the body Element of an HTML documment.
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document testSetHTMLReport( Element body ) {

	    Element h2 = htmlDoc.createElement("H2");
		h2.appendChild( htmlDoc.createTextNode( "Test Set Coverage" ) );
		body.appendChild( h2 );
		
		if ( TestSet.getNumberOfTestCases() > 0 ) {
			
			String[] header = {"Criterion", "Number Of Covered Requirements", "Percentage"};
	
			// Test Set coverage
			String[][] tab = new String[Criterion.NUM_CRITERIA][3];
			for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {
				tab[i][0] = AbstractCriterion.getName(i);
				tab[i][1] = TestSet.getTestSetCoverage(i).toString();
				tab[i][2] = new Float( TestSet.getTestSetCoverage(i).getPercentage() ).toString();
			}
			
			body.appendChild( HTMLGen.createTable(htmlDoc, "Test Set Coverage", header, tab, 1 ) );			  
	
			// Creating links for each test case		
			String[] testcases = TestSet.getTestCaseLabels();
	
			if ( testcases.length > 0 ) {
			    Element h3 = htmlDoc.createElement("H3");
				h3.appendChild( htmlDoc.createTextNode( "List of Test Cases" ) );
				body.appendChild( h3 );
				
				Element ul = htmlDoc.createElement( "UL" );
				body.appendChild( ul );
				
				// List of Active test cases
				Element liActive = htmlDoc.createElement( "LI" );
				liActive.appendChild( htmlDoc.createTextNode( "Active" ) );
				ul.appendChild( liActive );
				
				Element ulActive = htmlDoc.createElement( "UL" );
				liActive.appendChild( ulActive );
	
				// List of Active test cases
				Element liInactive = htmlDoc.createElement( "LI" );
				liInactive.appendChild( htmlDoc.createTextNode( "Inactive" ) );
				ul.appendChild( liInactive );
				
				Element ulInactive = htmlDoc.createElement( "UL" );
				liInactive.appendChild( ulInactive );
				
				for ( int i = 0; i < testcases.length; i++ ) {
		
					Element li = htmlDoc.createElement( "LI" );
					Element a = htmlDoc.createElement( "A" );
					a.setAttribute( "HREF", "#" + testcases[i] );
					a.appendChild( htmlDoc.createTextNode( testcases[i] ) );
					li.appendChild( a );
					if ( TestSet.isActive( testcases[i] ) )
						ulActive.appendChild( li );
					else
						ulInactive.appendChild( li );
				}
			}
		}
		else {
			body.appendChild( htmlDoc.createTextNode( "EMPTY TEST SET" ) );
		}
		
		return htmlDoc;
    }


	/**
	 * This method generates a summary report for a test case
	 *
	 * @param tc - an {@link br.jabuti.project.TestCase} object
	 * @param body - the body Element of an HTML documment.
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document testCaseHTMLReport( TestCase tc, Element body ) {

	    Element h2 = htmlDoc.createElement("H2");

        h2.appendChild( htmlDoc.createTextNode("Test Case: " + new String( tc.getLabel() ) ) );

		Element a = htmlDoc.createElement( "A" );
		a.setAttribute( "NAME", new String( tc.getLabel() )  );
		a.appendChild( h2 );
		
		body.appendChild( a );

        
		String[] header = {"Criterion", "Number Of Covered Requirements", "Percentage"};

		// Test Case coverage
		String[][] tab = new String[Criterion.NUM_CRITERIA][3];
		for ( int i = 0; i < Criterion.NUM_CRITERIA; i++ ) {
			tab[i][0] = AbstractCriterion.getName(i);
			tab[i][1] = tc.getTestCaseCoverage(i).toString();
			tab[i][2] = new Float( tc.getTestCaseCoverage(i).getPercentage() ).toString();
		}
			  
		body.appendChild( HTMLGen.createTable(htmlDoc, "Class Coverage", header, tab, 1 ) );			  
		
		return htmlDoc;
    }

	public static Document requirementsHTMLReport( ClassMethod cm, Element body, int id ) {
		String name = AbstractCriterion.getName( id );
		
		Criterion criterion = cm.getCriterion( id );
				
	    Element h3 = htmlDoc.createElement("H3");
		h3.appendChild( htmlDoc.createTextNode( name + " Testing Requirements" ) );
		body.appendChild( h3 );
		
		HTMLGen.possibleRequirementsHTMLReport( body, criterion );
		HTMLGen.possibleCoveredHTMLReport( body, criterion );
		HTMLGen.infeasibleRequirementsHTMLReport( body, criterion );
		HTMLGen.inactiveRequirementsHTMLReport( body, criterion );
		return htmlDoc;
	}

	/**
	 * This method generates a summary report containing the set
	 * of active, uncovered and feasible testing requirements, called
	 * possible requirements.
	 *
	 * @param criterion a {@link criteria.Criterion} object
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document possibleRequirementsHTMLReport( Element body, Criterion criterion ) {

	    Element h4 = htmlDoc.createElement("H4");
		h4.appendChild( htmlDoc.createTextNode( "Uncovered" ) );
		body.appendChild( h4 );

		Requirement[] requirements = criterion.getPossibleRequirements();
		Arrays.sort( requirements );
		boolean none = true;
		
		Element ul = htmlDoc.createElement( "UL" );
		body.appendChild( ul );
		
		for ( int i = 0; i < requirements.length; i++ ) {
			Element li = htmlDoc.createElement( "LI" );
			if ( !criterion.isCovered( requirements[i] ) ) {

				// Removing the < and > in case of associations								
				String req = requirements[i].toString();
				if ( ( criterion instanceof AllUses ) || 
				     ( criterion instanceof AllPotUses ) ) {
					req = convertStr( req );
				}

				li.appendChild( htmlDoc.createTextNode( req ) );
				ul.appendChild( li );
				none = false;
			}
		}
		
		if ( none ) {
			Element li = htmlDoc.createElement( "LI" );
			li.appendChild( htmlDoc.createTextNode( "NONE" ) );
			ul.appendChild( li );
		}

		return htmlDoc;
    }


	/**
	 * This method generates a summary report containing the set
	 * of covered testing requirements.
	 *
	 * @param criterion a {@link criteria.Criterion} object
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document possibleCoveredHTMLReport( Element body, Criterion criterion ) {

	    Element h4 = htmlDoc.createElement("H4");
		h4.appendChild( htmlDoc.createTextNode( "Covered" ) );
		body.appendChild( h4 );

		HashSet set = criterion.getPossibleCoveredRequirements();

		Element ul = htmlDoc.createElement( "UL" );
		body.appendChild( ul );
		
		if ( set.size() > 0 ) {
			Object[] requirements = new Object[ set.size() ];
			int i = 0;
			Iterator it = set.iterator();
			while( it.hasNext() ) {
				requirements[i++] = it.next();
			}
			
			Arrays.sort( requirements );
			
			for ( i = 0; i < requirements.length; i++ ) {
				Element li = htmlDoc.createElement( "LI" );

				// Removing the < and > in case of associations				
				String req = requirements[i].toString();
				if ( ( criterion instanceof AllUses ) || 
				     ( criterion instanceof AllPotUses ) ) {
					req = convertStr( req );
				}
				
				li.appendChild( htmlDoc.createTextNode( req ) );
				ul.appendChild( li );
			}
		}
		else {
			Element li = htmlDoc.createElement( "LI" );
			li.appendChild( htmlDoc.createTextNode( "NONE" ) );
			ul.appendChild( li );
		}

		return htmlDoc;
    }


	/**
	 * This method generates a summary report containing the set
	 * of infeasible testing requirements.
	 *
	 * @param criterion a {@link criteria.Criterion} object
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document infeasibleRequirementsHTMLReport( Element body, Criterion criterion ) {

	    Element h4 = htmlDoc.createElement("H4");
		h4.appendChild( htmlDoc.createTextNode( "Infeasible" ) );
		body.appendChild( h4 );

		HashSet set = criterion.getInfeasibleRequirements();

		Element ul = htmlDoc.createElement( "UL" );
		body.appendChild( ul );
		
		if ( set.size() > 0 ) {
			Object[] requirements = new Object[ set.size() ];
			int i = 0;
			Iterator it = set.iterator();
			while( it.hasNext() ) {
				requirements[i++] = it.next();
			}
			
			Arrays.sort( requirements );
			
			for ( i = 0; i < requirements.length; i++ ) {
				Element li = htmlDoc.createElement( "LI" );

				// Removing the < and > in case of associations				
				String req = requirements[i].toString();
				if ( ( criterion instanceof AllUses ) || 
				     ( criterion instanceof AllPotUses ) ) {
					req = convertStr( req );
				}
				
				li.appendChild( htmlDoc.createTextNode( req ) );

				ul.appendChild( li );
			}
		}
		else {
			Element li = htmlDoc.createElement( "LI" );
			li.appendChild( htmlDoc.createTextNode( "NONE" ) );
			ul.appendChild( li );
		}

		return htmlDoc;
    }


	/**
	 * This method generates a summary report containing the set
	 * of infeasible testing requirements.
	 *
	 * @param criterion a {@link criteria.Criterion} object
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document inactiveRequirementsHTMLReport( Element body, Criterion criterion ) {

	    Element h4 = htmlDoc.createElement("H4");
		h4.appendChild( htmlDoc.createTextNode( "Inactive" ) );
		body.appendChild( h4 );

		HashSet set = criterion.getInactiveRequirements();

		Element ul = htmlDoc.createElement( "UL" );
		body.appendChild( ul );
		
		if ( set.size() > 0 ) {
			Object[] requirements = new Object[ set.size() ];
			int i = 0;
			Iterator it = set.iterator();
			while( it.hasNext() ) {
				requirements[i++] = it.next();
			}
			
			Arrays.sort( requirements );
			
			for ( i = 0; i < requirements.length; i++ ) {
				Element li = htmlDoc.createElement( "LI" );

				// Removing the < and > in case of associations				
				String req = requirements[i].toString();
				if ( ( criterion instanceof AllUses ) || 
				     ( criterion instanceof AllPotUses ) ) {
					req = convertStr( req );
				}
				
				li.appendChild( htmlDoc.createTextNode( req ) );

				ul.appendChild( li );
			}
		}
		else {
			Element li = htmlDoc.createElement( "LI" );
			li.appendChild( htmlDoc.createTextNode( "NONE" ) );
			ul.appendChild( li );
		}

		return htmlDoc;
    }

	/**
	 * This method converts a JTable to a HTML table.
	 *
	 * @param body - a given body HTML Element
	 * @param caption - the table caption
	 * @param table   - a JTable object
	 *
	 * @return a DOM Document representing the generated HTML file
	 */
    public static Document jtable2HTML( Element body, String caption, JTable table ) {
	
		if ( caption != null ) {
		    Element h3 = htmlDoc.createElement("H3");
			h3.appendChild( htmlDoc.createTextNode( caption ) );
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
				
				rows[i][j] = HTMLGen.convertStr( text );
			}
		}		

		body.appendChild( HTMLGen.createTable(htmlDoc, caption, header, rows, 1 ) );

		return htmlDoc;
    }


    /**
     * This method is responsible to create a table in a HTML format.
     *
     * @param header is the table header and is printed in bold;
     * @param rows is the table data. The first element of each row is printed in bold;
     * @param border is the border size.
     * @return a Element that represents the generated table.
     */
    private static Element createTable( Document htmlDoc, String caption, Object[] header, Object[][] rows, int border ) {
		Element table = htmlDoc.createElement("TABLE");
		
		table.setAttribute( "BORDER", new Integer( border ).toString() );
		
		if ( caption != null ) {
			Element cp = htmlDoc.createElement("CAPTION");
			table.appendChild( cp );
			
		    Element h4 = htmlDoc.createElement("H4");
	        cp.appendChild(h4);
	        h4.appendChild( htmlDoc.createTextNode( caption ) );
		}
		
		// Inserting the table header, if any
		if ( header != null ) {
			Element tr = htmlDoc.createElement("TR");
			table.appendChild( tr );
			
			for ( int i = 0; i < header.length; i++ ) {
				// All header elements in bold
				Element th = htmlDoc.createElement("TH");
				tr.appendChild( th );
				th.setAttribute( "ALIGN", "center" );
				th.setAttribute( "VALIGN", "center" );
				th.appendChild( htmlDoc.createTextNode(header[i].toString() ) );
			}
		}
		
		// Inserting the table data, if any
		if ( table != null ) {
			for ( int i = 0; i < rows.length; i++ ) {
				Element tr = htmlDoc.createElement("TR");
				table.appendChild( tr );
				
				Element th = htmlDoc.createElement("TH");
				tr.appendChild( th );
				th.setAttribute( "ALIGN", "left" );
				th.setAttribute( "VALIGN", "center" );
				
				// First element of each row in bold
				th.appendChild( htmlDoc.createTextNode( rows[i][0].toString() ) );
				
				for ( int j = 1; j < rows[i].length; j++ ) {
					Element td = htmlDoc.createElement("TD");
					tr.appendChild( td );
					td.setAttribute( "ALIGN", "right" );
					td.setAttribute( "VALIGN", "center" );
					
					td.appendChild( htmlDoc.createTextNode(rows[i][j].toString()));
				}
			}
		}
		
    	return table;
    }
    
    /**
     * This method includes a separator (HR element) in a specific body element.
     *
     *@param the body element
     *@param an integer number corresponding to the HR size
     */
     private static void addSeparator( Element body, int size, int perc ) {
     	Element hr = htmlDoc.createElement( "HR" );
     	hr.setAttribute( "ALIGN", "center" );
     	hr.setAttribute( "SIZE", new Integer( size ).toString() + "%" );     	
     	hr.setAttribute( "WIDTH", new Integer( perc ).toString() + "%" );     	
     	
     	body.appendChild( hr );
     }
     
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
     
/////////////////////////////////////////////////////////////////////////
	/**
	 * This method generates the Requirements Summary Report.
	 * It is assumed that the current summary report presented by
	 * JaBUTi is the Testing Requirements Summary Report.
	 */
	public static Document requirements2HTML( String title ) {
		JabutiProject prj = JabutiGUI.getProject();
		Element body = HTMLGen.getBody( prj.getProjectFileName() ); 

	    Element h1 = htmlDoc.createElement("H1");
		h1.appendChild( htmlDoc.createTextNode( prj.getProjectFileName() ) );
		body.appendChild( h1 );
	    
	    
	    Element h3 = htmlDoc.createElement("H3");
		h3.appendChild( htmlDoc.createTextNode( title ) );
		body.appendChild( h3 );
		
		HTMLGen.addSeparator( body, 4, 80 );
		
		// Name of the current class
	    Element h4 = htmlDoc.createElement("H4");
		h4.appendChild( htmlDoc.createTextNode( "Current Class: " + prj.getCurClassName() ) );
		body.appendChild( h4 );
		
		// Name of the current method
		h4 = htmlDoc.createElement("H4");
		h4.appendChild( htmlDoc.createTextNode( "Current Method: " + prj.getCurMethodName() ) );
		body.appendChild( h4 );
		
		body.appendChild( htmlDoc.createElement("BR") );
		
		HTMLGen.jtable2HTML( body, null, TableSorterPanel.getTable() );		
		
		HTMLGen.addSeparator( body, 4, 80 );		
		
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
		
		body.appendChild( HTMLGen.createTable(htmlDoc, null, null, tab, 1 ) );
		
		return htmlDoc;
	}

	/**
	 * This method generates the Complexity Metrics Summary Report.
	 * It is assumed that the current summary report presented by
	 * JaBUTi is the Metrics Summary Report.
	 */
	public static Document metrics2HTML( String title ) {
		JabutiProject prj = JabutiGUI.getProject();
		Element body = HTMLGen.getBody( prj.getProjectFileName() ); 
	 
	    Element h1 = htmlDoc.createElement("H1");
		h1.appendChild( htmlDoc.createTextNode( prj.getProjectFileName() ) );
		body.appendChild( h1 );
	    
	    Element h3 = htmlDoc.createElement("H3");
		h3.appendChild( htmlDoc.createTextNode( title ) );
		body.appendChild( h3 );
		
		HTMLGen.addSeparator( body, 4, 80 );
		
		HTMLGen.jtable2HTML( body, null, TableSorterPanel.getTable() );		
		
		HTMLGen.addSeparator( body, 4, 80 );		
		
		// Method and Total Coverage
		String[][] tab = { 
			{"Number of Metrics", new Integer( Metrics.metrics.length ).toString() },
			{"Number of Classes", new Integer( prj.getAllClassFileNames().length ).toString()} };
		
		body.appendChild( HTMLGen.createTable(htmlDoc, null, null, tab, 1 ) );
		
		return htmlDoc;
	}

	/**
	 * This method generates the a Summary Report for all
	 * others reports provided by JaBUTi graphical interface.
	 */
	public static Document overallCoverage2HTML( String title ) {
		JabutiProject prj = JabutiGUI.getProject();
		
		Element body = HTMLGen.getBody( prj.getProjectFileName() );
		
	    Element h1 = htmlDoc.createElement("H1");
		h1.appendChild( htmlDoc.createTextNode( prj.getProjectFileName() ) );
		body.appendChild( h1 );
	    
	    Element h3 = htmlDoc.createElement("H3");
		h3.appendChild( htmlDoc.createTextNode( title ) );
		body.appendChild( h3 );
		
		HTMLGen.addSeparator( body, 4, 80 );
		
		HTMLGen.jtable2HTML( body, null, TableSorterPanel.getTable() );		
		
		HTMLGen.addSeparator( body, 4, 80 );		
		
		// Finding the current criterion
		int c = JabutiGUI.mainWindow().getActiveCriterionId();
		
		// Method and Total Coverage
		String[][] tab = { 
			{"Total Coverage",
			 prj.getProjectCoverage(c).toString(),
			 new Float( prj.getProjectCoverage(c).getPercentage() ).toString()} };
		
		body.appendChild( HTMLGen.createTable(htmlDoc, null, null, tab, 1 ) );


		HTMLGen.addSeparator( body, 4, 80 );		
		
		// Method and Total Coverage
		tab = new String[][] { 
			{"Tool", JabutiGUI.mainWindow().getActiveToolName() },
			{"Active Test Cases", 
				TestSet.getNumberOfActiveTestCases() + 
				" of " + 
				TestSet.getNumberOfTestCases() } };
		
		body.appendChild( HTMLGen.createTable(htmlDoc, null, null, tab, 1 ) );
		
		return htmlDoc;
	}
}