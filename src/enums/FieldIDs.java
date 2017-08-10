package enums;

public enum FieldIDs {
    //fields keys of KAM
    ATRICLE_TITLE("302300502"), //Type Character in BMC
    DOC_ID("302300507"),    //Type Character in BMC
    ARTICLE_REQUEST_ID("302300532"), //Type Character in BMC
    ARTICLE_ID("1"), //Type Character in BMC
    ARTICLE_REQTEMPFORM("302300503"), //Type Character in BMC
    ASSIGNEE_ORG("302300586"), //Type Character in BMC
    ARTICLE_STATUS("302300500"), //Type Character in BMC
    CATEGORIZATION("1000000063"), //Type Character in BMC
    MODIFIED_DATE("6"), //Type Date/Time in BMC
    KSP_URL("1000006030"), //Type Character in BMC
    ARTICLE_BUSINESS_SERVICE("302300530"), //Type Character in BMC
    KEYWORDS("302301262"),

    //field keys of Teml Form
    REFERENCE_TEMPL_REQUEST("302311210"), //Type Character in BMC
    HOW_TO_TEMPL_REQUEST_Q("302311200"), //Type Character in BMC
    HOW_TO_TEMPL_REQUEST_A("302311201"), //Type Character in BMC
    DECISION_TREE_TEMPL_REQUEST("302311212"), //Type Character in BMC
    KNOWN_ERROR_TEMPL_REQUEST_E("302311207"), //Type Character in BMC
    KNOWN_ERROR_TEMPL_REQUEST_RC("302311208"), //Type Character in BMC
    KNOWN_ERROR_TEMPL_REQUEST_WF("302311209"), //Type Character in BMC
    PROBLEM_SOLUTION_TEMPL_REQUEST_P("302311205"), //Type Character in BMC
    PROBLEM_SOLUTION_TEMPL_REQUEST_S("302311206"), //Type Character in BMC
    PS_KE_HT_TEMPLS_REQUEST_TECH_NOTES("302311202"); //Type Character in BMC


    private final String key;

    FieldIDs(String key) {
        this.key = key;
    }
    public Integer getKey() {
        return Integer.parseInt(this.key);
    }
}
