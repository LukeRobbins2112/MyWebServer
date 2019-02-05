
import java.io.*;  
import java.util.* ;
import java.net.*;  

class FileHolder{

    String rootName;
    ArrayList<File> files;

    public FileHolder(String rootName){
        this.rootName = rootName;
        files = new ArrayList<File>();

        findFiles("C:\\temp");
    }

    void findFiles(String dirName){

        File f1 = new File (dirName);
        
        File[] strFilesDirs = f1.listFiles();
        
        for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
            if ( strFilesDirs[i].isDirectory ( ) ) {
                String subDir = dirName + "\\" + strFilesDirs[i];
                findFiles(subDir);
            }
            else if ( strFilesDirs[i].isFile()){
                this.files.add(strFilesDirs[i]);
            }
        }
       
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
        if (tokens[0] != "GET" || !tokens[2].contains("HTTP/1.")){
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
    response.add("HTTP/1.1 200 OK\r\n");
    
    // Date and time
    Date d = new Date();
    String date = "Date: " +  d.toString() + "\r\n";
    response.add(date);

    // Get files requested
    FileHolder fh = new FileHolder(input);

    // Content Length (file size)
    ArrayList<File> files = fh.getFiles();
    File f = files.get(0);
    int contentLength = (int)f.length();
    response.add("Content-Length: " + Integer.toString(contentLength) + "\r\n\r\n");

    // Content type
    if (f.getName().endsWith("txt")){
        response.add("Content-Type: text/plain" + "\r\n");
    }
    else if (f.getName().endsWith("html")){
        response.add("Content-Type: text/html" + "\r\n");
    }
    else{
        // Error
    }

    //Blank line, flush
    response.add("\r\n\r\n");

    // Data
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
        String line;
        while ((line = br.readLine()) != null) {
           response.add(line + "\r\n\r\n");
        }
    }catch(IOException e){
        e.printStackTrace();
    }

    return response;
  }

}

public class MyWebServer {

  public static boolean controlSwitch = true;

  public static void main(String a[]) throws IOException {
    int q_len = 6;
    int port = 2540;
    Socket sock;

    ServerSocket servsock = new ServerSocket(port, q_len);

    while (controlSwitch) {
      sock = servsock.accept();
      new WebServerWorker (sock).start();
    }
  }
}