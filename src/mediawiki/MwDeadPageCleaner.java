package mediawiki;

import com.bmc.arsys.api.Entry;
import fastily.jwiki.core.Wiki;

import java.util.ArrayList;
import java.util.List;

import static enums.FieldIDs.ATRICLE_TITLE;
import static enums.FieldIDs.DOC_ID;
import static enums.MwNamespace.MW_BMC_NAMESPACE;

/**
 * This class clear dead page from mediawiki.
 * If medeawiki has a page which doesn't exit in Kb remedy then this page will be deleted.
 */
public class MwDeadPageCleaner {
    private Wiki mwConnection;
    private List<Entry> kbEntries;
    private List<String> mwPages;

    public MwDeadPageCleaner(List<Entry> kbEntries, Wiki mwConnection, List<String> mwPages) {
        this.mwConnection = mwConnection;
        this.kbEntries = kbEntries;
        this.mwPages = mwPages;
    }

    /**
     * If medeawiki has a page which doesn't exit in Kb remedy then this page will be deleted.
     */
    public void clearDeadPages() {
        List<String> mwPages4Remove = new ArrayList();
        List<String> kbTitles = new ArrayList();
        for (Entry kbArticle : kbEntries) {
            String kbTitle = kbArticle.get(ATRICLE_TITLE.getKey()).toString().replace("[", "").replace("]", "").replace("#", "").replace("<", "").replace(">", "").replace("|", "").replace("{", "").replace("}", "").replace("_", "");
            String kbId = kbArticle.get(DOC_ID.getKey()).toString();
            kbTitles.add(MW_BMC_NAMESPACE.getName() + ":" + kbTitle + " (" + kbId + ")");
        }

        for (String mwPage : mwPages) {
            boolean DeadPage = true;
            for (String kbTitle : kbTitles) {
                if (mwPage.equalsIgnoreCase(kbTitle)) {
                    DeadPage = false;
                    break;
                }
            }
            if (DeadPage) {
                mwPages4Remove.add(mwPage);
            }
        }
        if (!mwPages4Remove.isEmpty()) {
            mwRemovePages(mwPages4Remove);
        }
    }

    private void mwRemovePages(List<String> mwTitles) {
        mwTitles.forEach(mwTitle -> mwConnection.delete(mwTitle, "page is dead"));
    }
}
