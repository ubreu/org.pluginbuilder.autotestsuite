package org.pluginbuilder.autotestsuite.junit4;

import java.io.IOException;
import java.util.Collection;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.pluginbuilder.autotestsuite.junit3.SystemOutputCapture;

/**
 * @author Markus Wiederkehr
 */
public class JUnit4TestRunner {

  public static final class TestResult {
    private final int runCount;
    private final int failureCount;
    private final int ignoreCount;
    private final long runTime;

    TestResult(Result result) {
      runCount = result.getRunCount();
      failureCount = result.getFailureCount();
      ignoreCount = result.getIgnoreCount();
      runTime = result.getRunTime();
    }

    public int getRunCount() {
      return runCount;
    }

    public int getFailureCount() {
      return failureCount;
    }

    public int getIgnoreCount() {
      return ignoreCount;
    }

    public long getRunTime() {
      return runTime;
    }

    public boolean wasSuccessful() {
      return failureCount == 0;
    }
  }

  // Client bundles that don't depend on org.junit4 can still us this method..
  public TestResult runFormatterTests(String collectionName, Collection<Class<?>> classes,
      Collection<? extends JUnit4Formatter> formatters) {
    return new TestResult( runTests( collectionName, classes, formatters ) );
  }

  public Result runTests(String collectionName, Collection<Class<?>> classes,
      Collection<? extends RunListener> listeners) {
    JUnitCore core = new JUnitCore();

    for (RunListener listener : listeners)
      core.addListener( listener );

    Request request = RequestCompatibility.createRequest( classes );

    SystemOutputCapture capture = new SystemOutputCapture();
    capture.start();

    Result result;
    try {
      result = core.run( request );
    } finally {
      capture.stop();
    }

    for (RunListener listener : listeners) {
      if (listener instanceof JUnit4Formatter) {
        try {
          JUnit4Formatter formatter = (JUnit4Formatter) listener;
          formatter.setSystemOutput( capture.getCapturedOut() );
          formatter.setSystemError( capture.getCapturedErr() );
          formatter.save();
        } catch (IOException ignored) {
          ignored.printStackTrace();
        }
      }
    }

    return result;
  }

}
