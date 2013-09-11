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

import java.io.File;

import br.jabuti.util.ToolConstants;

/*
 * This class is responsable to create a new thread
 * that keep checking whether the size of the trace file
 * has changed or not.
 * When changed, the update button ( located on the menu )
 * became RED indicating that new test cases can be added.
 */
class ProbeCheck extends Thread {
	long newSize, oldSize; // control the size of the trace file

	public ProbeCheck(JabutiGUI f) {
		newSize = 0L;
		oldSize = 0L;
	}

	public void run() {
		try {
			while (!interrupted()) {
				checkProbeChanged(JabutiGUI.getProject().getTraceFileName());
				sleep(6000);
			}
		} catch (InterruptedException e) {
		}
	}

	private void checkProbeChanged(String traceFileName) {
		if (traceFileName != null) {
			try {
				File fileTrace = new File(traceFileName);

				if (fileTrace.exists()) {
					newSize = fileTrace.length();
					if (newSize != oldSize) {
						JabutiGUI.mainWindow().setUpdateLabelImage(
								JabutiGUI.mainWindow().getSemaforoRedImage());
					}
				}
			} catch (Exception e) {
				ToolConstants.reportException(e, ToolConstants.STDERR);
			}
		}
	}

	void setOldSize(long s) {
		oldSize = s;
	}

	long getOldSize() {
		return oldSize;
	}

	void setNewSize(long s) {
		newSize = s;
	}

	long getNewSize() {
		return newSize;
	}
}