package loadsettingfromxml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ParserXPATH {
	
	private String nameFile;
	private String ruleCase;
	private String parametr;
   
	public String getNameFile() {
		return this.nameFile;
	}

	public  void setNameFile(String nameFile) {
		   this.nameFile = nameFile ;
	}

	public String getRuleCase()
	{
		return this.ruleCase;
	}

	public void setRuleCase(String ruleCase)
	{
		 this.ruleCase = ruleCase;
	}
	
	public String getParametr()
	{
		return this.parametr;
	}

	private  void setParametr(String parametr) {
		this.parametr = parametr;
	}
	
	public ParserXPATH(String rule,String name) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {

		   setRuleCase(rule);
		   setNameFile(name);
		  //System.out.println(getNameFile());
		   Document documentxml = null;

		try {
			//
			DocumentBuilderFactory document = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = document.newDocumentBuilder();
			documentxml =  documentBuilder.parse(new File(getNameFile()));

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			ex.printStackTrace(System.out);
		}
		System.out.println(documentxml.toString());
   	     	// Создать XPathFactory
		   XPathFactory pathFactory = XPathFactory.newInstance();
		   // Создать XPath
		    XPath xpath = pathFactory.newXPath();
		   // Получить скомпилированный вариант XPath-выражения
		   //XPathExpression expr = xpath.compile(getRuleCase());
		   // Применить XPath-выражение к документу для поиска нужных элементов
		   //NodeList nodes = (NodeList)
		   System.out.println(xpath.evaluate(getRuleCase(),documentxml));
       	   /**String parameter="";*/
		/**for(int i=0;i<nodes.getLength();i++) {
			setParametr(nodes.item(i).getNodeValue());
			System.out.println(nodes.item(i).getNodeValue());
		}*/
	}

	
	}
