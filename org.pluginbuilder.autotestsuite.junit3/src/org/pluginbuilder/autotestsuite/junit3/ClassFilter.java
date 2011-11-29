package org.pluginbuilder.autotestsuite.junit3;

public interface ClassFilter {
  ClassFilter ACCEPT_ALL = new ClassFilter() {
    public String name() {
      return "any";
    }

    public boolean accept(Class<?> clazz) {
      return true;
    }
  };

  String name();

  boolean accept(Class<?> clazz);
}
