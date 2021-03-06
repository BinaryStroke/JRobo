/*
 * JRobo - An Advanced IRC Bot written in Java
 *
 * Copyright (C) <2013> <Christopher Lemire>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jrobo;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.out;
import static java.lang.System.err;

/**
 *
 * @author chris
 */
public class Networking {
  private Socket sock = null;
  private BufferedWriter bwriter = null;
  private BufferedReader breader = null;
  private String received = null;
  /* Max chars per single irc message */
  private final int MAXCHARS = 401;

  public Networking() {
    super(); // Gets rid of java.lang.VerifyError
    try {
      //TODO Get server from config file instead
      sock = new Socket("frequency.windfyre.net", 6667); //TODO SSL conn on port 6697
    } catch (UnknownHostException ex) {
      Logger.getLogger(Networking.class.getName()).log(Level.SEVERE, null, ex);
      err.println("Possible DNS resolution failed");
      //@TODO close streams, connections, etc
      System.exit(-1);
    } catch (IOException ex) {
      Logger.getLogger(Networking.class.getName()).log(Level.SEVERE, null, ex);
      err.println("I/O Error, Check networking");
      //@TODO close streams, connections, etc
      System.exit(-1);
    }
    try {
      bwriter = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
      breader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    } catch (IOException ex) {
      Logger.getLogger(Networking.class.getName()).log(Level.SEVERE, null, ex);
      err.println("Error getting streams with server");
      //@TODO close streams, connections, etc
      System.exit(-1);
    }
  }

  /*
   * For sending in raw IRC protocol
   */
  public boolean sendln(String command) {
    try {
      bwriter.write(command);
      bwriter.newLine();
      bwriter.flush();
      out.println("[***]\t" + command); //@TODO Color-code me
      return true;

    } catch (IOException ex) {
      err.printf("Failed to send \"%s\"\n", command);
      Logger.getLogger(Networking.class.getName()).log(Level.SEVERE, null, ex);
      return false;

    }
  }

  /*
   * For receiving in raw IRC protocol
   */
  public String recieveln() {
    try {
      received = breader.readLine();
      out.printf("[---]\t%s\n", received); //@TODO Color-code this opposite of colorcode for sent, "[-]" should be blue
      return received;
    } catch (IOException ex) {
      Logger.getLogger(Networking.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  //TODO Add JavaDoc here using NB
  public boolean msgChannel(String chan, String msg) {
    boolean success = true;
    msg = addNewLines(msg);
    String[] msgArr = msg.split("\n");
    for(int j=0;j<msgArr.length;j++) {
      /*
       * Meaning if one call to sendln returns false
       * This entire function will return false
       */
      if( !sendln("PRIVMSG " + chan + " :" + msgArr[j]) ) {
        success = false; 
      }
    }
    return success;
  }

  public boolean msgUser(String user, String msg) {
    boolean success = true;
    String[] msgArr = msg.split("\n");
    for(int j=0;j<msgArr.length;j++) {
      /*
       * Meaning if one call to sendln returns false
       * This entire function will return false
       */
      if( !sendln("PRIVMSG " + user + " :" + msgArr[j]) ) {
        success = false;
      }
    }
    return success;
  } // EOF method
  
  public boolean noticeChan(String chan, String msg){
      boolean ok = true;
      
      String[] msgSplit = msg.split("\n");
      
      for(int i=0;i<msgSplit.length;i++) {
        if(!sendln("NOTICE " + chan + " :" + msgSplit[i]) ) {
          ok = false;
        }
     }
      return ok;
  }
  
  public boolean noticeUser(String user, String msg) {
      boolean ok = true;
      
      String[] msgSplit = msg.split("\n");
      
      for(int i=0;i<msgSplit.length;i++){
      if(!sendln("NOTICE " + user + " :" + msgSplit[i]) ) {
        ok = false;
      }
     }
      return ok;
  }

  /*
   * Useful for the other methods that split messages into several where
   * Newlines occure
   * This is used to prevent a message being truncated by IRC because
   * It exceeds MAXCHARS
   */
  private String addNewLines(String command) {
    int len = command.length();
    String tmp = "";
    if (len > MAXCHARS) {
      // pos = position
      for(int pos = 0; pos < len; pos+=400) {
        if( (len - pos) <= 400) {
          tmp += command.substring(pos);
        } else {
          tmp += command.substring(pos, pos+400) + '\n';
        }
      }
      return tmp;
    } else {
      return command;
    }
  }
} //EOF class