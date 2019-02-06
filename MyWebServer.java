/*--------------------------------------------------------

1. Luke Robbins / 2/5/2019

2. Java 1.8

3. Compilation Instructions

> javac MyWebServer.java

4. Run Instructions

> java MyWebServer
> Open FireFox, enter http://localhost:2540/[file or directory path]

Hit Control-C to end the server application

5. List of files needed for running the program.

 a. MyWebServer.java
 b. Any local directories/files

6. Notes:

----------------------------------------------------------*/


import java.io.*;  
import java.util.* ;
import java.net.*;  

class FileHolder{

    String dirName;
    int totalSize;
    ArrayList<File> files;

    public FileHolder(String dName){
        this.dirName = dName;
        this.totalSize = 0;
        files = new ArrayList<>();
    }

    void findFiles(String fName){
        
        try{

            // If it's a single file, just add it and return
            File f1 = new File (fName);
            if (f1.isFile()){
                files.add(f1);
                return;
            }
            else{
                // Else, recurse on all the files in this directory
                File[] strFilesDirs = f1.listFiles();
                if (strFilesDirs.length == 0) return;

                 for (int i = 0 ; i < strFilesDirs.length ; i ++ ) {
                     if ( strFilesDirs[i].isDirectory ( ) ) {
                        this.files.add(strFilesDirs[i]);
                         //findFiles(strFilesDirs[i].getName());
                     }
                    else if ( strFilesDirs[i].isFile()){
                        this.files.add(strFilesDirs[i]);
                    }
                    this.totalSize += (int)strFilesDirs[i].getName().length();
                }
            }
  
        } catch(Exception e){
            e.printStackTrace();
        }
        
    }

    ArrayList<File> getFiles(){
        return this.files;
    }

    int getTotalSize(){
        return this.totalSize;
    }

}

class WebServerWorker extends Thread {  

  Socket sock;                  
  
    WebServerWorker (Socket s) {
      sock = s;
    } 
                                      
  public void run(){

    PrintStream out = null;
    BufferedReader in = null;

    try {

        out = new PrintStream(sock.getOutputStream());
        in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

      

        // Get request
        String getRequest = in.readLine();
        String[] tokens = getRequest.split(" ");

        // Read through the rest of the GET request
        String sockdata = "";
        while (!(sockdata = in.readLine()).equals("stop") && sockdata.length() > 0) {
              // Ignore rest of GET request
        }

        // Check for GET, HTTP version
        if (!tokens[0].equals("GET") || !tokens[2].contains("HTTP/1.")){
            String errString = "<html> <h1> Error </h1> </html>";
            out.println("HTTP/1.1 200 OK");
            out.println("Date: " + new Date());
            out.println("Content-type: " + "text/html");
    	    out.println("Content-length: " + errString.length());
    	    out.println(); 
    	    out.flush(); 
            out.println(errString);
            out.flush();    
            return;
        }

        // Process the request using the file requested
        String fileName = tokens[1];
        ArrayList<String> httpResponse = processRequest(fileName);

        // Print each line of the response
        // Includes the header and data
        for (int i = 0; i < httpResponse.size(); i++){
            out.println(httpResponse.get(i));
        }
        out.flush();

        
        // sock.close(); 
    } catch (IOException x) {
        System.out.println("IO error");
    }
  }


  /*
  Sample response Header (Don't need all fields)
    HTTP/1.1 200 OK
    Date: Mon, 04 Feb 2019 20:08:50 GMT
    Server: Apache/2.2.3 (Red Hat)
    Last-Modified: Wed, 07 Oct 2015 20:29:55 GMT
    ETag: "8a1bfc-30-521899bff76c0"
    Accept-Ranges: bytes
    Content-Length: 48
    Content-Type: text/plain
    Connection: close
*/

  ArrayList<String> processRequest(String input){
    
    ArrayList<String> response = new ArrayList<String>();

    // Message type & HTTP version
    response.add("HTTP/1.1 200 OK");
    
    // Date and time
    response.add(new Date().toString());

    if (input.contains("..") || input.contains("~")){

        String errMsg = "Error: Must stay within directory";

        response.add("Content-length: " + errMsg.length());
        response.add("Content-type: text/plain");

        //Blank line, flush
        response.add("\r\n\r\n");
        response.add(errMsg);
        return response;
    }
    
    // Get path for file/directory requested

    String filePath = "";
     try{
        File base = new File(".");
        filePath += base.getCanonicalPath();
    } 
    catch (Throwable e){
        e.printStackTrace();
    }

    filePath += "\\";                   // Add leading back slash
    filePath += input.substring(1);     // Remove original front slash from request
    System.out.println(filePath);

    File f;

    try{
        f = new File(filePath);
    } catch(Exception  e){
        System.out.println("Error opening file -- incorrect path given");

        String errMsg = "Error: Invalid file or directory name given";

        response.add("Content-length: " + errMsg.length());
        response.add("Content-type: text/plain");

        //Blank line, flush
        response.add("\r\n\r\n");
        response.add(errMsg);
        return response;
    }
    


// ADD HEADER DATA 
   
    if (f.isDirectory()){

        // Get Directory files, name sizes
        FileHolder fh = new FileHolder(f.getName());
        fh.findFiles(filePath);
        ArrayList<File> files = fh.getFiles();

        // Type
        response.add("Content-type: text/html");

        // Size
        int htmlSize = 13;
        int breaklineSize = 4;
        int cr_lf_size = 4;
        int totalSize = htmlSize + fh.getTotalSize() + (files.size() * breaklineSize) + (files.size() * cr_lf_size);
        response.add("Content-length: " + Integer.toString(totalSize));

        //Blank line, flush
        response.add("\r\n\r\n");

        // ADD BODY DATA

        response.add("<html>");

        for (int i = 0; i < files.size(); i++){
            response.add(files.get(i).getName() + "<br>");
        }

        response.add("</html>");
    }
    else{

        // Content type
        if (f.getName().endsWith("txt")){
            int contentLength = (int)f.length();
            response.add("Content-length: " + Integer.toString(contentLength));
            response.add("Content-type: text/plain");
        }
        else if (f.getName().endsWith("html")){
            int contentLength = (int)f.length();
            response.add("Content-length: " + Integer.toString(contentLength));
            response.add("Content-type: text/html");
        }
        else{
            response.add("Content-type: text/html");
        }

        //Blank line, flush
        response.add("\r\n\r\n");

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
               response.add(line);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    return response;
  }

}

public class MyWebServer {

  public static boolean processRequests = true;

  public static void main(String a[]) throws IOException {
    int q_len = 6;
    int port = 2540;
    Socket sock;

    // Server socket
    ServerSocket servsock = new ServerSocket(port, q_len);

    // Service browser connections as they come in
    while (processRequests) {
      sock = servsock.accept();
      new WebServerWorker (sock).start();
    }
  }
}