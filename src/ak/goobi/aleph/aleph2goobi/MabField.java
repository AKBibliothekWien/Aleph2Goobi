package ak.goobi.aleph.aleph2goobi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MabField {
	
	private String fieldNo;
	private String i1;
	private String i2;
	private LinkedHashMap<String, String> subfields = new LinkedHashMap<String, String>();
	private List<String> fieldNames = new ArrayList<String>();
	
	
	public MabField(String fieldNo, String i1, String i2, LinkedHashMap<String, String> subfields, List<String> fieldNames) {
		this.fieldNo = fieldNo;
		this.i1 = i1;
		this.i2 = i2;
		this.subfields = subfields;
		this.fieldNames = fieldNames;
	}
	
	
	public String getFieldNo() {
		return fieldNo;
	}

	public void setFieldNo(String fieldNo) {
		this.fieldNo = fieldNo;
	}

	public String getI1() {
		return i1;
	}

	public void setI1(String i1) {
		this.i1 = i1;
	}

	public String getI2() {
		return i2;
	}

	public void setI2(String i2) {
		this.i2 = i2;
	}

	public Map<String, String> getSubfields() {
		return subfields;
	}

	public void setSubfields(LinkedHashMap<String, String> subfields) {
		this.subfields = subfields;
	}
	

	public List<String> getFieldNames() {
		return fieldNames;
	}
	
	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}


	@Override
	public String toString() {
		return "MabField [fieldNo=" + fieldNo + ", i1=" + i1 + ", i2=" + i2 + ", subfields=" + subfields + "]";
	}

	

}
