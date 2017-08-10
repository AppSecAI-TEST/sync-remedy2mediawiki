package remedy;

import enums.RegistryKey;

import static com.sun.jna.platform.win32.Advapi32Util.registryGetStringValue;
import static com.sun.jna.platform.win32.WinReg.HKEY;
import static enums.RegistryKey.*;

/**
 * This abstract class is implemented {@link mediawiki.MwCredentials} and {@link RemedyCredentials}
 */
public abstract class AbstractCredential {
    private String ConnPoint;
    private String User;
    private String Password;
    private String Port;

    /**
     * This method get integrated value from Registry by use of external api jna.platform.win32 methods.
     * @param HKEY the reserved name of registry root entity (i.e HKEY_LOCAL_MACHINE)
     * @param firstKeyInHKEY  the next key after HKEY
     * @param secondKeyInHKEY the next key after {@code firstKeyInHKEY}
     * @param thirdKeyInHKEY the next key after {@code secondKeyInHKEY}
     */
    protected final void requestCredentialValue(HKEY HKEY, RegistryKey firstKeyInHKEY, RegistryKey secondKeyInHKEY, RegistryKey thirdKeyInHKEY) {
        this.ConnPoint = registryGetStringValue(
                HKEY, firstKeyInHKEY.getName() + secondKeyInHKEY.getName() + thirdKeyInHKEY.getName(), VALUE_CONNECTIONPOINT.getName());
        this.User = registryGetStringValue(
                HKEY, firstKeyInHKEY.getName() + secondKeyInHKEY.getName() + thirdKeyInHKEY.getName(), VALUE_USER.getName());
        this.Password = registryGetStringValue(
                HKEY, firstKeyInHKEY.getName() + secondKeyInHKEY.getName() + thirdKeyInHKEY.getName(), VALUE_PASSWORD.getName());
        this.Port = registryGetStringValue(
                HKEY, firstKeyInHKEY.getName() + secondKeyInHKEY.getName() + thirdKeyInHKEY.getName(), VALUE_PORT.getName());
    }

    public String getConnPoint() {
        return ConnPoint;
    }

    public String getUser() {
        return User;
    }

    public String getPassword() {
        return Password;
    }

    public String getPort() {
        return Port;
    }
}
