/** File is: MyTelnetClient.java

Simple mechanism for sending data to a webserver and retrieving the results.

To use:

> java MyTelnet condor.depaul.edu

GET /elliott/dog.txt HTTP/1.1    [<-- Type this into the console window manually]
Host: condor.depaul.edu:80
[crlf]
[crlf]
stop

Should return something like:

HTTP/1.1 200 OK
Date: Mon, 02 Oct 2017 20:26:44 GMT
Server: Apache/2.2.3 (Red Hat)
Last-Modified: Wed, 07 Oct 2015 20:29:55 GMT
ETag: "8a1bfc-30-521899bff76c0"
Accept-Ranges: bytes
Content-Length: 48
Content-Type: text/plain
Connection: close

This is Elliott's dog file on condor. Good job!


----------------------------------------------------------------------*/

import java.io.*;
import java.net.*;

public class MyTelnetClient{
  public static void main (String args[]) {

    String serverName;
    int port;
    if (args.length < 1) serverName = "localhost";
    else serverName = args[0];
    if (args.length < 2) port = 80;
    else port = Integer.parseInt(args[1]);

    Socket sock;
    BufferedReader fromServer;
    PrintStream toServer;
    String textFromServer;

    System.out.println("MyTelnet Client\n");
    System.out.println("Using server: " + serverName + ", Port: " + args[1]);

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    try {

      sock = new Socket(serverName, port);
      fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      toServer = new PrintStream(sock.getOutputStream());

      String dataToSend;
      do {
        System.out.print("Enter text to send to the server, <stop> to end: ");
        System.out.flush ();
        dataToSend = in.readLine ();

        if (dataToSend.indexOf("stop") < 0){
            toServer.println(dataToSend);
            toServer.flush();
	    }
      } while (dataToSend.indexOf("stop") < 0);

      for (int i = 1; i <=20; i++){
        textFromServer = fromServer.readLine();

        if (textFromServer != null){
            System.out.println(textFromServer);
        }
      }

      sock.close();

    } catch (IOException x) {x.printStackTrace ();}
  }
}
