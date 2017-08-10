package remedy;

import static com.sun.jna.platform.win32.WinReg.*;
import static enums.RegistryKey.*;

public final class RemedyCredentials extends AbstractCredential{

    /**
     * This class request credential for connection to Remedy from Registry.
     */

    private static final RemedyCredentials REMEDY_CREDENTIALS = new RemedyCredentials();

    private RemedyCredentials() {
        requestCredentialValue(HKEY_LOCAL_MACHINE, KEY_FIRST_IN_HKLM, KEY_SECOND_IN_HKLM, KEY_BMC);}

    /**
     * @return singleton object of this class.
     */
    protected static RemedyCredentials getRemedyCredentials() {
        return REMEDY_CREDENTIALS;
    }

}
