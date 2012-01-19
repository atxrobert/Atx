package controllers;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import models.Entry;
import models.EntryType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import views.html.createForm;
import views.html.index;
import views.html.program;

public class Programmer extends Controller {
    
    public static final char ready = 0x55;
    public static final char[] startpart = { 0x30, 0x32, 0x30, 0x30, 0x33, 0x30 };
    public static final char messageDelimiter = 0x02;
    public static final char endpart = 0xf3;
    public static final char space = 0x20;
    public static final char[] bereit = {0x3c ,0x3c ,0x3c ,0x42 ,0x45 ,0x52 ,0x45 ,0x49, 0x54 ,0x21 ,0x3e ,0x3e ,0x3e};
    public static final char[] ok = {0x3c ,0x3c ,0x3c ,0x4f, 0x4b,0x21 ,0x3e ,0x3e ,0x3e};
    
    public static Result program() {
      try {
          Request request = Http.Context.current().request();
          Map<String, String[]> queryString = request.queryString();
          
          String type = queryString.get("t")[0].toUpperCase();
          
          String text = queryString.get("m")[0];
          
          StringBuffer entry = new StringBuffer();
          int numberOfSpaces = 0;
          
          if (type.equals("B")) {
              entry.append(type);
              String pagedelay = queryString.get("pd")[0];
              if (pagedelay.length() == 1)
                  pagedelay = "0"+pagedelay;
              entry.append(pagedelay);
              entry.append(text);
              numberOfSpaces = 63 - entry.length();
          }
          
          if (type.equals("S")) {
              entry.append(type);
              String linedelay = queryString.get("ld")[0];
              if (linedelay.length() == 1)
                  linedelay = "0"+linedelay;
              entry.append(linedelay);
              entry.append(text);
              numberOfSpaces = 33 - entry.length();
          }
          
          if (type.equals("F")) {
              entry.append(type);
              String pulse = queryString.get("p")[0];
              if (pulse.length() == 1)
                  pulse = "0"+pulse;
              entry.append(pulse);
              entry.append("0"+queryString.get("l")[0]);
              entry.append(text);
              numberOfSpaces = 30;
          }

          
          for (int i = 0; i < numberOfSpaces; i++) {
              entry.append(" ");
          }
          
          char[] checksum = calculateChecksum(new String[]{entry.toString()});
          
          char[] sendstring = ArrayUtils.addAll(startpart, entry.toString().toCharArray());
          sendstring = ArrayUtils.addAll(sendstring, new char[]{messageDelimiter});
          sendstring = ArrayUtils.addAll(sendstring, new char[]{endpart});
          sendstring = ArrayUtils.addAll(sendstring, checksum);
          
          writeToBoard(sendstring);
          
          return ok(program.render("WIN! Message is " + new String(sendstring)));
      }
      catch (Exception e) {
        return badRequest(program.render("ERROR! "+ e.getMessage()));
    }

  }

  
    private static void writeToBoard(char[] sendstring) throws Exception {
        OutputStream mOutputToPort = null;
        InputStream mInputFromPort = null;
        try
        {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("COM1");
            if (portIdentifier.isCurrentlyOwned())
            {
                throw new Exception("Port in use");
            }
            else {
                System.out.println(portIdentifier.getName());

                SerialPort serialPort = (SerialPort) portIdentifier.open("ListPortClass", 300);
                int b = serialPort.getBaudRate();
                System.out.println(Integer.toString(b));
                serialPort.setSerialPortParams(300, SerialPort.DATABITS_8, SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);
                mOutputToPort = serialPort.getOutputStream();
                mInputFromPort = serialPort.getInputStream();
                System.out.println("Ready? \r\n");
                mOutputToPort.write(ready);
                mOutputToPort.flush();
                System.out.println("Waiting for Reply \r\n");
                Thread.sleep(500);
                byte mBytesIn [] = new byte[20];
                mInputFromPort.read(mBytesIn);
                mInputFromPort.read(mBytesIn);
                String value = new String(mBytesIn);
                System.out.println("Response from Serial Device: "+value);
                System.out.print("Response from Serial Device in hex: ");
                for (char ch : value.toCharArray()) {
                    System.out.print(Integer.toHexString(ch) + " ");
                }
                if(value.getBytes().equals(bereit)) {
                    for (char c : sendstring) {
                        mOutputToPort.write(c);
                    }
                    mOutputToPort.flush();
                    Thread.sleep(500);
                    mInputFromPort.read(mBytesIn);
                    mInputFromPort.read(mBytesIn);
                    value = new String(mBytesIn);
                    if(value.getBytes().equals(ok)) {
                        return;
                    }
                }
                else {
                    throw new Exception("Could not get ready state from board");
                }
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally {
            mOutputToPort.close(); 
            mInputFromPort.close();
        }
    }


private static char[] calculateChecksum(String[] messages) {
      //See entry format to build custom messages
      //String[] messages = { "B03A block" , "S00A scroll" ,"F0501A flow" };
      int result = 0;
      int numberOfSpaces = 0;
      
      for (int i = 0; i < startpart.length; i++) {
          result+=startpart[i];
      }
      for (String msg : messages) {
          String type = msg.substring(0,1);
          if("B".equals(type))
              numberOfSpaces = 63 - msg.length();
          if("S".equals(type))
              numberOfSpaces = 33 - msg.length();
          if("F".equals(type))
              numberOfSpaces = 30;
          
          for (char letter : msg.toCharArray()) {
              result += letter;
          }       
          for (int i = 0; i < numberOfSpaces; i++) {
              result += space;
          }
          result+=messageDelimiter;
      }
      result+=endpart;
      
      String calculatedPart = String.valueOf(result);
      int numberOfChecksumSpaces = 8 - calculatedPart.length();
      String spacePart = "";
      for (int i = 0; i < numberOfChecksumSpaces; i++) {
          spacePart += "20 ";
      }
      System.out.print("Checksum in hex: " + spacePart);
      for (char ch : String.valueOf(result).toCharArray()) {
          System.out.print(Integer.toHexString(ch) + " ");
      }
      return String.valueOf(result).toCharArray();
  }
}