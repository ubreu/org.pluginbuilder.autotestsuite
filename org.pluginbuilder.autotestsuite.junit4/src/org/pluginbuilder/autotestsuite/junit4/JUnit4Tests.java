package org.pluginbuilder.autotestsuite.junit4;

import java.util.Collection;

import org.osgi.framework.Bundle;
import org.pluginbuilder.autotestsuite.junit3.AllTestSuite;
import org.pluginbuilder.autotestsuite.junit3.AutoTestSuite;
import org.pluginbuilder.autotestsuite.junit3.BundleFilter;
import org.pluginbuilder.autotestsuite.junit3.ClassFilter;
import org.pluginbuilder.autotestsuite.junit3.Property;
import org.pluginbuilder.autotestsuite.junit3.TestClassScanner;
import org.pluginbuilder.autotestsuite.junit3.TestPluginScanner;

/**
 * @author Markus Wiederkehr
 */
public class JUnit4Tests {

  public static final String PLUGIN_INCLUSIONS_PROPERTY = AllTestSuite.AUTOTEST_PLUGIN_INCLUSIONS;
  public static final String PLUGIN_EXCLUSIONS_PROPERTY = AllTestSuite.AUTOTEST_PLUGIN_EXCLUSIONS;
  public static final String PLUGIN_INCLUSIONS_DEFAULT = AllTestSuite.AUTOTEST_PLUGIN_DEFAULT_INCLUSIONS;
  public static final String PLUGIN_EXCLUSIONS_DEFAULT = AllTestSuite.AUTOTEST_PLUGIN_DEFAULT_EXCLUSIONS;

  public static final String CLASS_INCLUSIONS_PROPERTY = AutoTestSuite.AUTOTEST_CLASS_INCLUSIONS;
  public static final String CLASS_EXCLUSIONS_PROPERTY = AutoTestSuite.AUTOTEST_CLASS_EXCLUSIONS;
  public static final String CLASS_INCLUSIONS_DEFAULT = AutoTestSuite.AUTOTEST_CLASS_DEFAULT_INCLUSIONS;
  public static final String CLASS_EXCLUSIONS_DEFAULT = AutoTestSuite.AUTOTEST_CLASS_DEFAULT_EXCLUSIONS;

  private final String pluginInclusionPattern;
  private final String pluginExclusionPattern;

  private final String classInclusionPattern;
  private final String classExclusionPattern;

  public JUnit4Tests() {
    this( null, null, null, null );
  }

  public JUnit4Tests(String pluginInclusionPattern, String pluginExclusionPattern, String classInclusionPattern,
      String classExclusionPattern) {
    this.pluginInclusionPattern = Property.get( pluginInclusionPattern, PLUGIN_INCLUSIONS_PROPERTY,
        PLUGIN_INCLUSIONS_DEFAULT );
    this.pluginExclusionPattern = Property.get( pluginExclusionPattern, PLUGIN_EXCLUSIONS_PROPERTY,
        PLUGIN_EXCLUSIONS_DEFAULT );
    this.classInclusionPattern = Property.get( classInclusionPattern, CLASS_INCLUSIONS_PROPERTY,
        CLASS_INCLUSIONS_DEFAULT );
    this.classExclusionPattern = Property.get( classExclusionPattern, CLASS_EXCLUSIONS_PROPERTY,
        CLASS_EXCLUSIONS_DEFAULT );
  }

  public Collection<Bundle> findTestBundles() {
    BundleFilter filter = new IsJUnit4BundleFilter();
    TestPluginScanner scanner = new TestPluginScanner( pluginInclusionPattern, pluginExclusionPattern, filter );
    return scanner.findTestBundles();
  }

  public Collection<Class<?>> findTestClasses(Bundle bundle) {
    ClassFilter filter = new AndClassFilter( new IsJUnit4TestFilter(), new NotClassFilter( new IsSWTBotTestFilter(
        bundle ) ) );
    TestClassScanner scanner = new TestClassScanner( bundle, classInclusionPattern, classExclusionPattern, filter );
    return scanner.findTestClasses();
  }

  public Collection<Class<?>> findNonUIThreadTestClasses(Bundle bundle) {
    ClassFilter filter = new IsSWTBotTestFilter( bundle );
    TestClassScanner scanner = new TestClassScanner( bundle, classInclusionPattern, classExclusionPattern, filter );
    return scanner.findTestClasses();
  }

}
