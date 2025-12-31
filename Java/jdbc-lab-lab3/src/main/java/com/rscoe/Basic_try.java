package com.rscoe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Basic_try {

	public static void main(String[] args) {
		try {
			Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/adv_java","root","root@123");
			System.out.println("Success");
			System.out.println(con);
			
			String Sql1 = "INSERT INTO books (name) VALUES (?)";
			String Sql2 = "Update books set name = ? where name = ?";
			String Sql3= "delete from books where name = ?";
			String Sql4 = "select * from books";

			
			
			PreparedStatement ps = con.prepareStatement(Sql4);
			
			
			// ps.setString(1, "KIT_Books");
			// ps.setString(2, "COEP_Books");
			
			ResultSet rs = ps.executeQuery();
			
			
			while(rs.next()) {
				System.out.println(rs.getString("name"));
			}
		

			
			
			
		} catch (Exception e) {
		
			e.printStackTrace();
		}


	}

}