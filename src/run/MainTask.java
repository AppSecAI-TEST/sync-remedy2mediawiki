package run;

import com.bmc.arsys.api.Entry;
import remedy.entities.CustomArticle;
import mediawiki.MwData;
import mediawiki.MwDeadPageCleaner;
import mediawiki.MwUploader;
import remedy.entities.KBForm;
import remedy.RemedyConnection;
import tests.integrated.SyncChecker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import static enums.FormName.KB_KNOWLEDGE_ARTICLE_MANAGER;
import static java.lang.Thread.sleep;
import static remedy.SyncDate.getSyncDate;

public class MainTask extends TimerTask {

    private String pathContentFiles,
                   queryArg1,
                   queryArg1Val,
                   pathMwRefreshLinks;

    public MainTask(String pathContentFiles, String queryArg1, String queryArg1Val, String pathMwRefreshLinks) {
        this.pathContentFiles = pathContentFiles;
        this.queryArg1 = queryArg1;
        this.queryArg1Val = queryArg1Val;
        this.pathMwRefreshLinks = pathMwRefreshLinks;
    }

    @Override
    public void run() {
        try {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("StartDate" + new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date()));
            System.out.println("INFO: Started");
            Files.createDirectories(Paths.get(pathContentFiles));
            KBForm knowledgeArticleManager = new KBForm(KB_KNOWLEDGE_ARTICLE_MANAGER.getName(), pathContentFiles);
            knowledgeArticleManager.setListArticleKBForm("'" + queryArg1 + "' LIKE \"" + queryArg1Val + "\" AND 'ArticleStatusEnglish' LIKE \"Published\" AND 'Modified Date' > " + "\"" + getSyncDate().getLastStartDate() + "\"");
            List<Entry> allBmcKb = knowledgeArticleManager.getEntryKBForm("'" + queryArg1 + "' LIKE \"" + queryArg1Val + "\" AND 'ArticleStatusEnglish' LIKE \"Published\" AND 'Modified Date' > " + "\"0\"");
            if (!knowledgeArticleManager.getCustomArticles().isEmpty() & !allBmcKb.isEmpty()) {
                MwUploader mwUploader = new MwUploader();
                for (CustomArticle remedyArticle : knowledgeArticleManager.getCustomArticles()) {
                    MwData mwData = new MwData(remedyArticle);
                    mwUploader.upload(mwData);
                    sleep(2000);
                }
                Files.walk(Paths.get(pathContentFiles))
                        .map(Path::toFile)
                        .sorted((o1, o2) -> -o1.compareTo(o2))
                        .forEach(File::delete);
                getSyncDate().setLastStartDate();
                sleep(60000);
                MwDeadPageCleaner mwDeadPageCleaner = new MwDeadPageCleaner(allBmcKb, mwUploader.getMwConnection(), mwUploader.getMwPages());
                mwDeadPageCleaner.clearDeadPages();
                Process runtime = Runtime.getRuntime().exec("cmd /c start php.exe " + pathMwRefreshLinks);
                System.out.println(new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date()));
                System.out.println("INFO: Test begin");
                SyncChecker syncChecker = new SyncChecker();
                syncChecker.checkSyncPageCount(new MwUploader().getMwConnection() , knowledgeArticleManager);
                System.out.println(new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date()));
                System.out.println("INFO: Test completed");
            } else {
                System.out.println(new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date()));
                System.out.println("INFO: New published articles not found");
            }
        } catch (Exception e) {
            System.out.println("ERROR:");
            e.printStackTrace();
        } finally {
            RemedyConnection.getServer().logout();
            System.out.println(new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date()));
            System.out.println("INFO: Stopped");
        }
    }
}
