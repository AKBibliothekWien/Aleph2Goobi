package ak.goobi.aleph.aleph2goobi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import betullam.xmlhelper.XmlParser;

// TODO: Check for exact duplicate entries in meta.xml and meta_anchor.xml
// TODO: Add some success or error messages for usability reasons


public class Main {

	public static void main(String[] args) {

		// args[0] should be {metaFile}
		// args[1] should be {prefs}

		String metaFilename = args[0];
		String metaFileDir = new File(metaFilename).getParent();
		String rulesetFilename = args[1];

		// Get rules from ruleset.xml
		Ruleset ruleset = new Ruleset(metaFileDir, rulesetFilename);
		List<Rule> rules = ruleset.getRules();

		// Get info about Aleph server, database and the IDs:
		String server = (ruleset.getServer() != null && !ruleset.getServer().isEmpty()) ? ruleset.getServer() : null;
		String database = (ruleset.getDatabase() != null && !ruleset.getDatabase().isEmpty()) ? ruleset.getDatabase() : null;
		String idRegex = (ruleset.getIdRegex() != null && !ruleset.getIdRegex().isEmpty()) ? ruleset.getIdRegex() : null;

		// Search for the document with the given id:
		XSearcher xSearcher = new XSearcher(server, database);
		Map<String, String> catalogIds = getCatalogIds(metaFilename, idRegex);
		
		Map<String, List<MabField>> alephRecords = new HashMap<String, List<MabField>>();
		String topstructId = null;
		String firstchildId = null;
		for (Entry<String, String> catalogId : catalogIds.entrySet()) {
			if (catalogId.getKey().equals("topstructId")) {
				topstructId = catalogId.getValue();
				alephRecords.put("topstruct", xSearcher.getAlephRecord(catalogId.getValue()));
			} else if (catalogId.getKey().equals("firstchildId")) {
				firstchildId = catalogId.getValue();
				alephRecords.put("firstchild", xSearcher.getAlephRecord(catalogId.getValue()));
			}
		}

		for (Entry<String, List<MabField>> alephRecord : alephRecords.entrySet()) {
			
			String structType = alephRecord.getKey();
			String catalogId = null;
			if (structType.equals("topstruct")) {
				catalogId = topstructId;
			} else if (structType.equals("firstchild")) {
				catalogId = firstchildId;
			}
			
			if (alephRecord.getValue() != null && !alephRecord.getValue().isEmpty()) {

				List<GoobiField> goobiFieldsToAdd = new ArrayList<GoobiField>();
				
				// Apply all rules from ruleset.xml and get the desired info from the Aleph record
				for (Rule rule : rules) {
					//System.out.println(rule.toString());
					List<GoobiField> goobiFields = new FieldMatcher(alephRecord, rule).getGoobiFieldsToAdd();
					goobiFieldsToAdd.addAll(goobiFields); // Return a list of objects that represent xml tags in the meta.xml file of Goobi
				}
				
				Set<GoobiField> deduplicatedGoobiFieldsToAdd = deduplicateGoobiFieldsToAdd(goobiFieldsToAdd);
				/*for (GoobiField goobiField : deduplicatedGoobiFieldsToAdd) {
					System.out.println(goobiField.toString());
				}*/
				
				// Write the info we received from the aleph record to meta.xml
				new WriteToFile(deduplicatedGoobiFieldsToAdd, metaFileDir);

				System.out.println("Daten von " + catalogId + " aus Aleph X-Server erfolgreich importiert. Data from " + catalogId + " successfully imported from Aleph X-Server.");

			} else {
				System.err.println("Datensatz mit der ID " + catalogId + " in Aleph nicht vorhanden. Record with ID " + catalogId + " does not exist in Aleph.");
			}
		}
		

	}

	private static Set<GoobiField> deduplicateGoobiFieldsToAdd(List<GoobiField> goobiFieldsToAdd) {
		Set<GoobiField> goobiFieldsToAddSet = new HashSet<GoobiField>();
		for (GoobiField goobiFieldToAdd : goobiFieldsToAdd) {
			goobiFieldsToAddSet.add(goobiFieldToAdd);
		}

		return goobiFieldsToAddSet;
	}


	private static Map<String, String> getCatalogIds(String metaFilename, String idRegex/*, String scope*/) {
		Map<String, String> catalogIds = new HashMap<String, String>();
		File metaFile = new File(metaFilename);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			Document document = db.parse(metaFile);
			XmlParser xmlParser = new XmlParser();

			// Check if there is an ID for a topstruct element (meta_anchor.xml):
			String topstructId = xmlParser.getTextValue(document, "//metadata[@name='CatalogIDDigital' and @anchorId='true']");
			
			// If there is no ID for a topstruct element (meta_anchor.xml), use the current one as ID for the topstruct element:
			if (topstructId == null) {
				topstructId = xmlParser.getTextValue(document, "//metadata[@name='CatalogIDDigital' and not(contains(@anchorId, 'true'))]");
				catalogIds.put("topstructId", topstructId);
			} else { // Use the ID for meta_anchor.xml and find also the ID for the firstchild:
				String firstchildId = xmlParser.getTextValue(document, "//metadata[@name='CatalogIDDigital' and not(contains(@anchorId, 'true'))]");
				catalogIds.put("topstructId", topstructId);
				catalogIds.put("firstchildId", firstchildId);
			}

			if (idRegex != null) {
				for (Entry<String, String> catalogIdEntry : catalogIds.entrySet()) {
					String catalogIdKey = catalogIdEntry.getKey();
					if (catalogIdKey.equals("topstructId")) {
						String catalogIdTopstruct = catalogIdEntry.getValue();
						Pattern pattern = Pattern.compile(idRegex);
						Matcher matcher = pattern.matcher(catalogIdTopstruct);
						if (matcher.find()) {
							catalogIds.put("topstructId", matcher.group());
						}
					} else if (catalogIdKey.equals("firstchildId")) {
						String catalogIdFirstchild = catalogIdEntry.getValue();
						Pattern pattern = Pattern.compile(idRegex);
						Matcher matcher = pattern.matcher(catalogIdFirstchild);
						if (matcher.find()) {
							catalogIds.put("firstchildId", matcher.group());
						}
					}
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

		return catalogIds;
	}
	
	
}