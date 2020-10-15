package cmd;

import BD.DataHandler;
import BD.DataManager;
import BD.DataUserManager;
import consolehandler.TableController;
import productdata.Product;
import productdata.ReaderProductBuilder;
import clientserverdata.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

/**
 * Update whole element with given id
 *
 *
 */

public class CommandUpdate implements Command, Preparable{

    private String password;
    private String login;

    private static final long serialVersionUID = 1337000016L;

    /**
     * Iterates through all elements of collection and update element with given id
     *
     *
     */

    Product product = null;

    @Override
    public String execute(String[] args) {
        User user = new User();
        user.setUsername(login);
        user.setPassword(password);
        DataHandler handler = DataHandler.getInstance();
        DataUserManager userManager = new DataUserManager(handler);
        DataManager manager = new DataManager(handler, userManager);
        if(userManager.checkUserByUsernameAndPassword(user)) {
            if (product == null) {
                prepare(args);
            } else {
                try {
                    if (args == null || args[0] == null) {
                        return ("Please enter ID");
                    }
                    int counter = 0;
                    Iterator<Map.Entry<String, Product>> it = TableController.getCurrentTable().getSet().iterator();
                    int i = Integer.parseInt(args[0]);
                    while (it.hasNext()) {
                        Map.Entry<String, Product> map = it.next();
                        if (map.getValue().getId() == i) {
                            counter++;
                            if(manager.checkForRoots(i, user)) {
                                manager.updateProductById(map.getValue().getId(), product);
                                manager.deleteOrgAndLocByProductId(map.getValue().getId());
                                manager.insertOrgAndLoc(map.getValue().getId(),product,map.getKey(),user);
                            }else{
                                return "You are not product owner";
                            }
                        }
                    }
                    if (counter == 0) {
                        return ("There is no elements with that id.");
                    }
                } catch (NumberFormatException | SQLException e) {
                    return ("Argument must be a number");
                }
            }
        }else{
            return "You haven't got rights to do it(";
        }
        return "Element updated";
    }

    /**
     * get name of command
     *
     * @return String
     */

    @Override
    public String toString() {
        return "update_id";
    }

    @Override
    public void prepare(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        this.product = ReaderProductBuilder.buildProduct(reader);
    }
}
