package demo;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBCConnection
 */
public class JDBCConnection {

	public static Connection getConnection(){
		Connection conn = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://hadoop:3306/storm","root","root");
		}catch (Exception e){
			e.printStackTrace();
		}
		return conn;
	}

	public static void insert(String word,int value){
		try {
			Connection conn = getConnection();
			String sql = "insert into words(word,count) values('" +
					word+"','" + value+"')";
			Statement stmt = conn.createStatement();
			int result = stmt.executeUpdate(sql);
			System.out.println("向表中插入"+result+"条");

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
