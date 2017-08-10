package remedy;

import com.bmc.arsys.api.Entry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import remedy.entities.Request;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ContentParser {
    /**
     * This class response for parsing Remedy HTML request content.
     * Parsed content will be upload to Mediawiki.
     * Class use the Jsoup Api for parsing.
     */

    public static final ContentParser parser = new ContentParser();

    private ContentParser() {
    }

    /**
     * The method clear content from CDATA.
     * @param parsedContent some html content from Remedy request content.
     */
    public void parseCDATA(StringBuilder parsedContent) {
        if (parsedContent.toString().contains("![CDATA[]]")) {
            String cdataString = parsedContent.toString();
            Document description = Jsoup.parseBodyFragment(cdataString, "UTF-8");
            for (Element d : description.select("description")) {
                if (d.hasText()) {
                    String unescapedHtml = d.text().replace("o:p", "p");
                    Document text = Jsoup.parse(unescapedHtml, "UTF-8");
                    parsedContent.setLength(0);
                    for (Element p : text.select("p")) {
                        parsedContent.append("<p>" + p.html() + "</p>");
                    }
                }
            }
        }
    }

    /**
     * The method replace image url of KB content with mediawiki syntax link.
     * @param parsedContent some html content from Remedy request content.
     * @param requestAttachments The path/filedID pair of Remedy request attachments.
     *                           Those attachments were downloaded to file system ({@link run.MainTask#pathContentFiles})
     *                           by use of {@link Request#setReqAttachments(Entry, String)} from Remedy KB request.
     * @param requestID The Remedy KB requestID of {@link remedy.entities.Request#requestID}
     */
    public void replaceUrlImgToMwLocalImg(StringBuilder parsedContent, Map<String, Path> requestAttachments, String requestID) {
        Document doc = Jsoup.parseBodyFragment(parsedContent.toString(), "UTF-8");
        Elements imgElements = doc.select("img[src~=^https://.*.avp.ru/arsys/]");
        if (!imgElements.isEmpty()) {
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            String html = doc.body().html();
            for (Element img : imgElements) {
                String attachFiledID = img.attr("arattid");
                if (!attachFiledID.equals("")) {
                    if (!requestAttachments.isEmpty()) {
                        Path path = requestAttachments.get(attachFiledID);
                        if (path != null) {
                            Path filename = path.getFileName();
                            html = html.replace(img.toString(), "[[File:" + filename.toString() + "|RTENOTITLE]]");
                            parsedContent.replace(0, parsedContent.length(), html);

                        }
                    }
                }
            }
        }
    }

    /**
     * The method find base64 image of KB request content.
     * Then download to file system ({@link run.MainTask#pathContentFiles}).
     * Then put to {@link Request#requestAttachments} and
     * replace base64 image into KB content by mediawiki syntax link.
     *
     * The method find replace base64 image url into content with mediawiki syntax link.
     * @param pathContentFiles The path to media files where they will be downloaded.
     * @param parsedContent some html content from Remedy request content.
     * @param requestAttachments The path/filedID pair of Remedy request attachments.
     *                           Those attachments were downloaded to file system ({@link run.MainTask#pathContentFiles})
     *                           by use of {@link Request#setReqAttachments(Entry, String)} from Remedy KB request.
     * @param articleID The Remedy KB articleID of {@link remedy.entities.Request#articleID}
     * @param requestID The Remedy KB requestID of {@link remedy.entities.Request#requestID}
     * @throws IOException The exception would be throw during the download of images.
     */
    public void convertBase64ImgToMwLocalImg(String pathContentFiles, StringBuilder parsedContent, Map<String, Path> requestAttachments, String articleID, String requestID) throws IOException {
        Document doc = Jsoup.parseBodyFragment(parsedContent.toString(), "UTF-8");
        Elements base64imgElements = doc.select("img[src~=data:image/.+;base64]");
        if (!base64imgElements.isEmpty()) {
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            String html = doc.body().html();
            for (Element e : base64imgElements) {
                String base64String = e.attr("src");
                String base64Tag = requestID + "-base64Img-" + base64imgElements.indexOf(e);
                String base64Img = base64String.split(",")[1];
                String extension = base64String.substring(11).split(";(?=[^;]+$)")[0];
                Path path = Paths.get(pathContentFiles + articleID + "\\" + requestID + "\\" + base64Tag + "." + extension);
                Files.createDirectories(path.getParent());
                FileOutputStream fos = new FileOutputStream(String.valueOf(path));


                byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Img);
                fos.write(imageBytes);
                fos.close();
                requestAttachments.put(base64Tag, path);

                String fileName = path.getFileName().toString();
                html = html.replace(e.toString(), "[[File:" + fileName + "|RTENOTITLE]]");
                parsedContent.replace(0, parsedContent.length(), html);
            }
        }
    }

    /**
     * The method add mediawiki category link to content.
     * @param parsedContent some html content from Remedy request content.
     * @param categories The categories list of {@link remedy.entities.Article#categorization}
     */
    public void addCategory(StringBuilder parsedContent, List<String> categories) {
        for (String category : categories) {
            parsedContent.append("<p>[[Category:" + category + "]]</p>");
        }
    }

    /**
     * The method add mediawiki category link of KB article original page to content.
     * @param parsedContent some html content from Remedy request content.
     * @param originalPage The web url KB remedy article.
     */
    public void addOriginalPageUrl(StringBuilder parsedContent, String originalPage) {
        parsedContent.append("<br> <font size=\"4\" color=\"blue\"> [" + originalPage + " KB article original page] </font> <br>");
    }

    /**
     * The method replace any url of KB content with mediawiki syntax link.
     * @param parsedContent some html content from Remedy request content.
     */
    public void convertUrlToMwUrl(StringBuilder parsedContent) {
        Document doc = Jsoup.parseBodyFragment(parsedContent.toString(), "UTF-8");
        Elements extUrl = doc.select("a[href]");
        if (!extUrl.isEmpty()) {
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            String html = doc.body().html();
            for (Element e : extUrl) {
                String url = e.attr("href");
                String val = e.text();
                html = html.replace(e.toString(), "[" + url + " " + val + "]");
                parsedContent.replace(0, parsedContent.length(), html);
            }
        }
    }

    /**
     * The method add mediawiki syntax link of article attachments.
     * @param parsedContent some html content from Remedy request content.
     * @param articleAttachments The path/fileName pair of Remedy article attachments.
     *                           Those attachments were downloaded to file system ({@link run.MainTask#pathContentFiles})
     *                           by use of {@link remedy.entities.Article#setArticleAttachments(Entry, String)} from Remedy KB article.
     */
    public void addArticleAttach(StringBuilder parsedContent, Map<Path, String> articleAttachments) {
        if (articleAttachments != null) {
            parsedContent.append("<br><font size=\"4\" color=\"black\"> '''Attachments:''' </font> <br>");
            for (Map.Entry attach : articleAttachments.entrySet()) {
                Path path = (Path) attach.getKey();
                parsedContent.append("<p> [[Media:" + path.getFileName() + "|" + attach.getValue() + "]] </p>");
            }

        }
    }

    /**
     * The method replace Microsoft tags of KB content with html tag <i>span</i>.
     * @param parsedContent some html content from Remedy request content.
     */
    public void clearMicrosoftTag(StringBuilder parsedContent) {
        if (parsedContent.toString().contains("<o:p")) {
            parsedContent.replace(0, parsedContent.length(), parsedContent.toString().replace("o:p", "p"));
            Document doc = Jsoup.parseBodyFragment(parsedContent.toString(), "UTF-8");
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            parsedContent.replace(0, parsedContent.length(), doc.body().html());
        }
        if (parsedContent.toString().contains("v:shapetype")) {
            Document doc = Jsoup.parseBodyFragment(parsedContent.toString(), "UTF-8");
            Elements p = doc.select("p");
            for (Element el : p) {
                for (Element child: el.select("*")) {
                    if (child.tag().toString().matches("^v:.*")) {
                        child.tagName("span");
                    }
                }
            }
            Elements span = doc.select("span");
            for (Element el : span) {
                for (Element child: el.select("*")) {
                    if (child.tag().toString().matches("^v:.*")) {
                        child.tagName("span");
                    }
                }
            }
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            parsedContent.replace(0, parsedContent.length(), doc.body().html());
        }
        if (parsedContent.toString().contains("v:shape")) {
            Document doc = Jsoup.parseBodyFragment(parsedContent.toString(), "UTF-8");
            Elements p = doc.select("p");
            for (Element el : p) {
                for (Element child: el.select("*")) {
                    if (child.tag().toString().matches("^v:.*")) {
                        child.tagName("span");
                    }
                }
            }
            Elements span = doc.select("span");
            for (Element el : span) {
                for (Element child: el.select("*")) {
                    if (child.tag().toString().matches("^v:.*")) {
                        child.tagName("span");
                    }
                }
            }
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            parsedContent.replace(0, parsedContent.length(), doc.body().html());
        }
        if (parsedContent.toString().contains("o:lock")) {
            parsedContent.replace(0, parsedContent.length(), parsedContent.toString().replace("o:lock", "span"));
            Document doc = Jsoup.parseBodyFragment(parsedContent.toString(), "UTF-8");
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            parsedContent.replace(0, parsedContent.length(), doc.body().html());
        }

    }

    /**
     * The method replace other html tag unsupported by mediawiki.
     * @param parsedContent some html content from Remedy request content.
     */
    public void HTML2Mediawiki(StringBuilder parsedContent) {
        Document doc = Jsoup.parseBodyFragment(parsedContent.toString(), "UTF-8");
        Elements tables = doc.select("table");
        if (!tables.isEmpty()) {
            Elements colgroups = doc.select("colgroup");
            if (!colgroups.isEmpty()) {
                colgroups.remove();
            }
            Elements tbodys = doc.select("tbody").unwrap();
            if (!tbodys.isEmpty()) {
                tbodys.clear();
            }
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            String html = doc.body().html();
            parsedContent.replace(0, parsedContent.length(), html);
        }
        Elements meta = doc.select("meta");
        if (!meta.isEmpty()) {
            meta.remove();
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            String html = doc.body().html();
            parsedContent.replace(0, parsedContent.length(), html);
        }
        Elements title = doc.select("title");
        if (!title.isEmpty()) {
            title.remove();
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            String html = doc.body().html();
            parsedContent.replace(0, parsedContent.length(), html);
        }
        Elements divs = doc.select("div");
        if (!divs.isEmpty()) {
            for (Element div : divs) {
                List<Node> divchilds =  div.childNodes();
                for (Node node : divchilds) {
                    if (node instanceof Element) {
                        if (((Element) node).tag().getName().contains("div")) {
                            div = null;
                            break;
                        }
                    }
                }
                if (div != null) {
                    div.tagName("br");
                }
            }
            doc.select("div").unwrap().clear();
            String html = doc.body().html();
            html = html.replace("</br>", "");
            parsedContent.replace(0, parsedContent.length(), html);
        }
        Elements video = doc.select("video");
        if (!video.isEmpty()) {
            for (Element v: video) {
                String url = v.select("source").attr("src");
                v.parent().appendText("<html5media height=\"480\" width=\"640\">"+url+"</html5media>");
                v.remove();
            }
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            String html = doc.body().html().replace("&lt;","<").replace("&gt;",">");
            parsedContent.replace(0, parsedContent.length(), html);
        }
        Elements extImg = doc.select("img[src~=^http.*://.*]");
        if (!extImg.isEmpty()) {
            for (Element img: extImg) {
                String url = img.attr("src");
                img.after(url);
                img.remove();
            }
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            String html = doc.body().html();
            parsedContent.replace(0, parsedContent.length(), html);
        }
        Elements anchor = doc.select("a");
        if (!anchor.isEmpty()) {
            for (Element a: anchor) {
                a.tagName("span");
            }
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
            String html = doc.body().html();
            parsedContent.replace(0, parsedContent.length(), html);
        }
    }
}
