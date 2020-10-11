package BD;

import exceptions.InvalidYCoordinate;
import exceptions.NegativePrice;
import exceptions.NotUniqueFullName;
import exceptions.TooLargeFullName;
import productdata.*;
import clientserverdata.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DataManager {
    // PRODUCTS_TABLE
    private final String SELECT_ALL_PRODUCTS = "SELECT * FROM " + BD.DataHandler.PRODUCTS_TABLE;
    private final String SELECT_ALL_PRODUCTS_BY_USER_ID = "SELECT * FROM " + BD.DataHandler.PRODUCTS_TABLE + " WHERE " +
            BD.DataHandler.PRODUCTS_TABLE_USER_ID_COLUMN + " = ?";
    private final String SELECT_PRODUCTS_BY_ID = SELECT_ALL_PRODUCTS + " WHERE " +
            BD.DataHandler.PRODUCTS_TABLE_ID_COLUMN + " = ?";
    private final String SELECT_PRODUCTS_BY_ID_AND_USER_ID = SELECT_PRODUCTS_BY_ID + " AND " +
            BD.DataHandler.PRODUCTS_TABLE_USER_ID_COLUMN + " = ?";
    private final String INSERT_PRODUCTS = "INSERT INTO " +
            BD.DataHandler.PRODUCTS_TABLE + " (" +
            BD.DataHandler.PRODUCTS_TABLE_ID_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_KEY_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_NAME_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_CREATION_DATE_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_TYPE_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_X_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_Y_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_PRICE_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_USER_ID_COLUMN + ") VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final String INSERT_PRODUCTS_WITH_ID = "INSERT INTO " +
            BD.DataHandler.PRODUCTS_TABLE + " (" +
            BD.DataHandler.PRODUCTS_TABLE_ID_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_KEY_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_NAME_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_CREATION_DATE_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_TYPE_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_X_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_Y_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_PRICE_COLUMN + ", " +
            BD.DataHandler.PRODUCTS_TABLE_USER_ID_COLUMN + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final String DELETE_PRODUCTS_BY_USER_ID = "DELETE FROM " + BD.DataHandler.PRODUCTS_TABLE +
            " WHERE " + BD.DataHandler.PRODUCTS_TABLE_USER_ID_COLUMN + " = ?";
    private final String DELETE_PRODUCTS_BY_ID = "DELETE FROM " + BD.DataHandler.PRODUCTS_TABLE +
            " WHERE " + BD.DataHandler.PRODUCTS_TABLE_ID_COLUMN + " = ?";


    // COORDINATES_TABLE
    private final String SELECT_ALL_COORDINATES = "SELECT * FROM " + BD.DataHandler.LOCATION_TABLE;
    private final String SELECT_COORDINATES_BY_ORGANISATION_ID = SELECT_ALL_COORDINATES +
            " WHERE " + BD.DataHandler.COORDINATES_TABLE_ORGANISATION_ID_COLUMN + " = ?";
    private final String INSERT_COORDINATES = "INSERT INTO " +
            BD.DataHandler.LOCATION_TABLE + " (" +
            BD.DataHandler.COORDINATES_TABLE_ID_COLUMN + ", " +
            BD.DataHandler.COORDINATES_TABLE_ORGANISATION_ID_COLUMN + ", " +
            BD.DataHandler.COORDINATES_TABLE_STREET_COLUMN + ", " +
            BD.DataHandler.COORDINATES_TABLE_X_COLUMN + ", " +
            BD.DataHandler.COORDINATES_TABLE_Y_COLUMN + ", " +
            BD.DataHandler.COORDINATES_TABLE_Z_COLUMN + ") VALUES (DEFAULT, ?, ?, ?, ?, ?)";
    private final String UPDATE_COORDINATES_BY_ORGANISATION_ID = "UPDATE " + BD.DataHandler.LOCATION_TABLE + " SET " +
            BD.DataHandler.COORDINATES_TABLE_X_COLUMN + " = ?, " +
            BD.DataHandler.COORDINATES_TABLE_Y_COLUMN + " = ?" + " WHERE " +
            BD.DataHandler.COORDINATES_TABLE_ORGANISATION_ID_COLUMN + " = ?";
    private final String DELETE_COORDINATES_BY_ORGANISATION_ID = "DELETE FROM " + BD.DataHandler.LOCATION_TABLE +
            " WHERE " + BD.DataHandler.COORDINATES_TABLE_ORGANISATION_ID_COLUMN + " = ?";

    // ORGANISATIONS_TABLE
    private final String SELECT_ALL_ORGANISATIONS = "SELECT * FROM " + BD.DataHandler.ORGANISATIONS_TABLE;
    private final String SELECT_ORGANISATIONS_BY_ID = SELECT_ALL_ORGANISATIONS +
            " WHERE " + BD.DataHandler.ORGANISATIONS_TABLE_ID_COLUMN + " = ?";
    private final String SELECT_ORGANISATIONS_BY_PRODUCT_ID = SELECT_ALL_ORGANISATIONS +
            " WHERE " + BD.DataHandler.ORGANISATIONS_TABLE_PRODUCT_ID_COLUMN + " = ?";
    private final String INSERT_ORGANISATIONS = "INSERT INTO " +
            BD.DataHandler.ORGANISATIONS_TABLE + " (" +
            BD.DataHandler.ORGANISATIONS_TABLE_ID_COLUMN + ", " +
            BD.DataHandler.ORGANISATIONS_TABLE_NAME_COLUMN + ", " +
            BD.DataHandler.ORGANISATIONS_TABLE_FULLNAME_COLUMN + ", " +
            BD.DataHandler.ORGANISATIONS_TABLE_TYPE_COLUMN + ", " +
            BD.DataHandler.ORGANISATIONS_TABLE_PRODUCT_ID_COLUMN  + ") VALUES (DEFAULT, ?, ?, ?, ?)";
    private final String UPDATE_ORGANISATIONS_BY_PRODUCT_ID = "UPDATE " + BD.DataHandler.ORGANISATIONS_TABLE + " SET " +
            BD.DataHandler.ORGANISATIONS_TABLE_NAME_COLUMN + " = ?, " +
            BD.DataHandler.ORGANISATIONS_TABLE_FULLNAME_COLUMN + " = ?, " +
            BD.DataHandler.ORGANISATIONS_TABLE_TYPE_COLUMN + " = ?, " + " WHERE " +
            BD.DataHandler.PRODUCTS_TABLE_ID_COLUMN + " = ?";
    private final String DELETE_ORGANISATIONS_BY_ID = "DELETE FROM " + BD.DataHandler.ORGANISATIONS_TABLE +
            " WHERE " + BD.DataHandler.ORGANISATIONS_TABLE_ID_COLUMN + " = ?";
    private final String DELETE_ORGANISATIONS_BY_PRODUCT_ID = "DELETE FROM " + BD.DataHandler.ORGANISATIONS_TABLE +
            " WHERE " + BD.DataHandler.ORGANISATIONS_TABLE_PRODUCT_ID_COLUMN + " = ?";

    private DataHandler DataHandler;
    private DataUserManager dataUserManager;
    private ReentrantLock lock;

    public DataManager(DataHandler DataHandler, DataUserManager userManager) {
        this.DataHandler = DataHandler;
        this.dataUserManager = userManager;
        this.lock = new ReentrantLock();
    }

    private Product createProduct(ResultSet resultSet, Map<Long, Organization> organizationMap) throws SQLException, NegativePrice, NotUniqueFullName, TooLargeFullName, InvalidYCoordinate {
        try {
            lock.lock();
            long id = resultSet.getLong("id");
            String name = resultSet.getString(BD.DataHandler.PRODUCTS_TABLE_NAME_COLUMN);
            LocalDateTime creationDate = resultSet.getTimestamp(BD.DataHandler.PRODUCTS_TABLE_CREATION_DATE_COLUMN).toLocalDateTime();
            UnitOfMeasure type = UnitOfMeasure.valueOf(resultSet.getString(BD.DataHandler.PRODUCTS_TABLE_TYPE_COLUMN));
            double px = resultSet.getDouble(BD.DataHandler.PRODUCTS_TABLE_X_COLUMN);
            int py = resultSet.getInt(BD.DataHandler.PRODUCTS_TABLE_Y_COLUMN);
            Float price = resultSet.getFloat(BD.DataHandler.PRODUCTS_TABLE_PRICE_COLUMN);
            User owner = dataUserManager.getUserById(resultSet.getLong(BD.DataHandler.PRODUCTS_TABLE_USER_ID_COLUMN));
            Product product = new Product(
                    id,
                    name,
                    new Coordinates(px, py),
                    price,
                    type,
                    organizationMap.get(id),
                    creationDate
            );
            product.setOwner(owner);
            return product;
        }catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
        return null;
    }

    private Organization createOrganisation(ResultSet resultSet, Map<Integer, Address> addressMap){
        try {
            lock.lock();
            int org_id = resultSet.getInt(BD.DataHandler.ORGANISATIONS_TABLE_ID_COLUMN);
            String org_name = resultSet.getString(BD.DataHandler.ORGANISATIONS_TABLE_NAME_COLUMN);
            String org_fname = resultSet.getString("fullname");
            OrganizationType org_type = OrganizationType.valueOf(resultSet.getString(BD.DataHandler.ORGANISATIONS_TABLE_TYPE_COLUMN));
            UniqueController.deleteRow(org_fname);
            return new Organization(
                    org_id,
                    org_name,
                    org_fname,
                    org_type,
                    addressMap.get(org_id)
                    );
        }catch (SQLException e){
            System.out.println("Nothing to put in");
        }catch (TooLargeFullName e){
            System.out.println("Too large full name");
        }catch (NotUniqueFullName e){
            System.out.println("Not unique full name");
        }
        finally {
            lock.unlock();
        }
        return null;
    }

    private Address createLocation(ResultSet resultSet) {
        try {
            lock.lock();
            String street = resultSet.getString(BD.DataHandler.COORDINATES_TABLE_STREET_COLUMN);
            long x = resultSet.getLong(BD.DataHandler.COORDINATES_TABLE_X_COLUMN);
            int y = resultSet.getInt(BD.DataHandler.COORDINATES_TABLE_Y_COLUMN);
            long z = resultSet.getLong(BD.DataHandler.COORDINATES_TABLE_Z_COLUMN);
            Location loc = new Location(x, y, z);
            return new Address(street, loc);
        }catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
        return null;
    }

    public void insertProduct(long id, Product product, String key, User user) throws SQLException {
        PreparedStatement insertProductStatement = null;
        PreparedStatement insertOrganisationStatement =null;
        PreparedStatement insertLocationStatement = null;
        DataHandler.setCommitMode();

        try{
            DataHandler.setSavepoint();

            LocalDateTime creationtime = LocalDateTime.now();

            insertProductStatement = DataHandler.getPreparedStatement(INSERT_PRODUCTS,false);
            insertOrganisationStatement = DataHandler.getPreparedStatement(INSERT_ORGANISATIONS, false);
            insertLocationStatement = DataHandler.getPreparedStatement(INSERT_COORDINATES,false);

            insertProductStatement.setLong(1, id);
            insertProductStatement.setString(2, key);
            insertProductStatement.setString(3, product.getName());
            insertProductStatement.setString(4, String.valueOf(Timestamp.valueOf(creationtime)));
            insertProductStatement.setString(5, product.getUnitOfMeasure().toString());
            insertProductStatement.setDouble(6, product.getCoordinates().getX());
            insertProductStatement.setInt(7, product.getCoordinates().getY());
            insertProductStatement.setFloat(8, product.getPrice());
            insertProductStatement.setLong(9, dataUserManager.getUserIdByUsername(user));
            if (insertProductStatement.executeUpdate() == 0) throw new SQLException();
            long productId = 1;
            Statement s = DataHandler.getConnection().createStatement();
            ResultSet rs = s.executeQuery("select * from products");
            while(rs.next()){
                productId = rs.getInt(1);}

            if(product.getManufacturer()!=null) {
                insertOrganisationStatement.setString(1, product.getManufacturer().getName());
                insertOrganisationStatement.setString(2, product.getManufacturer().getFullName());
                insertOrganisationStatement.setString(3, product.getManufacturer().getType().toString());
                insertOrganisationStatement.setLong(4, productId);
                if (insertOrganisationStatement.executeUpdate() == 0) throw new SQLException();
                long orgId = 1;
                Statement st = DataHandler.getConnection().createStatement();
                ResultSet rst = st.executeQuery("select * from organisations");
                while (rst.next()) {
                    orgId = rst.getInt(1);
                }

                insertLocationStatement.setLong(1, orgId);
                insertLocationStatement.setString(2, product.getManufacturer().getPostalAddress().getStreet());
                insertLocationStatement.setDouble(3, product.getManufacturer().getPostalAddress().getTown().getX());
                insertLocationStatement.setInt(4, product.getManufacturer().getPostalAddress().getTown().getY());
                insertLocationStatement.setLong(5, product.getManufacturer().getPostalAddress().getTown().getZ());
                if (insertLocationStatement.executeUpdate() == 0) throw new SQLException();
            }
            DataHandler.commit();
        } catch (SQLException e) {
            DataHandler.rollback();
            e.printStackTrace();
        } finally {
            DataHandler.closePreparedStatement(insertLocationStatement);
            DataHandler.closePreparedStatement(insertOrganisationStatement);
            DataHandler.closePreparedStatement(insertProductStatement);
            DataHandler.setNormalMode();
        }
    }

    public void insertProduct(Product product, String key, User user) throws SQLException {
        PreparedStatement insertProductStatement = null;
        PreparedStatement insertOrganisationStatement =null;
        PreparedStatement insertLocationStatement = null;
        DataHandler.setCommitMode();

        try{
            lock.lock();
            DataHandler.setSavepoint();

            LocalDateTime creationtime = LocalDateTime.now();

            insertProductStatement = DataHandler.getPreparedStatement(INSERT_PRODUCTS,false);
            insertOrganisationStatement = DataHandler.getPreparedStatement(INSERT_ORGANISATIONS, false);
            insertLocationStatement = DataHandler.getPreparedStatement(INSERT_COORDINATES,false);

            insertProductStatement.setString(1, key);
            insertProductStatement.setString(2, product.getName());
            insertProductStatement.setString(3, String.valueOf(Timestamp.valueOf(creationtime)));
            insertProductStatement.setString(4, product.getUnitOfMeasure().toString());
            insertProductStatement.setDouble(5, product.getCoordinates().getX());
            insertProductStatement.setInt(6, product.getCoordinates().getY());
            insertProductStatement.setFloat(7, product.getPrice());
            insertProductStatement.setLong(8, dataUserManager.getUserIdByUsername(user));
            if (insertProductStatement.executeUpdate() == 0) throw new SQLException();
            long productId = 1;
            Statement s = DataHandler.getConnection().createStatement();
            ResultSet rs = s.executeQuery("select * from products");
            while(rs.next()){
            productId = rs.getInt(1);}

            if(product.getManufacturer()!=null) {
                insertOrganisationStatement.setString(1, product.getManufacturer().getName());
                insertOrganisationStatement.setString(2, product.getManufacturer().getFullName());
                insertOrganisationStatement.setString(3, product.getManufacturer().getType().toString());
                insertOrganisationStatement.setLong(4, productId);
                if (insertOrganisationStatement.executeUpdate() == 0) throw new SQLException();
                long orgId = 1;
                Statement st = DataHandler.getConnection().createStatement();
                ResultSet rst = st.executeQuery("select * from organisations");
                while (rst.next()) {
                    orgId = rst.getInt(1);
                }

                insertLocationStatement.setLong(1, orgId);
                insertLocationStatement.setString(2, product.getManufacturer().getPostalAddress().getStreet());
                insertLocationStatement.setDouble(3, product.getManufacturer().getPostalAddress().getTown().getX());
                insertLocationStatement.setInt(4, product.getManufacturer().getPostalAddress().getTown().getY());
                insertLocationStatement.setLong(5, product.getManufacturer().getPostalAddress().getTown().getZ());
                if (insertLocationStatement.executeUpdate() == 0) throw new SQLException();
            }
            DataHandler.commit();
        } catch (SQLException e) {
            DataHandler.rollback();
            e.printStackTrace();
        } finally {
            DataHandler.closePreparedStatement(insertLocationStatement);
            DataHandler.closePreparedStatement(insertOrganisationStatement);
            DataHandler.closePreparedStatement(insertProductStatement);
            DataHandler.setNormalMode();
            lock.unlock();
        }
    }

    public Hashtable<String,Product> getCollection(){
        Hashtable<String,Product> products = new Hashtable<>();
        Map<Integer, Address> addressMap = new HashMap<>();
        Map<Long, Organization> orgMap = new HashMap<>();
        PreparedStatement preparedSelectAllProducts = null;
        PreparedStatement preparedSelectAllOrganisations = null;
        PreparedStatement preparedSelectAllLocations = null;
        try {
            lock.lock();
            preparedSelectAllProducts = DataHandler.getPreparedStatement(SELECT_ALL_PRODUCTS, false);
            preparedSelectAllOrganisations = DataHandler.getPreparedStatement(SELECT_ALL_ORGANISATIONS,false);
            preparedSelectAllLocations = DataHandler.getPreparedStatement(SELECT_ALL_COORDINATES,false);
            ResultSet resultSet = preparedSelectAllLocations.executeQuery();
            while (resultSet.next()) {
                Address adr = createLocation(resultSet);
                addressMap.put(resultSet.getInt("organisation_id"),adr);
            }
            DataHandler.closePreparedStatement(preparedSelectAllLocations);
            resultSet.close();

            ResultSet resultSet1 = preparedSelectAllOrganisations.executeQuery();
            while (resultSet1.next()) {
                Organization org = createOrganisation(resultSet1, addressMap);
                assert org != null;
                orgMap.put(resultSet1.getLong("product_id"),org);
            }
            DataHandler.closePreparedStatement(preparedSelectAllOrganisations);
            resultSet1.close();

            ResultSet resultSet2 = preparedSelectAllProducts.executeQuery();
            while (resultSet2.next()) {
                Product product = createProduct(resultSet2, orgMap);
                assert product != null;
                products.put(resultSet2.getString("key"), product);
            }
            DataHandler.closePreparedStatement(preparedSelectAllOrganisations);
            resultSet1.close();
        } catch (SQLException exception) {
            System.out.println("Something went wrong with BD");
        } catch (NotUniqueFullName | NegativePrice | TooLargeFullName | InvalidYCoordinate notUniqueFullName) {
            notUniqueFullName.printStackTrace();
        } finally {
            lock.unlock();
            DataHandler.closePreparedStatement(preparedSelectAllProducts);
            DataHandler.closePreparedStatement(preparedSelectAllLocations);
            DataHandler.closePreparedStatement(preparedSelectAllOrganisations);
        }
        return products;
    }

    public void deleteProductByUser(User user) {
        PreparedStatement preparedSelectProductByUser = null;
        PreparedStatement preparedDeleteProductByUser = null;
        PreparedStatement preparedDeleteOrganisationByProductId = null;
        PreparedStatement preparedSelectOrganisationsByProductId = null;
        PreparedStatement preparedDeleteLocationByOrganisationId = null;
        try {
            lock.lock();
            DataHandler.setCommitMode();
            DataHandler.setSavepoint();
            ArrayList<Long> ids = new ArrayList<>();
            preparedSelectProductByUser = DataHandler.getPreparedStatement(SELECT_ALL_PRODUCTS_BY_USER_ID, false);
            preparedSelectProductByUser.setLong(1, dataUserManager.getUserIdByUsername(user));
            ResultSet rs = preparedSelectProductByUser.executeQuery();
            while (rs.next()){
                ids.add(rs.getLong("id"));
                System.out.println(rs.getLong("id"));
            }
            preparedDeleteProductByUser = DataHandler.getPreparedStatement(DELETE_PRODUCTS_BY_USER_ID, false);
            preparedDeleteProductByUser.setLong(1, dataUserManager.getUserIdByUsername(user));
            if (preparedDeleteProductByUser.executeUpdate() == 0) throw new SQLException();

            int i = 0;
            while(i <= ids.size()) {
                ArrayList<Long> orgIds = new ArrayList<>();
                preparedSelectOrganisationsByProductId = DataHandler.getPreparedStatement(SELECT_ORGANISATIONS_BY_PRODUCT_ID, false);
                preparedSelectOrganisationsByProductId.setLong(1, ids.get(i));
                ResultSet resultSet = preparedSelectOrganisationsByProductId.executeQuery();
                while(resultSet.next()){
                    orgIds.add(resultSet.getLong("id"));
                    System.out.println(resultSet.getLong("id"));
                }

                preparedDeleteOrganisationByProductId = DataHandler.getPreparedStatement(DELETE_ORGANISATIONS_BY_PRODUCT_ID, false);
                preparedDeleteOrganisationByProductId.setLong(1, ids.get(i));
                if (preparedDeleteOrganisationByProductId.executeUpdate() == 0) throw new SQLException();
                i++;
                DataHandler.closePreparedStatement(preparedDeleteOrganisationByProductId);

                int j = 0;
                while(j <= orgIds.size()) {
                    preparedDeleteLocationByOrganisationId = DataHandler.getPreparedStatement(DELETE_COORDINATES_BY_ORGANISATION_ID, false);
                    preparedDeleteLocationByOrganisationId.setLong(1, orgIds.get(j));
                    if (preparedDeleteLocationByOrganisationId.executeUpdate() == 0) throw new SQLException();
                    j++;
                    DataHandler.closePreparedStatement(preparedDeleteLocationByOrganisationId);
                }
            }
            DataHandler.commit();

        } catch (SQLException exception) {
            exception.printStackTrace();
            DataHandler.rollback();
            System.out.println("ERROR IN DELETE_PRODUCTS_BY_USER_ID!");
        } finally {
            lock.unlock();
            DataHandler.setNormalMode();
            DataHandler.closePreparedStatement(preparedSelectProductByUser);
            DataHandler.closePreparedStatement(preparedDeleteProductByUser);
            DataHandler.closePreparedStatement(preparedSelectOrganisationsByProductId);
        }
    }

    public boolean checkForRoots(long productId, User user){
        PreparedStatement preparedCheckForRoots = null;
        try{
            lock.lock();
            preparedCheckForRoots = DataHandler.getPreparedStatement(SELECT_PRODUCTS_BY_ID_AND_USER_ID, false);
            preparedCheckForRoots.setLong(1, productId);
            preparedCheckForRoots.setLong(2, dataUserManager.getUserIdByUsername(user));
            ResultSet rs = preparedCheckForRoots.executeQuery();
            boolean f = rs.next();
            rs.close();
            return f;
        }catch (SQLException e){
            System.out.println("Problema v BD a ne vo mne");
        }finally {
            lock.unlock();
            DataHandler.closePreparedStatement(preparedCheckForRoots);
        }
        return false;
    }

    public Product getProduct(long productid){
        PreparedStatement preparedStatement = null;
        try {
            lock.lock();
            preparedStatement = DataHandler.getPreparedStatement(SELECT_PRODUCTS_BY_ID, false);
            preparedStatement.setLong(1, productid);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            long id = resultSet.getLong("id");
            String name = resultSet.getString(BD.DataHandler.PRODUCTS_TABLE_NAME_COLUMN);
            LocalDateTime creationDate = resultSet.getTimestamp(BD.DataHandler.PRODUCTS_TABLE_CREATION_DATE_COLUMN).toLocalDateTime();
            UnitOfMeasure type = UnitOfMeasure.valueOf(resultSet.getString(BD.DataHandler.PRODUCTS_TABLE_TYPE_COLUMN));
            double px = resultSet.getDouble(BD.DataHandler.PRODUCTS_TABLE_X_COLUMN);
            int py = resultSet.getInt(BD.DataHandler.PRODUCTS_TABLE_Y_COLUMN);
            Float price = resultSet.getFloat(BD.DataHandler.PRODUCTS_TABLE_PRICE_COLUMN);
            User owner = dataUserManager.getUserById(resultSet.getLong(BD.DataHandler.PRODUCTS_TABLE_USER_ID_COLUMN));
            resultSet.close();
            Product product = new Product(
                    id,
                    name,
                    new Coordinates(px, py),
                    price,
                    type,
                    null,
                    creationDate
            );
            product.setOwner(owner);
            return product;
        }catch (SQLException | InvalidYCoordinate | NegativePrice e){
            e.printStackTrace();
        }finally {
            lock.unlock();
            DataHandler.closePreparedStatement(preparedStatement);
        }
        return null;
    }

    public void deleteProductById(long id){
        PreparedStatement preparedDeleteProductById = null;
        PreparedStatement preparedDeleteOrganisationByProductId = null;
        PreparedStatement preparedDeleteLocationByOrgId = null;
        PreparedStatement preparedSelectOrgByProdId = null;
        try{
            lock.lock();
            preparedDeleteProductById = DataHandler.getPreparedStatement(DELETE_PRODUCTS_BY_ID, false);
            preparedDeleteProductById.setLong(1, id);
            if(preparedDeleteProductById.executeUpdate() == 0) throw new SQLException();

            preparedSelectOrgByProdId = DataHandler.getPreparedStatement(SELECT_ORGANISATIONS_BY_PRODUCT_ID, false);
            preparedSelectOrgByProdId.setLong(1, id);
            ResultSet resultSet = preparedSelectOrgByProdId.executeQuery();
            resultSet.next();
            long org_id = resultSet.getLong("id");

            preparedDeleteOrganisationByProductId = DataHandler.getPreparedStatement(DELETE_ORGANISATIONS_BY_PRODUCT_ID, false);
            preparedDeleteOrganisationByProductId.setLong(1, id);
            if(preparedDeleteOrganisationByProductId.executeUpdate() == 0) throw new SQLException();

            preparedDeleteLocationByOrgId = DataHandler.getPreparedStatement(DELETE_COORDINATES_BY_ORGANISATION_ID, false);
            preparedDeleteLocationByOrgId.setLong(1, org_id);
            if(preparedDeleteLocationByOrgId.executeUpdate() == 0) throw new SQLException();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            DataHandler.closePreparedStatement(preparedDeleteProductById);
            DataHandler.closePreparedStatement(preparedDeleteLocationByOrgId);
            DataHandler.closePreparedStatement(preparedDeleteOrganisationByProductId);
            DataHandler.closePreparedStatement(preparedSelectOrgByProdId);
        }
    }

}