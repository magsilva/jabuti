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

import java.io.File;

import br.jabuti.project.JabutiProject;
import br.jabuti.project.TestSet;

/**
 * Esse script e utilizado para importar casos de teste gerados pelo class loader
 * da JaBUTi - ProberLoader.
 * 
 * Forma de utilizacao, execute:
 * 
 * cmdtool.ImportTestCase
 * 
 * @version: 0.00001
 * @author: Auri Marcelo Rizzo Vincenzi 
 * @author Marcio Eduardo Delamaro
 */
public class ImportTestCase {
	public static void usage() {
		System.out.println("JaBUTi ImportTestCase");
		System.out.println("\nUSAGE:");
		System.out
		.println("java -cp \"classpath\" cmdtool.ImportTestCase [-cut] -p <project_file_name>\n");
		System.out
		.println("      -cut 					If specified, only effective test case is imported.");
		System.out
		.println("      -p <project_file_name>  The full name of the project file to be created.");
		System.out
		.println("                              The default extension of the file is .jbt.");
		System.out.println("\nCopyright (c) 2004\n");
	}

	public static void main(String args[]) throws Throwable {
		String projectName = null;
		boolean cutOption = false;
		
		if (args.length > 0) {

			int i = 0;

			if (args.length == 1) {
				usage();
				System.exit(0);
			}
			
			while (i < args.length && args[i].startsWith("-")) {
				// The project file name
				if (("-p".equals(args[i])) && (i < args.length - 1)) {
					if (projectName == null) {
						i++;
						projectName = args[i];
					} else {
						System.out.println("Only one -p option is allowed. ");
						System.out
								.println("try java br.jabuti.cmdtool.ImportTestCase for help.");
						System.exit(1);
					}
				} // Import only effective test case
				else if ("-cut".equals(args[i])) {
					if (cutOption == false) {
						cutOption = true;
					} else {
						System.out.println("Only one -cut option is allowed. ");
						System.out
								.println("try java cmdtool.ImportTestCase for help.");
						System.exit(1);
					}
				}// Invalid Option
				else {
					System.out.println("Unrecognized option: " + args[i]);
					System.out
							.println("try java br.jabuti.cmdtool.ImportTestCase for help.");
					System.exit(1);
				}
				i++;
			}

			if (projectName == null) {
				System.out.println("Missing project name...");
				System.exit(1);
			}

			File theFile = new File(projectName);

			if (!theFile.isFile()) // verifica se existe
			{
				System.out.println("File " + theFile.getName()
						+ " not found...");
				System.exit(1);
			}

			JabutiProject jbtProject = JabutiProject.reloadProj(theFile
					.toString(), true);

			//Trace File
			String traceFileName = jbtProject.getTraceFileName();
			File traceFile = new File(traceFileName);
			if (!traceFile.exists()) {
				System.out.println("Trace file " + traceFileName
						+ " not found...");
				System.out.println("Use br.jabuti.probe.ProberLoader to generate it...");
				System.exit(1);
			} else if (traceFile.length() == 0L) {
				System.out.println("Empty trace file " + traceFileName);
				System.out.println("No test case to be imported...");
				System.exit(1);
			}

            // Loading the test set file
            int tcn = TestSet.getNumberOfTestCases();
            if (tcn == 0) {
                TestSet.initialize(jbtProject,traceFileName);
            }
            
            if (cutOption)
            	TestSet.loadAndCutTraceFile(jbtProject);
            else
            	TestSet.loadTraceFile(jbtProject);

			int ult = TestSet.getNumberOfTestCases();
			
			System.out.println(ult - tcn + " test case imported successfully...");
			
			// Refreshing the overall coverage
			TestSet.updateOverallCoverage( jbtProject );

			// Saving the updated project
			jbtProject.saveProject();
		} else {
			usage();
		}
		System.exit(0);
	}
}