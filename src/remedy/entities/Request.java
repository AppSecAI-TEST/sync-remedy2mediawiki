package remedy.entities;

import com.bmc.arsys.api.ARException;
import com.bmc.arsys.api.AttachmentValue;
import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.Value;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static enums.FieldIDs.*;
import static enums.FormName.*;
import static remedy.ContentParser.parser;
import static remedy.RemedyConnection.getServer;

/**
 * This class response for create custom request of {@link KBForm}.
 * The {@link KBForm} for this object must be only:
 * <p>1. {@link enums.FormName#KB_DECISION_TREE_TEMPLATE}
 * <p>2. {@link enums.FormName#KB_HOW_TO_TEMPLATE}
 * <p>3. {@link enums.FormName#KB_KNOWN_ERROR_TEMPLATE}
 * <p>4. {@link enums.FormName#KB_REFERENCE_TEMPLATE}
 * <p>5. {@link enums.FormName#KB_PROBLEM_SOLUTION_TEMPLATE}
 * <p> These object fields is populated based on filedID from {@link enums.FieldIDs}
 */
public class Request implements CustomRequest {

    private String requestID;
    private String articleID;
    private Map<String, Path> requestAttachments = new HashMap<>();
    private String originalContent;
    private StringBuilder parsedContent = new StringBuilder();
    private String pathContentFiles;

    /**
     * The constructor must be has the specific order of self methods running.
     * @param entry The clear entry of KB remedy article request.
     * @param formName The template form from {@link enums.FormName}
     * @param requestID The requestID is related {@link Article}
     * @param articleID The articleID is related {@link KBForm}
     * @param webUrl The web url KB article
     * @param categories Remedy categorization of {@link Article}
     * @param articleAttachments The path/fileName pair of Remedy article attachments.
     *                           Those attachments were downloaded to file system ({@link run.MainTask#pathContentFiles})
     *                           by use of {@link remedy.entities.Article#setArticleAttachments(Entry, String)} from Remedy KB article.
     * @param pathContentFiles The path to media files where they will be downloaded.
     * @throws IOException
     * @throws ARException
     */
    public Request(Entry entry, String formName, String requestID, String articleID, String webUrl, List<String> categories, Map<Path, String> articleAttachments, String pathContentFiles) throws IOException, ARException {
        this.pathContentFiles = pathContentFiles;
        this.articleID = articleID;
        this.requestID = requestID;
        setOriginalContent(entry, formName);
        setReqAttachments(entry, formName);
        this.parsedContent.append(this.originalContent);

        parser.parseCDATA(parsedContent);
        parser.clearMicrosoftTag(parsedContent);
        parser.replaceUrlImgToMwLocalImg(parsedContent, requestAttachments, requestID);
        parser.convertBase64ImgToMwLocalImg(pathContentFiles, parsedContent, requestAttachments, articleID, requestID);
        parser.convertUrlToMwUrl(parsedContent);
        parser.addArticleAttach(parsedContent, articleAttachments);
        parser.addOriginalPageUrl(parsedContent, webUrl);
        parser.addCategory(parsedContent, categories);
        parser.HTML2Mediawiki(parsedContent);
    }

    /**
     * This method set request content to {@code originalContent} field.
     * <p>For specific {@code formName} run specific method:
     * <p>1. {@link enums.FormName#KB_REFERENCE_TEMPLATE} - {@link Request#getRefTempContent(Entry)}
     * <p>2. {@link enums.FormName#KB_HOW_TO_TEMPLATE} - {@link Request#getHowToTempContent(Entry)}
     * <p>3. {@link enums.FormName#KB_KNOWN_ERROR_TEMPLATE} - {@link Request#getKnownErrorTempContent(Entry)}
     * <p>4. {@link enums.FormName#KB_DECISION_TREE_TEMPLATE} - {@link Request#getDecisionTreeTempContent(Entry)}
     * <p>5. {@link enums.FormName#KB_PROBLEM_SOLUTION_TEMPLATE} - {@link Request#getProblemSolutionTempContent(Entry)}
     * @param entry The clear entry of KB remedy article request.
     * @param formName The template form from {@link enums.FormName}
     */
    @Override
    public void setOriginalContent(Entry entry, String formName) {
        if (formName.equals(KB_REFERENCE_TEMPLATE.getName())) {
            originalContent = getRefTempContent(entry);
            return;
        }
        if (formName.equals(KB_HOW_TO_TEMPLATE.getName())) {
            originalContent = getHowToTempContent(entry);
            return;
        }
        if (formName.equals(KB_KNOWN_ERROR_TEMPLATE.getName())) {
            originalContent = getKnownErrorTempContent(entry);
            return;
        }
        if (formName.equals(KB_DECISION_TREE_TEMPLATE.getName())) {
            originalContent = getDecisionTreeTempContent(entry);
            return;
        }
        if (formName.equals(KB_PROBLEM_SOLUTION_TEMPLATE.getName())) {
            originalContent = getProblemSolutionTempContent(entry);
            return;
        }
    }

    /**
     * This method search attachments of {@link Request}.
     * If it found then download to file system ({@code pathContentFiles})
     * @param entry The clear entry of KB remedy article request.
     * @param formName The template form from {@link enums.FormName}
     * @throws IOException
     * @throws ARException
     */
    @Override
    public void setReqAttachments(Entry entry, String formName) throws IOException, ARException {
        for (Integer filedID :
                entry.keySet()) {
            Value val = entry.get(filedID);
            if (val.getValue() instanceof AttachmentValue && val.toString() != null) {
                AttachmentValue aVal = (AttachmentValue) val.getValue();
                String extension;
                String[] aDetails = aVal.getValueFileName().split("\\.(?=[^\\.]+$)");
                if (aDetails.length == 2) {
                    extension = aDetails[1];
                } else {
                    extension = "unknown";
                }
                Path path = Paths.get(pathContentFiles + "\\" + this.articleID + "\\" + this.requestID + "\\" + this.requestID + "-" + filedID + "." + extension);
                Files.createDirectories(path.getParent());

                FileOutputStream fos = new FileOutputStream(String.valueOf(path));
                byte[] attach = getServer().getEntryBlob(formName, this.requestID, filedID);
                fos.write(attach);
                fos.close();
                this.requestAttachments.put(filedID.toString(), path);
            }
        }

    }

    /**
     * This method get request content of specific {@link enums.FormName}
     * Every template form from {@link enums.FormName} has individual template content.
     * @param entry The clear entry of KB remedy article request.
     * @return the content of template form request.
     */
    @Override
    public String getProblemSolutionTempContent(Entry entry) {
        StringBuilder content = new StringBuilder("<b><font size=\"4\" color=\"red\">Problem:</font></b><br>");

        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(PROBLEM_SOLUTION_TEMPL_REQUEST_P.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", "<p>", "</p>")));
        content.append("<br><b><font size=\"4\" color=\"green\">Solution:</font></b><br>");
        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(PROBLEM_SOLUTION_TEMPL_REQUEST_S.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", "<p>", "</p>")));
        content.append("<br><b><font size=\"4\" color=\"olive\">Technical Notes:</font></b><br>");
        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(PS_KE_HT_TEMPLS_REQUEST_TECH_NOTES.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", "<p>", "</p>")));


        return content.toString();
    }
    /**
     * This method get request content of specific {@link enums.FormName}
     * Every template form from {@link enums.FormName} has individual template content.
     * @param entry The clear entry of KB remedy article request.
     * @return the content of template form request.
     */
    @Override
    public String getDecisionTreeTempContent(Entry entry) {
        StringBuilder content = new StringBuilder();

        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(DECISION_TREE_TEMPL_REQUEST.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                findFirst().
                orElse(""));
        return content.toString();
    }
    /**
     * This method get request content of specific {@link enums.FormName}
     * Every template form from {@link enums.FormName} has individual template content.
     * @param entry The clear entry of KB remedy article request.
     * @return the content of template form request.
     */
    @Override
    public String getKnownErrorTempContent(Entry entry) {
        StringBuilder content = new StringBuilder("<b><font size=\"4\" color=\"red\">Error:</font></b><br>");

        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(KNOWN_ERROR_TEMPL_REQUEST_E.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", "<p>", "</p>")));
        content.append("<br><b><font size=\"4\" color=\"green\">Root Cause:</font></b><br>");
        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(KNOWN_ERROR_TEMPL_REQUEST_RC.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", "<p>", "</p>")));
        content.append("<br><b><font size=\"4\" color=\"olive\">Workaround/Fix:</font></b><br>");
        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(KNOWN_ERROR_TEMPL_REQUEST_WF.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", "<p>", "</p>")));
        content.append("<br><b><font size=\"4\" color=\"olive\">Technical Notes:</font></b><br>");
        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(PS_KE_HT_TEMPLS_REQUEST_TECH_NOTES.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", "<p>", "</p>")));

        return content.toString();
    }
    /**
     * This method get request content of specific {@link enums.FormName}
     * Every template form from {@link enums.FormName} has individual template content.
     * @param entry The clear entry of KB remedy article request.
     * @return the content of template form request.
     */
    @Override
    public String getHowToTempContent(Entry entry) {
        StringBuilder content = new StringBuilder("<b><font size=\"4\" color=\"red\">Question:</font></b><br>");

        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(HOW_TO_TEMPL_REQUEST_Q.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", " ", "")));
        content.append("<br><b><font size=\"4\" color=\"green\">Answer:</font></b><br>");
        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(HOW_TO_TEMPL_REQUEST_A.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", " ", "")));
        content.append("<br><b><font size=\"4\" color=\"olive\">Technical Notes:</font></b><br>");
        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(PS_KE_HT_TEMPLS_REQUEST_TECH_NOTES.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                collect(Collectors.joining("", " ", "")));

        return content.toString();
    }
    /**
     * This method get request content of specific {@link enums.FormName}
     * Every template form from {@link enums.FormName} has individual template content.
     * @param entry The clear entry of KB remedy article request.
     * @return the content of template form request.
     */
    @Override
    public String getRefTempContent(Entry entry) {
        StringBuilder content = new StringBuilder();

        content.append(entry.keySet().stream().parallel().
                filter(id -> id.equals(REFERENCE_TEMPL_REQUEST.getKey())).
                map(id -> entry.get(id).toString()).
                filter(val -> val != null).
                findFirst().
                orElse(""));
        return content.toString();
    }

    public String getRequestID() {
        return requestID;
    }

    public Map<String, Path> getRequestAttachments() {
        return requestAttachments;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    @Override
    public String getParsedContent() {
        return parsedContent.toString();
    }
}








