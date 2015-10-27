package betullam.goobi.aleph.aleph2goobi;

import java.util.HashMap;
import java.util.Map;

public class GoobiField {

	private String name;
	private String type;
	private String scope;
	Map<String, String> values = new HashMap<String, String>();
	
	
	public GoobiField(String name, String type, String scope, Map<String, String> values) {
		this.name = name;
		this.type = type;
		this.scope = scope;
		this.values = values;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public Map<String, String> getValues() {
		return values;
	}
	public void setValues(Map<String, String> values) {
		this.values = values;
	}


	@Override
	public String toString() {
		return "GoobiField [name=" + name + ", type=" + type + ", scope=" + scope + ", values=" + values + "]";
	}


}
