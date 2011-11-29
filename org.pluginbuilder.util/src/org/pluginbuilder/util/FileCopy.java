/*******************************************************************************
 * Copyright (c) 2007 Markus Barchfeld
 * This program is distributed under the Eclipse Public License v1.0
 * which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.pluginbuilder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCopy {

  private static final int BUFFER_SIZE = 1024;

  public static void copyDirectory(final File sourceDir, final File destinationDir) throws IOException {
    if (sourceDir.getName().equals( ".svn" )) {
      return;
    }
    checkSourceDir( sourceDir );
    checkDestinationDir( destinationDir );
    File[] children = sourceDir.listFiles();
    if (children != null) {
      for (int i = 0; i < children.length; i++) {
        File child = children[i];
        copy( new File( sourceDir, child.getName() ), new File( destinationDir, child.getName() ) );
      }
    }
  }
  
  public static void copyStreamToFile(final InputStream input, final File destination) throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    int n = 0;
    OutputStream output = null;
    try {
      output = new FileOutputStream(destination);
      while ((n = input.read(buffer)) != -1) {
        output.write(buffer, 0, n);
      }
      output.flush();
    } finally {
      try {
        if (output != null) {
          output.close();
        }
      } catch (Exception e) {
        // do nothing
      }
      try {
        input.close();
      } catch (Exception e) {
        // do nothing
      }
    }
  }

  public static void copyFile(final File sourceFile, final File destinationFile) throws IOException {
    destinationFile.getParentFile().mkdirs();
    FileInputStream fis = new FileInputStream( sourceFile );
    try {
      FileOutputStream fos = new FileOutputStream( destinationFile );
      try {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = fis.read( buffer );
        while (bytesRead != -1) {
          fos.write( buffer, 0, bytesRead );
          bytesRead = fis.read( buffer );
        }
      } finally {
        fos.close();
      }
    } finally {
      fis.close();
    }
  }

  private static void copy(final File sourceDir, final File destination) throws IOException {
    if (sourceDir.isDirectory()) {
      copyDirectory( sourceDir, destination );
    } else if (sourceDir.isFile()) {
      copyFile( sourceDir, destination );
    }
  }

  private static void checkSourceDir(final File sourceDir) {
    // TODO
    if (sourceDir == null) {
      throw new NullPointerException( "Parameter sourceDir must not be null." );
    }
    if (!sourceDir.exists()) {
      throw new IllegalArgumentException( "Parameter sourceDir does not extist on the filesystem." );
    }
    if (!sourceDir.isDirectory()) {
      throw new IllegalArgumentException( "Parameter sourceDir is not a directory." );
    }
  }

  private static void checkDestinationDir(final File destDir) {
    // TODO
    if (destDir == null) {
      throw new NullPointerException( "Parameter sourceDir must not be null." );
    }
    if (!destDir.exists()) {
      destDir.mkdirs();
    }
    if (!destDir.isDirectory()) {
      throw new IllegalArgumentException( "Parameter sourceDir is not a directory." );
    }
  }
}
