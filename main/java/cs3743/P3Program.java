package cs3743;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class P3Program 
{
    private Connection connect = null;
    
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    public static final int ER_DUP_ENTRY = 1062;
    public static final int ER_DUP_ENTRY_WITH_KEY_NAME = 1586;
    public static final String[] strPropertyIdM =
    {   "MTNDDD"
       ,"NYCCC"
       ,"HOMEJJJ"
       ,"END"
    };
    
    public P3Program (String user, String password) throws Exception {
        try {
            // This will load the MySQL driver, each DBMS has its own driver
            MysqlDataSource ds = new MysqlDataSource();
            ds.setURL("jdbc:mysql://cs3743.fulgentcorp.com:3306/cs3743_" + user);
            ds.setUser(user);
            ds.setPassword(password);
            this.connect = ds.getConnection();
            connect.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        } catch (Exception e) {
            throw e;
        } 
        
    }
       
    public void runProgram() throws Exception 
    {
        try 
        {
            // your code
            // connect/3.a and 3.b
            statement = connect.createStatement();
            resultSet = statement.executeQuery("select c.* from Customer c");
            printCustomers("Beginning Customers ", resultSet);
            // 3.c and 3.d
            resultSet = statement.executeQuery("select m.* from Property m");
            MySqlUtility.printUtility("Beginning Properties", resultSet);
            // 3.e
            try
            {
                statement.executeUpdate("insert into cs3743_ejl483.Customer "
                    + "(`custNr`, `name`, `baseLoc`, `birthDt`, `gender`)"
                    + "values(1999, \"Alex Olivarez\", \"TX\", \"2001-01-31\", \"M\")");
            }
            catch (SQLException e)
            {
                switch (e.getErrorCode())
                {
                    case ER_DUP_ENTRY:
                    case ER_DUP_ENTRY_WITH_KEY_NAME:
                        System.out.printf("Duplicate key error: %s\n", e.getMessage());
                    break;
                    default:
                        throw e;
                }
            }
            catch (Exception e)
            {
                throw e;
            }
            // 3.f
            resultSet = statement.executeQuery("select c.* from Customer c");
            printCustomers("Customers after I was added", resultSet);
            // 3.g
            resultSet = statement.executeQuery("select TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX, COLUMN_NAME, CARDINALITY\n" +
                "from INFORMATION_SCHEMA.STATISTICS\n" +
                "where TABLE_SCHEMA = \"cs_3743_ejl483\" \n" +
                "and TABLE_NAME = \"Rental\"\n" +
                "order by INDEX_NAME, SEQ_IN_INDEX;"
            );
            MySqlUtility.printUtility("My Rental Indexes", resultSet);
            
            // 3.h
            preparedStatement = connect.prepareStatement("insert into cs3743_ejl483.Rental values " +
                "(?, ?, ?, ?)");

            // 3.i 
            double totalCost = 200;
            for(int i = 0; i < strPropertyIdM.length; i++){
                if(strPropertyIdM[i].equals("END"))
                    break;
                preparedStatement.setInt(1, 1999);
                preparedStatement.setString(2, strPropertyIdM[i]);
                preparedStatement.setDate(3, java.sql.Date.valueOf("2019-12-14"));
                preparedStatement.setDouble(4, totalCost);
                totalCost += 10.0;
        
                try{
                    preparedStatement.executeUpdate();
                }
                catch (SQLException e){
                    switch (e.getErrorCode()){
                        case ER_DUP_ENTRY:
                        case ER_DUP_ENTRY_WITH_KEY_NAME:
                            System.out.printf("Duplicate key error: %s\n", e.getMessage());
                            break;
                        default:
                            throw e;
                    }
                }
                catch (Exception e){
                    throw e;
                }
            }
            // 3.j
            preparedStatement = connect.prepareStatement("select r.* from Rental r where custNr = ?");
            preparedStatement.setInt(1, 1999);
            resultSet = preparedStatement.executeQuery();
            MySqlUtility.printUtility("My Rentals ", resultSet);
        
            // 3.k    
            resultSet = statement.executeQuery("select r.propId, r.custNr, c.name, r.totalCost " +
                    "from Rental r, Rental r1999, Customer c " +
                    "where r1999.custNr = 1999 " +
                    "and r.custNr <> 1999 " +
                    "and r1999.propId = r.propId " +
                    "and r.custNr = c.custNr;");
            MySqlUtility.printUtility("Other customers renting my properties", resultSet);
            // 3.l     
            statement.executeUpdate("update Rental set totalCost = totalCost * 0.9 where custNr = 1999;");
            preparedStatement = connect.prepareStatement("select r.* from Rental r where custNr = ?");
            preparedStatement.setInt(1, 1999);
            // 3.m
            resultSet = preparedStatement.executeQuery();
            MySqlUtility.printUtility("My Cheaper Rentals", resultSet);
             // 3.n
            resultSet = statement.executeQuery("select r.propId, COUNT(r.custNr) from Rental r " +
                    "group by r.propId having count(r.propId) > 2;");
            MySqlUtility.printUtility("Properties Having more than 2 rentals", resultSet);
            // 3.o 
            statement.executeUpdate("delete from Rental where custNr = 1999;");
            // 3.p
            resultSet = statement.executeQuery("select r.propId, r.custNr, c.name, r.totalCost " +
                    "from Rental r, Rental r1999, Customer c " +
                    "where r1999.custNr = 1999 " +
                    "and r.custNr <> 1999 " +
                    "and r1999.propId = r.propId " +
                    "and r.custNr = c.custNr;");
            MySqlUtility.printUtility("Other customers renting my properties after my rentals were deleted", resultSet);
        } 
        catch (Exception e) 
        {
            throw e;
        } 
        finally 
        {
            close();
        }

    }                                                                                                                        
    
    private void printCustomers(String title, ResultSet resultSet) throws SQLException 
    {
       // Your output for this must match the format of my sample output exactly. 
       // custNr, name, baseLoc, birthDt, gender
        System.out.printf("%s\n", title);
        // your code
        System.out.printf("%-6s     %-33s %-5s %-10s %s\n", "CustNr", "Name", "State", "BirthDt", "Gender");
        while (resultSet.next()){

            int custNr = resultSet.getInt("custNr");
            String nameStr = resultSet.getString("name");
            if(nameStr == null)
                nameStr = "---";
            String stateStr = resultSet.getString("baseLoc");
            if(stateStr == null)
                stateStr = "---";
            String birthDtStr = resultSet.getString("birthDt");
            if(birthDtStr == null)
                birthDtStr = "---";
            String gender = resultSet.getString("gender");
            System.out.printf("    %-6d %-33s %-5s %-10s %s\n"
                    , custNr
                    , nameStr
                    , stateStr
                    , birthDtStr
                    , gender);

        }
        System.out.printf("\n");

    }
    

    // Close the resultSet, statement, preparedStatement, and connect
    private void close() 
    {
        try 
        {
            if (resultSet != null) 
                resultSet.close();

            if (statement != null) 
                statement.close();
            
            if (preparedStatement != null) 
                preparedStatement.close();

            if (connect != null) 
                connect.close();
        } 
        catch (Exception e) 
        {

        }
    }

}
