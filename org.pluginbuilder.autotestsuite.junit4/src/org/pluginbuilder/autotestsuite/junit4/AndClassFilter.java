package org.pluginbuilder.autotestsuite.junit4;

import org.pluginbuilder.autotestsuite.junit3.ClassFilter;

public class AndClassFilter implements ClassFilter {
  private final ClassFilter a;
  private final ClassFilter b;

  public AndClassFilter(ClassFilter a, ClassFilter b) {
    this.a = a;
    this.b = b;
  }

  public boolean accept(Class<?> clazz) {
    return a.accept( clazz ) && b.accept( clazz );
  }

  public String name() {
    return a.name() + " AND " + b.name();
  }
}
