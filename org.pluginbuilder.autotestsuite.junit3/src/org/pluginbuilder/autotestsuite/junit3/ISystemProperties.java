package org.pluginbuilder.autotestsuite.junit3;

import java.io.File;

public interface ISystemProperties {
  public static final String AUTOTEST_DEBUG = "autotest.debug";
  public static final String AUTOTEST_HEADLESS = "autotestsuite.headless";
  public static final String AUTOTEST_IN_IDE = "autotestsuite.in.ide";
  public static final String AUTOTEST_RESULTS_DIRECTORY = "autotestsuite.results.directory";
  public static final String AGGREGATED_REPORT_BASENAME = "autotestsuite-results";
  public static final String AUTOTEST_RESULTS_DIRECTORY_DEFAULT = System.getProperty("java.io.tmpdir") + File.separator + AGGREGATED_REPORT_BASENAME;

  /**
   * A non-null value serving as a marker for an invalid directory. The value is
   * never shown to the user.
   */
  public static final String INVALID_AUTOTEST_RESULTS_DIRECTORY = "INVALID_AUTOTEST_RESULTS_DIRECTORY";
}
