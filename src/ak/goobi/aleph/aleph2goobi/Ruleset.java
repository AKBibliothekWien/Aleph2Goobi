package ak.goobi.aleph.aleph2goobi;

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

import ak.xmlhelper.XmlParser;

public class Ruleset {

	private XmlParser xmlParser = new XmlParser();
	private Document rulesetDoc = null;
	private String rulesetFilePath = null;
	private String metaFilePath = null;


	protected Ruleset(String metaFilePath, String rulesetFilePath) {
		this.metaFilePath = metaFilePath;
		this.rulesetFilePath = rulesetFilePath;
		this.setRulesetDoc();
	}

	private void setRulesetDoc() {
		File rulesetFile = new File(this.rulesetFilePath);
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

	public String getIdRegex() {
		String idRegex = null;
		try {
			idRegex = xmlParser.getAttributeValue(this.rulesetDoc, "/Preferences/Aleph", "idregex");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return idRegex;
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
							String targetDocstrctCommaSeparated = (mapElement.hasAttribute("docstrcttype")) ? mapElement.getAttribute("docstrcttype") : null;
							List<String> targetDocstrcts = (targetDocstrctCommaSeparated != null) ? Arrays.asList(targetDocstrctCommaSeparated.split("\\s*,\\s*")): null;

							// Check for targetDocstrcts. Stop if they are missing.
							// It would be too dangerous to write metadata to ALL DocStruct-Elements.
							if (targetDocstrcts == null) {
								System.err.println("Der Parameter \"docstrcttype\" fehlt im \"Map\" Element (siehe Bereich \"Aleph\" in der ruleset Datei). The parameter \"docstrcttype\" is missing in the \"Map\" element (see section \"Aleph\" in ruleset file).");
								System.exit(1);
							}

							String mabFields = null;
							List<String> lstMabFields = null;
							String goobiMdField = null;
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
											goobiMdField = (mapChildElement != null) ? mapChildElement.getAttribute("field") : null;
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
											//System.out.println("regex: " + regex);
										}										
									}
								}
							}


							for (String targetDocstrct : targetDocstrcts) {
								boolean goobiFieldAllowedInParent = goobiMdAllowedInDocstruct(this.rulesetDoc, goobiMdField, targetDocstrct);
								boolean goobiFieldExists = goobiFieldExists(this.rulesetDoc, goobiMdField);
								//System.out.println(goobiMdField + " exists: " + goobiFieldExists);
								//System.out.println(goobiMdField + " allowed in " + targetDocstrct + ": " + goobiFieldAllowedInParent);

								// Add rule to the list of rules, but only if the metadata field is officially defined in MetadataType section of ruleset.xml
								// and if it is an allowed metadata of the parent DocStrctType:
								if (goobiFieldExists && goobiFieldAllowedInParent) {
									List<String> dmdLogIds = getDmdLogIds(scope, targetDocstrct);
									boolean docStructExists = (dmdLogIds == null || dmdLogIds.isEmpty()) ? false : true;

									if (docStructExists) {
										rules.add(new Rule(type, scope, targetDocstrct, dmdLogIds, lstMabFields, goobiMdField, condSubfield, condContains, condContainsNot, condMissing, regex));
									}
								}
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



	private List<String> getDmdLogIds(String scope, String docstrcttype) {
		List<String> dmdLogIds = new ArrayList<String>();

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

		// Fallback, e. g. if "topstruct" is used for a Monograph
		// Attention: Could cause problems if we have a Journal but are missing the meta_anchor.xml file!
		if (!metaFile.exists()) {
			fileName = this.metaFilePath + "/meta.xml";
			metaFile = new File(fileName);
		}
		
		if (metaFile.exists()) {
			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.parse(fileName);

				// Get all DMDLOG_IDs for all structure types that the user wants
				dmdLogIds = xmlParser.getAttributeValues(document, "/mets/structMap[@TYPE='LOGICAL']//div[@TYPE='"+docstrcttype+"']", "DMDID");

			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}	
		}

		return dmdLogIds;
	}



	private boolean goobiMdAllowedInDocstruct(Document document, String goobiField, String parentGoobiField) {		

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
						String docStrctName = nameElement.getTextContent().trim();
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
								if (childElement.getTextContent().trim().equals(goobiField)) {
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
