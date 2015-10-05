package betullam.goobi.aleph.aleph2goobi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

public class XSearcher {

	private String server;
	private String database;

	public XSearcher(String server, String database) {
		this.server = server;
		this.database = database;
	}


	public String getSearchResult(String id) {

		XmlParser xmlParser = new XmlParser();
		String setNo = null;

		Map<String, String> requestParameter = new HashMap<String, String>();
		requestParameter.put("op", "find");
		requestParameter.put("code", "WID");
		requestParameter.put("request", id);
		requestParameter.put("base", this.database);


		Document docSearchResult = xServerRequest(this.server, requestParameter);

		try {
			setNo = xmlParser.getTextValue(docSearchResult, "/find/set_number");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return setNo;

	}



	public List<MabField> getAlephRecord(String id) {
		List<MabField> alephRecord = new ArrayList<MabField>();
		Document docAlephRecord = null;
		String setNo = getSearchResult(id);

		Map<String, String> requestParameter = new HashMap<String, String>();
		requestParameter.put("op", "present");
		requestParameter.put("set_entry", "000000001");
		requestParameter.put("set_number", setNo);

		if (setNo != null) {
			docAlephRecord = xServerRequest("https://aleph22-prod-sh2.obvsg.at", requestParameter);
			alephRecord = alephRecord2Map(docAlephRecord);
		}

		return alephRecord;	
	}



	public List<MabField> alephRecord2Map(Document alephRecordDoc) {
		List<MabField> alephRecord = new ArrayList<MabField>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression;

		try {
			// Varfields
			xPathExpression = xPath.compile("/present/record/metadata/oai_marc/varfield");
			NodeList varfieldNodes = (NodeList)xPathExpression.evaluate(alephRecordDoc, XPathConstants.NODESET);

			// Check if we have a node. If we have a search match in aleph xserver-serach, there should be a "/present/record/metadata/oai_marc" node:
			if (varfieldNodes.getLength() > 0) {
				for (int i = 0; i < varfieldNodes.getLength(); i++) {
					
					Element varfieldElement = (Element)varfieldNodes.item(i);

					// Get varfield attribute values:
					String varfieldId = (!varfieldElement.getAttribute("id").trim().isEmpty()) ? varfieldElement.getAttribute("id").trim() : null;
					String varfieldI1 = (!varfieldElement.getAttribute("i1").trim().isEmpty()) ? varfieldElement.getAttribute("i1").trim() : "*";
					String varfieldI2 = (!varfieldElement.getAttribute("i2").trim().isEmpty()) ? varfieldElement.getAttribute("i2").trim() : "*";

					// Get "subfield" Nodes:
					NodeList subfieldNodes = varfieldElement.getChildNodes();
					LinkedHashMap<String, String> subfields = new LinkedHashMap<String, String>();
					if (subfieldNodes.getLength() > 0) {
						
						int subfieldCounter = 0;
						String subfieldContent = null;
						String subfieldLabel = null;
						
						for (int j = 0; j < subfieldNodes.getLength(); j++) {
							Element subfieldElement = (subfieldNodes.item(j).getNodeType() == Node.ELEMENT_NODE) ? (Element)subfieldNodes.item(j) : null;
							
							if (subfieldElement != null) {
								subfieldCounter = subfieldCounter + 1;

								subfieldLabel = (subfieldElement.getAttribute("label") != null) ? subfieldElement.getAttribute("label") : "*";
								
								// If we have classification fields (902, 907, ...) and  an additional value is given besides the main classification value,
								// we need to concatenate the main and the addidional value. E. g. for the following structure, we need to build "Frankreich, Nord":
								//
								//	<varfield id="902" i1=" " i2=" ">
								//		<subfield label="g">Frankreich</subfield>
								//		<subfield label="z">Nord</subfield>
								//		<subfield label="9">(DE-588)4110086-4</subfield>
								//	</varfield>
								//
								// INFO: The main value is always on first position, the additional one always on second position.
								
								boolean isClassification = false;
								List<String> classificationFields = Arrays.asList("902", "907", "912", "917", "922", "927", "932", "937", "942", "947");
								if (classificationFields.contains(varfieldId)) {
									isClassification = true;									
								}
								
								if (isClassification) {
									if (subfieldCounter == 1 && !subfieldLabel.equals("9")) {
										subfieldContent = (!subfieldElement.getTextContent().isEmpty()) ? subfieldElement.getTextContent() : null;
									} else if (subfieldCounter == 2  && !subfieldLabel.equals("9")) {
										subfieldContent += (!subfieldElement.getTextContent().isEmpty()) ? ", " + subfieldElement.getTextContent() : null;
									} else {
										subfieldContent = (!subfieldElement.getTextContent().isEmpty()) ? subfieldElement.getTextContent() : null;
									}
									
								} else {
									subfieldContent = (!subfieldElement.getTextContent().isEmpty()) ? subfieldElement.getTextContent() : null;
								}
								
								subfields.put(subfieldLabel, subfieldContent);
								
							}
						}
					}
					
					// Set fieldnames
					List<String> fieldNames = new ArrayList<String>();
					for (Entry<String, String> subfield : subfields.entrySet()) {
						fieldNames.add(varfieldId + "$" + varfieldI1 + varfieldI2 + "$" + subfield.getKey());
					}
					
					MabField mabField = new MabField(varfieldId, varfieldI1, varfieldI2, subfields, fieldNames);
					alephRecord.add(mabField);
				}
			}

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return alephRecord;

	}



	public Document xServerRequest(String alephServer, Map<String, String> parameters) {
		Document xServerAnswer = null;
		URL uUrl = null;

		String strUrl = alephServer + "/X?";
		for (Entry<String, String> parameter : parameters.entrySet()) {
			strUrl += parameter.getKey() + "=" + parameter.getValue() + "&";
		}

		//System.out.println("URL: " + strUrl);

		try {
			uUrl = new URL(strUrl);
			URLConnection conn = uUrl.openConnection();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			xServerAnswer = db.parse(conn.getInputStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		return xServerAnswer;
	}

}
