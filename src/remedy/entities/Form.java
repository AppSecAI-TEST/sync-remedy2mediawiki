package remedy.entities;

import com.bmc.arsys.api.ARException;
import com.bmc.arsys.api.Entry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Form {
    List<CustomArticle> getCustomArticles();
    String getFormName();
    void setListArticleKBForm(String query) throws IOException;
    List<Entry> getEntryKBForm(String query) throws IOException;
    void setRequestTemplForm(String RequestID, String articleID, String webUrl, List<String> categories, Map<Path,String> articleAttachments) throws ARException, IOException;
    CustomRequest getCustomRequest ();
}
