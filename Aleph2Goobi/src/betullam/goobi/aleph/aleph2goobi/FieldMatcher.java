package betullam.goobi.aleph.aleph2goobi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class FieldMatcher {

	private List<String> xmlToAdd = new ArrayList<String>();
	private List<GoobiField> goobiFieldsToAdd = new ArrayList<GoobiField>();
	private List<MabField> alephRecord = null;
	private Rule rule = null;

	public FieldMatcher(List<MabField> alephRecord, Rule rule) {
		this.alephRecord = alephRecord;
		this.rule = rule;
		this.match();
	}

	public List<String> getXmlToAdd() {
		return this.xmlToAdd;
	}
	
	public List<GoobiField> getGoobiFieldsToAdd() {
		return this.goobiFieldsToAdd;
	}

	private void match() {

		// Rule values:
		String ruleGoobiFieldName = (rule.getGoobiField() != null && !rule.getGoobiField().isEmpty()) ? rule.getGoobiField() : null;
		String ruleType = (rule.getType() != null && !rule.getType().isEmpty()) ? rule.getType() : null;
		String ruleScope = (rule.getScope() != null && !rule.getScope().isEmpty()) ? rule.getScope() : null;
		List<String> ruleMabFields = (rule.getMabFields() != null && !rule.getMabFields().isEmpty()) ? rule.getMabFields() : null;
		String ruleCondField = (rule.getCondSubfield() != null && !rule.getCondSubfield().isEmpty()) ? rule.getCondSubfield() : null;
		boolean hasRuleCondField = (ruleCondField != null) ? true : false;
		String ruleCondContainsValue = (rule.getCondContains() != null && !rule.getCondContains().isEmpty()) ? rule.getCondContains() : null;
		String ruleCondContainsNotValue = (rule.getCondContainsNot() != null && !rule.getCondContainsNot().isEmpty()) ? rule.getCondContainsNot() : null;
		boolean ruleCondMissing = (rule.isCondMissing() == true) ? true : false;
		String ruleRegex = (rule.getRegex() != null && !rule.getRegex().isEmpty()) ? rule.getRegex() : null;
		

		for (String mfRule : ruleMabFields) {

			if (ruleType.equals("person")) {

				for (MabField mfAleph : this.alephRecord) {

					if (matchAny(mfRule, mfAleph.getFieldNo())) {

						// Aleph record values
						String name = null;
						String firstName = null;
						String lastName = null;
						String gndId = null;
						boolean matchesCondition = false;
						String alephCondValue = (mfAleph.getSubfields().get(ruleCondField) != null && !mfAleph.getSubfields().get(ruleCondField).isEmpty()) ? mfAleph.getSubfields().get(ruleCondField) : "missing";

						if (hasRuleCondField) { // A condition is defined in ruleset: go on and find out which one 
							if (!alephCondValue.equals("missing")) {
								if (ruleCondContainsValue != null) {
									matchesCondition = (alephCondValue.contains(ruleCondContainsValue)) ? true : false;
								} else if (ruleCondContainsNotValue != null) {
									List<String> ruleValues = Arrays.asList(ruleCondContainsNotValue.split("\\s*,\\s*"));
									matchesCondition = (containsAny(alephCondValue, ruleValues)) ? false : true;
								}
							} else if (alephCondValue.equals("missing")) {
								if (ruleCondMissing) {
									matchesCondition = true;
								}
							}
						} else { // No condition is defined, so we always add the value
							matchesCondition = true;
						}


						if (matchesCondition) {

							boolean isGnd = (mfAleph.getSubfields().get("p") != null) ? true : false;

							if (isGnd) {
								name = (mfAleph.getSubfields().get("p") != null && !mfAleph.getSubfields().get("p").isEmpty())? mfAleph.getSubfields().get("p") : null;
								gndId = (mfAleph.getSubfields().get("9") != null && !mfAleph.getSubfields().get("9").isEmpty()) ? mfAleph.getSubfields().get("9").replaceFirst("\\(.*?\\)", "") : null;
							} else {
								name = mfAleph.getSubfields().get("a");
							}

							if (name.contains(",")) {
								int separatorIndex = name.indexOf(",");
								firstName = (separatorIndex != -1) ? name.substring(separatorIndex + 1).trim() : null;
								lastName = (separatorIndex != -1) ? name.substring(0, separatorIndex).trim() : null;
							} else {
								lastName = name;
							}
							
							Map<String, String> valueMap = new HashMap<String, String>();
							valueMap.put("lastName", lastName);
							valueMap.put("firstName", firstName);
							valueMap.put("authorityID", "gnd");
							valueMap.put("authorityURI", "http://d-nb.info/gnd/");
							valueMap.put("authorityValue", gndId);
							valueMap.put("displayName", name);

							GoobiField goobiField = new GoobiField(ruleGoobiFieldName, "person", ruleScope, valueMap);
							goobiFieldsToAdd.add(goobiField);
						}
					}
				}

			} else if (rule.getType().equals("default")) {

				for (MabField mfAleph : this.alephRecord) {

					boolean matchesCondition = false;
					String alephCondValue = (mfAleph.getSubfields().get(ruleCondField) != null && !mfAleph.getSubfields().get(ruleCondField).isEmpty()) ? mfAleph.getSubfields().get(ruleCondField) : "missing";

					if (hasRuleCondField) { // A condition is defined in ruleset: go on and find out which one 

						if (!alephCondValue.equals("missing")) {
							if (ruleCondContainsValue != null) {
								matchesCondition = (alephCondValue.contains(ruleCondContainsValue)) ? true : false;
							} else if (ruleCondContainsNotValue != null) {
								List<String> ruleValues = Arrays.asList(ruleCondContainsNotValue.split("\\s*,\\s*"));
								matchesCondition = (containsAny(alephCondValue, ruleValues)) ? false : true;
							}
						} else if (alephCondValue.equals("missing")) {
							if (ruleCondMissing) {
								matchesCondition = true;
							}
						}
					} else { // No condition is defined, so we always add the value
						matchesCondition = true;
					}

					if (matchesCondition) {
						int subfieldIndex = 0;
						for (Entry<String, String> subfield : mfAleph.getSubfields().entrySet()) {
							String alephFieldName = mfAleph.getFieldNo() + "$" + mfAleph.getI1() + mfAleph.getI2() + "$" + subfield.getKey();
							subfieldIndex = subfieldIndex + 1;
							String subfieldValue = null;

							if (mfRule.substring(3).equals("$**$*")) { // Match against all indicators and subfields. E. g. 100$**$* matches 100$a1$b, 100$b3$z, 100$z*$-, etc.
								if (matchAny(mfRule, mfAleph.getFieldNo())) {	
									//System.out.println(rule.getGoobiField() + ": " + subfield.getValue());
									subfieldValue = subfield.getValue();
								}
							} else if (Pattern.matches("\\$[\\w-]{1}\\*\\$\\*", mfRule.substring(3, 8)) == true) { // 100$a*$*
								if (matchInd1(mfRule, alephFieldName) == true) {
									//System.out.println(rule.getGoobiField() + ": " + subfield.getValue());
									subfieldValue = subfield.getValue();
								}
							} else if (Pattern.matches("\\$\\*[\\w-]{1}\\$\\*", mfRule.substring(3, 8)) == true) { // 100$*a$*
								if (matchInd2(mfRule, alephFieldName) == true) {
									//System.out.println(rule.getGoobiField() + ": " + subfield.getValue());
									subfieldValue = subfield.getValue();
								}
							} else if (Pattern.matches("\\$[\\w-]{2}\\$\\*", mfRule.substring(3, 8)) == true) { // 100$aa$*
								if (matchInd1AndInd2(mfRule, alephFieldName) == true) {
									//System.out.println(rule.getGoobiField() + ": " + subfield.getValue());
									subfieldValue = subfield.getValue();
								}
							} else if (Pattern.matches("\\$[\\w-]{1}\\*\\$\\w{1}", mfRule.substring(3, 8)) == true) { // 100$a*$a
								if (matchInd1AndSubfield(mfRule, alephFieldName) == true) {
									//System.out.println(rule.getGoobiField() + ": " + subfield.getValue());
									subfieldValue = subfield.getValue();
								}
							} else if (Pattern.matches("\\$\\*[\\w-]{1}\\$\\w{1}", mfRule.substring(3, 8)) == true) { // 100$*a$a
								if (matchInd2AndSubfield(mfRule, alephFieldName) == true) {
									//System.out.println(rule.getGoobiField() + ": " + subfield.getValue());
									subfieldValue = subfield.getValue();
								}
							} else if (mfRule.substring(3, 6).equals("$**")) { // 100$**$a
								if (matchSubfield(mfRule, alephFieldName) == true) {
									//System.out.println(rule.getGoobiField() + " (" + subfield.getKey() + "): " + subfield.getValue() + " (" + subfieldIndex + ")");
									subfieldValue = subfield.getValue();
								}
							} else {
								if (mfRule.equals(alephFieldName)) { // Match against the value as it is. E. g. 100$a1$z matches only against 100$a1$z
									//System.out.println(rule.getGoobiField() + ": " + subfield.getValue());
									subfieldValue = subfield.getValue();
								}
							}

							if (subfieldValue != null) {
								Map<String, String> valueMap = new HashMap<String, String>();
								valueMap.put("content", getRegexedValue(subfieldValue, ruleRegex));
								GoobiField goobiField = new GoobiField(ruleGoobiFieldName, "default", ruleScope, valueMap);
								goobiFieldsToAdd.add(goobiField);
							}

						}
					}
				}


			}
		}
	}

	
	private String getRegexedValue(String value, String regex) {
		if (regex != null) {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(value);
			if (matcher.find()) {
				value = matcher.group();
			}
		}
		return value;
	}

	

	private boolean containsAny(String value, List<String> list) {
		boolean containsAny = false;
		for (String string : list) {
			containsAny = (value.contains(string)) ? true : false;
			if (containsAny) {
				return true;
			}
		}
		return containsAny;
	}


	private boolean matchAny(String rule, String alephField) {
		String match = rule.substring(0, 3); // Would be "100" from "100$**$*"
		boolean matches = Pattern.matches(match, alephField);
		return matches;
	}

	public boolean matchInd1(String rule, String alephField) {
		String fieldNo = rule.substring(0, 3);
		String indicator1 = rule.substring(4, 5);
		boolean matches = Pattern.matches(fieldNo + "\\$" + indicator1 + ".\\$.", alephField); // Fieldnumber and indicator1 must match (100$a*$*)
		return matches;
	}

	public boolean matchInd2(String rule, String alephField) {
		String fieldNo = rule.substring(0, 3);
		String indicator2 = rule.substring(5, 6);
		boolean matches = Pattern.matches(fieldNo + "\\$." + indicator2 + "\\$.", alephField); // Fieldnumber and indicator2 must match (100$*a$*)
		return matches;
	}

	public boolean matchInd1AndInd2(String rule, String alephField) {
		String fieldNo = rule.substring(0, 3);
		String indicator1 = rule.substring(4, 5);
		String indicator2 = rule.substring(5, 6);
		boolean matches = Pattern.matches(fieldNo + "\\$" + indicator1 + indicator2 + "\\$.", alephField); // Fieldnumber, indicator1 and indicator2 must match (100$aa$*)
		return matches;
	}

	public boolean matchInd1AndSubfield(String rule, String alephField) {
		String fieldNo = rule.substring(0, 3);
		String indicator1 = rule.substring(4, 5);
		String subfield = rule.substring(7, 8);
		boolean matches = Pattern.matches(fieldNo + "\\$" + indicator1 + ".\\$" + subfield, alephField); // Fieldnumber, indicator1 and subfield must match (100$a*$a)
		return matches;
	}

	public boolean matchInd2AndSubfield(String rule, String alephField) {
		String fieldNo = rule.substring(0, 3);
		String indicator2 = rule.substring(5, 6);
		String subfield = rule.substring(7, 8);
		boolean matches = Pattern.matches(fieldNo + "\\$." + indicator2 + "\\$" + subfield, alephField); // Fieldnumber, indicator1 and subfield must match (100$*a$a)
		return matches;
	}

	public boolean matchSubfield(String rule, String alephField) {
		String fieldNo = rule.substring(0, 3);
		String subfield = rule.substring(7, 8);
		boolean matches = Pattern.matches(fieldNo + "\\$..\\$" + subfield, alephField); // Fieldnumber and subfield must match (100$**$a)
		return matches;
	}



}
