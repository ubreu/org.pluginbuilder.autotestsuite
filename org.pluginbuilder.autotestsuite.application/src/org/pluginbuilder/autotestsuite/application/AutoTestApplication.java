package org.pluginbuilder.autotestsuite.application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.XSLTProcess;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator;
import org.apache.tools.ant.types.FileSet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.testing.ITestHarness;
import org.eclipse.ui.testing.TestableObject;
import org.osgi.framework.Bundle;
import org.pluginbuilder.autotestsuite.junit3.AllTestSuite;
import org.pluginbuilder.autotestsuite.junit3.AutoTestSuite;
import org.pluginbuilder.autotestsuite.junit3.ISystemProperties;
import org.pluginbuilder.autotestsuite.junit4.JUnit4Formatter;
import org.pluginbuilder.autotestsuite.junit4.JUnit4TestRunner;
import org.pluginbuilder.autotestsuite.junit4.JUnit4Tests;
import org.pluginbuilder.autotestsuite.junit4.XmlJUnit4Formatter;
import org.pluginbuilder.util.FileCopy;

public class AutoTestApplication implements IApplication, ITestHarness, ISystemProperties {

  private TestableObject testableObject;
  private IApplicationContext context;
  private int testRunnerResult;
  private File resultsDirectory;
  private static final String RESULTS_DIRECTORY_ABSOLUTE_PATH = System.getProperty( AUTOTEST_RESULTS_DIRECTORY );

  private static final String AUTO_TEST_SUITE_SUFFIX = ".AutoTestSuite";
  private static final String JUNIT_XSL = "JUNIT.XSL";
  private static final String TEST_RESULT_EXTENSION = ".xml";

  /**
   * @see IApplication#start(IApplicationContext)
   */
  public Object start(IApplicationContext context) throws Exception {
    this.context = context;

    // validate results directory
    if (INVALID_AUTOTEST_RESULTS_DIRECTORY.equals( RESULTS_DIRECTORY_ABSOLUTE_PATH )) {
      System.err.println( AutoTestSuite.UNDEFINED_VARIABLE_IN_RESULTS_DIRECTORY );
      return IApplication.EXIT_RELAUNCH;
    }

    this.resultsDirectory = getResultsDirectory();

    String[] args = (String[]) context.getArguments().get( IApplicationContext.APPLICATION_ARGS );
    if (args == null)
      args = new String[0];

    return run( args );
  }

  /**
   * @see IApplication#stop()
   */
  public void stop() {
  }

  /**
   * @see ITestHarness#runTests()
   */
  public void runTests() {
    testableObject.testingStarting();
    testableObject.runTest( new Runnable() {
      public void run() {
        runAllJUnitTestsOnUIThread();
      }
    } );
    runAllJUnit4TestsOnNonUIThread();
    testableObject.testingFinished();
  }

  private int run(String[] args) throws CoreException, Exception {
    testRunnerResult = AutoTestRunner.SUCCESS;

    if (isHeadlessMode()) {
      runHeadless( args );
    } else {
      // Get the application to test
      String applicationToRun = getApplicationToRun( args );
      IApplication application = getApplication( applicationToRun );
      if (application == null) {
        System.err.println( "Failed to locate test application \"" + applicationToRun + "\"" );
        return AutoTestRunner.ERRORS;
      }

      runApplication( application, args );
    }

    // process and display results if we are running inside the IDE
    if (isRunningInIDE()) {
      aggregateAndDisplayResults();
    }

    return testRunnerResult;
  }

  private boolean isRunningInIDE() {
    return Boolean.parseBoolean( System.getProperty( AUTOTEST_IN_IDE ) );
  }

  private String getApplicationToRun(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals( "-testApplication" ) && i < args.length - 1) //$NON-NLS-1$
        return args[i + 1];
    }
    return "org.eclipse.ui.ide.workbench";
  }

  /*
   * return the application to run, or null if not even the default application
   * is found.
   */
  private IApplication getApplication(String applicationToRun) throws CoreException {
    // Assume we are in 3.0 mode.
    // Find the name of the application as specified by the PDE JUnit launcher.
    // If no application is specified, the 3.0 default workbench application
    // is returned.
    IExtension extension = Platform.getExtensionRegistry().getExtension( Platform.PI_RUNTIME, Platform.PT_APPLICATIONS,
        applicationToRun );
    if (extension == null)
      return null;

    // If the extension does not have the correct grammar, return null.
    // Otherwise, return the application object.
    IConfigurationElement[] elements = extension.getConfigurationElements();
    if (elements.length > 0) {
      IConfigurationElement[] runs = elements[0].getChildren( "run" ); //$NON-NLS-1$
      if (runs.length > 0) {
        Object runnable = runs[0].createExecutableExtension( "class" ); //$NON-NLS-1$
        if (runnable instanceof IApplication)
          return (IApplication) runnable;
      }
    }

    return null;
  }

  private void runHeadless(String[] args) {
    runAllJUnitTestsOnUIThread();
  }

  private Object runApplication(IApplication application, String[] args) throws Exception {
    testableObject = PlatformUI.getTestableObject();
    testableObject.setTestHarness( this );
    return application.start( context );
  }

  private void runAllJUnitTestsOnUIThread() {
    // clear results directory
    final Delete delete = new Delete();
    delete.setDir( resultsDirectory );
    delete.execute();
    resultsDirectory.mkdirs();

    runAllJUnit3Tests();
    runAllJUnit4Tests();
  }

  /**
   * Aggregates results using the same technique used in the "run" target in
   * run-tests.xml. The resulting HTML is then displayed in the SWT browser
   * widget if possible.
   */
  private void aggregateAndDisplayResults() {
    // use an artificial project
    final Project project = new Project();

    // run <junitreport> task

    final String junitReportResult = AGGREGATED_REPORT_BASENAME + TEST_RESULT_EXTENSION;
    final XMLResultAggregator junitReport = new XMLResultAggregator();
    junitReport.setProject( project );
    final FileSet fileSet = new FileSet();
    fileSet.setDir( resultsDirectory );
    fileSet.setIncludes( "*" + TEST_RESULT_EXTENSION );
    junitReport.addFileSet( fileSet );
    junitReport.setTodir( resultsDirectory );
    junitReport.setTofile( junitReportResult );
    junitReport.execute();

    // run <xslt> task

    final InputStream in = getClass().getResourceAsStream( "/" + JUNIT_XSL );
    final File xslFile = new File( resultsDirectory, JUNIT_XSL );
    try {
      // copy file from inside bundle
      FileCopy.copyStreamToFile( in, xslFile );
    } catch (IOException e) {
      throw new IllegalStateException( "Could not copy " + JUNIT_XSL + " to results directory" );
    }

    final XSLTProcess xslt = new XSLTProcess();
    xslt.setProject( project );
    xslt.setStyle( xslFile.getAbsolutePath() );
    xslt.setBasedir( resultsDirectory );
    xslt.setIncludes( junitReportResult );
    xslt.setDestdir( resultsDirectory );
    xslt.execute();

    // show overall success/failure message in the console
    final boolean success = (testRunnerResult == AutoTestRunner.SUCCESS);
    final String summary = "TEST RESULT: "
        + (success ? "all tests passed" : "there were some test failures or errors\n");
    System.out.println( summary );

    displayInBrowser( resultsDirectory );
  }

  @SuppressWarnings("deprecation")
  private void displayInBrowser(final File resultsDirectory) {
    // display in browser
    final File junitReportHtml = new File( resultsDirectory, AGGREGATED_REPORT_BASENAME + ".html" );
    final String path = junitReportHtml.getAbsolutePath();
    try {
      final URL url = junitReportHtml.toURL();
      System.out.println( "Opening " + path + " in a browser..." );

      final IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
      final IWebBrowser browser = browserSupport.createBrowser( IWorkbenchBrowserSupport.AS_VIEW,
          "autotestsuite-results", "Autotestsuite Results", "" );
      browser.openURL( url );
    } catch (Exception e) {
      System.err.println( "Could not open browser to view test results at " + path );
    }
  }

  private void runAllJUnit3Tests() {
    AllTestSuite allTestSuite = new AllTestSuite();
    Collection<Bundle> testBundles = allTestSuite.findTestBundles();
    for (Bundle bundle : testBundles) {
      // the XMLJUnitResultFormatter will later split up the name in class and
      // package part
      JUnitTest unitTest = new JUnitTest( bundle.getSymbolicName() + AUTO_TEST_SUITE_SUFFIX );
      Properties props = new Properties();
      props.putAll( System.getProperties() );
      unitTest.setProperties( props );
      AutoTestSuite autoTestSuite = allTestSuite.getAutoTestSuite( bundle );
      AutoTestRunner autoTestRunner = new AutoTestRunner( unitTest, bundle.getSymbolicName(), autoTestSuite,
          resultsDirectory );
      mergeTestResult( autoTestRunner.run() );
    }
  }

  private void runAllJUnit4Tests() {
    JUnit4Tests jUnit4Tests = new JUnit4Tests();
    for (Bundle bundle : jUnit4Tests.findTestBundles()) {
      Collection<Class<?>> testClasses = jUnit4Tests.findTestClasses( bundle );
      if (testClasses.isEmpty()) {
        continue;
      }
      runFormatterTests( bundle, testClasses, "" );
    }
  }

  private void runAllJUnit4TestsOnNonUIThread() {
    JUnit4Tests jUnit4Tests = new JUnit4Tests();
    for (Bundle bundle : jUnit4Tests.findTestBundles()) {
      Collection<Class<?>> testClasses = jUnit4Tests.findNonUIThreadTestClasses( bundle );
      if (testClasses.isEmpty()) {
        continue;
      }
      runFormatterTests( bundle, testClasses, "_swtbot" );
    }
  }

  private void runFormatterTests(Bundle bundle, Collection<Class<?>> testClasses, String fileNameExtension) {
    String collectionName = bundle.getSymbolicName() + AUTO_TEST_SUITE_SUFFIX;

    List<JUnit4Formatter> formatters = new ArrayList<JUnit4Formatter>();
    File file = new File( resultsDirectory, bundle.getSymbolicName() + fileNameExtension + TEST_RESULT_EXTENSION );
    formatters.add( new XmlJUnit4Formatter( file ) );

    JUnit4TestRunner testRunner = new JUnit4TestRunner();
    boolean success = testRunner.runFormatterTests( collectionName, testClasses, formatters ).wasSuccessful();
    mergeTestResult( success ? AutoTestRunner.SUCCESS : AutoTestRunner.FAILURES );
  }

  private void mergeTestResult(int result) {
    testRunnerResult = Math.max( testRunnerResult, result );
  }

  private boolean isHeadlessMode() {
    final String result = System.getProperty( AUTOTEST_HEADLESS );
    return Boolean.parseBoolean( result );
  }

  private File getResultsDirectory() throws CoreException {
    return new File( RESULTS_DIRECTORY_ABSOLUTE_PATH );
  }
}
