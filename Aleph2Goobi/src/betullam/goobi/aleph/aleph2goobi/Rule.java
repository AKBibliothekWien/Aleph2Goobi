package betullam.goobi.aleph.aleph2goobi;

import java.util.ArrayList;
import java.util.List;

public class Rule {
	private String type = null;
	String scope = null;
	String docstrcttype = null;
	private List<String> mabFields = new ArrayList<String>();
	private String goobiField = null;
	private String condSubfield = null;
	private String condContains = null;
	private String condContainsNot = null;
	private boolean condMissing = false;
	String regex = null;
	
	
	public Rule(String type, String scope, String docstrcttype, List<String> mabFields, String goobiField, String condSubfield, String condContains, String condContainsNot, boolean condMissing, String regex) {
		this.type = type;
		this.scope = scope;
		this.docstrcttype = docstrcttype;
		this.mabFields = mabFields;
		this.goobiField = goobiField;
		this.condSubfield = condSubfield;
		this.condContains = condContains;
		this.condContainsNot = condContainsNot;
		this.condMissing = condMissing;
		this.regex = regex;
	}
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getDocstrcttype() {
		return docstrcttype;
	}
	public void setDocstrcttype(String docstrcttype) {
		this.docstrcttype = docstrcttype;
	}
	public List<String> getMabFields() {
		return mabFields;
	}
	public void setMabFields(List<String> mabFields) {
		this.mabFields = mabFields;
	}
	public String getGoobiField() {
		return goobiField;
	}
	public void setGoobiField(String goobiField) {
		this.goobiField = goobiField;
	}
	public String getCondSubfield() {
		return condSubfield;
	}
	public void setCondSubfield(String condSubfield) {
		this.condSubfield = condSubfield;
	}
	public String getCondContains() {
		return condContains;
	}
	public void setCondContains(String condContains) {
		this.condContains = condContains;
	}
	public String getCondContainsNot() {
		return condContainsNot;
	}
	public void setCondContainsNot(String condContainsNot) {
		this.condContainsNot = condContainsNot;
	}
	public boolean isCondMissing() {
		return condMissing;
	}
	public void setCondMissing(boolean condMissing) {
		this.condMissing = condMissing;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}

	@Override
	public String toString() {
		return "Rule [type=" + type + ", scope=" + scope + ", docstrcttype=" + docstrcttype + ", mabFields=" + mabFields
				+ ", goobiField=" + goobiField + ", condSubfield=" + condSubfield + ", condContains=" + condContains
				+ ", condContainsNot=" + condContainsNot + ", condMissing=" + condMissing + ", regex=" + regex + "]";
	}

}
