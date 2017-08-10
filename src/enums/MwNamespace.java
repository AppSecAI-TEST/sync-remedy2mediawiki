package enums;

public enum MwNamespace {
    MW_BMC_NAMESPACE("BMC");

    private final String name;

    MwNamespace(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
