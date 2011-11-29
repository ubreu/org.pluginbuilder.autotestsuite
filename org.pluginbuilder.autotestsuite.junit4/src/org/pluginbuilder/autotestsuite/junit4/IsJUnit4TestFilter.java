package org.pluginbuilder.autotestsuite.junit4;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Test;
import junit.framework.TestCase;

import org.pluginbuilder.autotestsuite.junit3.ClassFilter;
import org.pluginbuilder.autotestsuite.junit3.IsJUnit3TestCaseFilter;

/**
 * @author Markus Wiederkehr
 */
public final class IsJUnit4TestFilter implements ClassFilter {

  private final ClassFilter testCaseFilter = new IsJUnit3TestCaseFilter( Test.class, TestCase.class );

  public String name() {
    return "JUnit 4";
  }

  public boolean accept(Class<?> clazz) {
    // look for @Test annotated methods
    for (Method method : clazz.getMethods())
      if (isJUnit4TestMethod( method ))
        return true;

    // a bundle that depends on org.junit4 might still have JUnit 3-style
    // TestCase classes
    return testCaseFilter.accept( clazz );
  }

  private boolean isJUnit4TestMethod(Method method) {
    if (method.getAnnotation( org.junit.Test.class ) == null)
      return false;

    if (method.getReturnType() != void.class || method.getParameterTypes().length != 0)
      return false;

    int mod = method.getModifiers();
    return Modifier.isPublic( mod ) && !Modifier.isStatic( mod ) && !Modifier.isAbstract( mod );
  }

}
