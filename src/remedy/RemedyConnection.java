package remedy;

import com.bmc.arsys.api.ARServerUser;

public class RemedyConnection {

    /**
     * This class established connection to Remedy by use of {@link ARServerUser} Remedy Api class.
     * Private field keep credentials from Registry.
     */
    private final String connPoint = RemedyCredentials.getRemedyCredentials().getConnPoint();
    private final String user = RemedyCredentials.getRemedyCredentials().getUser();
    private final String password = RemedyCredentials.getRemedyCredentials().getPassword();
    private final Integer port = Integer.parseInt(RemedyCredentials.getRemedyCredentials().getPort());

    private final ARServerUser server;

    private static final RemedyConnection REMEDY_CONNECTION = new RemedyConnection();

    private RemedyConnection() {
        this.server = new ARServerUser(user, password, "", connPoint, port);
    }

    /**
     *
     * @return singleton object of this class.
     */
    public static ARServerUser getServer() {
        return REMEDY_CONNECTION.server;
    }
}
