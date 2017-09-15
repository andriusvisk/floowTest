# The Challenge
The functional goal of the challenge is to analyse a file with a large body of text and
to count unique words so that the most common and least common words can be
identified. The system is designed for scaling, just run another instance.

Requirements: Java 8

**How to install**

git clone https://github.com/andriusvisk/floowTest.git

cd floowTest

mvn clean install

java -Xmx500m -jar target/floowTest.jar  -m localhost:27017 -s /finfull/path/to/dump.xml -u user1 -p password1

Look at output for line "Use link to track system - http://localhost:36380", paste the link to chrome. 
The port picks in random, just for testing purposes, running different instances on one server.

**Brief description**

I decided to use spring boot and freemarker. The application starts two threads, first - "worker", makes data processing, 
the other - makes "pings" to mongoDB every 2s, to show others, that this instance is working. The master is the oldest active instance (with ping time out 30s), whick sends jobs for 
slaves (just saves records to mongo). The whole job is divided into small jobs (100 lines of text).
One record has just instructions what slave must to do - two indexes - the starting line number and last line number to read from file. Every worker reads from the file by him self.










