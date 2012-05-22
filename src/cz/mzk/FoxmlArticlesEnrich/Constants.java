/**
 * 
 */
package cz.mzk.FoxmlArticlesEnrich;

/**
 * @author Matous Jobanek
 * 
 */
public class Constants {

	public static final String SUFFIX = "xml";

	public static final String DC_MODEL_PREFIX = "model:";

	public static final String INFO_FEDORA_PREFIX = "info:fedora/";

	public static final int PROGRESS_BAR_LENGTH = 50;

	public static final String XPATH_MODS = "//*[name()=\'mods:mods\']";

	public static final String XPATH_DC_TYPE = "//*[name()=\'dc:type\']";

	public static final String XPATH_MODS_UUID = "//*[name()=\'mods:mods\']/*[name()=\'mods:identifier\'][@type=\'uuid\']";

	public static final String XPATH_MODS_TITLE = "//*[name()=\'mods:mods\']/*[name()=\'mods:titleInfo\']/*[name()=\'mods:title\']";

	public static final String XPATH_MODS_PART_NUMBER = "//*[name()=\'mods:mods\']/*[name()=\'mods:titleInfo\']/*[name()=\'mods:partNumber\']";

	public static final String XPATH_MODS_DATE_ISSUES = "//*[name()=\'mods:mods\']/*[name()=\'mods:originInfo\']/*[name()=\'mods:dateIssued\']";

	public static final String XPATH_MODS_IDENTIFIER = "//*[name()=\'mods:mods\']/*[name()=\'mods:identifier\']";

	public static enum DigitalObjectModel {

		/** The PERIODICAL. */
		PERIODICAL("periodical"),

		/** The PERIODICALVOLUME. */
		PERIODICALVOLUME("periodicalvolume"),

		/** The PERIODICALITEM. */
		PERIODICALITEM("periodicalitem"),

		/** The PAGE. */
		PAGE("page"),

		/** The INTERNALPART. */
		INTERNALPART("internalpart"),

		/** The SUPPLEMENT. */
		SUPPLEMENT("supplement"),

		/** The ARTICLE. */
		ARTICLE("article");

		/*
		 * Instantiates a new kramerius model.
		 * 
		 * @param value the value
		 */

		private DigitalObjectModel(String value) {
			this.value = value;
		}

		/** The value. */
		private final String value;

		/**
		 * Gets the value.
		 * 
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * To string.
		 * 
		 * @param km
		 *            the km
		 * @return the string
		 */
		public static String toString(DigitalObjectModel km) {
			return km.getValue();
		}

		/**
		 * Parses the string.
		 * 
		 * @param type
		 *            the s
		 * @return the model
		 */
		public static DigitalObjectModel parseString(String type, String path) {
			DigitalObjectModel[] values = DigitalObjectModel.values();
			for (DigitalObjectModel model : values) {
				if (model.getValue().equalsIgnoreCase(type))
					return model;
			}
			throw new RuntimeException("Unsupported type: " + type
					+ " in the file " + path);
		}

		public static DigitalObjectModel getModel(int ordinal) {
			for (DigitalObjectModel model : values()) {
				if (ordinal == model.ordinal())
					return model;
			}
			return null;
		}
	}

	public enum FedoraRelationship {

		/** The has volume. */
		hasVolume(
				"//*[name()=\'rdf:RDF\']/*/*[name()=\'kramerius:hasVolume\']/@resource"),

		/** The has item. */
		hasItem(
				"//*[name()=\'rdf:RDF\']/*/*[name()=\'kramerius:hasItem\']/@resource"),

		/** The has internal part. */
		hasInternalPart(
				"//*[name()=\'rdf:RDF\']/*/*[name()=\'kramerius:hasInternalPart\']/@resource"),

		/** The has int comp part. */
		hasIntCompPart(
				"//*[name()=\'rdf:RDF\']/*/*[name()=\'kramerius:hasIntCompPart\']/@resource"),

		/** The is on page. */
		isOnPage(
				"//*[name()=\'rdf:RDF\']/*/*[name()=\'kramerius:isOnPage\']/@resource");

		private final String xPathNamespaceAwareQuery;

		private FedoraRelationship(String xPathNamespaceAwareQuery) {
			this.xPathNamespaceAwareQuery = xPathNamespaceAwareQuery;
		}

		public String getXPathNamespaceAwareQuery() {
			return xPathNamespaceAwareQuery;
		}
	}
}
