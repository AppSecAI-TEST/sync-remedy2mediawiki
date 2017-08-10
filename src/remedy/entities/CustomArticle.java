package remedy.entities;

import com.bmc.arsys.api.ARException;
import com.bmc.arsys.api.Entry;
import enums.FieldIDs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface CustomArticle {
    String getArticleValue(FieldIDs filedId, Entry article);

    String getTitle();

    String getRequestID();

    String getDocID();

    String getStatus();

    List<String> getCategories();

    String getModifiedDate();
    void setModifiedDate(FieldIDs fieldId, Entry entry) throws ARException;

    String getAssigneeOrganization();

    Form getArticleTemplForm();

    void setArticleTemplForm(Entry article, String webUrl) throws ARException, IOException;

    Map<Path, String> getArticleAttachments();

    void setArticleAttachments(Entry article, String formName) throws IOException, ARException;

    String getWebUrl();
    public String getBusService();

    String getOrigBusService();

    String getOrigCategorization();

    String getArticleRequestID();

    String getKeyWords();
}
