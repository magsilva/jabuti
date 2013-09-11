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

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.runner.Description;
import org.junit.runner.Result;

import br.jabuti.probe.DefaultProber;

public class InstrumenterListener extends
		org.junit.internal.TextListener {
	private Set<String> testSet;

	private String traceFileName;

	private boolean enable;
	
	private PrintStream fWriter;

	public InstrumenterListener(String trace, Set<String> ts, PrintStream writer) {
		super(writer);
		traceFileName = trace;
		enable = false;
		if (ts == null)
			testSet = new HashSet<String>();
		else
			testSet = ts;
		fWriter = writer;
	}

	@Override
	public void testRunStarted(Description description) throws Exception {
		super.testRunStarted(description);
		fWriter.append(JUnitUtil.integratorName + ": Instrumentor Mode\n");
		DefaultProber.init(traceFileName);
		System.out.println("Trace file: " + traceFileName);
	}

	@Override
	public void testRunFinished(Result result) {
		super.testRunFinished(result);
		try {
		DefaultProber.finished();
		} catch (Exception e) {
			e.printStackTrace(fWriter);
		}
	}
	
	@Override
	public void testStarted(Description description) {
		String tcName = JUnitUtil.getTestCaseName(description.getDisplayName());
		if (testSet.contains(tcName)) {
			try {
				DefaultProber.startTrace(tcName);
			} catch (Exception e) {
				e.printStackTrace(fWriter);
			}
			enable = true;
			fWriter.append(JUnitUtil.traceMark);
		} else {
			super.testStarted(description);
		}
	}

	// @Override
	public void testFinished(Description description) throws Exception {
		super.testFinished(description);
		if (enable) {
			enable = false;
			DefaultProber.stopTrace();
		}
	}
}