package bgu.spl.net.impl.stomp;

import java.util.Map;
import java.util.Iterator;


/**
 * (c)2005 Sean Russell
 */
public class Message {
  private Command _command;
  private Map<String, String> _headers;
  private String _body;
  protected Message( Command c, Map<String, String> h, String b ) {
    _command = c;
    _headers = h;
    _body = b;
  }
  public Map<String, String> headers() { return _headers; }
  public String body() { return _body; }
  public Command command() { return _command; }
  //public String toString() {return _command.toString() +_headers.toString() +  _body;}
  public String toString() {
      //String s = "";//"/n/n"[command,header1:value,...,"",body]

      //java.io.OutputStream out 
      StringBuffer message = new StringBuffer(_command.toString());
      message.append( "\n" );

      if (_headers != null) {
        for (Iterator keys = _headers.keySet().iterator(); keys.hasNext(); ) {
            String key = (String)keys.next();
            String value = (String)_headers.get(key);
            message.append( key );
            message.append( ":" );
            message.append( value );
            message.append( "\n" );
        }
      }
      message.append( "\n" );

      if (_body != null) message.append( _body );
      //else message.append("body is null");

      //message.append( "\u0000" );
      return message.toString(); //uses utf8 by default
    }
}



