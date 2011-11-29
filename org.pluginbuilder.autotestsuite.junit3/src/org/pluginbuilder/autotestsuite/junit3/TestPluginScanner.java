package org.pluginbuilder.autotestsuite.junit3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.pluginbuilder.autotestsuite.junit3.internal.Activator;

public class TestPluginScanner {
  private final String pluginInclusionPattern;
  private final String pluginExclusionPattern;
  private final InclusionFilter inclusionFilter;
  private final BundleFilter bundleFilter;

  public TestPluginScanner(String pluginInclusionPattern, String pluginExclusionPattern, BundleFilter bundleFilter) {
    if (pluginInclusionPattern == null)
      throw new IllegalArgumentException();

    this.pluginInclusionPattern = pluginInclusionPattern;
    this.pluginExclusionPattern = pluginExclusionPattern;
    this.inclusionFilter = new InclusionFilter( pluginInclusionPattern, pluginExclusionPattern );
    this.bundleFilter = bundleFilter == null ? BundleFilter.ACCEPT_ALL : bundleFilter;
  }

  public Collection<Bundle> findTestBundles() {
    Activator.traceMessage( "~~~~~~~~~~~~ Searching for " + bundleFilter.name() + " test plug-ins." );
    Activator.traceMessage( "Using inclusion regular expression(s): " + pluginInclusionPattern );
    Activator.traceMessage( "Using exclusion regular expression(s): "
        + (pluginExclusionPattern == null ? "none" : pluginExclusionPattern) );

    Set<Bundle> result = new HashSet<Bundle>();

    Bundle[] bundles = Activator.getDefault().getBundleContext().getBundles();
    for (Bundle bundle : bundles) {
      if (!inclusionFilter.matches( bundle.getSymbolicName() ))
        continue;

      boolean isFragment = Platform.isFragment( bundle );
      Bundle host = isFragment ? getFragmentHost( bundle ) : bundle;
      if (host == null) {
        Activator.traceMessage( "Unable to determine host plug-in of fragment " + bundle.getSymbolicName()
            + ". Skipping fragment." );
        continue;
      }

      if (bundleFilter.accept( bundle, host )) {
        Activator.traceMessage( "Bundle matches: " + bundle.getSymbolicName() );
        if (isFragment)
          Activator.traceMessage( "Bundle is a fragment; using host bundle " + host.getSymbolicName() + " instead." );

        result.add( host );
      }
    }

    Activator.traceMessage( "Found " + result.size() + " plug-ins." );
    return result;
  }

  /*
   * Determines the host bundle of the given fragment bundle. This is a
   * necessary step because fragment bundles cannot load classes.
   */
  private static Bundle getFragmentHost(Bundle fragment) {
    Bundle[] hosts = Platform.getHosts( fragment );

    // PackageAdmin.getHosts(Bundle) states that "A fragment may only be
    // attached to a single host bundle".
    if (hosts == null || hosts.length != 1) {
      Activator.traceMessage( "Could not determine host bundle of fragment " + fragment.getSymbolicName() );
      return null;
    }

    return hosts[0];
  }
}
