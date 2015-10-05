package betullam.goobi.aleph.aleph2goobi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import betullam.xmlhelper.XmlParser;

public class Main {
		
	public static void main(String[] args) {
		
		String vorgangId = args[0];

		String catalogId = getCatalogId(vorgangId);

		List<GoobiField> goobiFieldsToAdd = new ArrayList<GoobiField>();

		// Get rules from ruleset.xml
		Ruleset ruleset = new Ruleset("../metadata/"+vorgangId);
		List<Rule> rules = ruleset.getRules();

		// Get info about Aleph server and database:
		String server = (ruleset.getServer() != null && !ruleset.getServer().isEmpty()) ? ruleset.getServer() : null;
		String database = (ruleset.getDatabase() != null && !ruleset.getDatabase().isEmpty()) ? ruleset.getDatabase() : null;

		// Search for the document with the given id:
		XSearcher xSearcher = new XSearcher(server, database);
		List<MabField> alephRecord = xSearcher.getAlephRecord(catalogId);		

		// Apply all rules from ruleset.xml and get the desired info from the Aleph record
		for (Rule rule : rules) {
			//System.out.println(rule.toString());			
			List<GoobiField> goobiFields = new FieldMatcher(alephRecord, rule).getGoobiFieldsToAdd();
			goobiFieldsToAdd.addAll(goobiFields); // Return a list of objects that represent xml tags in the meta.xml file of Goobi
		}

		// Write the info we received from the aleph record to meta.xml
		new WriteToFile(goobiFieldsToAdd, "../metadata/"+vorgangId);

	}

	
	private static String getCatalogId(String vorgangId) {
		String catalogId = null;

		String fileName = "../metadata/"+vorgangId+"/meta.xml";
		File rulesetFile = new File(fileName);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			Document document = db.parse(rulesetFile);

			XmlParser xmlParser = new XmlParser();
			catalogId = xmlParser.getTextValue(document, "//metadata[@name='CatalogIDDigital' and not(contains(@anchorId, 'true'))]");

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return catalogId;
	}




}
