package remedy.entities;

import com.bmc.arsys.api.ARException;
import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.Field;
import enums.FieldIDs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface CustomRequest {
    void setReqAttachments(Entry article, String formName) throws IOException, ARException;
    void setOriginalContent(Entry entry, String formName);
    String getDecisionTreeTempContent(Entry entry);
    String getKnownErrorTempContent(Entry entry);
    String getHowToTempContent(Entry entry);
    String getRefTempContent(Entry entry);
    String getProblemSolutionTempContent(Entry entry);
    String getRequestID();
    Map<String, Path> getRequestAttachments();
    String getOriginalContent();
    String getParsedContent();
}
