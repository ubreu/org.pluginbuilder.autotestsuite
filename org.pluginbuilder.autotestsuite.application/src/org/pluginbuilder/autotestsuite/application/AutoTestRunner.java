/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.pluginbuilder.autotestsuite.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import junit.framework.TestResult;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.pluginbuilder.autotestsuite.junit3.AutoTestSuite;
import org.pluginbuilder.autotestsuite.junit3.SystemOutputCapture;

/**
 * A TestRunner for JUnit that supports Ant JUnitResultFormatters and running
 * tests inside Eclipse. Example call: EclipseTestRunner -classname
 * junit.samples.SimpleTest
 * formatter=org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter
 */
public class AutoTestRunner {

  class TestFailedException extends Exception {

    private static final long serialVersionUID = 6009335074727417445L;

    TestFailedException(String message) {
      super( message );
    }

    TestFailedException(Throwable e) {
      super( e );
    }
  }

  /**
   * No problems with this test.
   */
  public static final int SUCCESS = 0;
  /**
   * Some tests failed.
   */
  public static final int FAILURES = 1;
  /**
   * An error occured.
   */
  public static final int ERRORS = 2;
  /**
   * The current test result
   */
  private TestResult fTestResult;

  /**
   * The corresponding testsuite.
   */
  private AutoTestSuite fSuite;
  /**
   * The TestSuite we are currently running.
   */
  private JUnitTest fJunitTest;
  /**
   * Returncode
   */
  private int fRetCode = SUCCESS;
  JUnitResultFormatter formatter;

  /**
   * 
   */
  public AutoTestRunner(JUnitTest test, String testPluginName, AutoTestSuite autoTestSuite, File outputDirectory) {
    fJunitTest = test;
    fSuite = autoTestSuite;
    formatter = new XMLJUnitResultFormatter();
    File file = new File( outputDirectory, testPluginName + ".xml" );
    try {
      formatter.setOutput( new FileOutputStream( file ) );
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void runFailed(String message) throws TestFailedException {
    System.err.println( message );
    throw new TestFailedException( message );
  }

  protected void runFailed(Throwable e) throws TestFailedException {
    e.printStackTrace();
    throw new TestFailedException( e );
  }

  protected void clearStatus() {
  }

  public int run() {
    // IPerformanceMonitor pm = PerfMsrCorePlugin.getPerformanceMonitor(true);
    fTestResult = new TestResult();
    // fTestResult.addListener( this );
    fTestResult.addListener( formatter );
    long start = System.currentTimeMillis();
    fireStartTestSuite();

    SystemOutputCapture capture = new SystemOutputCapture();
    capture.start();
    try {
      // pm.snapshot(1); // before
      fSuite.run( fTestResult );
    } finally {
      // pm.snapshot(2); // after
      capture.stop();
      sendOutAndErr( capture.getCapturedOut(), capture.getCapturedErr() );
      fJunitTest.setCounts( fTestResult.runCount(), fTestResult.failureCount(), fTestResult.errorCount() );
      fJunitTest.setRunTime( System.currentTimeMillis() - start );
    }

    fireEndTestSuite();
    if (fRetCode != SUCCESS || fTestResult.errorCount() != 0) {
      fRetCode = ERRORS;
    } else if (fTestResult.failureCount() != 0) {
      fRetCode = FAILURES;
    }
    // pm.upload(getClass().getName());
    return fRetCode;
  }

  /**
   * Returns what System.exit() would return in the standalone version.
   * 
   * @return 2 if errors occurred, 1 if tests failed else 0.
   */
  public int getRetCode() {
    return fRetCode;
  }

  private void fireStartTestSuite() {
    formatter.startTestSuite( fJunitTest );
  }

  private void fireEndTestSuite() {
    formatter.endTestSuite( fJunitTest );
  }

  private void sendOutAndErr(String out, String err) {
    formatter.setSystemOutput( out );
    formatter.setSystemError( err );
  }

}
