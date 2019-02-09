/*--------------------------------------------------------

1. Luke Robbins / 2/5/2019

2. Java 1.8

3. Compilation Instructions

> javac MyWebServer.java

4. Run Instructions

> java MyWebServer
> Open FireFox, enter http://localhost:2540/[file or directory path]
> Click through directory links to navigate or view HTML/text files

Hit Control-C to end the server application

5. List of files needed for running the program.

 a. MyWebServer.java
 b. Any local directories/files

6. Notes:

----------------------------------------------------------*/


import java.io.*;  
import java.util.* ;
import java.net.*;  


// *****************************************
// Utility class for handling CGI requests
// *****************************************
class CGI_Data{

    String name;
    int num1;
    int num2;
    int result;
 
    public CGI_Data(String input){
        this.name = "";
        this.num1 = 0;
        this.num2 = 0;
        this.result = 0;

        extractInfo(input);
    }

    // GET /cgi/addnums.fake-cgi?person=LukeRobbins&num1=7&num2=8 HTTP/1.1

    void extractInfo(String data){

        data = data.substring(data.indexOf('?') + 1);     // person=LukeRobbins&num1=7&num2=8
        String[] fields = data.split("&");                // [person=LukeRobbins, num1=7, num2=8]

        this.name = fields[0].substring(fields[0].indexOf("=") + 1);
        this.num1 = Integer.parseInt(fields[1].substring(fields[1].indexOf("=") + 1));
        this.num2 = Integer.parseInt(fields[2].substring(fields[2].indexOf("=") + 1));

        this.result = num1 + num2;
    }

    // "Dear [NAME], the sum of [NUM1] and [NUM2] is [RESULT]."
    String getOutput(){
        String result = "Dear " + this.name + ", the sum of " 
                      + Integer.toString(num1) + " and " + Integer.toString(num2) + " is " + Integer.toString(this.result);

        return result;
    }
}

// *****************************************
// Utility class for parsing directories
// *****************************************
class FileHolder{

    String dirName;
    int totalSize;
    ArrayList<File> files;

    public FileHolder(String dName){
        this.dirName = dName;
        this.totalSize = 0;
        files = new ArrayList<>();
    }

    // Uses the idea from ReadFiles.java displayed - get all files in directory
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
                File[] filesInDirectory = f1.listFiles();
                if (filesInDirectory.length == 0) return;

                 for (int i = 0; i < filesInDirectory.length; i++) {

                    if (filesInDirectory[i].getName().contains(".")){
                        String dfName = filesInDirectory[i].getName();
                        if (!dfName.endsWith(".txt") && !dfName.endsWith(".html") && !dfName.endsWith(".java")){
                            continue;
                        }
                    }

                     if (filesInDirectory[i].isDirectory()) {
                        this.files.add(filesInDirectory[i]);
                         //findFiles(filesInDirectory[i].getName());
                     }
                    else if ( filesInDirectory[i].isFile()){
                        this.files.add(filesInDirectory[i]);
                    }
                    this.totalSize += (int)filesInDirectory[i].getName().length();
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

// *******************************************************************
// Main worker class, handles each incoming connection request
// *******************************************************************
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
        System.out.println("RECEIVED REQUEST FROM CLIENT\n");
        String getRequest = in.readLine();
        if (getRequest == null) return;
        String[] tokens = getRequest.split(" ");
        String fileName = tokens[1];

        // Debug printing
        System.out.println("GET REQUEST:\n");
        System.out.println(getRequest);

        // Read through the rest of the GET request
        // Ignore this "unnecessary" info, as in 150-line web server
        String sockdata = "";
        while (!(sockdata = in.readLine()).equals("stop") && sockdata.length() > 0) {
              // Ignore rest of GET request
              System.out.println(sockdata);
        }
        System.out.println();

        // Process the request using the file requested
        ArrayList<String> httpResponse = processRequest(fileName);

        // Print each line of the response
        // Includes the header and data
        System.out.println("SERVER RESPONSE: ");
        for (int i = 0; i < httpResponse.size(); i++){
            out.println(httpResponse.get(i));
            System.out.println(httpResponse.get(i));
        }
        out.flush();
        System.out.println("\n\n");
        
        sock.close(); 

        System.out.println("REQUEST COMPLETE-------------------------------------------\n");

    } catch (IOException x) {
        System.out.println("IO error");
    }
  }


  // Addnums for fake_cgi request...having a separate function for this is kinda unnecessary
  // But might as well follow the assignment to the letter
  String addnums(String input){

    CGI_Data cgiData = new CGI_Data(input);
    return cgiData.getOutput();

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
    
    // Get path for file/directory requested

    String basePath = "";
    String filePath = "";
    File base = new File("");
    String rootName = base.getAbsoluteFile().getName();

     try{
        
        basePath += base.getCanonicalPath();
        filePath += base.getCanonicalPath();
    } 
    catch (Throwable e){
        e.printStackTrace();
    }

    // Must stay within server directory tree
    // Check ".." and "~", but not the ".ht" files like 150-line webserver, not sure what that's for
    if (basePath.equals(filePath) && (input.contains("..") || input.contains("~"))){

        String errMsg = "Error: Must stay within directory";

        response.add("Content-length: " + errMsg.length());
        response.add("Content-type: text/plain");

        //Blank line, flush
        response.add("\r\n\r\n");
        response.add(errMsg);
        return response;
    }


    /**************************** */
    // Script
    // GET /cgi/addnums.fake-cgi?person=LukeRobbins&num1=7&num2=8 HTTP/1.1
    // "Dear [NAME], the sum of [NUM1] and [NUM2] is [RESULT]."

    if (input.startsWith("/cgi/")){

        response.add("Content-type: text/html");
        response.add("Content-length: ");
        response.add("\r\n\r\n");     //Blank line, flush

        String output = addnums(input);
    
        // Script response
        response.add("<html>");
    
        response.add(output);
    
        response.add("</html>");

        int dataSize = 0;
        for (int i = 5; i < response.size(); i++){
            dataSize += response.get(i).length();
        }
        response.set(3, "Content-length: " + Integer.toString(dataSize));
        return response;
    
    }

    //*************************** */


    filePath += "\\";                   // Add leading back slash
    basePath += "\\";

    String relPath = input.substring(1);
    String cPath = relPath.replace("/", "\\");
    filePath += cPath;     // Remove original front slash from request
    //System.out.println(filePath);  // Used for debugging

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
        response.add("Content-length: ");  // Placeholder until we get length

        //Blank line, flush
        response.add("\r\n\r\n");

        //******************
        // ADD BODY DATA
        //******************

        response.add("<html>");

        // Dynamic HTML
        response.add("Directory contents as of: " + (new Date().toString()) + "<br><br>");

        // Paren directory link
        if (!filePath.equals(basePath)){
            File parFile = new File(f.getParent());
            String parentDir = parFile.getName();
            if (parentDir.equals(rootName)) parentDir = "";
            
            response.add("<a href=\\" + parentDir + ">" + "Parent Dir" + "</a> <br>");
        }

        // Other files/dirs
        for (int i = 0; i < files.size(); i++){
            String parDir = f.getName();
            if (f.getName().equals(rootName)) parDir = "";

            // Wrap each file name in a link
            response.add("<a href=\"" + parDir + "/" + files.get(i).getName() + "\">" + files.get(i).getName() + "</a> <br>");
        }

        response.add("</html>");

        int totalSize = 0;
        for (int i = 5; i < response.size() - 1; i++){
            totalSize += response.get(i).length();
            totalSize += 2;
        }
        response.set(3, "Content-length: " + Integer.toString(totalSize));
    }
    else{

        // Content type, length
        // For length, just read the file size
        if (f.getName().endsWith("txt") || f.getName().endsWith("java")){
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

        // Read each line of the file
        // Add it to the response
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
    // Like Inet/Joke Server, spawn new threads to handle each
    while (processRequests) {
      sock = servsock.accept();
      new WebServerWorker (sock).start();
    }
  }
}