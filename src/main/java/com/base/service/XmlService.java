package com.base.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.print.DocFlavor.STRING;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.base.exception.FileNotSupportedException;
import com.base.exception.TooLargeFileException;

@Service
public class XmlService {

	@Autowired
	private JdbcTemplate jdbc;

	public List<String> columnNames;

	public String processXmlFIle(MultipartFile file) throws Exception {

		columnNames = new ArrayList<>();
		InputStream stream = file.getInputStream();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(stream);

		document.getDocumentElement().normalize();

		// root element
		String tableName = document.getDocumentElement().getNodeName();
		// System.out.println("Parent Node --> " + tableName);

		List<String> tables = getTables(); // all available tables

		NodeList childNodes = document.getDocumentElement().getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// System.out.println(node.getNodeName() + " --> Prt"); // parent element
				NodeList childNodes2 = node.getChildNodes();

				for (int j = 0; j < childNodes2.getLength(); j++) {

					Node node2 = childNodes2.item(j);
					if (node2.getNodeType() == Node.ELEMENT_NODE) {
						String column = node2.getNodeName(); // child elements
						columnNames.add(column);
					}
				}
				break;

			}

		}
		System.out.println(columnNames);

		createTable(tableName, columnNames);
		System.out.println("Table created Successfully....");

		String message = insertData(tableName, document, columnNames);
		// System.out.println("data inserted Successfully..");

		return message;
	}

	public void createTable(String tableName, List<String> columnNames) throws SQLException {
		// CREATE TABlE TABLE_NAME (id INT PRIMRY KEY AUTO_INCREMENT,title
		// VARCHAR(255),author VARcHAR(255), price VARCHAR(255));
		String dropQuery = "DROP TABLE IF EXISTS " + tableName;

		StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
		query.append(tableName).append(" (SERIAL_ID INT PRIMARY KEY AUTO_INCREMENT,");
		for (String columnName : columnNames) {
			query.append(columnName).append(" VARCHAR(255),");
		}
		query.deleteCharAt(query.length() - 1); // extra coma delte
		query.append(");");

		System.out.println(query.toString());
		jdbc.execute(dropQuery);
		jdbc.update(query.toString());
	}

	public String insertData(String tableName, Document document, List<String> columnNames) {
		// INSERT INTO songs (TITLE,ARtIST,ALBUM,YEAR) ValUES(
		StringBuilder query = new StringBuilder("INSERT INTO ");
		query.append(tableName).append(" (");

		for (String columnName : columnNames) {
			query.append(columnName).append(",");
		}

		query.deleteCharAt(query.length() - 1); // extra comma delete
		query.append(")").append(" VALUES(");

		NodeList childNodes = document.getDocumentElement().getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// System.out.println(node.getNodeName() + " --> child node");

				NodeList childNodes2 = node.getChildNodes();
				for (int j = 0; j < childNodes2.getLength(); j++) {
					Node node2 = childNodes2.item(j);

					if (node2.getNodeType() == Node.ELEMENT_NODE) {
						// System.out.println(node2.getTextContent()+" -- child elements value");
						query.append("'").append(node2.getTextContent()).append("',");

					}

				}
				query.deleteCharAt(query.length() - 1);
				query.append("),(");

			}

		}
		query.delete(query.length() - 2, query.length());
		query.append(";");

		jdbc.execute(query.toString());
		// System.out.println(query.toString()); // to insert data query

		String msg = "";
		if (!tableName.isEmpty() & !columnNames.isEmpty()) {
			msg = "Data Inserted Successfully...!";
		}
		return msg;
	}

	public void validateFIle(MultipartFile file) {

		if (!file.getOriginalFilename().toLowerCase().endsWith(".xml")) {
			throw new FileNotSupportedException("Unsupported file : plese selct xml file.");
		}

		if (file.getSize() > 1024 * 100 * 1024) { // file size should not more than 100 mb
			throw new TooLargeFileException("Larger FIle : file size should not be more than 100mb.");
		}
	}

	public List<String> getTables() throws SQLException {
		Connection connection = jdbc.getDataSource().getConnection();
		Statement stmt = connection.createStatement();

		String query = "show tables;";

		ResultSet rs = stmt.executeQuery(query);

		// System.out.println(rs.getFetchSize());

		List<String> tableNames = new ArrayList<>();

		while (rs.next()) {
			tableNames.add(rs.getString(1));
		}

		System.out.println(tableNames);

		return tableNames;
	}

	public void createXmlFIle(String TableName) throws Exception {

		List<String[]> records = new ArrayList<>();

		Connection connection = jdbc.getDataSource().getConnection();
		Statement stmt = connection.createStatement();

		String query = "SELECT * FROM " + TableName + " LIMIT 100;";

		ResultSet rs = stmt.executeQuery(query);

		int columnCount = rs.getMetaData().getColumnCount();

		while (rs.next()) {
			String[] rowData = new String[columnCount];

			for (int i = 1; i <= columnCount; i++) {
				rowData[i - 1] = rs.getString(i);
			}
			records.add(rowData);
		}

		// create xml file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();

		Element rootELement = document.createElement(TableName); // root eleement
		document.appendChild(rootELement);
		System.out.println(rootELement);

		// create child elements and add to root element

		StringBuilder sb = new StringBuilder(TableName);
		sb.deleteCharAt(sb.length() - 1);

		List<String> columns = new ArrayList<>();
		for (int i = 1; i <= columnCount; i++) {
			// System.out.println(rs.getMetaData().getColumnName(i)+" **");
			columns.add(rs.getMetaData().getColumnName(i));
		}

		for (String[] record : records) {
			Element ParentElement = document.createElement(sb.toString());
			rootELement.appendChild(ParentElement);

			for (int i = 0; i < columnCount; i++) {
				String columnName = columns.get(i);
				String columnValue = record[i];

				Element columnElement = document.createElement(columnName);
				columnElement.appendChild(document.createTextNode(columnValue));
				ParentElement.appendChild(columnElement);

			}

		}

		FileOutputStream fos = new FileOutputStream("xmlFiles/" + TableName.toUpperCase() + ".xml");

		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(fos));

		fos.close();

	}
}
