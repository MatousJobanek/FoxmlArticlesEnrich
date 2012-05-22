package cz.mzk.FoxmlArticlesEnrich;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.mzk.FoxmlArticlesEnrich.Constants.DigitalObjectModel;
import cz.mzk.FoxmlArticlesEnrich.Constants.FedoraRelationship;

public class Main {

	// "/home/job/Desktop/testConverted";
	private static final String DIR_PATH = "/home/job/Desktop/ANLConverting/converted";

	private static final String SUFFIX = "xml";

	private static final String DC_MODEL_PREFIX = "model:";

	public static final String INFO_FEDORA_PREFIX = "info:fedora/";

	private static Map<String, String> uuidIsOwnedByUuid;

	private static Map<String, String> articleFiles;

	private static List<String> allFilePaths;

	private static Map<String, MyXmlFile> xmlFiles;

	public static final String PATH_TO_NEW_ARTICLES_DIR = "/home/job/Desktop/newArticles";

	private static XPathFactory xpfactory;

	/**
	 * @param args
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 */
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {

		System.out.println("Processing...");

		uuidIsOwnedByUuid = new HashMap<String, String>();
		articleFiles = new HashMap<String, String>();
		allFilePaths = new ArrayList<String>();
		xmlFiles = new HashMap<String, MyXmlFile>();
		scanDirectoryStructure(DIR_PATH);

		System.out.println("Parsing...");
		parseFiles();

		System.out.println("Refilling...");
		int numberTodo = articleFiles.size();
		for (String uuidArticle : articleFiles.keySet()) {
			refillArticle(uuidArticle);
			System.out.println("To refill: " + --numberTodo);
		}

		System.out.println("Done!");

	}

	private static void scanDirectoryStructure(String dirPath)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {

		// System.out.println(dirPath);

		File path = new File(dirPath);
		FileFilter dirFilter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return !pathname.isFile();
			}

		};
		// System.out.println(dirPath);
		File[] dirs = path.listFiles(dirFilter);
		if (dirs != null) {
			for (File dir : dirs) {
				scanDirectoryStructure(dir.getPath());
			}
		}

		FileFilter fileFilter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile()
						&& pathname.getName().toLowerCase().endsWith(
								"." + SUFFIX.toLowerCase());
			}
		};

		File[] files = path.listFiles(fileFilter);
		if (files == null)
			return;
		for (File file : files) {
			allFilePaths.add(file.getPath());
		}
	}

	private static ItemVolumeSupplXmlFile processPerItem(String uuid) {

		MyXmlFile itemParentXmlFile = xmlFiles.get(uuidIsOwnedByUuid.get(uuid));

		if (itemParentXmlFile.getModel() == DigitalObjectModel.PERIODICALVOLUME) {
			return (ItemVolumeSupplXmlFile) itemParentXmlFile;
		} else {
			throw new RuntimeException("Wrong model");
		}
	}

	private static void refillArticle(String uuidArticle) {

		// System.out.println(uuidArticle);

		PeriodicumXmlFile periodical = null;
		ItemVolumeSupplXmlFile periodicalItem = null;
		ItemVolumeSupplXmlFile supplement = null;
		ItemVolumeSupplXmlFile periodicalVolume = null;

		MyXmlFile parentXmlFile = xmlFiles.get(uuidIsOwnedByUuid
				.get(uuidArticle));

		if (parentXmlFile.getModel() == DigitalObjectModel.SUPPLEMENT) {

			supplement = (ItemVolumeSupplXmlFile) parentXmlFile;
			MyXmlFile supplParentXmlFile = xmlFiles.get(uuidIsOwnedByUuid
					.get(supplement.getUuid()));

			if (supplParentXmlFile.getModel() == DigitalObjectModel.PERIODICALITEM) {
				periodicalVolume = processPerItem(parentXmlFile.getUuid());

			} else if (supplParentXmlFile.getModel() == DigitalObjectModel.PERIODICALVOLUME) {
				periodicalVolume = (ItemVolumeSupplXmlFile) supplParentXmlFile;

			} else {
				throw new RuntimeException("Wrong model");
			}

		} else if (parentXmlFile.getModel() == DigitalObjectModel.PERIODICALITEM) {

			periodicalItem = (ItemVolumeSupplXmlFile) parentXmlFile;
			periodicalVolume = processPerItem(parentXmlFile.getUuid());

		} else if (parentXmlFile.getModel() == DigitalObjectModel.PERIODICALVOLUME) {
			periodicalVolume = (ItemVolumeSupplXmlFile) parentXmlFile;
		}

		periodical = (PeriodicumXmlFile) xmlFiles.get(uuidIsOwnedByUuid
				.get(periodicalVolume.getUuid()));

		if (periodical == null || periodicalVolume == null)
			throw new RuntimeException(
					"There are missing some parents for the file: "
							+ articleFiles.get(uuidArticle));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();

			Document document = db
					.parse(new File(articleFiles.get(uuidArticle)));

			XPathExpression all = makeNSAwareXpath().compile(
					"//*[name()=\'mods:mods\']");
			NodeList nodesOfStream = (NodeList) all.evaluate(document,
					XPathConstants.NODESET);

			if (nodesOfStream == null || nodesOfStream.getLength() == 0)
				throw new RuntimeException(
						"There is no mods element in the file: "
								+ articleFiles.get(uuidArticle));
			Element mods = (Element) nodesOfStream.item(0);
			Element relatedItemEl = document.createElement("mods:relatedItem");
			relatedItemEl.setAttribute("type", "host");

			if (periodical.getTitle() != null) {
				Element titleInfoEl = document.createElement("mods:titleInfo");
				Element titleEl = document.createElement("mods:title");
				titleEl.setTextContent(periodical.getTitle());
				titleInfoEl.appendChild(titleEl);
				relatedItemEl.appendChild(titleInfoEl);
			}

			for (String ident : periodical.getIdentifiers().keySet()) {
				Element identifierEl = document
						.createElement("mods:identifier");
				identifierEl.setAttribute("type", ident);
				identifierEl.setTextContent(periodical.getIdentifiers().get(
						ident));
				relatedItemEl.appendChild(identifierEl);
			}

			Element partEl = document.createElement("mods:part");
			partEl.setAttribute("type", "article");

			relatedItemEl.appendChild(getPartDetail(document, periodicalVolume,
					"volume", "ročník"));

			Element dateEl = document.createElement("mods:date");

			if (periodicalItem != null)
				relatedItemEl.appendChild(getPartDetail(document,
						periodicalItem, "issue", "číslo"));

			if (periodicalItem != null
					&& periodicalItem.getDateIssued() != null
					&& !"".equals(periodicalItem.getDateIssued())) {
				dateEl.setTextContent(periodicalItem.getDateIssued());
			} else {
				dateEl.setTextContent(periodicalVolume.getDateIssued());
			}

			if (supplement != null) {
				relatedItemEl.appendChild(getPartDetail(document, supplement,
						"supplement", "příloha"));
			}

			relatedItemEl.appendChild(dateEl);
			mods.appendChild(relatedItemEl);

			writeNewArticle(document, uuidArticle);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"reading article file problem for path: "
							+ articleFiles.get(uuidArticle));

		} catch (SAXException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"reading article file problem for path: "
							+ articleFiles.get(uuidArticle));

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"reading article file problem for path: "
							+ articleFiles.get(uuidArticle));
		} catch (XPathExpressionException e) {
			throw new RuntimeException(
					"xpath reading article problem for path: "
							+ articleFiles.get(uuidArticle));
		}
	}

	private static Element getPartDetail(Document document,
			ItemVolumeSupplXmlFile itemVolumeXmlFile, String detailType,
			String caption) {
		Element detailEl = document.createElement("mods:detail");

		if (itemVolumeXmlFile.getPartNumber() != null
				&& !"".equals(itemVolumeXmlFile.getPartNumber())) {
			Element numberEl = document.createElement("mods:number");
			numberEl.setTextContent(itemVolumeXmlFile.getPartNumber());
			detailEl.appendChild(numberEl);
		}

		Element captionEl = document.createElement("mods:caption");

		detailEl.setAttribute("type", detailType);

		captionEl.setTextContent(caption);

		detailEl.appendChild(captionEl);
		return detailEl;
	}

	private static void writeNewArticle(Document doc, String uuid) {
		try {
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer;

			transformer = transformerFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(
					PATH_TO_NEW_ARTICLES_DIR + "/"
							+ uuid.substring("uuid:".length()) + "." + SUFFIX));

			transformer.transform(source, result);

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException("writing new article problem for uuid: "
					+ uuid);

		} catch (TransformerException e) {
			e.printStackTrace();
			throw new RuntimeException("writing new article problem for uuid: "
					+ uuid);
		}
	}

	public static Document parseDocument(InputStream is)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		return builder.parse(is);
	}

	private static void parseFiles() throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {

		int numberToParse = allFilePaths.size();

		for (String filePath : allFilePaths) {
			System.out.println("To parse: " + numberToParse--);

			File file = new File(filePath);
			FileInputStream fileStream = new FileInputStream(file);
			Document document = parseDocument(fileStream);

			// System.err.println(file.getPath());

			List<String> typeList = getElementsContent(document,
					"//*[name()=\'dc:type\']");
			if (typeList.size() == 0)
				throw new RuntimeException("there is no model");

			String modelString = null;
			for (String type : typeList) {
				if (type.startsWith(DC_MODEL_PREFIX)) {
					modelString = type;
					break;
				}
			}

			if (modelString == null)
				throw new RuntimeException("there is no model");

			DigitalObjectModel model = Constants.DigitalObjectModel
					.parseString(modelString
							.substring(DC_MODEL_PREFIX.length()), file
							.getPath());

			if (model != DigitalObjectModel.PAGE) {

				List<String> uuidList = getElementsContent(document,
						"//*[name()=\'mods:mods\']/*[name()=\'mods:identifier\'][@type=\'uuid\']");

				if (uuidList.size() == 0)
					throw new RuntimeException("there is no uuid");
				String uuid = "uuid:" + uuidList.get(0);

				List<String> childrenList = new ArrayList<String>();
				if (model == DigitalObjectModel.PERIODICAL) {

					removeInfoFedoraPrefix(getElementsContent(document,
							FedoraRelationship.hasItem
									.getXPathNamespaceAwareQuery()),
							childrenList, uuid);
					removeInfoFedoraPrefix(getElementsContent(document,
							FedoraRelationship.hasVolume
									.getXPathNamespaceAwareQuery()),
							childrenList, uuid);

					String title = null;
					List<String> titleList = getElementsContent(
							document,
							"//*[name()=\'mods:mods\']/*[name()=\'mods:titleInfo\']/*[name()=\'mods:title\']");
					if (titleList != null && titleList.size() > 0)
						title = titleList.get(0);

					xmlFiles.put(uuid, new PeriodicumXmlFile(uuid, model,
							childrenList, title, getIdentifiers(document)));

				} else if (model == DigitalObjectModel.PERIODICALVOLUME
						|| model == DigitalObjectModel.PERIODICALITEM
						|| model == DigitalObjectModel.SUPPLEMENT) {
					removeInfoFedoraPrefix(getElementsContent(document,
							FedoraRelationship.hasInternalPart
									.getXPathNamespaceAwareQuery()),
							childrenList, uuid);
					removeInfoFedoraPrefix(getElementsContent(document,
							FedoraRelationship.hasIntCompPart
									.getXPathNamespaceAwareQuery()),
							childrenList, uuid);

					if (model == DigitalObjectModel.PERIODICALVOLUME)
						removeInfoFedoraPrefix(getElementsContent(document,
								FedoraRelationship.hasItem
										.getXPathNamespaceAwareQuery()),
								childrenList, uuid);

					String partNumber = null;
					List<String> partNumList = getElementsContent(
							document,
							"//*[name()=\'mods:mods\']/*[name()=\'mods:titleInfo\']/*[name()=\'mods:partNumber\']");
					if (partNumList != null && partNumList.size() > 0)
						partNumber = partNumList.get(0);

					String dateIssued = null;
					List<String> dateList = getElementsContent(
							document,
							"//*[name()=\'mods:mods\']/*[name()=\'mods:originInfo\']/*[name()=\'mods:dateIssued\']");
					if (dateList != null && dateList.size() > 0)
						dateIssued = dateList.get(0);

					xmlFiles.put(uuid, new ItemVolumeSupplXmlFile(uuid, model,
							childrenList, partNumber, dateIssued));

				} else if (model == DigitalObjectModel.ARTICLE) {
					if (articleFiles.keySet().contains(uuid)) {
						System.err.println(uuid + "   " + file.getPath());
					}
					articleFiles.put(uuid, file.getAbsolutePath());
				}
			}
		}
	}

	private static void removeInfoFedoraPrefix(List<String> toParse,
			List<String> children, String parentUuid) {
		for (String uuidToParse : toParse) {
			String uuid = uuidToParse.substring(INFO_FEDORA_PREFIX.length());
			children.add(uuid);
			uuidIsOwnedByUuid.put(uuid, parentUuid);
		}
	}

	public static List<String> getElementsContent(Document foxmlDocument,
			String xPath) throws XPathExpressionException {

		XPathExpression all = makeNSAwareXpath().compile(xPath);

		NodeList nodesOfStream = (NodeList) all.evaluate(foxmlDocument,
				XPathConstants.NODESET);

		List<String> textContents = new ArrayList<String>();
		if (nodesOfStream != null && nodesOfStream.getLength() != 0) {

			for (int i = 0; i < nodesOfStream.getLength(); i++) {
				textContents.add(nodesOfStream.item(i).getTextContent());

				// System.out.println(xPath + "   " + i + " "
				// + nodesOfStream.item(i).getNodeName() + "  "
				// + nodesOfStream.item(i).getTextContent());
			}
		}
		return textContents;
	}

	public static Map<String, String> getIdentifiers(Document foxmlDocument)
			throws XPathExpressionException {
		XPathExpression all = makeNSAwareXpath().compile(
				"//*[name()=\'mods:mods\']/*[name()=\'mods:identifier\']");

		NodeList nodesOfStream = (NodeList) all.evaluate(foxmlDocument,
				XPathConstants.NODESET);

		Map<String, String> identifiers = new HashMap<String, String>();
		if (nodesOfStream != null && nodesOfStream.getLength() != 0) {
			for (int i = 0; i < nodesOfStream.getLength(); i++) {
				NamedNodeMap attributes = nodesOfStream.item(i).getAttributes();
				// System.err.println(attributes.getNamedItem("type")
				// .getTextContent()
				// + "   " + nodesOfStream.item(i).getTextContent());
				identifiers.put(attributes.getNamedItem("type")
						.getTextContent(), nodesOfStream.item(i)
						.getTextContent());
			}
		}
		return identifiers;
	}

	/**
	 * Make ns aware xpath.
	 * 
	 * @return the x path
	 */
	public static XPath makeNSAwareXpath() {
		if (xpfactory == null) {
			xpfactory = XPathFactory.newInstance();
		}
		XPath xpath = xpfactory.newXPath();
		// xpath.setNamespaceContext(new FedoraNamespaceContext());
		return xpath;
	}
}
