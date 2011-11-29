package org.pluginbuilder.autotestsuite.junit4;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.pluginbuilder.autotestsuite.junit3.BundleFilter;
import org.pluginbuilder.autotestsuite.junit3.internal.Activator;

public class IsJUnit4BundleFilter implements BundleFilter {

  public String name() {
    return "JUnit 4";
  }

  public boolean accept(Bundle bundle, Bundle host) {
    String testClassName = Test.class.getName();
    try {
      if (host.loadClass( testClassName ) != Test.class) {
        Activator.traceMessage( "Plug-in " + bundle.getSymbolicName() + " has a class " + testClassName
            + " that is not compatible with JUnit 4. Which is strange." );
        return false;
      }
      return true;
    } catch (ClassNotFoundException e) {
      Activator.traceMessage( "Could not find class " + testClassName + " in plug-in " + bundle.getSymbolicName()
          + ". Possible reason is a missing dependency on JUnit 4." );
      return false;
    }
  }

}
