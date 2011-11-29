package org.pluginbuilder.autotestsuite.junit3;

import java.util.ArrayList;
import java.util.List;

public class ListSerializer<T> {

  private static final String SEPARATOR = ";";
  private static final String ESCAPED_SEPARATOR = "\\;";
  private final SerializerClient<T> client;

  public ListSerializer(SerializerClient<T> client) {
    this.client = client;
  }

  public String serialize(List<T> list) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < list.size(); i++) {
      T element = list.get( i );
      String strRepresentation = client.getString( element );
      sb.append( strRepresentation.replaceAll( SEPARATOR, ESCAPED_SEPARATOR ) );
      if (i != list.size() - 1) {
        sb.append( SEPARATOR );
      }
    }
    return sb.toString();
  }

  public List<T> deserialize(String str) {
    List<T> result = new ArrayList<T>();
    // (?<=) matches via zero-width positive lookbehind, see Pattern
    // String subsitutedLine = line.replaceAll( "(?<=[^\\\\])\\\\n", "\n" );
    String[] elements = str.split( "(?<=[^\\\\])" + SEPARATOR );
    for (int i = 0; i < elements.length; i++) {
      result.add( client.getObject( elements[i] ) );
    }
    return result;
  }
}
