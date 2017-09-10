package com;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;

import java.util.UUID;

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
    private final String mongoWordStatCol = "wordsStatistics";
    private final String mongoChunksCol = "chunks";
    private final String mongoRunnersCol = "runners";
    private final String mongoElectionQueueCol = "electionQueue";
    private String mongoUsername = "";
    private String mongoPassowrd = "";


    final int keepAlivePingTimeStepInS = 2;

    final int chunkOfLinesSize = 100;

    public Parameters(String[] args) {

        Options options = new Options();

        Option sourceFileOpt = new Option("s", "source", true, "source file");
        sourceFileOpt.setRequired(true);
        options.addOption(sourceFileOpt);

        Option mongoOpt = new Option("m", "mongo", true, "[hostname]:[port]");
        mongoOpt.setRequired(true);
        options.addOption(mongoOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            this.sourceFileStr = cmd.getOptionValue("source");
            String mongoPar = cmd.getOptionValue("mongo");
            if ((mongoPar.indexOf(":") < 0)||
                    (mongoPar.indexOf(":")==0)||
                    (!mongoPar.substring(mongoPar.indexOf(":")+1).matches("\\d+"))) throw new ParseException("Bad mongo host:port parameter");
            this.mongoPort = Integer.parseInt(mongoPar.substring(mongoPar.indexOf(":")+1));
            this.mongoHost = mongoPar.substring(0, mongoPar.indexOf(":"));

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

    public String getMongoChunksCol() {
        return mongoChunksCol;
    }

    public String getMongoWordStatCol() {
        return mongoWordStatCol;
    }

    public String getMongoRunnersCol() {
        return mongoRunnersCol;
    }

    public String getMongoElectionQueueCol() {
        return mongoElectionQueueCol;
    }

    public int getKeepAlivePingTimeStepInS() {
        return keepAlivePingTimeStepInS;
    }
}
