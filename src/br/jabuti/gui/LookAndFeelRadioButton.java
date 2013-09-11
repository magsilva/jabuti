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


package br.jabuti.gui;

import javax.swing.JRadioButtonMenuItem;

/**
 * This class represents a JRadioButton which stores the text of
 * the radio button and also the corresponding plataform info of a
 * given look and feel.
 * 
 * @author Auri Vincenzi
 */
class LookAndFeelRadioButton extends JRadioButtonMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1059178686754558002L;
	private String lookInfo; // the class name of the look and feel class
	private String name;	  // the name of the look and feel 
	
	public LookAndFeelRadioButton() {
		super();
	}
	
	public void setLookInfo(String p) {
		if ( p != null ) {
			lookInfo = p;
			int pos = p.lastIndexOf('.');
			name = p.substring(pos+1);
		}
	}
	
	public String getLookInfo() {
		return lookInfo;
	}
	
	public String getName() {
		return name;
	}
}