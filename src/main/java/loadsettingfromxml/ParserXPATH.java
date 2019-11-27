package loadsettingfromxml;

import java.io.File;
import java.io.FileInputStream;
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

import org.w3c.dom.*;
import org.xml.sax.InputSource;
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
		/**DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			// use the factory to create a documentbuilder
			DocumentBuilder builder = factory.newDocumentBuilder();

			// create a new document from input source
			FileInputStream fis = new FileInputStream("basesetting.xml");
			InputSource is = new InputSource(fis);
			Document doc = builder.parse(is);

			// get the first element
			Element element = doc.getDocumentElement();

			// get all child nodes
			NodeList nodes = element.getChildNodes();

			// print the text content of each child
			for (int i = 0; i < nodes.getLength(); i++) {
				System.out.println("" + nodes.item(i).getTextContent());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
		try {
			DocumentBuilderFactory document = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = document.newDocumentBuilder();
			documentxml =  documentBuilder.parse(new File(getNameFile()));

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			ex.printStackTrace(System.out);
		}

		System.out.println(getRuleCase());
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath xpath = pathFactory.newXPath();

		// Пример записи XPath
		// Подный путь до элемента
		//XPathExpression expr = xpath.compile("BookCatalogue/Book/Cost");
		// Все элементы с таким именем
		//XPathExpression expr = xpath.compile("//Cost");
		// Элементы, вложенные в другой элемент
		XPathExpression expr = xpath.compile(getRuleCase());
		NodeList nodes = (NodeList) expr.evaluate(documentxml, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			System.out.println("Value:" + n.getTextContent());
		}
		System.out.println();
	}

	
	}
