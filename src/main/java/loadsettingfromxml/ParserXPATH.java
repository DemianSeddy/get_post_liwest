package loadsettingfromxml;

import java.io.FileNotFoundException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
 
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

	public String getRuleCase() {
		return this.ruleCase;
	}

	public void setRuleCase(String ruleCase) {
		 this.ruleCase = ruleCase;
	}
	
	public String getParametr() {
		return this.parametr;
	}

	private  void setParametr(String parametr) {
		this.parametr = parametr;
	}
	
	public ParserXPATH(String rule,String name) throws XPathExpressionException, FileNotFoundException {
		setRuleCase(rule);
		setNameFile(name);
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xPath = xpf.newXPath();//активировали парсер
		XPathExpression expr =  xPath.compile(getRuleCase());
		NodeList nodes = (NodeList)(expr.evaluate(new InputSource(getNameFile()),XPathConstants.NODESET));
		String parameter="";
		for(int i=0;i<nodes.getLength();i++) {
			setParametr(nodes.item(i).getNodeValue());
			System.out.println(nodes.item(i).getNodeValue());
		}
	}

	
	}
