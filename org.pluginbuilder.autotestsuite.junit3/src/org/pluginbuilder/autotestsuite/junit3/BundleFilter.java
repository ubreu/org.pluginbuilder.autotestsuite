package org.pluginbuilder.autotestsuite.junit3;

import org.osgi.framework.Bundle;

public interface BundleFilter {
  BundleFilter ACCEPT_ALL = new BundleFilter() {
    public String name() {
      return "any";
    }

    public boolean accept(Bundle bundle, Bundle host) {
      return true;
    }
  };

  /**
   * Returns the name of this bundle filter.
   * 
   * @return the name of this bundle filter.
   */
  String name();

  /**
   * Tests if the given bundle should be included in a set of bundles.
   * 
   * @param bundle
   *          a plug-in or a fragment.
   * @param host
   *          the fragment's host plug-in if <code>bundle</code> is a fragment
   *          or <code>bundle</code> itself if it is a plug-in. The filter
   *          should use this host plug-in if it wants to load classes because
   *          only host bundles can do so.
   * @return <code>true</code> if this filter accepts the plug-in,
   *         <code>false</code> otherwise.
   */
  boolean accept(Bundle bundle, Bundle host);
}
