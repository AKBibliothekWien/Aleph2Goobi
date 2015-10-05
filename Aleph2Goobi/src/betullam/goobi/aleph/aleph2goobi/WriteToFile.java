package betullam.goobi.aleph.aleph2goobi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WriteToFile {

	List<GoobiField> goobiFieldsToWrite = new ArrayList<GoobiField>();
	String metaFilePath;

	public WriteToFile(List<GoobiField> goobiFieldsToWrite, String metaFilePath) {
		this.goobiFieldsToWrite = goobiFieldsToWrite;
		this.metaFilePath = metaFilePath;
		this.writeToMeta();
	}

	
	private void writeToMeta() {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		Document document = null;
		Element goobi = null;

		try {

			// First, backup all meta.xml and meta_anchor.xml files:
			backupMetaFiles(this.metaFilePath);

			documentBuilder = documentBuilderFactory.newDocumentBuilder();

			// Get goobi:goobi from meta.xml
			String metaFilename = this.metaFilePath + "/meta.xml";
			Document documentMeta = null;
			Element goobiMeta = null;
			if (fileExists(metaFilename)) {
				documentMeta = documentBuilder.parse(metaFilename);
				goobiMeta = getElementForInsertion(documentMeta);
			}

			// Get goobi:goobi from meta_anchor.xml
			String metaAnchorFilename = this.metaFilePath + "/meta_anchor.xml";
			Document documentMetaAnchor = null;
			Element goobiMetaAnchor = null;
			if (fileExists(metaAnchorFilename)) {
				documentMetaAnchor = documentBuilder.parse(metaAnchorFilename);
				goobiMetaAnchor = getElementForInsertion(documentMetaAnchor);
			}


			for (GoobiField goobiField : this.goobiFieldsToWrite) {

				String fieldType = goobiField.getType();
				String scope = goobiField.getScope();
				Map<String, String> values = goobiField.getValues();
				document = null;
				goobi = null;
				String fileName = null;

				if (scope != null) {
					if (scope.equals("topstruct")) {
						document = documentMetaAnchor;
						goobi = goobiMetaAnchor;
						fileName = metaAnchorFilename;
						//System.out.println("Write to: anchor - scope not null");
					} else if (scope.equals("firstchild")) {
						document = documentMeta;
						goobi = goobiMeta;
						fileName = metaFilename;
						//System.out.println("Write to: meta   - scope not null");
					}
				} else {
					document = documentMeta;
					goobi = goobiMeta;
					fileName = metaFilename;
					//System.out.println("Write to: meta   - scope is null");
				}

				if (document != null && goobi != null) {

					Element newGoobiElement = null;

					if (fieldType.equals("person")) {
						newGoobiElement = document.createElement("goobi:metadata");
						
						newGoobiElement.setAttribute("name", goobiField.getName());
						newGoobiElement.setAttribute("type", goobiField.getType());

						for (String key : values.keySet()) {
							Element subElement = document.createElement("goobi:" + key);
							subElement.setTextContent(values.get(key));
							newGoobiElement.appendChild(subElement);
						}

					} else if (fieldType.equals("default")) {
						newGoobiElement = document.createElement("goobi:metadata");
						newGoobiElement.setAttribute("name", goobiField.getName());
						newGoobiElement.setTextContent(goobiField.getValues().get("content"));
					}
					

					if (newGoobiElement != null) {
						goobi.appendChild(newGoobiElement);
					}
					
					
					DOMSource source = new DOMSource(document);

					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					StreamResult result = new StreamResult(fileName);
					transformer.transform(source, result);
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}


	}

	

	private boolean fileExists(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}


	private Element getElementForInsertion(Document document) {		

		Element root = document.getDocumentElement();
		Element goobi = null;
		NodeList dmdSecs = root.getElementsByTagName("mets:dmdSec");
		if (dmdSecs.getLength() > 0) {
			for (int i = 0; i < dmdSecs.getLength(); i++) {
				if (dmdSecs.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
					Element dmdSec = (Element)dmdSecs.item(i);

					// Find out if we are in the right section. We need do append our values to the "goobi:goobi" section
					// that resides in the "mets:dmdSec" section with the attribute 'ID="DMDLOG_1234"'.
					if (dmdSec.getAttribute("ID").contains("DMDLOG")) {

						// Get the "goobi:goobi" node
						NodeList goobiNodes = dmdSec.getElementsByTagName("goobi:goobi");
						if (goobiNodes.getLength() > 0) {
							for (int j = 0; j < goobiNodes.getLength(); j++) {
								if (goobiNodes.item(j).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
									goobi = (Element)goobiNodes.item(j);
								}
							}
						}
					}
				}
			}
		}

		return goobi;
	}



	private void backupMetaFiles(String metaFilePath) throws IOException {

		File backupDirectory = new File(metaFilePath);

		// Copy existing backup file to a new temporary file (add + 1 to number-suffix), so that nothing gets overwritten
		for (File existingBackupFile : FileUtils.listFiles(backupDirectory, new RegexFileFilter("meta.*\\.xml(\\.\\d+)") , TrueFileFilter.INSTANCE)) {
			String fileName = existingBackupFile.getName();

			// Get number-suffix from existing backup-files and add 1
			Pattern p = Pattern.compile("\\d+");
			Matcher m = p.matcher(fileName);
			boolean b = m.find();
			int oldBackupNo = 0;
			int newBackupNo = 0;
			if (b) {
				oldBackupNo = Integer.parseInt(m.group(0));
				newBackupNo = oldBackupNo + 1;					
			}

			// Create temporary files:
			String newTempBackupFilename = "";
			File newTempBackupFile = null;
			if (fileName.matches("meta\\.xml.*")) {
				newTempBackupFilename = "meta.xml." + newBackupNo + ".temp";
				newTempBackupFile = new File(backupDirectory+File.separator+newTempBackupFilename);
			}
			if (fileName.matches("meta_anchor\\.xml.*")) {
				newTempBackupFilename = "meta_anchor.xml." + newBackupNo + ".temp";
				newTempBackupFile = new File(backupDirectory+File.separator+newTempBackupFilename);
			}

			// Copy existing file to temporary backup-file with new filename:
			FileUtils.copyFile(existingBackupFile, newTempBackupFile);

			// Delete the existing old backup file:
			existingBackupFile.delete();
		}

		// Remove the ".temp" suffix from the newly created temporary backup-files
		for (File tempBackupFile : FileUtils.listFiles(backupDirectory, new RegexFileFilter(".*\\.temp") , TrueFileFilter.INSTANCE)) {
			String newBackupFilename = tempBackupFile.getName().replace(".temp", "");
			File newBackupFile = new File(backupDirectory+File.separator+newBackupFilename);

			// Copy temporary file to real backup-file with new filename:
			FileUtils.copyFile(tempBackupFile, newBackupFile);

			// Delete temporary backup file:
			tempBackupFile.delete();
		}

		// Copy meta.xml and/or meta_anchor.xml and append the suffix ".1" to it, so that it becomes the newest backup file
		for (File productiveFile : FileUtils.listFiles(backupDirectory, new WildcardFileFilter(new String[]{"meta.xml", "meta_anchor.xml"}, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE)) {
			// Copy current productive file and append ".1" so that it gets the newes backup-file: 
			File newBackupFile = new File(productiveFile.getAbsoluteFile()+".1");
			FileUtils.copyFile(productiveFile, newBackupFile);
		}
		
		// Remove all files with a suffix bigger than 9, because Goobi keeps only max. 9 backup files:
		for (File backupFileHigher9 : FileUtils.listFiles(backupDirectory, new RegexFileFilter("meta.*\\.xml(\\.\\d{2,})") , TrueFileFilter.INSTANCE)) {
			backupFileHigher9.delete();
		}

	}



}
