package com.base.controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.base.service.XmlService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainCOntroller {

	private final XmlService serv;

	@Autowired
	public MainCOntroller(XmlService serv) {
		this.serv = serv;
	}

	boolean status1 = false;
	

	// simple form to upload files and check tables
	@GetMapping("/")
	public String loadForm(Model mode, HttpSession sess) throws SQLException {
		List<String> tables = serv.getTables();
		mode.addAttribute("tables", tables);

		String message = (String) sess.getAttribute("mess");
		Resource xmlResource = (Resource) sess.getAttribute("xmlResource");

		if (status1 == true) {
			mode.addAttribute("msg", message);
			status1 = false;
		}

		return "form";
	}

	// upload file, validate & store it in db
	@PostMapping("/upload")
	public String uploadFIle(MultipartFile file, HttpSession sess) throws Exception {

		serv.validateFIle(file);
		String mess = serv.processXmlFIle(file);

		sess.setAttribute("mess", mess);
		if (true) {
			status1 = true;
		}

		return "redirect:/";
	}

	// create xml file in application
	@PostMapping("/generateXml")
	public String generateXMlFIleForTable(String selectedTable, Model mod) throws Exception {

		if (selectedTable != null) {
			serv.createXmlFIle(selectedTable);
			mod.addAttribute("tableName", selectedTable);

			return "xml_page";
		} else {
			throw new NullPointerException("Table not Selected or Not available.");
		}

	}

	// download xml file
	@GetMapping("/download")
	public ResponseEntity<Resource> downloadXmlFile(String tableName) throws IOException {

		String fileName = tableName.toUpperCase() + ".xml";

		String filePath = "xmlFiles/" + fileName;

		Resource resource = new FileSystemResource(filePath);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

		return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength())
				.contentType(MediaType.APPLICATION_XML).body(resource);
	}
}
