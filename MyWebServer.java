
import java.io.*;  
import java.util.* ;
import java.net.*;  

class FileHolder{

    String rootName;
    ArrayList<File> files;

    public FileHolder(String rootName){
        this.rootName = rootName;
        files = new ArrayList<File>();
    }

    void findFiles(String fName){

        
        // String filePath = "";
        // // try{
        // //     File base = new File(".");
        // //     filePath = base.getCanonicalPath();
        // // } catch (Throwable e){
        // //     e.printStackTrace();
        // // }
        
        // // filePath += "\\";
        // // filePath += fName;
        
        // try{
        //     // File f1 = new File (fName);
        //     // if (f1.isFile()){
        //     //     files.add(f1);
        //     //     return;
        //     // }

        //     // File[] strFilesDirs = f1.listFiles();
        
        //     // for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
        //     //     if ( strFilesDirs[i].isDirectory ( ) ) {
        //     //         String subDir = dirName + "\\" + strFilesDirs[i];
        //     //         findFiles(subDir);
        //     //     }
        //     //     else if ( strFilesDirs[i].isFile()){
        //     //         this.files.add(strFilesDirs[i]);
        //     //     }
        //     // }
        // } catch(IOException e){
        //     e.printStackTrace();
        // }
        
        
        
    }

    ArrayList<File> getFiles(){
        return this.files;
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
        response.add("Content-length: 5");
        response.add("Content-type: text/plain");

        //Blank line, flush
        response.add("\r\n\r\n");
        response.add("Error");
        return response;
    }

    // Get files requested
    //FileHolder fh = new FileHolder(input.substring(1));
    //fh.findFiles(input.substring(1));
    String filePath = "";
     try{
        File base = new File(".");
        filePath += base.getCanonicalPath();
    } 
    catch (Throwable e){
        e.printStackTrace();
    }

    filePath += "\\";
    filePath += input.substring(1);
    System.out.println(filePath);
    File f = new File(filePath);


// ADD HEADER DATA 

    // Content Length (file size)
    //ArrayList<File> files = fh.getFiles();
    //File f = files.get(0);
    int contentLength = (int)f.length();
    response.add("Content-length: " + Integer.toString(contentLength));

    // Content type
    if (f.getName().endsWith("txt")){
        response.add("Content-type: text/plain");
    }
    else if (f.getName().endsWith("html")){
        response.add("Content-Type: text/html");
    }
    else{
        // Error
    }

    //Blank line, flush
    response.add("\r\n\r\n");

// ADD BODY DATA

    // Data
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
        String line;
        while ((line = br.readLine()) != null) {
           response.add(line);
        }
    }catch(IOException e){
        e.printStackTrace();
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