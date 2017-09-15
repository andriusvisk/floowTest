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








