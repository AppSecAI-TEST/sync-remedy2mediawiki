package mediawiki;

import remedy.entities.CustomArticle;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static enums.MwNamespace.MW_BMC_NAMESPACE;

/**
 * This class response for representation data of KB Request to mediawiki.
 * This object is used by {@link MwUploader} class.
 */
public class MwData {
    private String kbTitle;
    private String mwTitle;
    private String content;
    private List<String> categories;
    private Map<Path, String> articleAttach;
    private Map<String, Path> requestAttach;
    private String fileArticleUploadComment;
    private String fileRequestUploadComment;
    private String nameSpace = MW_BMC_NAMESPACE.getName();
    private String kbID;
    private String articleReqId;
    private String busService;
    private String requestId;
    private String contentUploadComment;


    /**
     * @param remedyArticle The {@link CustomArticle} object.
     */
    public MwData(CustomArticle remedyArticle) {
        kbTitle = remedyArticle.getTitle();
        kbID = remedyArticle.getDocID();
        setMwTitle(remedyArticle);
        content = remedyArticle.getArticleTemplForm().getCustomRequest().getParsedContent();
        busService = remedyArticle.getBusService();
        categories = remedyArticle.getCategories();
        articleReqId = remedyArticle.getArticleRequestID();
        requestId = remedyArticle.getRequestID();
        fileArticleUploadComment = kbID + "->" + articleReqId;
        fileRequestUploadComment = kbID + "->" + requestId;
        contentUploadComment = articleReqId + "->" + requestId;
        articleAttach = remedyArticle.getArticleAttachments();
        requestAttach = remedyArticle.getArticleTemplForm().getCustomRequest().getRequestAttachments();
    }

    /**
     * The method replace character in title with "" which by mediawiki "opinion" has service character.
     * <p><b>Such as:</b>
     * <p><i>[</i>
     * <p><i>]</i>
     * <p><i>#</i>
     * <p><i>{@literal <}</i>
     * <p><i>{@literal >}</i>
     * <p><i>{</i>
     * <p><i>}</i>
     * <p><i>_</i>
     * <p><i>|</i>
     * @param remedyArticle The {@link CustomArticle} object.
     */
    public void setMwTitle(CustomArticle remedyArticle) {
        String parseTitle = remedyArticle.getTitle().replace("[", "").replace("]", "").replace("#", "").replace("<", "").replace(">", "").replace("|", "").replace("{", "").replace("}", "").replace("_", "");
        mwTitle = getNameSpace() + ":" + parseTitle + " (" + remedyArticle.getDocID() + ")";
    }

    public String getKbTitle() {
        return kbTitle;
    }

    public String getMwTitle() {
        return mwTitle;
    }

    public String getFileRequestUploadComment() {
        return fileRequestUploadComment;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getArticleReqId() {
        return articleReqId;
    }

    public String getKbID() {
        return kbID;
    }

    public String getContent() {
        return content;
    }

    public List<String> getCategories() {
        return categories;
    }

    public Map<Path, String> getArticleAttach() {
        return articleAttach;
    }

    public Map<String, Path> getRequestAttach() {
        return requestAttach;
    }

    public String getFileArticleUploadComment() {
        return fileArticleUploadComment;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public String getBusService() {
        return busService;
    }

    public String getContentUploadComment() {
        return contentUploadComment;
    }
}
