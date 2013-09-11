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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XMLPrettyPrinter {

    static HashMap<Integer, String> displayStrings = new HashMap<Integer, String>();

    static int numberDisplayLines = 0;

    public static void displayDocument(String uri) {
		displayStrings.clear();
   		numberDisplayLines = 0;
        try {
            // Step 1: create a DocumentBuilderFactory and configure it
            DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();

            // Step 2: create a DocumentBuilder that satisfies the constraints
            // specified by the DocumentBuilderFactory
            DocumentBuilder db = dbf.newDocumentBuilder();
	
            // Step 3: parse the input file
            Document document = db.parse(new File(uri));

            display(document, "");
            
	        for (int loopIndex = 0; loopIndex < numberDisplayLines; loopIndex++) {
	            System.out.println( (String) displayStrings.get( new Integer( loopIndex ) ) );
	        }
            
        } catch (Exception e) {
        	ToolConstants.reportException( e, ToolConstants.STDERR);
        }
    }

    public static void writeDocument( Document document, PrintStream out ) {
        try {
			displayStrings.clear();
	   		numberDisplayLines = 0;
        	
            display(document, "");
            
	        for (int loopIndex = 0; loopIndex < numberDisplayLines; loopIndex++) {
	            out.println( (String) displayStrings.get( new Integer( loopIndex ) ) );
	        }
            out.flush();
            out.close();
        } catch (Exception e) {
        	ToolConstants.reportException( e, ToolConstants.STDERR);
        }
    }

    public static void writeDocument( Document document, String name ) {
        try {
        	PrintStream out = new PrintStream( new FileOutputStream( name ) );
        	writeDocument( document, out );
        } catch (Exception e) {
        	ToolConstants.reportException( e, ToolConstants.STDERR);
        }
    }

    public static void display(Node node, String indent) {

        if (node == null) {
            return;
        }

        int type = node.getNodeType();

        switch (type) {

        case Node.DOCUMENT_NODE: {

                addString(numberDisplayLines, indent);

                //appendString( numberDisplayLines,  "<?xml version=\"1.0\" encoding=\"" + "UTF-8" + "\"?>" );

                numberDisplayLines++;

                display(((Document) node).getDocumentElement(), "");

                break;

            }

        case Node.ELEMENT_NODE: {

                addString(numberDisplayLines, indent);

                appendString( numberDisplayLines, "<" );

                appendString( numberDisplayLines, node.getNodeName() );

                int length = (node.getAttributes() != null)
                        ? node.getAttributes().getLength()
                        : 0;

                Attr attributes[] = new Attr[length];

                for (int loopIndex = 0; loopIndex < length; loopIndex++) {

                    attributes[loopIndex] =

                            (Attr) node.getAttributes().item(loopIndex);

                }

                for (int loopIndex = 0; loopIndex < attributes.length; loopIndex++) {

                    Attr attribute = attributes[loopIndex];

                    appendString( numberDisplayLines, " " );

                    appendString( numberDisplayLines, attribute.getNodeName() );

                    appendString( numberDisplayLines, "=\"" );

                    appendString( numberDisplayLines, attribute.getNodeValue() );

                    appendString( numberDisplayLines, "\"" );
                }

                appendString( numberDisplayLines, ">" );

                numberDisplayLines++;

                NodeList childNodes = node.getChildNodes();

                if (childNodes != null) {

                    length = childNodes.getLength();

                    indent += "    ";

                    for (int loopIndex = 0; loopIndex < length; loopIndex++) {

                        display(childNodes.item(loopIndex), indent);

                    }

                }

                break;

            }

        case Node.CDATA_SECTION_NODE: {

                addString( numberDisplayLines, indent );

                appendString( numberDisplayLines, "<![CDATA[" );

                appendString( numberDisplayLines, node.getNodeValue() );

                appendString( numberDisplayLines, "]]>" );

                numberDisplayLines++;

                break;

            }

        case Node.TEXT_NODE: {

                addString( numberDisplayLines, indent );

                String newText = node.getNodeValue().trim();

                if (newText.indexOf("\n") < 0 && newText.length() > 0) {

                    appendString( numberDisplayLines, newText );

                    numberDisplayLines++;

                }

                break;

            }

        case Node.PROCESSING_INSTRUCTION_NODE: {

                addString( numberDisplayLines, indent );

                appendString( numberDisplayLines, "<?" );

                appendString( numberDisplayLines, node.getNodeName() );

                String text = node.getNodeValue();

                if (text != null && text.length() > 0) {

                    appendString( numberDisplayLines, text );

                }

                appendString( numberDisplayLines, "?>" );

                numberDisplayLines++;

                break;

            }

        }

        if (type == Node.ELEMENT_NODE) {

            addString( numberDisplayLines, indent.substring(0, indent.length() - 4) );

            appendString( numberDisplayLines, "</" );

            appendString( numberDisplayLines, node.getNodeName() );

            appendString( numberDisplayLines, ">" );

            numberDisplayLines++;

            indent += "    ";

        }

    }

	private static void addString( int index, String str ) {
		Integer ind = new Integer( index );
		String current = new String( str );
		
		displayStrings.put( ind, current );
	}

	private static void appendString( int index, String str ) {
		Integer ind = new Integer( index );
		String current = new String( str );
		
		if ( displayStrings.containsKey( ind ) ) {
			current = (String) displayStrings.get( ind );
			current += str;
		}
		displayStrings.put( ind, current );
	}


    /**
     * This method is responsible to create a table in a HTML format.
     *
     * @param header is the table header and is printed in bold;
     * @param rows is the table data. The first element of each row is printed in bold;
     * @param border is the border size.
     * @return a Element that represents the generated table.
     */
    public static Element createTable( Document htmlDoc, Object[] header, Object[][] rows, int border ) throws ParserConfigurationException {
		Element table = htmlDoc.createElement("TABLE");
		
		table.setAttribute( "BORDER", new Integer( border ).toString() );
		
		// Inserting the table header, if any
		if ( header != null ) {
			for ( int i = 0; i < header.length; i++ ) {
				Element tr = htmlDoc.createElement("TR");
				table.appendChild( tr );
				
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
					td.appendChild( htmlDoc.createTextNode(rows[i][j].toString()));
				}
			}
		}
		
    	return table;
    }

    public static void main(String args[]) {

        displayDocument(args[0]);

        for (int loopIndex = 0; loopIndex < numberDisplayLines; loopIndex++) {
            System.out.println( (String) displayStrings.get( new Integer( loopIndex ) ) );
        }
    }
}

