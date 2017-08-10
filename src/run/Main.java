package run;

import java.util.Timer;

public class Main {
    /**
     * This main method do cyclically.
     * It run {@link MainTask} by schedule.
     * @param args The arguments from NSSM wrapper witch run in SDKnowledge server.
     *
     */
    public static void main(String[] args) {
//        final String queryArg1 = args[0];
//        final String queryArg1Val = args[1];
//        final String pathContentFiles = args[2];
//        final Integer minutes = Integer.parseInt(args[3]);
//        final String pathMwRefreshLinks = args[4];
       final String queryArg1 = "Request ID";
        final String queryArg1Val = "KBA000000005125";
        final String pathContentFiles = "E:\\Content File";
        final Integer minutes = 5;
        final String pathMwRefreshLinks = ".\\";
//        final String queryArg1 = "Owner_Organization";
//        final String queryArg1Val = "SERVICE DESK";
        Timer timer = new Timer();
        timer.schedule(new MainTask(pathContentFiles, queryArg1, queryArg1Val, pathMwRefreshLinks), 0, 1000 * 60 * minutes);
    }
}

