package org.pluginbuilder.autotestsuite.junit3;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Test;
import junit.framework.TestCase;

import org.pluginbuilder.autotestsuite.junit3.internal.Activator;

public final class IsJUnit3TestCaseFilter implements ClassFilter {

  private final Class<?> testClass;
  private final Class<?> testCaseClass;

  public IsJUnit3TestCaseFilter() {
    this.testClass = Test.class;
    this.testCaseClass = TestCase.class;
  }

  public IsJUnit3TestCaseFilter(Class<?> testClass, Class<?> testCaseClass) {
    this.testClass = testClass;
    this.testCaseClass = testCaseClass;
  }

  public String name() {
    return "JUnit 3";
  }

  public boolean accept(Class<?> clazz) {
    if (!testCaseClass.isAssignableFrom( clazz ))
      return false;

    for (Method method : clazz.getMethods())
      if (isJUnit3SuiteMethod( method ) || isJUnit3TestMethod( method ))
        return true;

    Activator.traceMessage( "No test or suite methods found in " + clazz.getName() );
    return false;
  }

  private boolean isJUnit3SuiteMethod(Method method) {
    if (!method.getName().equals( "suite" ))
      return false;

    if (method.getReturnType() != testClass || method.getParameterTypes().length != 0)
      return false;

    int mod = method.getModifiers();
    return Modifier.isPublic( mod ) && Modifier.isStatic( mod ) && !Modifier.isAbstract( mod );
  }

  private boolean isJUnit3TestMethod(Method method) {
    if (!method.getName().startsWith( "test" ))
      return false;

    if (method.getReturnType() != void.class || method.getParameterTypes().length != 0)
      return false;

    int mod = method.getModifiers();
    return Modifier.isPublic( mod ) && !Modifier.isStatic( mod ) && !Modifier.isAbstract( mod );
  }
}