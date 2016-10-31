package ak.goobi.aleph.aleph2goobi;

public class GoobiField_v1 {

	private String name;
	private String type;
	private String value;
	private String lastName;
	private String firstName;
	private String authorityID;
	private String authorityURI;
	private String authorityValue;
	private String displayName;
	
	
	public GoobiField_v1(String name, String type, String value, String lastName, String firstName, String authorityID, String authorityURI, String authorityValue, String displayName) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.lastName = lastName;
		this.firstName = firstName;
		this.authorityID = authorityID;
		this.authorityURI = authorityURI;
		this.authorityValue = authorityValue;
		this.displayName = displayName;
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
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getAuthorityID() {
		return authorityID;
	}
	public void setAuthorityID(String authorityID) {
		this.authorityID = authorityID;
	}
	public String getAuthorityURI() {
		return authorityURI;
	}
	public void setAuthorityURI(String authorityURI) {
		this.authorityURI = authorityURI;
	}
	public String getAuthorityValue() {
		return authorityValue;
	}
	public void setAuthorityValue(String authorityValue) {
		this.authorityValue = authorityValue;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	@Override
	public String toString() {
		return "GoobiField [name=" + name + ", type=" + type + ", value=" + value + ", lastName=" + lastName
				+ ", firstName=" + firstName + ", authorityID=" + authorityID + ", authorityURI=" + authorityURI
				+ ", authorityValue=" + authorityValue + ", displayName=" + displayName + "]";
	}


	
	

	
}
