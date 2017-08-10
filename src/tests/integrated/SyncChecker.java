package tests.integrated;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import mediawiki.MwData;
import remedy.entities.CustomArticle;
import remedy.entities.KBForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static enums.MwNamespace.MW_BMC_NAMESPACE;

/**
 * This integration test is used for matching amount remedy articles with amount loaded articles to MW.
 */
public class SyncChecker {

    /**
     * @param mwConnection the established connection to mediawiki
     * @param kbForm       The form from {@link enums.FormName}
     */

    public void checkSyncPageCount(Wiki mwConnection, KBForm kbForm) {
        int countRemedyPage = 0;
        int countMwPageLoaded = 0;
        if (!kbForm.getCustomArticles().isEmpty()) {
            for (CustomArticle remedyArticles : kbForm.getCustomArticles()) {
                countRemedyPage++;
                MwData mwData = new MwData(remedyArticles);
                boolean iPage = mwConnection.exists(mwData.getMwTitle());
                if (iPage) {
                    countMwPageLoaded++;
                }
            }
        }
        System.out.println("Amount remedy articles: " + countRemedyPage);
        System.out.println("Amount loaded articles to MW: " + countRemedyPage);
    }

    public void comparePageCountByCateg(Wiki mwConnection, KBForm kbForm) {
        Map<String, List<String>> bmcListCateg = new TreeMap<>(); //список категорий и относящиеся к ним страницы из Remedy
        Map<String, List<String>> noExistPageByCateg = new TreeMap<>(); // список стриниц по категориям которых не видно в СДвики
        NS namespace = mwConnection.getNS("BMC");
        for (CustomArticle remedyArticles : kbForm.getCustomArticles()) {
            if (remedyArticles != null) {
                if (!bmcListCateg.containsKey(remedyArticles.getBusService())) {
                    List articleID = new ArrayList<>();
                    articleID.add(MW_BMC_NAMESPACE.getName() + ":" + remedyArticles.getDocID() + " - " + remedyArticles.getTitle());
                    bmcListCateg.put(remedyArticles.getBusService(), articleID);
                } else {
                    List articleID = bmcListCateg.get(remedyArticles.getBusService());
                    articleID.add(MW_BMC_NAMESPACE.getName() + ":" + remedyArticles.getDocID() + " - " + remedyArticles.getTitle());
                    bmcListCateg.put(remedyArticles.getBusService(), articleID);
                }
            }
        }

        for (Map.Entry<String, List<String>> listEntry : bmcListCateg.entrySet()) { //проход по кажой категории
            List mwPagebyCateg = mwConnection.getCategoryMembers(listEntry.getKey(), namespace); //список страниц по запрашиваемой категории из MediaWiki
            List noExistPage = listEntry.getValue().stream().
                    filter(t -> (!mwPagebyCateg.contains(t))).
                    collect(Collectors.toList());
            noExistPageByCateg.put(listEntry.getKey(), noExistPage);
        }

    }
}

