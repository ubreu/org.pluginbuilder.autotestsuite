package org.pluginbuilder.autotestsuite.junit4;

import org.pluginbuilder.autotestsuite.junit3.ClassFilter;

public class NotClassFilter implements ClassFilter {
  private final ClassFilter a;

  public NotClassFilter(ClassFilter a) {
    this.a = a;
  }

  public boolean accept(Class<?> clazz) {
    return !a.accept( clazz );
  }

  public String name() {
    return "NOT " + "a.name()";
  }

}
