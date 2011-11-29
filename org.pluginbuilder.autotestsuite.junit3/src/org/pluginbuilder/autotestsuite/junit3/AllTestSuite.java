/*******************************************************************************
 * Copyright (c) 2007 Markus Barchfeld
 * This program is distributed under the Eclipse Public License v1.0
 * which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.pluginbuilder.autotestsuite.junit3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.osgi.framework.Bundle;

public class AllTestSuite extends TestSuite {

  public static final String AUTOTEST_PLUGIN_INCLUSIONS = "autotestsuite.plugin.inclusions";
  public static final String AUTOTEST_PLUGIN_EXCLUSIONS = "autotestsuite.plugin.exclusions";
  public static final String AUTOTEST_PLUGIN_DEFAULT_INCLUSIONS = ".*(test|tests)$";
  public static final String AUTOTEST_PLUGIN_DEFAULT_EXCLUSIONS = null;

  private final String pluginInclusionPattern;
  private final String pluginExclusionPattern;
  private final String classInclusionPattern;
  private final String classExclusionPattern;

  private Map<Bundle, AutoTestSuite> testSuites = new HashMap<Bundle, AutoTestSuite>();

  public static Test suite() {
    return new AllTestSuite();
  }

  public AllTestSuite() {
    this( "AllTestSuite", null );
  }

  public AllTestSuite(String name, String pluginInclusionPattern) {
    this( name, pluginInclusionPattern, null );
  }

  public AllTestSuite(String name, String pluginInclusionPattern, String pluginExclusionPattern) {
    this( name, pluginInclusionPattern, pluginExclusionPattern, null, null );
  }

  public AllTestSuite(String name, String pluginInclusionPattern, String pluginExclusionPattern,
      String classInclusionPattern, String classExclusionPattern) {
    super( name );
    this.pluginInclusionPattern = Property.get( pluginInclusionPattern, AUTOTEST_PLUGIN_INCLUSIONS,
        AUTOTEST_PLUGIN_DEFAULT_INCLUSIONS );
    this.pluginExclusionPattern = Property.get( pluginExclusionPattern, AUTOTEST_PLUGIN_EXCLUSIONS,
        AUTOTEST_PLUGIN_DEFAULT_EXCLUSIONS );
    this.classInclusionPattern = classInclusionPattern;
    this.classExclusionPattern = classExclusionPattern;
    this.createAutoTestSuites();
  }

  public Collection<Bundle> findTestBundles() {
    return testSuites.keySet();
  }

  public AutoTestSuite getAutoTestSuite(Bundle bundle) {
    return testSuites.get( bundle );
  }

  private void createAutoTestSuites() {
    BundleFilter filter = new IsJUnit3BundleFilter();
    TestPluginScanner scanner = new TestPluginScanner( pluginInclusionPattern, pluginExclusionPattern, filter );
    Collection<Bundle> bundles = scanner.findTestBundles();

    for (Bundle bundle : bundles) {
      AutoTestSuite autoTestSuite = new AutoTestSuite( bundle, classInclusionPattern, classExclusionPattern );
      if (autoTestSuite.testCount() > 0) {
        this.addTest( autoTestSuite );
        testSuites.put( bundle, autoTestSuite );
      }
    }
  }
}
