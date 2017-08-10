package enums;

public enum FormName {

        KB_KNOWLEDGE_ARTICLE_MANAGER("RKM:KnowledgeArticleManager"),
        KB_REFERENCE_TEMPLATE("RKM:ReferenceTemplate"),
        KB_HOW_TO_TEMPLATE("RKM:HowToTemplate"),
        KB_KNOWN_ERROR_TEMPLATE("RKM:KnownErrorTemplate"),
        KB_DECISION_TREE_TEMPLATE("RKM:DecisionTreeTemplate"),
        KB_PROBLEM_SOLUTION_TEMPLATE("RKM:ProblemSolutionTemplate");

        private final String name;

        FormName (String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
}
