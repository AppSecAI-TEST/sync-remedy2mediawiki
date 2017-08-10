package mediawiki;

import remedy.AbstractCredential;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;
import static enums.RegistryKey.*;

/**
 * This class request credential for connection to Remedy from Registry.
 */

public class MwCredentials extends AbstractCredential {
    private static final MwCredentials MW_CREDENTIALS = new MwCredentials();

    private MwCredentials() {
        requestCredentialValue(HKEY_LOCAL_MACHINE, KEY_FIRST_IN_HKLM, KEY_SECOND_IN_HKLM, KEY_MW);
    }

    public static MwCredentials getMwCredentials() {
        return MW_CREDENTIALS;
    }
}
