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

/**
 * @author Matous Jobanek
 * 
 */
public class Main {

	public static String dirPath = "";

	public static String pathToNewArticlesDir = "";

	private static Map<String, String> uuidIsOwnedByUuid;

	private static Map<String, String> articleFiles;

	private static List<String> allFilePaths;

	private static Map<String, MyXmlFile> xmlFiles;

	private static XPathFactory xpfactory;

	/**
	 * @param args
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 */
	public static void main(String[] args) throws IOException,
			XPathExpressionException, ParserConfigurationException,
			SAXException {

		checkArgs(args);

		System.out.println("Processing...");

		uuidIsOwnedByUuid = new HashMap<String, String>();
		articleFiles = new HashMap<String, String>();
		allFilePaths = new ArrayList<String>();
		xmlFiles = new HashMap<String, MyXmlFile>();
		scanDirectoryStructure(dirPath);

		System.out.println("Parsing...");
		parseFiles();
		System.out.println("Refilling...");
		refillArticles();
		System.out.println("Done!");
	}

	private static void refillArticles() throws IOException {
		int numberToIncrement = articleFiles.size()
				/ Constants.PROGRESS_BAR_LENGTH;
		int increment = 0;
		int count = 0;
		String toWrite = getProgressBar(increment, false);

		for (String uuidArticle : articleFiles.keySet()) {
			if (count >= numberToIncrement) {
				increment++;
				toWrite = getProgressBar(increment, false);
				count = 0;
			} else {
				count++;
			}
			System.out.write(toWrite.getBytes());
			refillArticle(uuidArticle);
		}
		System.out.write(getProgressBar(Constants.PROGRESS_BAR_LENGTH, true)
				.getBytes());
	}

	private static void checkArgs(String[] args) {
		if (args.length < 2) {
			System.err.println("There are missing some argument(s)!");
			return;
		}

		dirPath = args[0];
		if (!new File(dirPath).exists()) {
			System.err.println("The input directory does not exists!");
			return;
		}

		pathToNewArticlesDir = args[1];
		if (!new File(pathToNewArticlesDir).exists()) {
			new File(pathToNewArticlesDir).mkdirs();
		}
	}

	private static void scanDirectoryStructure(String dirPath) {

		File path = new File(dirPath);
		FileFilter dirFilter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return !pathname.isFile();
			}

		};
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
								"." + Constants.SUFFIX.toLowerCase());
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
					Constants.XPATH_MODS);
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
					pathToNewArticlesDir + "/"
							+ uuid.substring("uuid:".length()) + "."
							+ Constants.SUFFIX));

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

	private static String getProgressBar(int increment, boolean isLast) {

		StringBuffer toWriteBuffer = new StringBuffer("|");
		for (int i = 0; i < Constants.PROGRESS_BAR_LENGTH - 2; i++) {
			if (i <= increment)
				toWriteBuffer.append("=");
			else
				toWriteBuffer.append(" ");
		}
		toWriteBuffer.append("|" + (!isLast ? "\r" : "\n"));

		return toWriteBuffer.toString();
	}

	private static void parseFiles() throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {

		int numberToIncrement = allFilePaths.size()
				/ Constants.PROGRESS_BAR_LENGTH;
		int increment = 0;
		int count = 0;
		String toWrite = getProgressBar(increment, false);

		for (String filePath : allFilePaths) {

			if (count >= numberToIncrement) {
				increment++;
				toWrite = getProgressBar(increment, false);
				count = 0;
			} else {
				count++;
			}
			System.out.write(toWrite.getBytes());

			File file = new File(filePath);
			FileInputStream fileStream = new FileInputStream(file);
			Document document = parseDocument(fileStream);

			// System.err.println(file.getPath());

			List<String> typeList = getElementsContent(document,
					Constants.XPATH_DC_TYPE);
			if (typeList.size() == 0)
				throw new RuntimeException("There is no model");

			String modelString = null;
			for (String type : typeList) {
				if (type.startsWith(Constants.DC_MODEL_PREFIX)) {
					modelString = type;
					break;
				}
			}

			if (modelString == null)
				throw new RuntimeException("There is no model");

			DigitalObjectModel model = Constants.DigitalObjectModel
					.parseString(modelString
							.substring(Constants.DC_MODEL_PREFIX.length()),
							file.getPath());

			if (model != DigitalObjectModel.PAGE) {

				List<String> uuidList = getElementsContent(document,
						Constants.XPATH_MODS_UUID);

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
					List<String> titleList = getElementsContent(document,
							Constants.XPATH_MODS_TITLE);
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
					List<String> partNumList = getElementsContent(document,
							Constants.XPATH_MODS_PART_NUMBER);
					if (partNumList != null && partNumList.size() > 0)
						partNumber = partNumList.get(0);

					String dateIssued = null;
					List<String> dateList = getElementsContent(document,
							Constants.XPATH_MODS_DATE_ISSUES);
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
		System.out.write(getProgressBar(Constants.PROGRESS_BAR_LENGTH, true)
				.getBytes());
	}

	private static void removeInfoFedoraPrefix(List<String> toParse,
			List<String> children, String parentUuid) {
		for (String uuidToParse : toParse) {
			String uuid = uuidToParse.substring(Constants.INFO_FEDORA_PREFIX
					.length());
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
			}
		}
		return textContents;
	}

	public static Map<String, String> getIdentifiers(Document foxmlDocument)
			throws XPathExpressionException {
		XPathExpression all = makeNSAwareXpath().compile(
				Constants.XPATH_MODS_IDENTIFIER);

		NodeList nodesOfStream = (NodeList) all.evaluate(foxmlDocument,
				XPathConstants.NODESET);

		Map<String, String> identifiers = new HashMap<String, String>();
		if (nodesOfStream != null && nodesOfStream.getLength() != 0) {
			for (int i = 0; i < nodesOfStream.getLength(); i++) {
				NamedNodeMap attributes = nodesOfStream.item(i).getAttributes();
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
		return xpath;
	}
}
