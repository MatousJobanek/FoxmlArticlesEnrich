/**
 * 
 */
package cz.mzk.FoxmlArticlesEnrich;

import java.util.List;
import java.util.Map;

import cz.mzk.FoxmlArticlesEnrich.Constants.DigitalObjectModel;

/**
 * @author Matous Jobanek
 * 
 */
public class PeriodicumXmlFile extends MyXmlFile {

	private String title;
	private Map<String, String> identifiers;

	public PeriodicumXmlFile(String uuid, DigitalObjectModel model,
			List<String> children) {
		super(uuid, model, children);
	}

	public PeriodicumXmlFile(String uuid, DigitalObjectModel model,
			List<String> children, String title, Map<String, String> identifiers) {
		super(uuid, model, children);
		this.title = title;
		this.identifiers = identifiers;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the identifiers
	 */
	public Map<String, String> getIdentifiers() {
		return identifiers;
	}

	/**
	 * @param identifiers
	 *            the identifiers to set
	 */
	public void setIdentifiers(Map<String, String> identifiers) {
		this.identifiers = identifiers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((identifiers == null) ? 0 : identifiers.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PeriodicumXmlFile other = (PeriodicumXmlFile) obj;
		if (identifiers == null) {
			if (other.identifiers != null)
				return false;
		} else if (!identifiers.equals(other.identifiers))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PeriodicumXmlFile [identifiers=" + identifiers + ", title="
				+ title + "]";
	}

}
