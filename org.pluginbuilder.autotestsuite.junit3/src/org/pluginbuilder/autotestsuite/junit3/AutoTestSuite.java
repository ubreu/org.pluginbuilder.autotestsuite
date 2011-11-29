/*******************************************************************************
 * Copyright (c) 2007 Markus Barchfeld
 * This program is distributed under the Eclipse Public License v1.0
 * which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.pluginbuilder.autotestsuite.junit3;

import java.lang.reflect.Method;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.pluginbuilder.autotestsuite.junit3.internal.Activator;

/*
 * Class to be run from PDE tests after plug-in have been compiled and installed
 * into an eclipse instance. The tests will not reside in the workspace but in
 * the installed plug-ins.
 */
public class AutoTestSuite extends TestSuite {

  public static final String AUTOTEST_PLUGIN = "autotestsuite.plugin";
  public static final String AUTOTEST_CLASS_INCLUSIONS = "autotestsuite.class.inclusions";
  public static final String AUTOTEST_CLASS_EXCLUSIONS = "autotestsuite.class.exclusions";
  public static final String AUTOTEST_CLASS_DEFAULT_INCLUSIONS = ".*";
  public static final String AUTOTEST_CLASS_DEFAULT_EXCLUSIONS = ".*All(Tests|PDETests|_Tests|PDETests).*";
  public static final String UNDEFINED_VARIABLE_IN_RESULTS_DIRECTORY = "The Result Directory contains an undefined variable. Please correct this and try again.";
  
  private final Bundle autotestPlugin;
  private final String autotestInclusions;
  private final String autotestExclusions;

  public AutoTestSuite() {
    this( loadAutoTestPlugin(), null, null );
  }

  public AutoTestSuite(Bundle autotestPlugin, String autotestInclusions) {
    this( autotestPlugin, autotestInclusions, null );
  }

  public AutoTestSuite(Bundle autotestPlugin, String autotestInclusions, String autotestExclusions) {
    this.autotestPlugin = autotestPlugin;
    this.autotestInclusions = Property.get( autotestInclusions, AUTOTEST_CLASS_INCLUSIONS,
        AUTOTEST_CLASS_DEFAULT_INCLUSIONS );
    this.autotestExclusions = Property.get( autotestExclusions, AUTOTEST_CLASS_EXCLUSIONS,
        AUTOTEST_CLASS_DEFAULT_EXCLUSIONS );
    createTests();
  }

  public static Test suite() {
    return new AutoTestSuite();
  }

  private void createTests() {
    setName( autotestPlugin.getSymbolicName() + " auto test suite" );

    IsJUnit3TestCaseFilter filter = new IsJUnit3TestCaseFilter();
    TestClassScanner scanner = new TestClassScanner( autotestPlugin, autotestInclusions, autotestExclusions, filter );
    Collection<Class<?>> testClasses = scanner.findTestClasses();
    if (testClasses.isEmpty()) {
      Activator.traceMessage( "Could not find any JUnit 3 test classes in plug-in " + autotestPlugin.getSymbolicName()
          + "." );
      return;
    }

    for (Class<?> testClass : testClasses) {
      addTest( getTest( testClass ) );
    }
  }

  private Test getTest(Class<?> clazz) {
    try {
      Method method = clazz.getMethod( "suite", new Class[0] );
      Object result = method.invoke( null, new Object[0] );
      if (result instanceof Test) {
        return (Test) result;
      }
    } catch (Exception e) {
    }
    return new TestSuite( clazz );
  }

  private static Bundle loadAutoTestPlugin() {
    String autotestPlugin = System.getProperty( AUTOTEST_PLUGIN );
    if (autotestPlugin == null)
      throw new RuntimeException( "The system property " + AUTOTEST_PLUGIN + " must be set!" );
    return Platform.getBundle( autotestPlugin );
  }
}
