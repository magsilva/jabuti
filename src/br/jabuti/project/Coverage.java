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



/**
 *
 * This class stores general information about coverage.
 * Basically the number of requirements e the ones that were
 * covered.
 *
 * @version: 1.0
 * @author: Auri Vincenzi
 * @author: Marcio Delamaro
 * @author: Tatiana Sugeta
 *
 */
public class Coverage {
	
    /** stores the information about coverage */
    private int numberOfRequirements;

    private int numberOfCovered;
	
    public Coverage() {
        numberOfCovered = numberOfRequirements = 0;
    }

    public Coverage(int cov, int total) {
        numberOfCovered = cov;
        numberOfRequirements = total;
    }

    public Coverage( Coverage c ) {
        this( c.numberOfCovered, c.numberOfRequirements );
    }
	
    /***********************************************************/
    
    /* Get and Set Methods implementation                      */
    
    /***********************************************************/
	
    public int getNumberOfRequirements() {
        return numberOfRequirements;
    }

    public void setNumberOfRequirements(int n) {
        numberOfRequirements = n;
    }

    public int getNumberOfCovered() {
        return numberOfCovered;
    }

    public void setNumberOfCovered(int n) {
        numberOfCovered = n;
    }

    public float getPercentage() {
        try {
            return (getNumberOfCovered() * 100) / getNumberOfRequirements();
        } catch (ArithmeticException ae) {
            return 0.0f;
        }
    }
	
    public String toString() {
        return new String( getNumberOfCovered() + " of " + getNumberOfRequirements() );
    }
    
    public boolean equals(Object o)
    {
    	if (! (o instanceof Coverage))
    		return false;
    	Coverage cv = (Coverage) o;
    	return cv.getNumberOfCovered() == getNumberOfCovered() &&
		       cv.getNumberOfRequirements() == getNumberOfRequirements();
    }
    
}
