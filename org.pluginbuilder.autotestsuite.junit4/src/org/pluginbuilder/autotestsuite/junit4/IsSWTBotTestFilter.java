package org.pluginbuilder.autotestsuite.junit4;

import org.osgi.framework.Bundle;
import org.pluginbuilder.autotestsuite.junit3.ClassFilter;

public class IsSWTBotTestFilter implements ClassFilter {

  @SuppressWarnings("rawtypes")
private Class swtbotTestCase;

  public IsSWTBotTestFilter(Bundle bundle) {
    try {
      swtbotTestCase = bundle.loadClass( "org.eclipse.swtbot.swt.finder.SWTBotTestCase" );
    } catch (ClassNotFoundException e) {
    	// nop
    }
  }

  @SuppressWarnings("unchecked")
  public boolean accept(Class<?> clazz) {
    if (swtbotTestCase == null) {
      return false;
    }
    return swtbotTestCase.isAssignableFrom( clazz );
  }

  public String name() {
    return "SWTBotTestCase";
  }

}
