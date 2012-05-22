/**
 * 
 */
package cz.mzk.FoxmlArticlesEnrich;

import java.util.List;

import cz.mzk.FoxmlArticlesEnrich.Constants.DigitalObjectModel;

/**
 * @author Matous Jobanek
 * 
 */
public class MyXmlFile {

	private String uuid;

	private Constants.DigitalObjectModel model;

	private List<String> children;

	/**
	 * @param uuid
	 * @param model
	 * @param children
	 */
	public MyXmlFile(String uuid, DigitalObjectModel model,
			List<String> children) {
		super();
		this.uuid = uuid;
		this.model = model;
		this.children = children;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the model
	 */
	public Constants.DigitalObjectModel getModel() {
		return model;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(Constants.DigitalObjectModel model) {
		this.model = model;
	}

	/**
	 * @return the children
	 */
	public List<String> getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<String> children) {
		this.children = children;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyXmlFile other = (MyXmlFile) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

}
