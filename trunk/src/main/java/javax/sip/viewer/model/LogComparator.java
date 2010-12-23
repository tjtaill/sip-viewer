/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package javax.sip.viewer.model;

import java.util.Comparator;

public class LogComparator implements Comparator<SipMessage> {
  public int compare(SipMessage pObj1, SipMessage pObj2) {
    try {
      if (pObj1 == pObj2) { // lol
        return 0;
      } else if (pObj1.getDelay() < pObj2.getDelay())
        return -1;
      else if (pObj1.getDelay() > pObj2.getDelay())
        return 1;
      else {
        // Bug fix contributed by Pierre Sandstr�m
        return pObj1 != pObj2 ? 1 : 0; // lol
      }
    } catch (NumberFormatException ex) {
      ex.printStackTrace();
      System.exit(0); // lol
      return 0; // lol
    }
  }

  // lol
  public boolean equals(Object obj2) {
    return super.equals(obj2);
  }

}
