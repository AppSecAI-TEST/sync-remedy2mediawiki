package remedy;

import enums.RegistryKey;

import java.util.Date;

import static com.sun.jna.platform.win32.Advapi32Util.*;
import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;
import static enums.RegistryKey.*;

/**
 * This class response for check, initialise, set, get of integrated in {@link RegistryKey#VALUE_SYNCDATE_LONG} registry value.
 */

public class SyncDate {

    private static final SyncDate SYNC_DATE = new SyncDate();

    /**
     * @return singleton object of this class.
     */
    public static SyncDate getSyncDate() {
        return SYNC_DATE;
    }

    /**
     * call {@link SyncDate#checkLastStartDate()} method
     */
    private SyncDate() {
        this.checkLastStartDate();
    }

    /**
     * This method check existence of {@link RegistryKey#VALUE_SYNCDATE_LONG} registry value.
     * If {@link RegistryKey#VALUE_SYNCDATE_LONG} registry value isn't exist, it create {@link RegistryKey#VALUE_SYNCDATE_LONG} registry value and
     * set zero.
     */
    private void checkLastStartDate() {
        boolean iLastStartDateValue = registryValueExists(
                HKEY_LOCAL_MACHINE, KEY_FIRST_IN_HKLM.getName() + KEY_SECOND_IN_HKLM.getName() + KEY_BMC.getName(), VALUE_SYNCDATE_LONG.getName());
        if (!iLastStartDateValue) {
            registrySetStringValue(
                    HKEY_LOCAL_MACHINE, KEY_FIRST_IN_HKLM.getName() + KEY_SECOND_IN_HKLM.getName() + KEY_BMC.getName(), VALUE_SYNCDATE_LONG.getName(), "0");
        }
        if (iLastStartDateValue) {
            String dataLastStartDateValue = registryGetStringValue(
                    HKEY_LOCAL_MACHINE, KEY_FIRST_IN_HKLM.getName() + KEY_SECOND_IN_HKLM.getName() + KEY_BMC.getName(), VALUE_SYNCDATE_LONG.getName());
            if (dataLastStartDateValue.equals("")) {
                registrySetStringValue(
                        HKEY_LOCAL_MACHINE, KEY_FIRST_IN_HKLM.getName() + KEY_SECOND_IN_HKLM.getName() + KEY_BMC.getName(), VALUE_SYNCDATE_LONG.getName(), "0");
            }
        }
    }

    /**
     * @return integrated of {@link RegistryKey#VALUE_SYNCDATE_LONG} registry value.
     */
    public String getLastStartDate() {
        checkLastStartDate();
        return registryGetStringValue(
                HKEY_LOCAL_MACHINE, KEY_FIRST_IN_HKLM.getName() + KEY_SECOND_IN_HKLM.getName() + KEY_BMC.getName(), VALUE_SYNCDATE_LONG.getName());
    }

    /**
     * Set current time (unix time) to integrated of {@link RegistryKey#VALUE_SYNCDATE_LONG} registry value.
     */
    public void setLastStartDate() {
        registrySetStringValue(
                HKEY_LOCAL_MACHINE, KEY_FIRST_IN_HKLM.getName() + KEY_SECOND_IN_HKLM.getName() + KEY_BMC.getName(),
                VALUE_SYNCDATE_LONG.getName(), String.valueOf(new Date().getTime() / 1000));
    }
}
