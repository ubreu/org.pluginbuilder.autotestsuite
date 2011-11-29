package org.pluginbuilder.autotestsuite.junit4;

import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.runner.Request;

public class RequestCompatibility {

  public static Request createRequest(Collection<Class<?>> classes) {
    Class<?>[] classesArray = classes.toArray( new Class<?>[classes.size()] );
    // junit < 4.5 => Request.classes has two arguments
    // junit >= 4.5 => Request.classes has one argument only
    try {
      try {
        Method method = Request.class.getMethod( "classes", String.class, classesArray.getClass() );
        // the string argument is not used later, in the final test report the
        // the tests are grouped by the file name of the XML JUnit report.
        return (Request) method.invoke( null, "tests", classesArray );
      } catch (NoSuchMethodException nsme) {
        Method method = Request.class.getMethod( "classes", classesArray.getClass() );
        return (Request) method.invoke( null, (Object) classesArray );
      }
    } catch (Exception ex) {
      throw new RuntimeException(
          RequestCompatibility.class.getName()
              + ": Can not find or call static method 'classes' for class 'Request'. This could be an issue with the version of junit4.", ex );
    }
  }
}
