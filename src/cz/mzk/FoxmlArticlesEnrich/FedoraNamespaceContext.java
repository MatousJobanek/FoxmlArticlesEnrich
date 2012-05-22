/**
 * 
 */
package cz.mzk.FoxmlArticlesEnrich;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * @author job
 * 
 */
public class FedoraNamespaceContext implements NamespaceContext {

	public static final String FOXML_NAMESPACE_URI = "info:fedora/fedora-system:def/foxml#";

	public static final String FEDORA_NAMESPACE_URI = "http://www.fedora.info/definitions/1/0/types/";

	public static final String SCHEMA_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

	public static final String TEI_NAMESPACE_URI = "http://www.tei-c.org/ns/1.0";

	public static final String ADM_NAMESPACE_URI = "http://www.qbizm.cz/kramerius-fedora/image-adm-description";

	/** RDF namespace. */
	public static final String RDF_NAMESPACE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/** Our ontology relationship namespace. */
	public static final String ONTOLOGY_RELATIONSHIP_NAMESPACE_URI = "http://www.nsdl.org/ontologies/relationships#";

	/** Dublin core namespace. */
	public static final String DC_NAMESPACE_URI = "http://purl.org/dc/elements/1.1/";

	/** The Constant FEDORA_MODELS_URI. */
	public static final String FEDORA_MODELS_URI = "info:fedora/fedora-system:def/model#";

	/** The Constant KRAMERIUS_URI. */
	public static final String KRAMERIUS_URI = "http://www.nsdl.org/ontologies/relationships#";

	/** The Constant BIBILO_MODS_URI. */
	public static final String BIBILO_MODS_URI = "http://www.loc.gov/mods/v3";

	/** OAI namespace. */
	public static final String OAI_NAMESPACE_URI = "http://www.openarchives.org/OAI/2.0/";

	/** OAI Dublin core namespace. */
	public static final String OAI_DC_NAMESPACE_URI = "http://www.openarchives.org/OAI/2.0/oai_dc/";

	public static final String RELS_EXT_NAMESPACE_URI = "info:fedora/fedora-system:FedoraRELSExt-1.0";

	/** The Constant MAP_PREFIX2URI. */
	private static final Map<String, String> MAP_PREFIX2URI = new IdentityHashMap<String, String>();

	/** The Constant MAP_URI2PREFIX. */
	private static final Map<String, String> MAP_URI2PREFIX = new IdentityHashMap<String, String>();

	static {
		MAP_PREFIX2URI.put("mods", BIBILO_MODS_URI);
		MAP_PREFIX2URI.put("dc", DC_NAMESPACE_URI);
		MAP_PREFIX2URI.put("fedora-models", FEDORA_MODELS_URI);
		MAP_PREFIX2URI.put("kramerius", KRAMERIUS_URI);
		MAP_PREFIX2URI.put("rdf", RDF_NAMESPACE_URI);
		MAP_PREFIX2URI.put("oai", OAI_NAMESPACE_URI);
		MAP_PREFIX2URI.put("oai_dc", OAI_DC_NAMESPACE_URI);
		MAP_PREFIX2URI.put("foxml", FOXML_NAMESPACE_URI);

		for (Map.Entry<String, String> entry : MAP_PREFIX2URI.entrySet()) {
			MAP_URI2PREFIX.put(entry.getValue(), entry.getKey());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String )
	 */
	@Override
	public String getNamespaceURI(String arg0) {
		return MAP_PREFIX2URI.get(arg0.intern());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	@Override
	public String getPrefix(String arg0) {
		return MAP_URI2PREFIX.get(arg0.intern());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	 */
	@Override
	public Iterator<String> getPrefixes(String arg0) {
		String prefixInternal = MAP_URI2PREFIX.get(arg0.intern());
		if (prefixInternal != null) {
			return Arrays.asList(prefixInternal).iterator();
		} else {
			return Collections.<String> emptyList().iterator();
		}
	}
}