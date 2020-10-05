package cmd;
import consolehandler.TableController;

/**
 * removes element with given key
 *
 *
 */

public class CommandRemove implements Command {

    private String password;
    private String login;

    private static final long serialVersionUID = 1337000012L;

    @Override
    public String execute(String[] args) {
        int count = 0;
        for(String key : TableController.getCurrentTable().getKey()){
            if(key.equals(args[0])){
                count++;
            }
        }
        if(count==0){
            return ("No such key\nAvailable keys: " + TableController.getCurrentTable().getKey());
        }else{
            TableController.getCurrentTable().remove(args[0]);
            return ("Element has been removed.");
        }
    }

    /**
     * get name of command
     *
     * @return String
     */

    public String toString(){
        return "remove_key";
    }
}
