
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

      String sockdata = "";

      while (!sockdata.equals("stop")) {

        sockdata = in.readLine ();
        if (sockdata != null){
            System.out.println(sockdata);
        }
        System.out.flush ();
      }
      
     // sock.close(); 
    } catch (IOException x) {
      System.out.println("IO error");
    }
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