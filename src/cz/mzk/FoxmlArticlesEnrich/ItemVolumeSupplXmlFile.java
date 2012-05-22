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
public class ItemVolumeSupplXmlFile extends MyXmlFile {

	private String partNumber;
	private String dateIssued;

	public ItemVolumeSupplXmlFile(String uuid, DigitalObjectModel model,
			List<String> children) {
		super(uuid, model, children);
	}

	public ItemVolumeSupplXmlFile(String uuid, DigitalObjectModel model,
			List<String> children, String partNumber, String dateIssued) {
		super(uuid, model, children);
		this.partNumber = partNumber;
		this.dateIssued = dateIssued;
	}

	/**
	 * @return the partNumber
	 */
	public String getPartNumber() {
		return partNumber;
	}

	/**
	 * @param partNumber
	 *            the partNumber to set
	 */
	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	/**
	 * @return the dateIssued
	 */
	public String getDateIssued() {
		return dateIssued;
	}

	/**
	 * @param dateIssued
	 *            the dateIssued to set
	 */
	public void setDateIssued(String dateIssued) {
		this.dateIssued = dateIssued;
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
				+ ((dateIssued == null) ? 0 : dateIssued.hashCode());
		result = prime * result
				+ ((partNumber == null) ? 0 : partNumber.hashCode());
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
		ItemVolumeSupplXmlFile other = (ItemVolumeSupplXmlFile) obj;
		if (dateIssued == null) {
			if (other.dateIssued != null)
				return false;
		} else if (!dateIssued.equals(other.dateIssued))
			return false;
		if (partNumber == null) {
			if (other.partNumber != null)
				return false;
		} else if (!partNumber.equals(other.partNumber))
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
		return "PerItemVolumeXmlFile [dateIssued=" + dateIssued
				+ ", partNumber=" + partNumber + "]";
	}

}
