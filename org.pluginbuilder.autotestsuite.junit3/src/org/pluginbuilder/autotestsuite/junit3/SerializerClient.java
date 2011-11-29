package org.pluginbuilder.autotestsuite.junit3;

public interface SerializerClient<T> {
  public String getString(T object);

  public T getObject(String string);
}
