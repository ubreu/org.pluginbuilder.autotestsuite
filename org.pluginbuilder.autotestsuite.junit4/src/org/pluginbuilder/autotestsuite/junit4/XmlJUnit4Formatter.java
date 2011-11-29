package org.pluginbuilder.autotestsuite.junit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.taskdefs.optional.junit.XMLConstants;
import org.apache.tools.ant.util.DateUtils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * {@link JUnit4Formatter} that mimics the output of
 * {@link org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter}.
 * 
 * @author Markus Wiederkehr
 */
public class XmlJUnit4Formatter extends JUnit4Formatter implements XMLConstants {

  private static final boolean GENERATE_PROPERTIES = true;
  private static final String UNKNOWN = "unknown";

  private final File file;

  private final Map<Description, TestHelper> testHelpers = new HashMap<Description, TestHelper>();

  private Document doc;
  private Element rootElement;

  public XmlJUnit4Formatter(File file) {
    if (file == null)
      throw new IllegalArgumentException();

    this.file = file;
  }

  @Override
  public void testRunStarted(Description description) throws Exception {
    testHelpers.clear();

    doc = getDocumentBuilder().newDocument();
    rootElement = doc.createElement( TESTSUITE );
    doc.appendChild( rootElement );

    String name = description.getDisplayName();
    rootElement.setAttribute( ATTR_NAME, name == null ? UNKNOWN : name );

    final String timestamp = DateUtils.format( new Date(), DateUtils.ISO8601_DATETIME_PATTERN );
    rootElement.setAttribute( TIMESTAMP, timestamp );
    rootElement.setAttribute( HOSTNAME, getHostname() );

    Properties props = System.getProperties();

    if (GENERATE_PROPERTIES) {
      Element propsElement = doc.createElement( PROPERTIES );
      rootElement.appendChild( propsElement );

      for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
        String propName = (String) e.nextElement();
        Element propElement = doc.createElement( PROPERTY );
        propsElement.appendChild( propElement );
        propElement.setAttribute( ATTR_NAME, propName );
        propElement.setAttribute( ATTR_VALUE, props.getProperty( propName ) );
      }
    }
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    rootElement.setAttribute( ATTR_TESTS, "" + result.getRunCount() );
    rootElement.setAttribute( ATTR_FAILURES, "" + result.getFailureCount() );
    // JUnit 4 no longer seems to distinguish between failures and errors
    rootElement.setAttribute( ATTR_ERRORS, "" + 0 );
    rootElement.setAttribute( ATTR_TIME, "" + (result.getRunTime() / 1000.0) );
  }

  @Override
  public void testStarted(Description description) throws Exception {
    getTestHelper( description );
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    getTestHelper( failure.getDescription() ).testFailure( failure );
  }

  @Override
  public void testFinished(Description description) throws Exception {
    getTestHelper( description ).testFinished( rootElement );

    testHelpers.remove( description );
  }

  @Override
  public void setSystemOutput(String out) {
    formatOutput( SYSTEM_OUT, out );
  }

  @Override
  public void setSystemError(String err) {
    formatOutput( SYSTEM_ERR, err );
  }

  @Override
  public void save() throws IOException {
    write( doc, new FileOutputStream( file ), true );
  }

  private TestHelper getTestHelper(Description description) {
    TestHelper testHelper = testHelpers.get( description );
    if (testHelper == null) {
      testHelper = new TestHelper( doc, description );
      testHelpers.put( description, testHelper );
    }
    return testHelper;
  }

  private String getHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "localhost";
    }
  }

  private void formatOutput(String type, String output) {
    Element outputElement = doc.createElement( type );
    rootElement.appendChild( outputElement );
    outputElement.appendChild( doc.createCDATASection( output ) );
  }

  private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder();
  }

  private void write(Document doc, OutputStream out, boolean close) throws IOException {
    try {
      DOMImplementationLS ls = (DOMImplementationLS) doc.getImplementation().getFeature( "LS", "3.0" );
      LSSerializer serializer = ls.createLSSerializer();
      DOMConfiguration domConfig = serializer.getDomConfig();
      trySetPrettyPrint( domConfig );

      LSOutput lsOutput = ls.createLSOutput();
      lsOutput.setEncoding( "UTF-8" );
      lsOutput.setByteStream( out );

      try {
        serializer.write( doc, lsOutput );
      } catch (LSException e) {
        throw (IOException) new IOException( e.getMessage() ).initCause( e );
      }

      out.flush();
    } finally {
      if (close)
        out.close();
    }
  }

  private void trySetPrettyPrint(DOMConfiguration domConfig) {
    // Setting pretty-print works with Sun Java 6 but not Java 5
    try {
      domConfig.setParameter( "format-pretty-print", true );
    } catch (DOMException ignored) {
    }
  }

  private static final class TestHelper {
    private final Document doc;
    private final long timeStarted;
    private final Element testElement;

    public TestHelper(Document doc, Description description) {
      this.doc = doc;
      timeStarted = System.currentTimeMillis();
      testElement = doc.createElement( TESTCASE );

      testElement.setAttribute( ATTR_NAME, getTestName( description ) );
      testElement.setAttribute( ATTR_CLASSNAME, getTestClassName( description ) );
    }

    public void testFailure(Failure failure) {
      Element failureElement = doc.createElement( FAILURE );
      testElement.appendChild( failureElement );

      String message = failure.getMessage();
      if (message != null && message.length() > 0)
        failureElement.setAttribute( ATTR_MESSAGE, message );

      failureElement.setAttribute( ATTR_TYPE, failure.getException().getClass().getName() );

      String strace = failure.getTrace();
      Text trace = doc.createTextNode( strace );
      failureElement.appendChild( trace );
    }

    public void testFinished(Element rootElement) {
      long testDuration = System.currentTimeMillis() - timeStarted;
      testElement.setAttribute( ATTR_TIME, Double.toString( testDuration / 1000.0 ) );

      rootElement.appendChild( testElement );
    }

    private String getTestName(Description description) {
      String name = description.getDisplayName();
      int paren = name.lastIndexOf( '(' );
      if (paren != -1 && name.endsWith( ")" ))
        return name.substring( 0, paren );
      return name;
    }

    private String getTestClassName(Description description) {
      String name = description.getDisplayName();
      int paren = name.lastIndexOf( '(' );
      if (paren != -1 && name.endsWith( ")" ))
        return name.substring( paren + 1, name.length() - 1 );
      return UNKNOWN;
    }
  }

}
