package remedy.entities;

import com.bmc.arsys.api.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static remedy.RemedyConnection.getServer;

/**
 * This class response for create KB form.
 * The form is populated requests\articles are related with it.
 */
public class KBForm implements Form {
    private String formName;
    private List<CustomArticle> customArticles;
    private CustomRequest customRequest;
    private String pathContentFiles;

    public KBForm(String formName, String pathContentFiles) {
        this.formName = formName;
        this.pathContentFiles = pathContentFiles;
    }

    /**
     * default getter of {@code customArticle} field.
     * @return {@code customArticle}
     */
    public List<CustomArticle> getCustomArticles() {
        return customArticles;
    }

    /**
     * default getter of {@code formName} field.
     * @return {@code formName}
     */
    @Override
    public String getFormName() {
        return formName;
    }

    /**
     * The method return clear entries from {@code query}
     * @param query The query to Remedy, the syntax like T-SQL
     * @return entries list
     * @throws IOException
     */
    @Override
    public List<Entry> getEntryKBForm(String query) throws IOException {
        ARServerUser serverRemedy = getServer();
        List<Entry> entries = new ArrayList<>();
        try {
            QualifierInfo qual = serverRemedy.parseQualification(formName, query);
            List<EntryListInfo> eListInfos = serverRemedy.getListEntry(formName, qual, 0, 0, null, null, false, null);
            if (!eListInfos.equals(null)) {
                for (EntryListInfo eListInfo : eListInfos) {
                    entries.add(serverRemedy.getEntry(formName, eListInfo.getEntryID(), null));
                }
            } else {/* вывод лога в файл*/}
        } catch (ARException e) {
            /* вывод лога в файл*/
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * The method set {@link Article} to {@code customArticle} field from {@code query}
     * @param query The query to Remedy, the syntax like T-SQL
     * @throws IOException
     */
    @Override
    public void setListArticleKBForm(String query) throws IOException {
        ARServerUser serverRemedy = getServer();
        this.customArticles = new ArrayList<>();
        try {
            QualifierInfo qual = serverRemedy.parseQualification(formName, query);
            List<EntryListInfo> eListInfos = serverRemedy.getListEntry(formName, qual, 0, 0, null, null, false, null);
            if (!eListInfos.equals(null)) {
                for (EntryListInfo eListInfo : eListInfos) {
                    Entry entry = serverRemedy.getEntry(formName, eListInfo.getEntryID(), null);
                    CustomArticle customArticle = new Article(entry, formName, pathContentFiles);
                    this.customArticles.add(customArticle);
                }
            } else {/* вывод лога в файл*/}
        } catch (ARException e) {
            /* вывод лога в файл*/
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The method set {@link Request} of {@link Article} to {@code customRequest} field of some KB Template Form.
     * A KB Template Form create by use of this class in {@link Article#setArticleTemplForm(Entry, String)}
     * @param requestID The requestID is related to current {@link Article}
     * @param articleID The articleID is related to {@link KBForm}
     * @param webUrl The web url KB article
     * @param categories Remedy categorization of {@link Article}
     * @param articleAttachments The path/fileName pair of Remedy article attachments.
     *                           Those attachments were downloaded to file system ({@link run.MainTask#pathContentFiles})
     *                           by use of {@link remedy.entities.Article#setArticleAttachments(Entry, String)} from Remedy KB article.
     * @throws ARException
     * @throws IOException
     */
    @Override
    public void setRequestTemplForm(String requestID, String articleID, String webUrl, List<String> categories, Map<Path, String> articleAttachments) throws ARException, IOException {
        ARServerUser serverRemedy = getServer();
        try {
            Entry entry = serverRemedy.getEntry(formName, requestID, null);
            CustomRequest customRequest = new Request(entry, formName, requestID, articleID, webUrl, categories, articleAttachments, pathContentFiles);
            this.customRequest = customRequest;
        } catch (ARException e) {
            /* вывод лога в файл*/
            System.out.println(e.getMessage());
        }

    }
    /**
     * default getter of {@code customRequest} field.
     * @return {@code customRequest}
     */
    @Override
    public CustomRequest getCustomRequest() {
        return this.customRequest;
    }
}
