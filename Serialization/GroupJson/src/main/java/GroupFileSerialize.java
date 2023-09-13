import java.io.PrintWriter;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

/**
 * Purpose:
 * A class to demonstrate transferring a user-defined object via a file
 * using json serialization. Serialized object Group includes User objects.
 * <p/>
 * Ser321 Principles of Distributed Software Systems
 * @author Tim Lindquist (Tim.Lindquist@asu.edu) CIDSE - Software Engineering
 *                       Ira Fulton Schools of Engineering, ASU Polytechnic
 * @file    GroupFileSerialize.java
 * @date    January, 2020
 */
public class GroupFileSerialize {
  public static void main (String args[]) {

    try {

      //********************from java to json and back********************************************************//
      //starts as a Java 'Group' instance
      Group admin = new Group();
      admin.setName("Administration");
      admin.addUserToGroup("Tim","timWord");
      admin.addUserToGroup("Joe","joeWord");
      admin.addUserToGroup("Sue","sueWord");
      admin.printGroup();

      //displays the Java 'Group' instance like a JSON formatted object
      System.out.println("Administration group as json string: "+ admin.toJSONString());

      //exports Java 'Group' instance like a JSON formatted object to a json file
      PrintWriter out = new PrintWriter("admin.json");
      out.println(admin.toJSONString());
      out.close();
      System.out.println("Done exporting group in json to admin.json");

      // Imports the admin.json file and creates a Java 'Group' instance
      System.out.println("Importing group from admin.json");
      Group adminToo = new Group("admin.json");
      adminToo.printGroup();
      //********************from java to json and back********************************************************//

      //********************serialize and deserialize java objects********************************************************//
      // create .ser file of serialized 'Group' java objects
      //goes from 'Group' object to .ser file
      File outFile = new File("admin.ser");
      ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(outFile));
      os.writeObject(admin);
      os.flush(); //flush resource
      System.out.println("Used Java serialization of the group to admin.ser");
      os.close(); //close resource

      // deserialize 'Group' java objects from a .ser file
      // goes from .ser file to 'Group' java object
      File inFile = new File("admin.ser");
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(inFile));
      Group groupAgain = (Group)in.readObject();
      System.out.println("Done importing the group from admin.ser as:");
      groupAgain.printGroup();
      in.close(); //close resource
      //********************serialize and deserialize java objects********************************************************//

    } catch (Exception e) {
       System.out.println("exception: "+e.getMessage());
       e.printStackTrace();
    }
  }
}
