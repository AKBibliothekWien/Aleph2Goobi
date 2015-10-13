package betullam.goobi.aleph.aleph2goobi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import betullam.xmlhelper.XmlParser;

// TODO: Deduplicate classification (900 fields). Maybe also use own field type or, even better, a concatenate-feature?
// TODO: Check for exact duplicate entries in meta.xml and meta_anchor.xml
// TODO: Add some success or error messages for usability reasons


public class Main {

	public static void main(String[] args) {

		// args[0] should be {metaFile}
		// args[1] should be {prefs}

		String metaFilename = args[0];
		String metaFileDir = new File(metaFilename).getParent();
		String rulesetFilename = args[1];

		List<GoobiField> goobiFieldsToAdd = new ArrayList<GoobiField>();

		// Get rules from ruleset.xml
		Ruleset ruleset = new Ruleset(metaFileDir, rulesetFilename);
		List<Rule> rules = ruleset.getRules();

		// Get info about Aleph server, database and the IDs:
		String server = (ruleset.getServer() != null && !ruleset.getServer().isEmpty()) ? ruleset.getServer() : null;
		String database = (ruleset.getDatabase() != null && !ruleset.getDatabase().isEmpty()) ? ruleset.getDatabase() : null;
		String idRegex = (ruleset.getIdRegex() != null && !ruleset.getIdRegex().isEmpty()) ? ruleset.getIdRegex() : null;

		// Search for the document with the given id:
		XSearcher xSearcher = new XSearcher(server, database);
		
		String catalogId = getCatalogId(metaFilename, idRegex);
		List<MabField> alephRecord = xSearcher.getAlephRecord(catalogId);

		if (alephRecord != null) {

			// Apply all rules from ruleset.xml and get the desired info from the Aleph record
			for (Rule rule : rules) {
				//System.out.println(rule.toString());			
				List<GoobiField> goobiFields = new FieldMatcher(alephRecord, rule).getGoobiFieldsToAdd();
				goobiFieldsToAdd.addAll(goobiFields); // Return a list of objects that represent xml tags in the meta.xml file of Goobi
			}

			// Write the info we received from the aleph record to meta.xml
			new WriteToFile(goobiFieldsToAdd, metaFileDir);

			System.out.println("Daten von " + catalogId + " aus Aleph X-Server erfolgreich importiert. Data from " + catalogId + " successfully imported from Aleph X-Server.");

		} else {
			System.err.println("Datensatz mit der ID " + catalogId + " in Aleph nicht vorhanden. Record with ID " + catalogId + " does not exist in Aleph.");
		}

	}


	private static String getCatalogId(String metaFilename, String idRegex) {
		String catalogId = null;

		File metaFile = new File(metaFilename);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			Document document = db.parse(metaFile);

			XmlParser xmlParser = new XmlParser();
			catalogId = xmlParser.getTextValue(document, "//metadata[@name='CatalogIDDigital' and not(contains(@anchorId, 'true'))]");
			
			if (idRegex != null) {
				Pattern pattern = Pattern.compile(idRegex);
				Matcher matcher = pattern.matcher(catalogId);
				if (matcher.find()) {
					catalogId = matcher.group();
				}
			}

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
