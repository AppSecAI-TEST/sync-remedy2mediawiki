package remedy.entities;

import com.bmc.arsys.api.*;
import enums.FieldIDs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static enums.FieldIDs.*;
import static remedy.RemedyConnection.getServer;

/**
 * This class response for create custom article of {@link KBForm}
 * <p> These object fields is populated based on filedID from {@link enums.FieldIDs}
 */

public class Article implements CustomArticle {
    private String title;
    private String RequestID;
    private String articleRequestID;
    private String docID;
    private String status;
    private String categorization;
    private String modifiedDate;
    private String assigneeOrganization;
    private Form articleTemplForm;
    private String webUrl;
    private String busService;
    private Map<Path, String> articleAttachments = new HashMap<>();
    private String pathContentFiles;
    private String keywords;

    /**
     * The constructor must be has the specific order of self methods running.
     * @param article The clear entry from KB Remedy.
     * @param form The main KB form - {@link enums.FormName#KB_KNOWLEDGE_ARTICLE_MANAGER}
     * @param pathContentFiles The path to media files where they will be downloaded.
     * @throws ARException
     * @throws IOException
     */
    public Article(Entry article, String form, String pathContentFiles) throws ARException, IOException {
        this.keywords = getArticleValue(KEYWORDS, article);
        this.pathContentFiles = pathContentFiles;
        this.articleRequestID = getArticleValue(ARTICLE_ID, article);
        this.docID = getArticleValue(DOC_ID, article);
        this.RequestID = getArticleValue(ARTICLE_REQUEST_ID, article);
        this.title = getArticleValue(ATRICLE_TITLE, article);
        this.assigneeOrganization = getArticleValue(ASSIGNEE_ORG, article);
        this.status = getArticleValue(ARTICLE_STATUS, article);
        this.busService = getArticleValue(ARTICLE_BUSINESS_SERVICE, article);
        this.categorization = getArticleValue(CATEGORIZATION, article);
        setModifiedDate(MODIFIED_DATE, article);
        this.webUrl = getArticleValue(KSP_URL, article);
        setArticleAttachments(article, form);
        setArticleTemplForm(article, webUrl);
    }

    /**
     * The method get value of specific {@link FieldIDs} enums.
     * @param filedId some {@link FieldIDs}
     * @param article The clear entry from KB Remedy.
     * @return The value of fieldID from {@link FieldIDs}
     */
    @Override
    public String getArticleValue(FieldIDs filedId, Entry article) {
        String value = article.keySet().stream().parallel().
                filter(id -> id.equals(filedId.getKey())).
                map(id -> article.get(id).toString()).
                filter(val -> val != null).
                findFirst().
                orElse(null);
        return value;
    }

    /**
     * The method set value to {@code modifiedDate} from {@link FieldIDs#MODIFIED_DATE}
     * @param fieldId some {@link FieldIDs}
     * @param article The clear entry from KB Remedy.
     * @throws ARException
     */
    @Override
    public void setModifiedDate(FieldIDs fieldId, Entry article) throws ARException {
        Value value = article.keySet().stream().parallel().
                filter(id -> id.equals(fieldId.getKey())).
                map(id -> (article.get(id))).
                filter(val -> val != null).
                findFirst().
                get();

        this.modifiedDate = ((Timestamp) value.getValue()).toDate().toString();
    }

    /**
     * The method create {@link KBForm} base on value of {@link FieldIDs#ARTICLE_REQTEMPFORM}.
     * <p>Then it is populated of {@link Request} by use of {@link KBForm#setRequestTemplForm(String, String, String, List, Map)} method.
     * @param article The clear entry from KB Remedy.
     * @param webUrl The web url KB article
     * @throws ARException
     * @throws IOException
     */
    @Override
    public void setArticleTemplForm(Entry article, String webUrl) throws ARException, IOException {
        for (Integer fieldID : article.keySet()) {
            if (fieldID.equals(ARTICLE_REQTEMPFORM.getKey())) {
                String temlFormName = article.get(fieldID).toString();
                KBForm templateForm = new KBForm(temlFormName, this.pathContentFiles);
                templateForm.setRequestTemplForm(RequestID, articleRequestID, webUrl, getCategories(), articleAttachments);
                this.articleTemplForm = templateForm;
                continue;
            }
        }
    }

    /**
     * This method search attachments of {@link Article}.
     * If it found it then download to file system ({@code pathContentFiles})
     * @param article The clear entry from KB Remedy.
     * @param formName The KB form
     * @throws IOException
     * @throws ARException
     */
    @Override
    public void setArticleAttachments(Entry article, String formName) throws IOException, ARException {
        for (Integer filedID :
                article.keySet()) {
            Value val = article.get(filedID);
            if (val.getValue() instanceof AttachmentValue && val.toString() != null) {
                AttachmentValue aVal = (AttachmentValue) val.getValue();
                String aName;
                String extension;
                String[] aDetails = aVal.getValueFileName().split("\\.(?=[^\\.]+$)");
                if (aDetails.length == 2) {
                    aName = aDetails[0] + "." + aDetails[1];
                    extension = aDetails[1];
                } else {
                    aName = aDetails[0];
                    extension = "unknown";
                }
                int lastPos = aName.lastIndexOf('\\');
                String fileName = (lastPos < 0) ? aName : aName.substring(lastPos + 1);

                Path path = Paths.get(pathContentFiles + "\\" + this.articleRequestID + "\\" + this.articleRequestID + "-" + filedID.intValue() + "." + extension);
                Files.createDirectories(path.getParent());

                FileOutputStream fos = new FileOutputStream(String.valueOf(path));
                byte[] attach = getServer().getEntryBlob(formName, this.articleRequestID, filedID);
                fos.write(attach);
                fos.close();
                this.articleAttachments.put(path, fileName);
            }
        }
    }


    /**
     * This method do make the two things:
     * <p>1. It find <b>SDKS</b> key word in {@link FieldIDs#KEYWORDS}
     * <p>   If it's found then <b>SD Knowledge Sharing</b> add to {@code categories}
     * <p>2. It form categories from {@link Article#getOrigCategorization()} and {@link Article#getBusService()}
     * @return the parsed category list.
     */
    @Override
    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        if (getKeyWords() != null) {
            String[] sentence = getKeyWords().split(" ");
            for (String word : sentence) {
                if (word.equals("SDKS")) {
                    categories.add("SD Knowledge Sharing" + " (" + getBusService() + ")");
                }
            }
        }
        if (getOrigCategorization() != null) {
            categories.add(getOrigCategorization() + " (" + getBusService() + ")");
        } else {
            categories.add("Main category" + " (" + getBusService() + ")");
        }
        return categories;
    }

    /**
     * If {@code busService} isn't null this method return {@code busService} else it return <b>Other services</b>.
     * @return parsed {@code busService}
     */
    public String getBusService() {
        return getOrigBusService() != null ? getOrigBusService() : "Other services";
    }

    @Override
    public Map<Path, String> getArticleAttachments() {
        return articleAttachments;
    }

    @Override
    public String getAssigneeOrganization() {
        return assigneeOrganization;
    }

    @Override
    public Form getArticleTemplForm() {
        return articleTemplForm;
    }

    @Override
    public String getWebUrl() {
        return webUrl;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public String getRequestID() {
        return RequestID;
    }

    @Override
    public String getDocID() {
        return docID;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getOrigBusService() {
        return busService;
    }

    @Override
    public String getOrigCategorization() {
        return categorization;
    }

    @Override
    public String getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public String getArticleRequestID() {
        return articleRequestID;
    }

    @Override
    public String getKeyWords() {
        return keywords;
    }
}