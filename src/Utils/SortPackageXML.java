package Utils;

import Beans.Type;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class SortPackageXML {
    public static Boolean checkIfSalesforceManifestFileIsOpen(AnActionEvent e) {
        VirtualFile vFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        String fileName = vFile != null ? vFile.getName() : null;
        return fileName.equals("package.xml");
    }

    public static String format(String unformattedXml) {
        try {
            org.w3c.dom.Document document = parseXmlFile(unformattedXml);
            OutputFormat format = new OutputFormat(document);
            format.setIndenting(true);
            format.setIndent(4);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static org.w3c.dom.Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Beans.Package getBeanFromXML(org.w3c.dom.Document doc) {
        Beans.Package pckg = new Beans.Package();
        pckg.setXmlns(doc.getDocumentElement().getAttributes().getNamedItem("xmlns").getNodeValue());
        pckg.setFullName(getFullName(doc));
        pckg.setVersion(getVersion(doc));
        pckg.setTypes(getPackageTypes(doc));
        return pckg;
    }

    public static org.w3c.dom.Document createXMLString(Beans.Package pckg) throws ParserConfigurationException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        org.w3c.dom.Document newDocument = documentBuilder.newDocument();

        Element root = newDocument.createElement("Package");
        root.setAttribute("xmlns", pckg.getXmlns());
        newDocument.appendChild(root);
        if (pckg.getFullName() != null) {
            Element fullName = newDocument.createElement("fullName");
            fullName.appendChild(newDocument.createTextNode(pckg.getFullName()));
            root.appendChild(fullName);
        }
        for (Type name : pckg.getTypes()) {
            Element newType = newDocument.createElement("types");
            root.appendChild(newType);
            for (String member : name.getMembers()) {
                Element newMember = newDocument.createElement("members");
                newMember.appendChild(newDocument.createTextNode(member));
                newType.appendChild(newMember);
            }
            Element newName = newDocument.createElement("name");
            newName.appendChild(newDocument.createTextNode(name.getName()));
            newType.appendChild(newName);
        }
        Element newVersion = newDocument.createElement("version");
        newVersion.appendChild(newDocument.createTextNode(pckg.getVersion()));
        root.appendChild(newVersion);
        return newDocument;
    }

    private static List<Type> getPackageTypes(org.w3c.dom.Document doc) {
        NodeList nList = doc.getChildNodes();
        List<Type> types = new ArrayList<Type>();
        for (int i = 0; i < nList.getLength(); i++) {
            NodeList childList = nList.item(i).getChildNodes();
            for (int j = 0; j < childList.getLength(); j++) {
                Node childNode = childList.item(j);
                if (childNode.getNodeName().equalsIgnoreCase("types")) {
                    Type newSalesforceType = new Type();
                    NodeList childListType = childNode.getChildNodes();
                    for (int k = 0; k < childListType.getLength(); k++) {
                        Node typeChildNode = childListType.item(k);
                        if (typeChildNode.getNodeName().equalsIgnoreCase("members")) {
                            newSalesforceType.addMember(typeChildNode.getTextContent());
                        } else if (typeChildNode.getNodeName().equalsIgnoreCase("name")) {
                            newSalesforceType.setName(typeChildNode.getTextContent());
                        }
                    }
                    newSalesforceType.getMembers().sort((o1, o2) -> o1.compareTo(o2));
                    types.add(newSalesforceType);
                }
            }
        }
        types.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        return types;
    }

    private static String getVersion(org.w3c.dom.Document doc) throws RuntimeException {
        NodeList nListVersion = doc.getElementsByTagName("version");
        if (nListVersion.getLength() > 1) {
            throw new RuntimeException("Only 1 <version> tag is allowed.");
        } else if (nListVersion.getLength() < 1) {
            throw new RuntimeException("<version> tag is missing.");
        } else {
            return ((Element) nListVersion.item(0)).getTextContent();
        }
    }

    private static String getFullName(org.w3c.dom.Document doc) throws RuntimeException {
        NodeList nListFullName = doc.getElementsByTagName("fullName");

        if ((nListFullName.getLength() == 1)) {
            return ((Element) nListFullName.item(0)).getTextContent();
        } else if ((nListFullName.getLength() == 0)) {
            return null;
        } else {
            throw new RuntimeException("Only 1 <fullName> tag is allowed.");
        }
    }
}
