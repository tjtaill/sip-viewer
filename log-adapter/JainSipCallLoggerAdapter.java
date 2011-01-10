package javax.sip.viewer.utils;

import gov.nist.core.ServerLogger;
import gov.nist.javax.sip.message.SIPMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.sip.SipStack;

public class JainSipCallLoggerAdapter implements ServerLogger {
  public static String CALL_LOGGER_NAME = "calls";
  private Logger mCallLogger = Logger.getLogger(CALL_LOGGER_NAME);

  private Logger sLogger = Logger.getLogger(this.getClass().getName());
  
  private boolean mLogContent = true;
  private boolean mLogOptions = false;
  
  private Pattern mOptionLogPattern = Pattern.compile(".*^CSeq: \\d* OPTIONS$.*", Pattern.DOTALL | Pattern.MULTILINE);


  public void closeLogFile() {

  }

  /**
   * Log a message into the log directory.
   * 
   * @param message a SIPMessage to log
   * @param from from header of the message to log into the log directory
   * @param to to header of the message to log into the log directory
   * @param sender is the server the sender
   * @param time is the time to associate with the message.
   */
  public void logMessage(SIPMessage message, String source, String destination, boolean sender, long time) {

    StringBuilder lMesssageStringBuilder = new StringBuilder();
    if (mLogContent) { 
      message.encode(lMesssageStringBuilder);
    } else {
      message.encodeMessage(lMesssageStringBuilder);
    }
    
    String lEncodedMessage = lMesssageStringBuilder.toString().trim();
    try {
      // filter out options depending on config
      boolean lIsOptionMessage = mOptionLogPattern.matcher(lEncodedMessage).matches();
      if (!lIsOptionMessage || (lIsOptionMessage && mLogOptions)) {
        StringBuilder lLogMessage = new StringBuilder();
        lLogMessage.append("[");
        lLogMessage.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date(time)));
        lLogMessage.append("]");
        lLogMessage.append(sender? " OUT " : " IN ");
        lLogMessage.append(source);
        lLogMessage.append(" --> ");
        lLogMessage.append(destination);
        lLogMessage.append("\n\n");
        lLogMessage.append(lEncodedMessage);
        lLogMessage.append("\n\n-----------------------\n");
        
        mCallLogger.log(Level.INFO, lLogMessage.toString());
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }

  }

  /**
   * Log a message into the log directory.
   * 
   * @param message a SIPMessage to log
   * @param from from header of the message to log into the log directory
   * @param to to header of the message to log into the log directory
   * @param status the status to log.
   * @param sender is the server the sender or receiver (true if sender).
   * @param time is the reception time.
   */
  public void logMessage(SIPMessage message,
                         String from,
                         String to,
                         String status,
                         boolean sender,
                         long time)
  {
    logMessage(message, from, to, sender, time);
  }

  /**
   * Log a message into the log directory. Time stamp associated with the message is the current
   * time.
   * 
   * @param message a SIPMessage to log
   * @param from from header of the message to log into the log directory
   * @param to to header of the message to log into the log directory
   * @param status the status to log.
   * @param sender is the server the sender or receiver (true if sender).
   */
  public void logMessage(SIPMessage message, String from, String to, String status, boolean sender)
  {
    logMessage(message, from, to, sender, System.currentTimeMillis());
  }

  public void logException(Exception ex) {
    sLogger.log(Level.WARNING, "Exception in server logger", ex);
  }

  public void setStackProperties(Properties stackProperties) {
    mLogContent = Boolean.parseBoolean(stackProperties.getProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
                                                                   "true"));
    mLogOptions = Boolean.parseBoolean(stackProperties.getProperty("gov.nist.javax.sip.LOG_OPTIONS",
                                                                   "false"));
  }

  public void setSipStack(SipStack sipStack) {
    // Nothing to do
  }

}
