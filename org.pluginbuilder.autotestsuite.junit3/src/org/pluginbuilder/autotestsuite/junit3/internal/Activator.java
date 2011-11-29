/*******************************************************************************
 * Copyright (c) 2007 Markus Barchfeld
 * This program is distributed under the Eclipse Public License v1.0
 * which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.pluginbuilder.autotestsuite.junit3.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.pluginbuilder.autotestsuite.junit3.ISystemProperties;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin implements ISystemProperties {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.pluginbuilder.autotestsuite.junit3";
  // The shared instance
  private static Activator plugin;
  private static final String DEBUG_OPTION_TRACE_TESTFINDING = "org.pluginbuilder.autotestsuite.junit3/trace/testfinding"; //$NON-NLS-1$
  private static boolean trace;
  private BundleContext bundleContext;
  static {
    String value = Platform.getDebugOption( DEBUG_OPTION_TRACE_TESTFINDING );
    trace = value != null && value.equalsIgnoreCase( "true" ); //$NON-NLS-1$
    if (!trace) {
      // allow switching on of trace via system property as well since it can
      // be complicated (actually I just don't know if it can be done at all)
      // to enable tracing when running eclipse with the uitest application
      String autotestDebug = System.getProperty( AUTOTEST_DEBUG );
      trace = autotestDebug != null && autotestDebug.toLowerCase().equals( "true" );
    }
  }

  /**
   * The constructor
   */
  public Activator() {
    plugin = this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    this.bundleContext = context;
    super.start( context );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop( context );
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  public static void traceMessage(String message) {
    if (trace) {
      System.out.println( message );
    }
  }

  public BundleContext getBundleContext() {
    return bundleContext;
  }
}
