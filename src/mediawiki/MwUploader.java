package mediawiki;

import enums.MwNamespace;
import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.dwrap.ImageInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static enums.MwNamespace.MW_BMC_NAMESPACE;

/**
 * This class response for upload {@link MwData} to mediawiki.
 */

public class MwUploader {

    private List<String> mwPages = new ArrayList();
    private NS nameSpace;
    private Wiki mwConnection;

    /**
     * The construction establish connection to mediawiki.
     * <p>And populate {@code mwPage} field of pages witch has {@link MwNamespace#MW_BMC_NAMESPACE} namespace.
     */
    public MwUploader() {
        try {
            mwConnection = new Wiki(MwCredentials.getMwCredentials().getUser(), MwCredentials.getMwCredentials().getPassword(), MwCredentials.getMwCredentials().getConnPoint());
            nameSpace = mwConnection.getNS(MW_BMC_NAMESPACE.getName());
            mwPages = mwConnection.allPages("", false, false, 5000, nameSpace).stream().distinct().collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The method is entry point for create/update mediawiki data by use of methods mentioned below.
     * IF {@link MwData#getMwTitle()} is exist in {@code mwPages} field then run {@code createPage} else run {@code changePage}.
     * <p>1. {@code createCategory} method - if category doesn't exist in mediawiki then create a category from {@link MwData}.
     * <p>2. {@code createPage} method - it create new page in mediawiki.
     * <p>3. {@code changePage} method - If {@link MwData} content differ from current mediawiki page content then
     * it change page content, else it do nothing.
     * <p>4. {@code uploadFile} method - If a file of {@link MwData#requestAttach} doesn't exist in mediawiki then
     * it upload file, else it do nothing.
     * @param data The {@link MwData} object which has parsed content/articleAttachment/requestAttachment of KB request and service information.
     */
    public void upload(MwData data) {
        createCategory(data);
        List<String> mwPage = mwPages.stream().
                filter(p -> p.equalsIgnoreCase(data.getMwTitle())).
                collect(Collectors.toList());
        if (mwPage.isEmpty()) {
            createPage(data);
        } else {
            changePage(data);
        }
        uploadFile(data);
    }

    private void deleteBusServiceCategory(MwData data) {
        mwConnection.delete("Category:" + data.getBusService(), data.getFileArticleUploadComment());
    }

    private void uploadFile(MwData data) {

        if (data.getRequestAttach() != null) {
            for (Map.Entry reqAttach : data.getRequestAttach().entrySet()) {
                Path path = (Path) reqAttach.getValue();
                try {
                    ImageInfo imageInfo = mwConnection.getImageInfo("File:" + path.getFileName()).get(0);
                    if (!imageInfo.summary.equals(data.getFileRequestUploadComment())) {
                        throw new IndexOutOfBoundsException();
                    }
                } catch (IndexOutOfBoundsException e) {
                    mwConnection.upload(path, path.getFileName().toString(), "", data.getFileRequestUploadComment());
                }
            }
        }
        if (data.getArticleAttach() != null) {
            for (Map.Entry artAttach : data.getArticleAttach().entrySet()) {
                Path path = (Path) artAttach.getKey();
                try {
                    ImageInfo imageInfo = mwConnection.getImageInfo("File:" + path.getFileName()).get(0);
                    if (!imageInfo.summary.equals(data.getFileArticleUploadComment())) {
                        throw new IndexOutOfBoundsException();
                    }
                } catch (IndexOutOfBoundsException e) {
                    mwConnection.upload(path, path.getFileName().toString(), "", data.getFileArticleUploadComment());
                }
            }
        }
    }

    private void createPage(MwData data) {
        mwConnection.addText(data.getMwTitle(), data.getContent(), data.getContentUploadComment(), false);
    }

    private void changePage(MwData data) {
        String wikiPageContent = mwConnection.getPageText(data.getMwTitle());
        if (!data.getContent().equals(wikiPageContent)) {
            mwConnection.edit(data.getMwTitle(), data.getContent(), data.getContentUploadComment());
        }
    }

    private void createCategory(MwData data) {
        boolean iService = mwConnection.exists("Category:" + data.getBusService());
        if (!iService) {
            mwConnection.addText("Category:" + data.getBusService(), "[[Category:Service]]", data.getContentUploadComment(), false);
        }
        for (String category : data.getCategories()) {
            boolean iCategory = mwConnection.exists("Category:" + category);
            if (!iCategory) {
                mwConnection.addText("Category:" + category, "[[Category:" + data.getBusService() + "]]", data.getContentUploadComment(), false);
            }
        }
    }

    public List<String> getMwPages() {
        return mwPages;
    }

    public Wiki getMwConnection() {
        return mwConnection;
    }
}
