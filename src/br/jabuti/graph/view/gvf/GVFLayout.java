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


package br.jabuti.graph.view.gvf;


/**
 * This class interacts with ILOG's JView graph visualization API
 * to allow for layout definition for the GVF Objects.
 *
 * @author  Plinio Vilela
 * @version 1.0
 */



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Enumeration;
import java.util.Vector;

import br.jabuti.graph.layout.graphviz.DotParser;
import br.jabuti.graph.layout.graphviz.GraphViz;
import br.jabuti.graph.layout.graphviz.ParseException;


class GVFLayout {

    // Plan to make class more general 
    // and allow it to change the layout algorithm on the fly.
    Vector vNodes;
    Vector vLinks;

    int maxX, maxY;
	
    public GVFLayout(Vector vN, Vector vL) {
        vNodes = vN;
        vLinks = vL;
        maxX = 0;
        maxY = 0;
        
        try {
			performLayout();
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}
        for (Enumeration e = vNodes.elements(); e.hasMoreElements();) {
            GVFDisplayable n = (GVFDisplayable) e.nextElement();
			
            if (((GVFNode) n).getX() > maxX) {
                maxX = ((GVFNode) n).getX();
            }
			
            if (((GVFNode) n).getY() > maxY) {
                maxY = ((GVFNode) n).getY();
            }
			
          //  n.translate(36, 36);
        }
    }

    public void performLayout() throws FileNotFoundException, ParseException {
    	GraphViz gviz = new GraphViz();
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
		DecimalFormatSymbols symbols=format.getDecimalFormatSymbols();
		char decimalSeparator = symbols.getDecimalSeparator();

    	
    	gviz.addln(gviz.start_graph());
    	for (int i = 0; i < vNodes.size(); i++)	{
    		GVFNode node = (GVFNode) vNodes.get(i);
    		// gviz.addln(node.getSource() + " [width=\"0" + decimalSeparator + "50\", height=\"0" + decimalSeparator + "50\"];");
    		gviz.addln(node.getSource());
    	}
    	for (int i = 0; i < vLinks.size(); i++)	{
    		GVFLink link = (GVFLink) vLinks.get(i);
    		gviz.addln(link.getSourceNode().getSource() + " -> " + link.getDestinationNode().getSource());
    	}    	
    	gviz.addln(gviz.end_graph());

    	String result = gviz.getDotGraph(gviz.getDotSource());

    	System.out.println("RESULT\n*********\n" + result + "\n*********\n");
    	
    	File f = new File(result);
    	DotParser dt = new DotParser(vNodes, vLinks, new FileInputStream(f));
    	dt.parse();

    	f.delete();
    }

    public Vector getNodes() {
        return vNodes;
    }

    public Vector getLinks() {
        return vLinks;
    }

    public int getMaxX() { 
        return maxX;
    }

    public int getMaxY() { 
        return maxY;
    }
}
