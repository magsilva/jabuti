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


package br.jabuti.junitexec;

public class JUnitUtil {
	public static final String SUCCESS = "S";
	public static final String FAILURE = "E";
	public static final String IGNORED = "I";
	
	public static final String traceMark = "T";
	public static final String integratorName = "JUnit/JaBUTi Integrator";
	
	static String getTestCaseName(String s) {
		return s.substring(0, s.indexOf('('));
	}	
}