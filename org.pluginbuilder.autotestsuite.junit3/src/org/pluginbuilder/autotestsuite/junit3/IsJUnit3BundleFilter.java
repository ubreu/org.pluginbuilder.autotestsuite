package org.pluginbuilder.autotestsuite.junit3;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.pluginbuilder.autotestsuite.junit3.internal.Activator;

public class IsJUnit3BundleFilter implements BundleFilter {

  public String name() {
    return "JUnit 3";
  }

  public boolean accept(Bundle bundle, Bundle host) {
    String testCaseClassName = TestCase.class.getName();
    try {
      if (host.loadClass( testCaseClassName ) != TestCase.class) {
        Activator.traceMessage( "Plug-in " + bundle.getSymbolicName() + " has a class " + testCaseClassName
            + " that is not compatible with JUnit 3. Maybe it is a JUnit 4 test bundle." );
        return false;
      }
      return true;
    } catch (ClassNotFoundException e) {
      Activator.traceMessage( "Could not find class " + testCaseClassName + " in plug-in " + bundle.getSymbolicName()
          + ". Possible reason is a missing dependency on JUnit 3." );
      return false;
    }
  }

}
