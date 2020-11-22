
/* ====================================================================== */
/* ====[                   SIRS - Contact Tracing                   ]==== */
/* ====[                   Server - Basic Version                   ]==== */
/* ====================================================================== */

// status - receive a single pair of <number, key>, not encrypted

package io.grpc.contact;

import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.sql.*;


public class Storage {

	// Class InfectedData
	public class InfectedData {
		private int _number;
		private int _key;
		private Instant _timestamp;

		InfectedData(int number, int key, Instant timestamp) {
			_number = number;
			_key = key;
			_timestamp = timestamp;
		}

		public int getNumbers(){
			return _number;
		}

		public int getKeys(){
			return _key;
		}

		public Instant getTimestamp(){
			return _timestamp;
		}
	}

	private Connection conn;

	/*
	private String yourUser = "afonso";
	private String yourPass = "yo";
	*/

	private String dbName = "contact";
	private String dbUser = "server";
	private String dbPass = "server";
	private String tableName = "numbers";

	Statement stmt = null;
	ResultSet rs = null;
	
	public Storage(){
		
		try{ 

			/*
			// Create database, if it doesn't exist
			this.conn = DriverManager.getConnection("jdbc:mysql://localhost/mysql?user=" + yourUser + "&password=" + yourPass);
			stmt = conn.createStatement();
			stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " +  dbName);
			stmt.executeUpdate("CREATE USER IF NOT EXISTS '" + dbUser + "'@'localhost' IDENTIFIED BY '" + dbPass + "';");
			stmt.executeUpdate("GRANT ALL PRIVILEGES ON " + dbName + " TO '" + dbUser + "'@'localhost';");
			*/

			// Connect to MySQL database
			this.conn = DriverManager.getConnection("jdbc:mysql://localhost/" + dbName + "?user=" + dbUser + "&password=" + dbPass + "&serverTimezone=UTC");

			// Create table data, if it doesn't exist
			String sqlCreate = "CREATE TABLE IF NOT EXISTS " + this.tableName
				+ "  (number          INT PRIMARY KEY,"
				+ "   pkey            INT,"
				+ "   seconds         LONG,"
				+ "   nanos           LONG);";
				
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlCreate);

		}catch(SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
		}catch(Exception ex){
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public List<InfectedData> getUpdates(Instant lastUpdate) {
		List<InfectedData> infectedData = new ArrayList<InfectedData>();

		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + this.tableName);
			
			while (rs.next()) {
				int number = rs.getInt("number");
				int key = rs.getInt("pkey");
				long seconds = rs.getLong("seconds");
				long nanos = rs.getLong("nanos");
				Instant timestamp = Instant.ofEpochSecond(seconds, nanos);
				InfectedData data = new InfectedData(number, key, timestamp);
				infectedData.add(data);
			}

		}catch(SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
		}

		List<InfectedData> new_data = new ArrayList<InfectedData>();

		for (InfectedData i : infectedData){
			Instant ts = i.getTimestamp();
			if (ts.compareTo(lastUpdate) >= 0)
				new_data.add(i);
		}

		return new_data;
		
	}

	public void storeInfectedData(int number, int key, Instant timestamp) {

		long seconds = timestamp.getEpochSecond() ;
		long nanos = timestamp.getNano();
		
		try{
			String statement = "INSERT INTO " + this.tableName + " VALUES("
				+ Integer.toString(number) + ","
				+ Integer.toString(key) +    ","
				+ Long.toString(seconds) +   ","
				+ Long.toString(nanos) +     ");";
			
			stmt = conn.createStatement();
			stmt.executeUpdate(statement);
		}catch(SQLException ex){
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
		}
	}
}
