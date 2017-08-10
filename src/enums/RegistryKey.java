package enums;

public enum RegistryKey {
    KEY_FIRST_IN_HKLM("SOFTWARE\\"),
    KEY_SECOND_IN_HKLM("Sync-KBArticles\\"),
    KEY_BMC("BMC"),
    KEY_MW("MW"),
    VALUE_CONNECTIONPOINT("ConnPoint"),
    VALUE_USER("User"),
    VALUE_PASSWORD("Password"),
    VALUE_PORT("Port"),
    VALUE_SYNCDATE_LONG("LastStartDate");

    private final String name;

    RegistryKey (String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}



