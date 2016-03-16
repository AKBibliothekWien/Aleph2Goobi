package betullam.goobi.aleph.aleph2goobi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoobiField {

	private String name;
	private String type;
	private String scope;
	private List<String> dmdLogIds;
	Map<String, String> values = new HashMap<String, String>();
	
	
	public GoobiField(String name, String type, String scope, List<String> dmdLogIds, Map<String, String> values) {
		this.name = name;
		this.type = type;
		this.scope = scope;
		this.dmdLogIds = dmdLogIds;
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
	public List<String> getDmdLogIds() {
		return dmdLogIds;
	}
	public void setDmdLogIds(List<String> dmdLogIds) {
		this.dmdLogIds = dmdLogIds;
	}
	public Map<String, String> getValues() {
		return values;
	}
	public void setValues(Map<String, String> values) {
		this.values = values;
	}


	@Override
	public String toString() {
		return "GoobiField [name=" + name + ", type=" + type + ", scope=" + scope + ", dmdLogIds=" + dmdLogIds + ", values=" + values + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dmdLogIds == null) ? 0 : dmdLogIds.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof GoobiField)) {
			return false;
		}
		
		GoobiField other = (GoobiField) obj;
		
		if (dmdLogIds == null) {
			if (other.dmdLogIds != null) {
				return false;
			}
		} else if (!dmdLogIds.equals(other.dmdLogIds)) {
			return false;
		}
		
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		
		if (scope == null) {
			if (other.scope != null) {
				return false;
			}
		} else if (!scope.equals(other.scope)) {
			return false;
		}
		
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		
		return true;
	}
	
	
	
	


}
