package org.pluginbuilder.autotestsuite.junit3;

public class Property {
  public static String get(String value, String propertyName, String defaultValue) {
    if (value != null)
      return value;

    String property = System.getProperty( propertyName );
    return property == null ? defaultValue : property;
  }
}
