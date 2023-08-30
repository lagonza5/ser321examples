/*
Simple Web Server in Java which allows you to call 
localhost:9000/ and show you the root.html webpage from the www/root.html folder
You can also do some other simple GET requests:
1) /random shows you a random picture (well random from the set defined)
2) json shows you the response as JSON for /random instead the html page
3) /file/filename shows you the raw file (not as HTML)
4) /multiply?num1=3&num2=4 multiplies the two inputs and responses with the result
5) /github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving
   JSON which will for now only be printed in the console. See the todo below

The reading of the request is done "manually", meaning no library that helps making things a 
little easier is used. This is done so you see exactly how to pars the request and 
write a response back
*/

package funHttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;
//adding a new import, gradle.build has a json dependency
import org.json.*;

class WebServer {
  public static void main(String args[]) {

    WebServer server = new WebServer(9000);

  }

  /**
   * Main thread
   * @param port to listen on
   */
  public WebServer(int port) {
    ServerSocket server = null;
    Socket sock = null;
    InputStream in = null;
    OutputStream out = null;

    try {
      server = new ServerSocket(port);
      while (true) {
        sock = server.accept();
        out = sock.getOutputStream();
        in = sock.getInputStream();
        byte[] response = createResponse(in);
        out.write(response);
        out.flush();
        in.close();
        out.close();
        sock.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (sock != null) {
        try {
          server.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Used in the "/random" endpoint
   */
  private final static HashMap<String, String> _images = new HashMap<>() {
    {
      put("streets", "https://iili.io/JV1pSV.jpg");
      put("bread", "https://iili.io/Jj9MWG.jpg");
    }
  };

  private Random random = new Random();

  /**
   * Reads in socket stream and generates a response
   * @param inStream HTTP input stream from socket
   * @return the byte encoded HTTP response
   */
  public byte[] createResponse(InputStream inStream) {

    byte[] response = null;
    BufferedReader in = null;

    try {

      // Read from socket's input stream. Must use an
      // InputStreamReader to bridge from streams to a reader
      in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

      // Get header and save the request from the GET line:
      // example GET format: GET /index.html HTTP/1.1

      String request = null;

      boolean done = false;
      while (!done) {
        String line = in.readLine();

        System.out.println("Received: " + line);

        // find end of header("\n\n")
        if (line == null || line.equals(""))
          done = true;
        // parse GET format ("GET <path> HTTP/1.1")
        else if (line.startsWith("GET")) {
          int firstSpace = line.indexOf(" ");
          int secondSpace = line.indexOf(" ", firstSpace + 1);

          // extract the request, basically everything after the GET up to HTTP/1.1
          request = line.substring(firstSpace + 2, secondSpace);
        }
      }
      System.out.println("FINISHED PARSING HEADER\n");

      /*
      The order of the if-else statments that follow

      NULL request
          root page request (with requests explanation)
          /json request
          /random request
          /file/ request
          /multiply? request
          /github? request
          BAD request

       */

      // Generate an appropriate response to the user
      if (request == null) {
        //The method getBytes() encodes a String into a byte array using the platform's default charset if no argument is passed.
        response = "<html>Illegal request: no GET</html>".getBytes();
      } else {
        // create output buffer
        StringBuilder builder = new StringBuilder();
        // NOTE: output from buffer is at the end

        if (request.length() == 0) {
          // shows the default directory page

          // opens the root.html file
          String page = new String(readFileInBytes(new File("www/root.html")));
          //converts the file into HTML so later it can be displayed on the page

          // performs a template replacement in the page
          //The replace() method searches the HTML stored in 'page' for '${links}' and replaced it with files found at www directory
          page = page.replace("${links}", buildFileList());


          // The replace() method returns a new string with the value(s)/target '${links}' replaced with the files returned by buildFileList() method
          //buildFileList() returns a String of HTML that will contain the proper HTML tags/links with the filenames
          // in the right space for the root page of the website (when no GET request is made)


          // Generate response (Response, content type, then the String of HTML (directory of web page?)
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(page);

        } else if (request.equalsIgnoreCase("json")) {
          // shows the JSON of a random image and sets the header name for that image

          // pick an index from the map
          int index = random.nextInt(_images.size());

          // pull out the information
          String header = (String) _images.keySet().toArray()[index];
          String url = _images.get(header);

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: application/json; charset=utf-8\n");
          builder.append("\n");
          builder.append("{");
          builder.append("\"header\":\"").append(header).append("\",");
          builder.append("\"image\":\"").append(url).append("\"");
          builder.append("}");

        } else if (request.equalsIgnoreCase("random")) {
          // opens the random image page

          // open the index.html
          File file = new File("www/index.html");

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(new String(readFileInBytes(file)));

        } else if (request.contains("file/")) {
          // tries to find the specified file and shows it or shows an error

          // take the path and clean it. try to open the file
          File file = new File(request.replace("file/", ""));

          // Generate response
          if (file.exists()) { // success
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Would theoretically be a file but removed this part, you do not have to do anything with it for the assignment");
          } else { // failure
            builder.append("HTTP/1.1 404 Not Found\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("File not found: " + file);
          }
        } else if (request.contains("multiply?")) {
          // This multiplies two numbers, there is NO error handling, so when
          // wrong data is given this just crashes

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();

          // extract path parameters
          query_pairs = splitQuery(request.replace("multiply?", ""));
          // example return -> {{"num1", "1"}, {"num2","2"}} if they are provided of course...

          //Check if the LinkedHashMap was able to extract the parameters
          boolean num1Status = query_pairs.containsKey("num1");
          boolean num2Status = query_pairs.containsKey("num2");

          //The values that will be multiplied
          Integer num1;
          Integer num2;

          /*
          Possibilities
          Both parameters provided correctly
          Only one parameter provided correctly (could be either one)
          Neither parameter provided
          Query syntax did not make sense
           */

          //when both parameters are provided by user
          /*
          if (num1Status && num2Status) {
            num1 = parseIntOrDefault(query_pairs.get("num1"), 0);
            num2 = parseIntOrDefault(query_pairs.get("num2"), 0);

            // do math
            Integer result = num1 * num2;

            // Generate response
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Result is: " + result);
          }
          */


          //if the user does not provide a query that includes either parameter, the LinkedHashMap will be empty
          if (query_pairs.isEmpty()) { //both parameters missing
            num1 = parseIntOrDefault(query_pairs.get("num1"), 0);
            num2 = parseIntOrDefault(query_pairs.get("num2"), 0);

            builder.append("HTTP/1.1 488 Missing Both Parameters\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Result (using both default values) is: " + (num1 * num2) + "\n");
          } else if (num1Status && num2Status) {
            num1 = parseIntOrDefault(query_pairs.get("num1"), 0);
            num2 = parseIntOrDefault(query_pairs.get("num2"), 0);

            // do math
            Integer result = num1 * num2;

            // Generate response
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Result is: " + result);
          }


          // extract required fields from parameters
          //Integer num1 = Integer.parseInt(query_pairs.get("num1"));
          //Integer num2 = Integer.parseInt(query_pairs.get("num2"));

          // do math
          //Integer result = num1 * num2;

          // Generate response
          /*
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("Result is: " + result);

           */

          // TODO: Include error handling here with a correct error code and
          // a response that makes sense

        } else if (request.contains("github?")) {
          // pulls the query from the request and runs it with GitHub's REST API
          // check out https://docs.github.com/rest/reference/
          //
          // HINT: REST is organized by nesting topics. Figure out the biggest one first,
          //     then drill down to what you care about
          // "Owner's repo is named RepoName. Example: find RepoName's contributors" translates to
          //     "/repos/OWNERNAME/REPONAME/contributors"

          Map<String, String> query_pairs = new LinkedHashMap<String, String>();
          query_pairs = splitQuery(request.replace("github?", ""));
          String json = fetchURL("https://api.github.com/" + query_pairs.get("query"));
          System.out.println(json);

          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("Check the todos mentioned in the Java source file");
          // TODO: Parse the JSON returned by your fetch and create an appropriatE response based on what the assignment document asks for

        } else {
          // if the request is not recognized at all

          builder.append("HTTP/1.1 400 Bad Request\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("I am not sure what you want me to do...");
        }

        // Output
        response = builder.toString().getBytes();
      }
    } catch (IOException e) {
      e.printStackTrace();
      //TODO Are we supposed to print more than the exception stack trace?
      //My own addition ^
      response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes();
    }

    return response;
  }

  /**
   * Method to read in an integer value
   * @param number (initially in String format) from the LinkedHashMap generated by the splitQuery() method
   * @return
   * int -> when the user provides an actual integer
   * defaultVal -> only when the user does not provide a value
   *
   */
  public static Integer parseIntOrDefault (String number, int defaultVal) {
    try {
      return Integer.parseInt(number);
    } catch (NumberFormatException nfe) {
      return defaultVal;
    }
  }

  /**
   * Method to read in a query and split it up correctly
   * @param query parameters on path
   * @return Map of all parameters and their specific values
   * @throws UnsupportedEncodingException If the URLs aren't encoded with UTF-8
   */
  public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();

    if (query == null || query == "") {
      return query_pairs;
    }

    // "q=hello+world%2Fme&bob=5"
    String[] pairs = query.split("&");
    // ["q=hello+world%2Fme", "bob=5"]
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      //URLDecoder.decode() processes the URL query and replaces (plus signs, percent signs, etc) with proper whitespace in ASCII
      query_pairs.put( (URLDecoder.decode(pair.substring(0, idx), "UTF-8")), (URLDecoder.decode(pair.substring(idx + 1), "UTF-8")) );
      // key: (parameter name of query as String) , value: (variable String representation of integer value)
      // example for multiply? request -> query_pairs.put("num1", "1");
    }
    // {{"q", "hello world/me"}, {"bob","5"}}
    return query_pairs;
  }

  /**
   * Builds an HTML file list from the www directory
   * @return HTML string output of file list
   */
  public static String buildFileList() {

    //An array list that will contain all the files in a directory
    ArrayList<String> filenames = new ArrayList<>();

    // Creating a File object for directory start (project level)
    File directoryPath = new File("www/");

    //io.File.list() returns the array of files and directories in the directory defined by this abstract path name.
    //That array of files and directories is added to the array list
    filenames.addAll(Arrays.asList(directoryPath.list()));

    //Create String of HTML that will contain the proper HTML tags/links with the filenames in the right space
    if (filenames.size() > 0) {
      StringBuilder builder = new StringBuilder();
      //The <ul> tag defines an unordered (bulleted) list. Use the <ul> tag together with the <li> tag to create unordered lists.
      builder.append("<ul>\n");
      for (var filename : filenames) {
        builder.append("<li>" + filename + "</li>");
      }
      builder.append("</ul>\n");
      return builder.toString();
    } else {
      return "No files in directory";
    }
  }

  /**
   * Read bytes from a file and return them in the byte array. We read in blocks
   * of 512 bytes for efficiency.
   */
  public static byte[] readFileInBytes(File f) throws IOException {
    //file (FileInputStream) -> buffer -> data (ByteArrayOutputStream) -> result
    FileInputStream file = new FileInputStream(f);
    ByteArrayOutputStream data = new ByteArrayOutputStream(file.available());

    byte buffer[] = new byte[512];

    //The 'buffer' will read up to its own size (512 in this code) from the input stream (a file in this case), and
    // return the length of the bytes read from the file (could be full or partially full).
    int numRead = file.read(buffer);
    //file (FileInputStream) -> buffer, save how much was transferred in an int variable

    //confirm your buffer contains bytes that were just read from the file
    while (numRead > 0) {

      data.write(buffer, 0, numRead);
      //buffer -> data (ByteArrayOutputStream)

      //read the next 512 bytes from the file (FileInputStream); equivalent to the increment step for a while-loop
      numRead = file.read(buffer);
      //file (FileInputStream) -> buffer, save how much was transferred in an int variable
    }
    file.close(); //always close the file input stream at end of file

    //convert the output stream of bytes into an array of bytes
    byte[] result = data.toByteArray();
    data.close();
    //always close the data output stream (from buffer of bytes) once the array of bytes is created

    return result; //return the array of bytes
  }

  /**
   *
   * a method to make a web request. Note that this method will block execution
   * for up to 20 seconds while the request is being satisfied. Better to use a
   * non-blocking request.
   * 
   * @param aUrl the String indicating the query url for the OMDb api search
   * @return the String result of the http request.
   *
   **/
  public String fetchURL(String aUrl) {
    StringBuilder sb = new StringBuilder();
    URLConnection conn = null;
    InputStreamReader in = null;
    try {
      URL url = new URL(aUrl);
      conn = url.openConnection();
      if (conn != null)
        conn.setReadTimeout(20 * 1000); // timeout in 20 seconds
      if (conn != null && conn.getInputStream() != null) {
        in = new InputStreamReader(conn.getInputStream(), Charset.defaultCharset());
        BufferedReader br = new BufferedReader(in);
        if (br != null) {
          int ch;
          // read the next character until end of reader
          while ((ch = br.read()) != -1) {
            sb.append((char) ch);
          }
          br.close();
        }
      }
      in.close();
    } catch (Exception ex) {
      System.out.println("Exception in url request:" + ex.getMessage());
    }
    return sb.toString();
  }
}
