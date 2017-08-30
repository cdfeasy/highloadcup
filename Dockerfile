FROM openjdk:8-jdk-alpine
COPY target/highloadcup-1.0-jar-with-dependencies.jar /home/highloadcup-1.0-jar-with-dependencies.jar
#COPY highloadcup-1.0-jar-with-dependencies.jar c:/tmp/highloadcup-1.0-jar-with-dependencies.jar
ENV JAVA_OPTS=""
EXPOSE 80
#CMD ["java","-jar","/home/highloadcup-1.0-jar-with-dependencies.jar /tmp/data/data.zip"]
CMD ["sh", "-c", "java -jar /home/highloadcup-1.0-jar-with-dependencies.jar /tmp/data/data.zip /tmp/data/options.txt"]
#CMD ["java","-jar","c:/tmp/highloadcup-1.0-jar-with-dependencies.jar D:\work\highloadcup\src\main\resources\data.zip"]