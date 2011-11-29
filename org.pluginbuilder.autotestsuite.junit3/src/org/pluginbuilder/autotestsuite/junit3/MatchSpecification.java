/**
 *
 */
package org.pluginbuilder.autotestsuite.junit3;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MatchSpecification {

  protected Pattern pattern;
  protected String patternSource;

  public MatchSpecification(String patternSource) throws PatternSyntaxException {
    this.setPatternSource( patternSource );
  }

  public Pattern getPattern() {
    return pattern;
  }

  public boolean matches(String arg) {
    return pattern.matcher( arg ).matches();
  }

  public String getPatternSource() {
    return patternSource;
  }

  public void setPatternSource(String patternSource) throws PatternSyntaxException {
    this.pattern = Pattern.compile( patternSource );
    this.patternSource = patternSource;
  }
}