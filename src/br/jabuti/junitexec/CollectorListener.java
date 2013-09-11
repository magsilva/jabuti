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
import java.util.HashMap;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

public class CollectorListener extends
		org.junit.internal.TextListener {
	private HashMap<String, String> testSet = new HashMap<String, String>();

	private PrintStream fWriter;
	
	public CollectorListener(PrintStream writer) {
		super(writer);
		this.fWriter= writer;
	}
	
	@Override
	public void testRunStarted(Description description) throws Exception {
		super.testRunStarted(description);
		fWriter.append(JUnitUtil.integratorName + ": Collector Mode\n");
	}

	@Override
	public void testStarted(Description description) {
		super.testStarted(description);
		String tc = JUnitUtil.getTestCaseName(description.getDisplayName());
		// System.out.println("Begin: " + tc);
		testSet.put(tc, JUnitUtil.SUCCESS);
	}

	@Override
	public void testFinished(Description description) throws Exception {
		super.testFinished(description);
		//String tc = getTestCaseName(description.getDisplayName());
		// System.out.println("End: " + tc);
	}

	@Override
	public void testFailure(Failure failure) {
		super.testFailure(failure);
		String tc = JUnitUtil.getTestCaseName(failure.getDescription().getDisplayName());
		testSet.put(tc, JUnitUtil.FAILURE);
	}

	@Override
	public void testIgnored(Description description) {
		super.testIgnored(description);
		String tc = JUnitUtil.getTestCaseName(description.getDisplayName());
		testSet.put(tc, JUnitUtil.IGNORED);
	}

	public HashMap<String, String> getTestSet() {
		return testSet;
	}
}