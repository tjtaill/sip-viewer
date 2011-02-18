package javax.sip.viewer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ListOfFiles implements Enumeration {

  private String[] listOfFiles;
  private int current = 0;

  public ListOfFiles(String[] listOfFiles) {
    this.listOfFiles = listOfFiles;
  }

  public boolean hasMoreElements() {
    if (current < listOfFiles.length)
      return true;
    else
      return false;
  }

  public Object nextElement() {
    InputStream in = null;

    if (!hasMoreElements())
      throw new NoSuchElementException("No more files.");
    else {
      String nextElement = listOfFiles[current];
      current++;
      try {
        if (nextElement.endsWith(".gz")) {
          in = new GZIPInputStream(new FileInputStream(nextElement));
        } else {
          in = new FileInputStream(nextElement);
        }
      } catch (FileNotFoundException e) {
        System.err.println("ListOfFiles: Can't open " + nextElement);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return in;
  }

}