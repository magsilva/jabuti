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


package br.jabuti.cmdtool;

import br.jabuti.lookup.Program;

/** 
 * This script lists the complete class closure, considering
 * a given base class file.
 * 
 * Type:
 * 
 *    cmdtool.ListClasses
 * 
 * for help
 * 
 * @version: 0.00001
 * @author: Auri Marcelo Rizzo Vincenzi
 * Marcio Eduardo Delamaro
 */
public class ListClasses {
    public static void usage() {
        System.out.println("JaBUTi ListClasses");
        System.out.println("\nUSAGE:");
        System.out.println("java -cp \"classpath\" cmdtool.ListClasses -b <base_class_file>\n");
        System.out.println("      -b <base_class>         The name of the base class file (without the .class extension)");
        System.out.println("                              Observe that the classpath varible should be");
        System.out.println("                              set such that the base class can be found.");
        System.out.println("\nCopyright (c) 2004\n");
    }

    public static void main(String args[]) throws Throwable {
        String baseClass = null;

        if (args.length > 0) {

            int i = 0;
			
            if (args.length == 1) {
               usage();
               System.exit(0);
            }
			
            while (i < args.length && args[i].startsWith("-")) {
                // -b: Base Class Name
                if (("-b".equals(args[i])) && (i < args.length - 1)) {
                    if (baseClass == null) {
                        i++;
                        baseClass = args[i];
                    } 
                }
				else {
                    System.out.println("Unrecognized option: " + args[i]);
                    System.out.println("try java cmdtool.CreateProject -h for help.");
                    System.exit(0);
                }
                i++;
            }
            
            if (baseClass == null) {
            	System.out.println("Missing parameter: base class.");
            	System.exit(1);
            }
            
            String classPath = System.getProperty("java.class.path");

            Program prog = new Program(baseClass, true, null, classPath);
            System.out.println("\nCode classes");
            String[] ccl = prog.getCodeClasses();
            for (int j = 0; j < ccl.length; j++)
            {
            	System.out.print(j + ") " + ccl[j] + "\n");
            }
            System.out.println("\n\nSystem classes");
            String[] scl = prog.getSysClasses();
            for (int j = 0; j < scl.length; j++)
            {
            	System.out.print(j + ") " + scl[j] + "\n");
            }
            
            System.out.println();
            System.out.println("Summary: " + ccl.length + " code class(es) and " + scl.length + " system class(es)");
        } else {
            usage();
        }
        System.exit(0);
    }
}
