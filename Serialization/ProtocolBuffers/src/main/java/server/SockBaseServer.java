package server;

import java.net.*;
import java.io.*;

import server.Base;
import buffers.OperationProtos.Operation;
import buffers.ResponseProtos.Response;


class SockBaseServer {
    public static void main (String args[]) throws Exception {

        int count = 0;

        ServerSocket serv = null; //server socket

        InputStream in = null;
        OutputStream out = null;

        Socket clientSocket = null; //client socket

        int port = 9099; // default port

        int sleepDelay = 10000; // default delay

        //check args
        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
		}

        //parsing arguments should stay in a try-catch block when you are parsing values
        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }

        //fetching server parameters should be in a try-catch block
        try {
            serv = new ServerSocket(port);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        //I haven't seen these conditions yet...
        while (serv.isBound() && !serv.isClosed()) {
            System.out.println("Ready..."); //for debugging

            try {
                //set up client socket connection and streams for input/output
                clientSocket = serv.accept();
                in = clientSocket.getInputStream();
                out = clientSocket.getOutputStream();

                // read the stream message and create a proto object from it (from file operation.proto and its definitions)
                Operation op = Operation.parseDelimitedFrom(in); //proto library function

                String result = null;
                String num1 = op.getVal1(); //proto library function, created by the proto library due to our .proto file definitions
                String num2 = op.getVal2(); //proto library function
                int baseN = op.getBase(); //proto library function

                //Base.java contains code for all the valid operations and for converting numbers to appropriate numerical formats
                Base base = new Base();

                //The enum types come directly from the 'Operation' protobuf file
                if (op.getOperationType() == Operation.OperationType.ADD) {
                    //proto library function, created by the proto library due to our .proto file definitions for enum type
                    result = base.add(num1, num2, baseN);
                    System.out.println("base " + baseN + ": " + num1 + " + " + num2 + " = " + result);
                } else if (op.getOperationType() == Operation.OperationType.SUB) {
                    result = base.substract(num1, num2, baseN);
                    System.out.println("base " + baseN + ": " + num1 + " - " + num2 + " = " + result);
                }
                if (op.getResponseType() == Operation.ResponseType.JSON){
                    //proto library function, created by the proto library due to our .proto file definitions

                    //just building a JSON string
                    result = "{'result':'" + result +"'}";
                }


                Response response = buildResponse(result); //custom function that uses proto library functions within

                //proto library function to write data to an output stream
                response.writeDelimitedTo(out);

            } catch (Exception ex) {
                  ex.printStackTrace();
            } finally {
                  if (out != null)  out.close();
                  if (in != null)   in.close();
                  if (clientSocket != null) clientSocket.close();
                  //close resources
            }
        }
    }

    //server only has to build responses to client requests/messages
    private static Response buildResponse(String result) {
      Response.Builder response = Response.newBuilder(); //proto library functions
      response.setResultString(result); //proto library function
      return response.build(); //proto library function
    }
}

