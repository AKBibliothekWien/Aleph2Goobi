package betullam.goobi.aleph.aleph2goobi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import betullam.xmlhelper.XmlParser;

public class Ruleset {

	private XmlParser xmlParser = new XmlParser();
	private Document rulesetDoc = null;
	private String metaFilePath = null;

	protected Ruleset(String metaFilePath) {
		this.metaFilePath = metaFilePath;
		this.setRulesetDoc();
	}

	private void setRulesetDoc() {
		String fileName = "../rulesets/ruleset.xml";
		File rulesetFile = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			this.rulesetDoc = db.parse(rulesetFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String getServer() {
		String server = null;
		try {
			server = xmlParser.getAttributeValue(this.rulesetDoc, "/Preferences/Aleph", "server");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return server;
	}

	public String getDatabase() {
		String database = null;
		try {
			database = xmlParser.getAttributeValue(this.rulesetDoc, "/Preferences/Aleph", "database");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return database;
	}


	public List<Rule> getRules() {
		List<Rule> rules = new ArrayList<Rule>();

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression;

		try {
			xPathExpression = xPath.compile("/Preferences/Aleph");
			NodeList alephNodes = (NodeList)xPathExpression.evaluate(this.rulesetDoc, XPathConstants.NODESET);


			if (alephNodes.getLength() > 0) {
				// Iterate over node-list, get attribute-values and add them to a list:
				for (int i = 0; i < alephNodes.getLength(); i++) {
					Element alephElement = (Element)alephNodes.item(i);

					// Get "Map" Nodes:
					NodeList mapNodes = alephElement.getElementsByTagName("Map");

					// Check if there ara Map nodes. If yes, go on.
					if (mapNodes.getLength() > 0) {

						// Iterate over "Map" nodes:
						for (int j = 0; j < mapNodes.getLength(); j++) {
							//System.out.println("----------------------------");

							Element mapElement = (Element)mapNodes.item(j);

							String type = (mapElement.hasAttribute("type")) ? mapElement.getAttribute("type") : "default";
							String scope = (mapElement.hasAttribute("scope")) ? mapElement.getAttribute("scope") : null;

							String mabFields = null;
							List<String> lstMabFields = null;
							String goobiField = null;
							String condSubfield = null;
							String condContains = null;
							String condContainsNot = null;
							boolean condMissing = false;
							String regex = null;

							NodeList mapChilds = mapElement.getChildNodes();
							if (mapChilds.getLength() > 0) {
								for (int k = 0; k < mapChilds.getLength(); k++) {
									if (mapChilds.item(k).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {

										Element mapChildElement = (Element)mapChilds.item(k);
										String nodeName = mapChildElement.getNodeName();

										if (nodeName.equals("Mab")) {
											mabFields = (mapChildElement != null) ? mapChildElement.getAttribute("field") : null;
											lstMabFields = Arrays.asList(mabFields.split("\\s*,\\s*"));
											//System.out.println("mabFields: " + mabFields);
										}

										if (nodeName.equals("Goobi")) {
											goobiField = (mapChildElement != null) ? mapChildElement.getAttribute("field") : null;
											//System.out.println("goobiField: " + goobiField);
										}

										if (nodeName.equals("Condition")) {

											condSubfield = (mapChildElement.hasAttribute("subfield")) ? mapChildElement.getAttribute("subfield") : null;
											condContains = (mapChildElement.hasAttribute("contains")) ? mapChildElement.getAttribute("contains") : null;
											condContainsNot = (mapChildElement.hasAttribute("containsNot")) ? mapChildElement.getAttribute("containsNot") : null;
											condMissing = (mapChildElement.hasAttribute("missing") && mapChildElement.getAttribute("missing").equals("missing")) ? true : false;
											//System.out.println("condSubfield: " + condSubfield + ", condContains: " + condContains + ", condContainsNot: " + condContainsNot + ", condMissing: " + condMissing);
										}

										if (nodeName.equals("Regex")) {
											regex = (mapChildElement != null) ? mapChildElement.getAttribute("match") : null;
											//System.out.println("goobiField: " + goobiField);
										}										
									}
								}
							}


							// Add rule to the list of rules, but only if the metadata field is officially defined in MetadataType section of ruleset.xml
							// and if it is an allowed metadata of the parent DocStrctType:							
							if (goobiFieldExists(this.rulesetDoc, goobiField) && goobiFieldAllowedInParent(this.rulesetDoc, goobiField, getParentDocStruct(scope))) {
								rules.add(new Rule(type, scope, lstMabFields, goobiField, condSubfield, condContains, condContainsNot, condMissing, regex));
							}
						}
					}
				}
			}

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return rules;
	}


	
	private String getParentDocStruct(String scope) {

		String parentDocStruct = null;
		String fileName = null;

		if (scope != null) {
			if (scope.equals("topstruct")) {
				fileName = this.metaFilePath + "/meta_anchor.xml";
			} else if (scope.equals("firstchild")) {
				fileName = this.metaFilePath + "/meta.xml";
			}
		} else {
			fileName = this.metaFilePath + "/meta.xml";
		}

		File metaFile = new File(fileName);

		if (metaFile.exists()) {
			try {
				
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.parse(fileName);

				String dmdSecId = xmlParser.getAttributeValue(document, "/mets/dmdSec", "ID");

				if (dmdSecId != null) {
					parentDocStruct = xmlParser.getAttributeValue(document, "/mets/structMap[@TYPE='LOGICAL']//div[@DMDID='"+dmdSecId+"']", "TYPE");
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}

		return parentDocStruct;
	}

	
	private boolean goobiFieldAllowedInParent(Document document, String goobiField, String parentGoobiField) {

		boolean goobiFieldAllowedInParent = false;

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression;
		Node docStrctTypeNode = null;

		try {
			xPathExpression = xPath.compile("/Preferences/DocStrctType/Name");
			NodeList nameNodes = (NodeList)xPathExpression.evaluate(this.rulesetDoc, XPathConstants.NODESET);
			if (nameNodes.getLength() > 0) {
				for (int i = 0; i < nameNodes.getLength(); i++) {
					if (nameNodes.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
						// Name Element
						Element nameElement = (Element)nameNodes.item(i);
						String docStrctName = nameElement.getTextContent();
						if (docStrctName.equals(parentGoobiField)) {
							docStrctTypeNode = nameElement.getParentNode();
						}
					}
				}
			}

			if (docStrctTypeNode != null) {
				NodeList childNodes = docStrctTypeNode.getChildNodes();
				if (childNodes.getLength() > 0) {
					for (int i = 0; i < childNodes.getLength(); i++) {
						if (childNodes.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
							Element childElement = (Element)childNodes.item(i);
							if (childElement.getTagName().equals("metadata")) {
								if (childElement.getTextContent().equals(goobiField)) {
									goobiFieldAllowedInParent = true;
									return goobiFieldAllowedInParent;
								}
							}
						}
					}
				}
			}
		}  catch (XPathExpressionException e) {
			e.printStackTrace();
		}


		return goobiFieldAllowedInParent;
	}



	private boolean goobiFieldExists(Document document, String goobiField) {
		boolean goobiFieldExists = false;
		try {
			List<String> nameValues = xmlParser.getTextValues(document, "/Preferences/MetadataType/Name");
			goobiFieldExists = (nameValues.contains(goobiField)) ? true : false;
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return goobiFieldExists;
	}


}
