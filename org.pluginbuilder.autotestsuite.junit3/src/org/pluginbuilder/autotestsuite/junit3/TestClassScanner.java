package org.pluginbuilder.autotestsuite.junit3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.objectweb.asm.ClassReader;
import org.osgi.framework.Bundle;
import org.pluginbuilder.autotestsuite.junit3.internal.Activator;

public class TestClassScanner {
  private static final boolean TRACE_JAR_ENTRIES = false;
  private static final boolean TRACE_DIR_ENTRIES = false;

  private final Bundle bundle;
  private final String classInclusionPattern;
  private final String classExclusionPattern;
  private final InclusionFilter inclusionFilter;
  private final ClassFilter classFilter;

  public TestClassScanner(Bundle bundle, String classInclusionPattern, String classExclusionPattern,
      ClassFilter classFilter) {
    if (bundle == null)
      throw new IllegalArgumentException();
    if (classInclusionPattern == null)
      throw new IllegalArgumentException();

    this.bundle = bundle;
    this.classInclusionPattern = classInclusionPattern;
    this.classExclusionPattern = classExclusionPattern;
    this.inclusionFilter = new InclusionFilter( classInclusionPattern, classExclusionPattern );
    this.classFilter = classFilter == null ? ClassFilter.ACCEPT_ALL : classFilter;
  }

  public Collection<Class<?>> findTestClasses() {
    Activator.traceMessage( "~~~~~~ Searching for " + classFilter.name() + " test classes in "
        + bundle.getSymbolicName() );
    Activator.traceMessage( "Using inclusion regular expression(s): " + classInclusionPattern );
    Activator.traceMessage( "Using exclusion regular expression(s): "
        + (classExclusionPattern == null ? "none" : classExclusionPattern) );

    Set<Class<?>> classes = new HashSet<Class<?>>();

    findTestsInJarFiles( classes );
    findTestsInBinDirectory( classes );

    return classes;
  }

  private void findTestsInJarFiles(Set<Class<?>> classes) {
    Enumeration<URL> enumeration = findBundleEntries( "/", "*.jar", true );
    if (enumeration == null) {
      Activator.traceMessage( "The plug-in " + bundle.getSymbolicName()
          + " does not contain any jar files to search for tests." );
      return;
    }

    while (enumeration.hasMoreElements()) {
      URL jarBundleUrl = enumeration.nextElement();
      findTestCasesInJarFile( classes, jarBundleUrl );
    }
  }

  private void findTestCasesInJarFile(Set<Class<?>> classes, URL jarBundleUrl) {
    Activator.traceMessage( "Searching " + jarBundleUrl + " for tests." );
    JarFile jarFile = getJarFile( jarBundleUrl );
    if (jarFile == null)
      return;

    int count = 0;
    for (Enumeration<JarEntry> jarFileEntries = jarFile.entries(); jarFileEntries.hasMoreElements(); count++) {
      JarEntry jarEntry = jarFileEntries.nextElement();
      String jarEntryName = jarEntry.getName();

      if (TRACE_JAR_ENTRIES)
        Activator.traceMessage( "Jar-Entry: " + jarEntryName );

      if (!jarEntryName.endsWith( ".class" ))
        continue;

      String className = getClassName( jarEntryName );
      addClassSafely( classes, className );
    }

    Activator.traceMessage( "Scanned " + count + " Jar entries." );
  }

  private String getClassName(String classFileName) {
    return classFileName.replaceAll( ".class$", "" ).replaceAll( "/", "." );
  }

  private void findTestsInBinDirectory(Set<Class<?>> classes) {
    Activator.traceMessage( "Searching bundle directory for tests." );
    Enumeration<URL> enumeration = findBundleEntries( "/", "*.class", true );
    if (enumeration == null) {
      Activator.traceMessage( "The bundle directory of " + bundle.getSymbolicName()
          + " does not contain any class files." );
      return;
    }

    int count = 0;
    for (; enumeration.hasMoreElements(); count++) {
      URL classBundleUrl = enumeration.nextElement();

      if (TRACE_DIR_ENTRIES)
        Activator.traceMessage( "Dir-Entry: " + classBundleUrl );
      String className = getClassName( classBundleUrl );
      addClassSafely( classes, className );
    }

    Activator.traceMessage( "Scanned " + count + " directory entries." );
  }

  private String getClassName(URL classBundleUrl) {
    try {
      InputStream in = classBundleUrl.openStream();
      try {
        ClassReader classReader = new ClassReader( in );
        return classReader.getClassName().replaceAll( "/", "." );
      } finally {
        in.close();
      }
    } catch (Throwable ignored) {
      // IOException, ArrayIndexOutOfBoundException if class file is invalid
    }

    // fallback: try to guess class name from file name
    String classFileName = classBundleUrl.getFile();
    if (classFileName.startsWith( "/bin/" )) {
      classFileName = classFileName.substring( 5 );
    } else if (classFileName.startsWith( "/" )) {
      classFileName = classFileName.substring( 1 );
    }
    return getClassName( classFileName );
  }

  private JarFile getJarFile(URL jarBundleUrl) {
    try {
      URL url = FileLocator.resolve( jarBundleUrl );
      return new JarFile( url.getPath() );
    } catch (IOException e) {
      try {
        Activator.traceMessage( "Could not open " + jarBundleUrl + " directly. It seems to be in a jar." );
        File file = File.createTempFile( "bundleJar", ".jar" );
        file.deleteOnExit();
        InputStream in = FileLocator.openStream( bundle, new Path( jarBundleUrl.getPath() ), false );
        copy( in, new FileOutputStream( file ) );
        return new JarFile( file );
      } catch (IOException e1) {
        Activator.traceMessage( "Could not get content of  " + jarBundleUrl + ". " + e1.getMessage() );
        return null;
      }
    }
  }

  private void copy(InputStream in, OutputStream out) throws IOException {
    try {
      byte[] buffer = new byte[1024];

      while (true) {
        int count = in.read( buffer );
        if (count < 0)
          return;

        out.write( buffer, 0, count );
      }
    } finally {
      try {
        in.close();
      } finally {
        out.close();
      }
    }
  }

  private void addClassSafely(Set<Class<?>> classes, String className) {
    try {
      addClass( classes, className );
    } catch (ClassNotFoundException e) {
      Activator.traceMessage( "Bundle could not load class: " + className );
    } catch (NoClassDefFoundError ncdfe) {
      Activator.traceMessage( "Bundle could not load class: " + className + ", NoClassDefFoundError: " + ncdfe.getMessage() );
    } catch (Error e) {
      // e.g. ClassFormatError, see o.pluginbuilder.autotestsuite.tests.examples.root.InvalidByteCode
      Activator.traceMessage( "Bundle could not load class: " + className + ", " + e.getClass().getName() + ": " + e.getMessage() );      
    }
  }

  private void addClass(Set<Class<?>> classes, String className) throws ClassNotFoundException {
    Class<?> candidateClass = bundle.loadClass( className );
    if (candidateClass == null)
      return;

    if (!inclusionFilter.matches( candidateClass.getName() )) {
      Activator.traceMessage( "Excluded test class: " + candidateClass.getName() );
      return;
    }

    if (!classFilter.accept( candidateClass ))
      return;

    if (Modifier.isAbstract( candidateClass.getModifiers() )) {
      Activator.traceMessage( "Test class is abstract: " + candidateClass.getName() );
      return;
    }

    classes.add( candidateClass );
    Activator.traceMessage( "Added test class: " + candidateClass.getName() );
  }

  private Enumeration<URL> findBundleEntries(String path, String filePattern, boolean recurse) {
    return bundle.findEntries( path, filePattern, recurse );
  }
}
