package javax.sip.viewer.model;

/**
 * @copyright ©2010, Bell Canada
 * @author jean.lalande (CTS)
 */
public class Actor {
  private int mIndex;
  private int mLength;
  private String mValue;

  public Actor(int pIndex, int pLength, String pValue) {
    mIndex = pIndex;
    mLength = pLength;
    mValue = pValue;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(int index) {
    mIndex = index;
  }

  /**
   * @return the index
   */
  public int getIndex() {
    return mIndex;
  }

  /**
   * @param length the length to set
   */
  public void setLength(int length) {
    mLength = length;
  }

  /**
   * @return the length
   */
  public int getLength() {
    return mLength;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value) {
    mValue = value;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return mValue;
  }
}
