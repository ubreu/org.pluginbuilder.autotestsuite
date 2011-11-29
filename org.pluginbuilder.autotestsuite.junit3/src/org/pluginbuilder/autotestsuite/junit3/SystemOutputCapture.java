package org.pluginbuilder.autotestsuite.junit3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class SystemOutputCapture {

  private final boolean captureOut;
  private PrintStream oldOut;
  private ByteArrayOutputStream out;
  private String capturedOut;

  private final boolean captureErr;
  private PrintStream oldErr;
  private ByteArrayOutputStream err;
  private String capturedErr;

  public SystemOutputCapture() {
    this.captureOut = true;
    this.captureErr = true;
  }

  public SystemOutputCapture(boolean captureOut, boolean captureErr) {
    this.captureOut = captureOut;
    this.captureErr = captureErr;
  }

  public void start() {
    if (captureOut) {
      oldOut = System.out;
      out = new ByteArrayOutputStream();
      System.setOut( new PrintStream( new TeeOutputStream( oldOut, out ) ) );
    }

    if (captureErr) {
      oldErr = System.err;
      err = new ByteArrayOutputStream();
      System.setErr( new PrintStream( new TeeOutputStream( oldErr, err ) ) );
    }
  }

  public void stop() {
    capturedOut = capture( System.out, out );
    capturedErr = capture( System.err, err );

    out = null;
    err = null;

    try {
      try {
        if (oldOut != null)
          System.setOut( oldOut );
      } finally {
        if (oldErr != null)
          System.setErr( oldErr );
      }
    } finally {
      oldOut = null;
      oldErr = null;
    }
  }

  public String getCapturedOut() {
    return capturedOut;
  }

  public String getCapturedErr() {
    return capturedErr;
  }

  private String capture(PrintStream printStream, ByteArrayOutputStream captureStream) {
    if (captureStream == null)
      return "";

    printStream.flush();
    return new String( captureStream.toByteArray() );
  }

  private static final class TeeOutputStream extends OutputStream {

    private final OutputStream first;
    private final OutputStream second;

    public TeeOutputStream(OutputStream first, OutputStream second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public void write(int b) throws IOException {
      try {
        first.write( b );
      } finally {
        second.write( b );
      }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      try {
        first.write( b, off, len );
      } finally {
        second.write( b, off, len );
      }
    }

    @Override
    public void flush() throws IOException {
      try {
        first.flush();
      } finally {
        second.flush();
      }
    }

    @Override
    public void close() throws IOException {
      try {
        first.close();
      } finally {
        second.close();
      }
    }

  }

}
