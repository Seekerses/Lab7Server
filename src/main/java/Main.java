
import consolehandler.*;
import server.ServerController;

import java.io.*;

public class Main {

    public static void main(String[] args){
        TableManager prodTable = new TableManager("products");
        TableController.setCurrentTable(prodTable);
        try {
            if( new File("saved.csv").createNewFile()){
                System.out.println("Save file created.");
            }
        }
        catch (Exception e ){
            System.out.println("Could not create default save file, please specify it manually\n");
        }
        if(args.length != 0) {
            Initializer.init(prodTable, new File(args[0]));
        }
        else {
            try {
                Initializer.init(prodTable, new File("saved.csv").exists() ? new File("saved.csv") : null);
            } catch (NullPointerException e) {
                Initializer.init(prodTable, null);
            }
        }
        ServerController.connect();
        new Thread(() -> {
        try {
            ServerController.start();
        }
        catch (IOException e){
            System.out.println("Oh, no. Server got a wrong data and fall...");
        }
    }).start();
        CommandController cmd = new CommandController();
        cmd.start(new CommandInterpreter());
        System.out.println("Enter Command or Help to display a list of commands:");
    }
}
