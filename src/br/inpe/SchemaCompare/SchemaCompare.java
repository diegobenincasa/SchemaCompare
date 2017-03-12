package br.inpe.SchemaCompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class SchemaCompare {

	public static void main(String[] args){
		try
		{
			Class.forName("org.postgresql.Driver");
			List<String> projects = new ArrayList<String>();
			XMLUnitCompare comp = new XMLUnitCompare();
			
			String url = "jdbc:postgresql://localhost/xsdminer";
            Properties props = new Properties();
            props.setProperty("user","postgres");
            props.setProperty("password","070910");
            Connection conn = DriverManager.getConnection(url, props);

			String query = "SELECT DISTINCT project FROM files ORDER BY project";
        	PreparedStatement st = conn.prepareStatement(query);
        	ResultSet rs = st.executeQuery();
  
        	while(rs.next())
        	{
        		projects.add(rs.getString(1));
        	}
        	rs.close();
        	
        	for(String p : projects)
//        	String p = "apache-ode";
        	{
        		String checkProcessed = "SELECT processed FROM projects WHERE projectname = ?;";
        		PreparedStatement psCheckProcessed = conn.prepareStatement(checkProcessed);
        		psCheckProcessed.setString(1, p);
        		ResultSet rsCheckProcessed = psCheckProcessed.executeQuery();
        		rsCheckProcessed.next();
        		if(rsCheckProcessed.getBoolean(1) == true)
        				continue;

        		String query2 = "select sq.fn as filename, count(sq.sc) as schemas from "
        				+ "(select distinct schemacount as sc, filename as fn from files where project = ? and valid = true) sq "
        				+ "group by sq.fn";
            	PreparedStatement st2 = conn.prepareStatement(query2);
            	st2.setString(1, p);
            	rs = st2.executeQuery();
            	Map<String, Integer> files = new HashMap<String, Integer>();
        		
            	while(rs.next())
            	{
            		files.put(rs.getString(1), rs.getInt(2));
//            		System.out.println(rs.getString(1) + ", " + rs.getInt(2));
            	}
            	rs.close();
            	
            	for(String fn : files.keySet())
            	{
            		Integer nSchemas = files.get(fn);
            		for(int i = 1; i <= nSchemas; i++)
            		{
		        		String query3 = "SELECT filename, hash, schemacount, schema, commitdate FROM files "
		        				+ "WHERE project = ? "
		        				+ "AND filename = ? "
		        				+ "AND schemacount = ?"
		        				+ "ORDER BY commitdate";
		        		PreparedStatement st3 = conn.prepareStatement(query3);
		        		st3.setString(1, p);
		        		st3.setString(2, fn);
		        		st3.setInt(3, i);
		        		rs = st3.executeQuery();
		        		
		        		int counter = 0;
		        		String baseSchema = "",
		        				testSchema = "",
		        				baseHash = "";
		        		
		        		while(rs.next())
		        		{
		        			System.out.println(p);
		        			System.out.println(counter + " " + fn);
		        			if(counter > 0)
		        			{
		        				testSchema = rs.getString(4);
		        				if(testSchema.equals(""))
		        				{
		        					String queryProcessed = "UPDATE files "
			    	        				+ "SET processed = TRUE "
			    	        				+ "WHERE "
			    	        				+ "project = ? "
			    	        				+ "AND filename = ? "
			    	        				+ "AND hash = ? "
			    	        				+ "AND schemacount = ?";
				        			PreparedStatement st6 = conn.prepareStatement(queryProcessed);
				        			st6.setString(1, p);
				        			st6.setString(2, fn);
				        			st6.setString(3, rs.getString(2));
				        			st6.setInt(4, i);
				        			st6.executeUpdate();
				        			st6.close();
				        			continue;
		        				}
		        				else
		        				{	
			    	        		if(baseSchema.isEmpty() && !testSchema.isEmpty())
			    	        			comp.init(testSchema, 2);
			    	        		else if(!baseSchema.isEmpty() && testSchema.isEmpty())
			    	        			comp.init(baseSchema, 1);
			    	        		else if(baseSchema.isEmpty() && testSchema.isEmpty())
			    	        			comp.init(baseSchema, 3);
			    	        		else
			    	        			comp.init(baseSchema, testSchema);

			    	        		String query4 = "INSERT INTO comparison VALUES "
			        						+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			        				PreparedStatement st4 = conn.prepareStatement(query4);
			        				st4.setInt(1, counter); // Comparison ID
			        				st4.setTimestamp(2, rs.getTimestamp(5));
			    	        		st4.setString(3, p); // Project
			    	        		st4.setString(4, baseHash); // Base hash
			    	        		st4.setString(5, rs.getString(2)); // Test hash
			    	        		st4.setString(6, fn); // Filename
			    	        		st4.setInt(7, i); // Schema count
			    	        		st4.setInt(8, comp.add_attributes); // Added attributes
			    	        		st4.setInt(9, comp.add_elements); // Added elements
			    	        		st4.setInt(10, comp.add_imports); // Added imports
			    	        		st4.setInt(11, comp.add_complextypes); // Added complexTypes
			    	        		st4.setInt(12, comp.rem_attributes); // Removed attributes
			    	        		st4.setInt(13, comp.rem_elements); // Removed elements
			    	        		st4.setInt(14, comp.rem_imports); // Removed imports
			    	        		st4.setInt(15, comp.rem_complextypes); // Removed complexTypes
			    	        		st4.setInt(16, comp.rel_attributes); // Relocated attributes
			    	        		st4.setInt(17, comp.rel_elements); // Relocated elements
			    	        		st4.setInt(18, comp.ref_attributes); // Refactored attributes
			    	        		st4.setInt(19, comp.ref_elements); // Refactored elements
			    	        		st4.setInt(20, comp.ref_imports); // Refactored elements
			    	        		st4.setInt(21, comp.getBeforeElements());// Number of elements before
			    	        		st4.setInt(22, comp.getBeforeAttributes());// Number of attributes before
			    	        		st4.setInt(23, comp.getBeforeImports());// Number of imports before
			    	        		st4.setInt(24, comp.getBeforeComplexTypes());// Number of complexTypes before
			    	        		st4.setInt(25, comp.getAfterElements());// Number of elements after
			    	        		st4.setInt(26, comp.getAfterAttributes());// Number of attributes after
			    	        		st4.setInt(27, comp.getAfterImports());// Number of imports after
			    	        		st4.setInt(28, comp.getAfterComplexTypes());// Number of complexTypes after
			    	        		st4.executeUpdate();
			    	        		comp.resetCounters();
			    	        		st4.close();
		        				}
		    	        	}
		        			
		        			String queryProcessed = "UPDATE files "
	    	        				+ "SET processed = TRUE "
	    	        				+ "WHERE "
	    	        				+ "project = ? "
	    	        				+ "AND filename = ? "
	    	        				+ "AND hash = ? "
	    	        				+ "AND schemacount = ?";
		        			PreparedStatement st5 = conn.prepareStatement(queryProcessed);
		        			baseHash = rs.getString(2);
		        			baseSchema = rs.getString(4);
		        			st5.setString(1, p);
		        			st5.setString(2, fn);
		        			st5.setString(3, baseHash);
		        			st5.setInt(4, i);
		        			st5.executeUpdate();
		        			st5.close();
		        			counter++;
		        		}
            		}
            	}
            	String queryProcessed = "UPDATE projects SET processed = true WHERE projectname = ?;";
            	PreparedStatement psProcessed = conn.prepareStatement(queryProcessed);
            	psProcessed.setString(1, p);
            	psProcessed.executeUpdate();
            	psProcessed.close();
        	}
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static byte[] fileToByteArray(File file) {
	     int len = (int)file.length();  
	     byte[] sendBuf = new byte[len];
	     FileInputStream inFile  = null;
	     try {
	    	 inFile = new FileInputStream(file);         
	    	 inFile.read(sendBuf, 0, len);  
	     }
	     catch (FileNotFoundException fnfex) {
	     }
	     catch (IOException ioex) {
	     }
	     return sendBuf;
	}
}
