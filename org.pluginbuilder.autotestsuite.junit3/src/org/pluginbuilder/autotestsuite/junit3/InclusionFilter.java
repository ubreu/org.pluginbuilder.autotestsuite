/*******************************************************************************
 * Copyright (c) 2007 Markus Barchfeld
 * This program is distributed under the Eclipse Public License v1.0
 * which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.pluginbuilder.autotestsuite.junit3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.pluginbuilder.autotestsuite.junit3.internal.Activator;

public class InclusionFilter {

  private List<MatchSpecification> includeSpecifications = new ArrayList<MatchSpecification>();
  private List<MatchSpecification> excludeSpecifications = new ArrayList<MatchSpecification>();
  private ListSerializer<MatchSpecification> listSerializer = null;

  public InclusionFilter() {
  }

  public InclusionFilter(String inclusionList, String exclusionList) throws PatternSyntaxException {
    try {
      includeSpecifications = readPatterns( true /* isinclude */, inclusionList );
      if (exclusionList != null) {
        excludeSpecifications = readPatterns( false /* isinclude */, exclusionList );
      }
    } catch (CoreException e) {
      Activator.traceMessage( e.getMessage() );
    } catch (IOException e) {
      Activator.traceMessage( e.getMessage() );
    }
  }

  public void addIncludePattern(String pattern) throws PatternSyntaxException {
    includeSpecifications.add( new MatchSpecification( pattern ) );
  }

  public void addIncludeSpecification(MatchSpecification matchSpecification) throws PatternSyntaxException {
    includeSpecifications.add( matchSpecification );
  }

  public void addExcludePattern(String pattern) throws PatternSyntaxException {
    excludeSpecifications.add( new MatchSpecification( pattern ) );
  }

  public void addExcludeSpecification(MatchSpecification matchSpecification) throws PatternSyntaxException {
    excludeSpecifications.add( matchSpecification );
  }

  private List<MatchSpecification> readPatterns(final boolean isInclude, final String data) throws CoreException,
      IOException, PatternSyntaxException {
    return getListSerializer().deserialize( data );
  }

  private ListSerializer<MatchSpecification> createListSerializer() {
    ListSerializer<MatchSpecification> serializer = new ListSerializer<MatchSpecification>(
        new SerializerClient<MatchSpecification>() {

          public MatchSpecification getObject(String string) {
            // TODO: parser exception
            return new MatchSpecification( string );
          }

          public String getString(MatchSpecification matchSpecification) {
            return matchSpecification.getPatternSource();
          }
        } );
    return serializer;
  }

  public String serializeIncludePatterns() {
    return serialize( includeSpecifications );
  }

  public String serializeExcludePatterns() {
    return serialize( excludeSpecifications );
  }

  private String serialize(List<MatchSpecification> list) {
    return getListSerializer().serialize( list );
  }

  public boolean matches(final String typeName) {
    boolean isIncluded = false;

    for (MatchSpecification matchSpec : includeSpecifications) {
      if (matchSpec.matches( typeName )) {
        isIncluded = true;
        break;
      }
    }
    if (isIncluded) {
      for (MatchSpecification matchSpec : excludeSpecifications) {
        if (matchSpec.matches( typeName )) {
          isIncluded = false;
          break;
        }
      }
    }
    return isIncluded;
  }

  public List<MatchSpecification> getExcludeSpecifications() {
    return excludeSpecifications;
  }

  public List<MatchSpecification> getIncludeSpecifications() {
    return includeSpecifications;
  }

  public ListSerializer<MatchSpecification> getListSerializer() {
    if (listSerializer == null) {
      listSerializer = createListSerializer();
    }
    return listSerializer;
  }
}
