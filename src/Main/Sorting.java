package Main;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import Utils.*;

public class Sorting extends AnAction {

    public Sorting() {
        super("Sort package.xml");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = e.getProject();
        final Document document = editor.getDocument();
        if (!SortPackageXML.checkIfSalesforceManifestFileIsOpen(e)) {
            displayError(project, "First you have to open Salesforce manifest file: 'package.xml'");
            return;
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(document.getText()));
            org.w3c.dom.Document doc = dBuilder.parse(inputSource);
            doc.getDocumentElement().normalize();
            Beans.Package pckg = SortPackageXML.getBeanFromXML(doc);
            org.w3c.dom.Document newXMLString = SortPackageXML.createXMLString(pckg);
            saveXMLStringToFile(project, document, newXMLString);
        } catch (Exception ex) {
            displayError(project, ex.getMessage());
        }
    }

    private void saveXMLStringToFile(Project project, Document document, org.w3c.dom.Document newDocument) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(newDocument), new StreamResult(sw));
        int end = document.getTextLength();
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.replaceString(0, end, SortPackageXML.format(sw.toString()))
        );
        displayInfo(project, "Entries in 'package.xml' sorted correctly.");
    }

    private void displayError(Project project, String textToDisplay) {
        displayText(project, textToDisplay, Messages.getErrorIcon());
    }

    private void displayInfo(Project project, String textToDisplay) {
        displayText(project, textToDisplay, Messages.getInformationIcon());
    }

    private void displayText(Project project, String textToDisplay, javax.swing.Icon iconType) {
        Messages.showMessageDialog(project, textToDisplay, "Information", iconType);
    }
}
