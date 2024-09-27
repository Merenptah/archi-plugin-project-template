package com.archiplugin.projectcreator.preferences;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jface.preference.IPreferenceStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.archiplugin.projectcreator.Activator;

public class Preferences {
	private static final String LIST_SEPARATOR = ",";
	private static final String MANDATORY_PROPS = "mandatoryProps";
	private static final String TO_FOLDER_ID = "toFolderId";
	private static final String FROM_FOLDER_ID = "fromFolderId";
	private static final String LIFECYCLE = "lifecycle";
	private static String PROJECT_CREATION_TEMPLATE_FOLDER = "ProjectCreationTemplateFolder";
	private static String PROJECT_LIFECYCLE_FROM_FOLDER = "ProjectLifeCycleFromFolder";
	private static String PROJECT_LIFECYCLE_TO_FOLDER = "ProjectLifeCycleToFolder";
	private static String PROJECT_LIFECYCLES = "ProjectLifeCycles";

	static void setDefault() {
		preferenceStore().setDefault(PROJECT_CREATION_TEMPLATE_FOLDER, "");
	}

	private static IPreferenceStore preferenceStore() {
		return Activator.INSTANCE.getPreferenceStore();
	}

	public static String getTemplateFolderId() {
		return preferenceStore().getString(PROJECT_CREATION_TEMPLATE_FOLDER);
	}

	public static void setTemplateFolderId(String id) {
		preferenceStore().setValue(PROJECT_CREATION_TEMPLATE_FOLDER, id);
	}

	public static String getLifecycleFromFolderId() {
		return preferenceStore().getString(PROJECT_LIFECYCLE_FROM_FOLDER);
	}

	public static void setLifecycleFromFolderId(String id) {
		preferenceStore().setValue(PROJECT_LIFECYCLE_FROM_FOLDER, id);
	}

	public static String getLifecycleToFolderId() {
		return preferenceStore().getString(PROJECT_LIFECYCLE_TO_FOLDER);
	}

	public static void setLifecycleToFolderId(String id) {
		preferenceStore().setValue(PROJECT_LIFECYCLE_TO_FOLDER, id);
	}

	public static PreferenceLifecycles getPreferenceLifecycles() {
		var defs = readFromXml(preferenceStore().getString(PROJECT_LIFECYCLES));

		return new PreferenceLifecycles(defs);
	}

	public static void setPreferenceLifecycles(List<LifecycleDefinition> defs) {
		if (!defs.isEmpty()) {
			preferenceStore().setValue(PROJECT_LIFECYCLES, writeToXml(defs));
		} else {
			preferenceStore().setToDefault(PROJECT_LIFECYCLES);
		}
	}

	private static List<LifecyclePreferenceDefinition> readFromXml(String input) {
		if (input == null || input.isBlank()) {
			return List.of();
		}

		// Parser that produces DOM object trees from XML content
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// API to obtain DOM Document instance
		DocumentBuilder builder = null;
		try {
			// Create DocumentBuilder with default configuration
			builder = factory.newDocumentBuilder();

			// Parse the content to Document object
			Document doc = builder.parse(new InputSource(new StringReader(input)));

			var lifecycles = doc.getElementsByTagName(LIFECYCLE);
			var result = new ArrayList<LifecyclePreferenceDefinition>();

			for (int i = 0; i < lifecycles.getLength(); i++) {
				var item = lifecycles.item(i);
				var childs = item.getChildNodes();
				String fromFolderId = null;
				String toFolderId = null;
				List<String> mandatoryProperties = List.of();

				for (int j = 0; j < childs.getLength(); j++) {
					var child = childs.item(j);
					switch (child.getNodeName()) {
					case FROM_FOLDER_ID -> fromFolderId = child.getTextContent();
					case TO_FOLDER_ID -> toFolderId = child.getTextContent();
					case MANDATORY_PROPS -> mandatoryProperties = child.getTextContent().isBlank() ? List.of()
							: List.of(child.getTextContent().split(LIST_SEPARATOR));
					}
				}

				var def = new LifecyclePreferenceDefinition(fromFolderId, toFolderId, mandatoryProperties);
				result.add(def);
			}

			return result;
		} catch (Exception e) {
			return List.of();
		}

	}

	private static String writeToXml(List<LifecycleDefinition> input) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("lifecycles");
			doc.appendChild(rootElement);

			input.forEach(def -> {
				var element = doc.createElement(LIFECYCLE);
				rootElement.appendChild(element);

				addAttribute(doc, element, FROM_FOLDER_ID, def.getFromFolderId());
				addAttribute(doc, element, TO_FOLDER_ID, def.getToFolderId());
				addAttribute(doc, element, MANDATORY_PROPS,
						def.getMandatoryProperties().stream().collect(Collectors.joining(LIST_SEPARATOR)));
			});

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			StringWriter stringWriter = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
			return stringWriter.toString();
		} catch (Exception e) {
			return null;
		}
	}

	private static void addAttribute(Document doc, Element parent, String fieldName, String content) {
		var child = doc.createElement(fieldName);
		child.setTextContent(content);
		parent.appendChild(child);
	}

}
