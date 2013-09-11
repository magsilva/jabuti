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
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import br.jabuti.util.ToolConstants;

/**
 * SAXHandler extends DefaultHandler to extract the necessary information from
 * the project file.
 */
public class ClasspathParser extends DefaultHandler {
	/*
	 * This constants represent the complete set of tags which can be found in a
	 * JaBUTi project file. Please see the project/jabutiprj.dtd file to know
	 * the set of attributes and their types.
	 */
	public static final String CLASSPATH = XML2Project.CLASSPATH;

	private SAXParser saxParser;

	private String urlString;
	private static String classPath;


	private ClasspathParser() {
		classPath = null;
		create();
	}

	/**
	 * Create the SAX parser
	 */
	private void create() {
		try {
			// Obtain a new instance of a SAXParserFactory.
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// Specifies that the parser produced by this code will provide
			// support for XML namespaces.
			factory.setNamespaceAware(true);
			// Specifies that the parser produced by this code will validate
			// documents as they are parsed.
			factory.setValidating(true);
			// Creates a new instance of a SAXParser using the currently
			// configured factory parameters.
			saxParser = factory.newSAXParser();
		} catch (Throwable t) {
			ToolConstants.reportException(t, ToolConstants.STDERR);
		}
	}

	/**
	 * Parse a URI
	 * 
	 * @param uri -
	 *            String
	 */
	public void parse(String uri) {
		try {
			urlString = uri;
			saxParser.parse(urlString, this);
		} catch (Throwable t) {
			ToolConstants.reportException(t, ToolConstants.STDERR);
		}
	}

	/**
	 * This method returns a reference to a JabutiProject object. It can only be
	 * called after the parse() method had been called.
	 */
	public static String getClassPath(String uri) {
		ClasspathParser cpp = new ClasspathParser();
		cpp.parse(uri);
		return classPath;
	}

	/**
	 * Receive notification of the start of an element.
	 * 
	 * @param namespaceURI -
	 *            The Namespace URI, or the empty string if the element has no
	 *            Namespace URI or if Namespace processing is not being
	 *            performed.
	 * @param localName -
	 *            The local name (without prefix), or the empty string if
	 *            Namespace processing is not being performed.
	 * @param qName -
	 *            The qualified name (with prefix), or the empty string if
	 *            qualified names are not available.
	 * @param atts -
	 *            The attributes attached to the element. If there are no
	 *            attributes, it shall be an empty Attributes object.
	 * @throws SAXException -
	 *             Any SAX exception, possibly wrapping another exception.
	 */
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (CLASSPATH.equals(localName)) {
			classPathFound(atts);
		}
		return;
	}

	/**
	 * Receive notification of the end of an element.
	 * 
	 * @param namespaceURI -
	 *            The Namespace URI, or the empty string if the element has no
	 *            Namespace URI or if Namespace processing is not being
	 *            performed.
	 * @param localName -
	 *            The local name (without prefix), or the empty string if
	 *            Namespace processing is not being performed.
	 * @param qName -
	 *            The qualified name (with prefix), or the empty string if
	 *            qualified names are not available.
	 * @throws SAXException -
	 *             Any SAX exception, possibly wrapping another exception.
	 */
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		return;
	}

	private void classPathFound(Attributes atts) throws SAXException {
		classPath = atts.getValue("path");

		// Converting the classpath in a system independent way (more less)
		StringTokenizer st = new StringTokenizer(classPath);
		StringBuffer sb = new StringBuffer();
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (File.pathSeparatorChar == ';') { // If windows
				s = s.replace(':', File.pathSeparatorChar); // replacing driver
															// letter d: by d?
															// for instance.
				s = s.replace('/', File.separatorChar);
			} else {
				s = s.replace(';', File.pathSeparatorChar); // replacing driver
															// letter d: by d?
															// for instance.
				s = s.replace('\\', File.separatorChar);
			}
			s = s.replace('?', ':');
			sb.append(s + File.pathSeparatorChar);
		}

		classPath = sb.toString();
	}

	public static void main(String[] args) {

		ClasspathParser jfs = new ClasspathParser();

		// Parse the XML file, handler generates the output
		jfs.parse("fact.jbt");

		System.out.println("\n\n The Order.xml parsed");
	}
}
