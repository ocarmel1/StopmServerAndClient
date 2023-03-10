package bgu.spl.net.impl.stomp;

/**
 * (c)2005 Sean Russell
 */
public class Command {
  public final static String ENCODING = "UTF-8";
  private String _command;

  private Command( String msg ) { 
    _command = msg;
  }
  public static Command SEND = new Command( "SEND" ),
         SUBSCRIBE = new Command( "SUBSCRIBE" ),
         UNSUBSCRIBE = new Command( "UNSUBSCRIBE" ),
         DISCONNECT = new Command( "DISCONNECT" ),
         CONNECT = new Command( "CONNECT" );

  public static Command MESSAGE = new Command( "MESSAGE" ),
         RECEIPT = new Command( "RECEIPT" ),
         CONNECTED = new Command( "CONNECTED" ),
         ERROR = new Command( "ERROR" );

  public static Command valueOf( String v ) {
    v = v.trim();
    if (v.equals("SEND")) return SEND;
    else if (v.equals( "SUBSCRIBE" )) return SUBSCRIBE;
    else if (v.equals( "UNSUBSCRIBE" )) return UNSUBSCRIBE;
    else if (v.equals( "CONNECT" )) return CONNECT;
    else if (v.equals( "MESSAGE" )) return MESSAGE;
    else if (v.equals( "RECEIPT" )) return RECEIPT;
    else if (v.equals( "CONNECTED" )) return CONNECTED;
    else if (v.equals( "DISCONNECT" )) return DISCONNECT;
    else if (v.equals( "ERROR" )) return ERROR;
    throw new Error( "Unrecognised command "+v );
  }

  public String toString() {
    return _command;
  }
}

