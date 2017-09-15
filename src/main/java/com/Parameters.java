package com;

import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by andrius on 08/09/2017.
 */
final public class Parameters {

    private String sourceFileStr;

    private final String myId = UUID.randomUUID().toString();

    //Mongo DB
    private String mongoHost;
    private int mongoPort;
    private final String mongoDatabase = "wordsDb";
    private String mongoUsername = "";
    private String mongoPassowrd = "";

    final int keepAlivePingTimeStepInS = 2;
    final int readingPauseInS = 2;
    final int runnerTimeOutInS = 30;

    final int chunkOfLinesSize = 100;
    final int mastersChunkBufferSize = 100;

    public Parameters(String[] args) {

        Options options = new Options();

        Option sourceFileOpt = new Option("s", "source", true, "source file");
        sourceFileOpt.setRequired(true);
        options.addOption(sourceFileOpt);

        Option mongoOpt = new Option("m", "mongo", true, "[hostname]:[port]");
        mongoOpt.setRequired(true);
        options.addOption(mongoOpt);

        Option usernameOpt = new Option("u", "user", true, "username");
        usernameOpt.setRequired(true);
        options.addOption(usernameOpt);

        Option passwordOpt = new Option("p", "password", true, "password");
        passwordOpt.setRequired(true);
        options.addOption(passwordOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            List<String> argsL = Arrays.stream(args).filter(p->p.toLowerCase().indexOf("-xmx")!=0).collect(Collectors.toList());
            args = argsL.toArray(new String[argsL.size()]);

            cmd = parser.parse(options, args);

            this.sourceFileStr = cmd.getOptionValue("source");
            String mongoPar = cmd.getOptionValue("mongo");
            if ((mongoPar.indexOf(":") < 0)||
                    (mongoPar.indexOf(":")==0)||
                    (!mongoPar.substring(mongoPar.indexOf(":")+1).matches("\\d+"))) throw new ParseException("Bad mongo host:port parameter");
            this.mongoPort = Integer.parseInt(mongoPar.substring(mongoPar.indexOf(":")+1));
            this.mongoHost = mongoPar.substring(0, mongoPar.indexOf(":"));
            mongoUsername = cmd.getOptionValue("user");
            mongoPassowrd = cmd.getOptionValue("password");

            System.setProperty("mongoHost", String.valueOf(mongoHost));
            System.setProperty("mongoPort", String.valueOf(mongoPort));
            System.setProperty("mongoDatabase", mongoDatabase);
            System.setProperty("mongoUsername", mongoUsername);
            System.setProperty("mongoPassword", mongoPassowrd); // it's not good :)
            System.setProperty("myUUID", myId);

            int stop = 0;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -Xmx8192m -jar floowTest.jar", options);
            System.exit(1);
            return;
        }
    }

    public String getSourceFileStr() {
        return sourceFileStr;
    }

    public String getMongoHost() {
        return mongoHost;
    }

    public int getMongoPort() {
        return mongoPort;
    }

    public String getMongoUsername() {
        return mongoUsername;
    }

    public String getMongoPassowrd() {
        return mongoPassowrd;
    }

    public String getMongoDatabase() {
        return mongoDatabase;
    }

    public String getMyId() {
        return myId;
    }

    public int getChunkOfLinesSize() {
        return chunkOfLinesSize;
    }

    public int getKeepAlivePingTimeStepInS() {
        return keepAlivePingTimeStepInS;
    }

    public int getRunnerTimeOutInS() {
        return runnerTimeOutInS;
    }

    public int getReadingPauseInS() {
        return readingPauseInS;
    }

    public int getMastersChunkBufferSize() {
        return mastersChunkBufferSize;
    }
}
