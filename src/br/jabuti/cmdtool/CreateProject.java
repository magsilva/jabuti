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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import br.jabuti.graph.CFG;
import br.jabuti.lookup.Program;
import br.jabuti.project.JabutiProject;
import br.jabuti.project.TestSet;


/** 
 * Esse script e utilizado para criar uma sessao de teste
 * da JaBUTi.
 * 
 * Forma de utilizacao, execute:
 * 
 *    cmdtool.CreateProject
 * 
 * @version: 0.00001
 * @author: Auri Marcelo Rizzo Vincenzi
 * Marcio Eduardo Delamaro
 */
public class CreateProject {
    public static void usage() {
        System.out.println("JaBUTi CreateProject");
        System.out.println("\nUSAGE:");
        System.out.println("java -cp \"classpath\" cmdtool.CreateProject -b <base_class_file> -p <project_file_name> [-mob] [-cn] [-i <file_name>] [-a <avoided_packages>]\n");
        System.out.println("      -b <base_class>         The name of the base class file (without the .class extension)");
        System.out.println("                              Observe that the classpath varible should be");
        System.out.println("                              set such that the base class can be found.");
        System.out.println("      -p <project_file_name>  The full name of the project file to be created.");
        System.out.println("                              The default extension of the file is .jbt.");
        System.out.println("      -mob                    To test mobile code. If not specified it is considered non-mobile.");
        System.out.println("      -cn                     Expand the call nodes during the CFG construction. If not specified");        
		System.out.println("                              the call nodes are hidden.");                
        System.out.println("      -i <file_name>          This is the name of a text file where the");
        System.out.println("                              name of the classes to be instrumented can be found.");
        System.out.println("                              The names should be one per line. If ommited, all");
        System.out.println("                              user classes derived from the base class are selected");
        System.out.println("                              to be instrumented.");
        System.out.println("      -a <avoided_packages>   This is the name of a text file where the");
        System.out.println("                              packages to be avoided can be found.");
        System.out.println("                              The names of the packages should be one per line."); 
        System.out.println("                              If ommited, no package is avoided.");
        System.out.println("\nCopyright (c) 2004\n");
    }

    public static void main(String args[]) throws Throwable {
		boolean isMobility = false;
    	
        HashSet toInstrument = null;
        HashSet toAvoid = null;
		
        String baseClass = null;
        String instFile = null;
        String avoidFile = null;
        String prjFile = null;
        int cfgOption = CFG.NO_CALL_NODE;

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
                    } else {
                        System.out.println("Only one -b option is allowed. ");
                        System.out.println("try java cmdtool.CreateProject -h for help.");
                        System.exit(0);
                    }
                } // Testing mobile code
                else if (("-mob".equals(args[i]))) {
                	isMobility = !isMobility;
                } // Show call nodes
                else if (("-cn".equals(args[i]))) {
                	cfgOption = CFG.NONE;
                } // The project file name
                else if (("-p".equals(args[i])) && (i < args.length - 1)) {
                    if (prjFile == null) {
                        i++;
                        prjFile = args[i];
                    } else {
                        System.out.println("Only one -p option is allowed. ");
                        System.out.println("try java cmdtool.CreateProject -h for help.");
                        System.exit(0);
                    }
                } // Classes to Instriment file...
                else if (("-i".equals(args[i])) && (i < args.length - 1)) {
                    if (instFile == null) {
                        i++;
                        instFile = args[i];
                    } else {
                        System.out.println("Only one -i option is allowed. ");
                        System.out.println("try java cmdtool.CreateProject -h for help.");
                        System.exit(0);
                    }
                } // Packages to Avoid file...
                else if (("-a".equals(args[i])) && (i < args.length - 1)) {
                    if (avoidFile == null) {
                        i++;
                        avoidFile = args[i];
                    } else {
                        System.out.println("Only one -a option is allowed. ");
                        System.out.println("try java cmdtool.CreateProject -h for help.");
                        System.exit(0);
                    }
                }
				else {
                    System.out.println("Unrecognized option: " + args[i]);
                    System.out.println("try java cmdtool.CreateProject -h for help.");
                    System.exit(0);
                }
                i++;
            }
            
            // Reading the file containing the name of the classes to be
            // instrumented
            if ( instFile != null ) {
            	BufferedReader br = null;
            	toInstrument = new HashSet();
            	
            	try {
            		br = new BufferedReader( new FileReader( instFile ) );
            		String fname = br.readLine().trim();
            		while( fname != null ) {
            			toInstrument.add( fname );
            			fname = br.readLine();
            		}
            	} catch( FileNotFoundException fnfe ) {
            		System.err.println( "File " + instFile + " cannot be found!" );
            		fnfe.getMessage();
            		fnfe.printStackTrace();
            	}
            	catch( IOException ioe ) {
            		System.err.println( "Error reading " + instFile + " file!" );
            		ioe.getMessage();
            	}
            	finally {
            		if ( br != null ) 
            			br.close();
            	}
            }

            // Reading the file containing the name of the packages to be
            // avoided
            if ( avoidFile != null ) {
            	BufferedReader br = null;
            	toAvoid = new HashSet();
            	
            	try {
            		br = new BufferedReader( new FileReader( avoidFile ) );
            		String fname = br.readLine();
            		while( fname != null ) {
            			toAvoid.add( fname );
            			fname = br.readLine();
            		}
            	} catch( FileNotFoundException fnfe ) {
            		System.err.println( "File " + avoidFile + " cannot be found!" );
            		fnfe.getMessage();
            		fnfe.printStackTrace();
            	}
            	catch( IOException ioe ) {
            		System.err.println( "Error reading " + avoidFile + " file!" );
            		ioe.getMessage();
            	}
            	finally {
            		if ( br != null ) 
            			br.close();
            	}
            }
            
            String classPath = System.getProperty("java.class.path");
            JabutiProject jbtProject = null;
			
			// Try to create a project from the base class
			if ( baseClass != null && prjFile != null ) {
				try {
					jbtProject = new JabutiProject( baseClass, classPath );
				}
				catch (Exception e ) {
					System.err.println( "Error creating the project " + prjFile );
					e.getMessage();
				}
			
				if ( jbtProject == null ) {
					System.err.println( "Error creating the project " + prjFile );
					System.exit(0);
				}
				// Setting the project file name.
				jbtProject.setProjectFile( new File( prjFile ) );
				jbtProject.setMobility( isMobility );
				jbtProject.setCFGOption( cfgOption );
				
				// Checking the set of packages to be avoided
				Program prg = jbtProject.getProgram();
				if ( toAvoid != null ) { // If null no package is avoided
					String[] packages = prg.getCodePackages();
					HashSet pkgSet = new HashSet();
					for ( int j = 0; j < packages.length; j++ ) {
						pkgSet.add( packages[j] );
					}
					String[] names = (String[]) toAvoid.toArray( new String[0] );
					for( int j = 0; j < names.length; j++ ) {
						if ( !pkgSet.contains( names[j] ) ) {
							System.out.println( "NOT Avoided Package: " + names[j] );
							toAvoid.remove( names[j] );
						} else {
							System.out.println( "Avoided Package: " + names[j] );
						}
					} 
					Iterator it = toAvoid.iterator();
					while( it.hasNext() ) {
						jbtProject.addAvoid( (String) it.next() );
					}
				}
				
				// Check the set of classes to be instrumented
				if ( toInstrument != null ) { // If null, all the user classes are instrumented
					String[] classes = prg.getCodeClasses();
					HashSet classSet = new HashSet();
					for ( int j = 0; j < classes.length; j++ ) {
						classSet.add( classes[j] );
					}
					String[] names = (String[]) toInstrument.toArray( new String[0] );					
					for ( int j = 0; j < names.length; j++ ) {
						if ( !classSet.contains( names[j] ) ) {
							System.out.println( "NOT Instrumented Class: " + names[j] );
							toInstrument.remove( names[j] );
						} else {
							System.out.println( "Instrumented Class: " + names[j] );
						}
					} 
					Iterator it = toInstrument.iterator();
					while( it.hasNext() ) {
							jbtProject.addInstr( (String) it.next() );
					}
				} else { // All user classes selected to be instrumented
					String[] classes = prg.getCodeClasses();
					for ( int j = 0; j < classes.length; j++ ) {
						jbtProject.addInstr( classes[j] );
					}
				}
				// Rebuilding all the project info, including the classes
				// to be instrumented
				jbtProject.rebuild();
				
				TestSet.initialize( jbtProject, jbtProject.getTraceFileName() );
				
				try{
					jbtProject.saveProject();
				} catch (Exception e) {
					System.err.println( "Error saving project file: " + jbtProject.getProjectFile() );
				}

				System.out.println( "Project Coverage" );				
				System.out.println( jbtProject.coverage2TXT("") );
				
			} else {
                System.out.println("One base class and a project file name should be specified!");
                System.out.println("try java cmdtool.CreateProject -h for help.");
                System.exit(0);
			}
        } else {
            usage();
        }
        System.exit(0);
    }
}
