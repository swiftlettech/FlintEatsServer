FROM maven:3.9.1-amazoncorretto-11 as maven
LABEL COMPANY="Flint Innovative Solutions"
LABEL MAINTAINER="dkurin@swiftlet.technology"
LABEL APPLICATION="Flint Eats Server App"

WORKDIR /usr/src/app
ADD pom.xml /usr/src/app/
RUN mvn dependency:resolve
#RUN mvn dependency:resolve-plugins
ADD . /usr/src/app
RUN mvn package -Dmaven.test.skip=true -DwarName=msu

FROM tomcat:9.0-jdk11-corretto-al2 as tomcat
ARG TOMCAT_FILE_PATH=/docker 

#Data & Config - Persistent Mount Point
ENV APP_DATA_FOLDER=/var/lib/msu
#ENV SAMPLE_APP_CONFIG=${APP_DATA_FOLDER}/config/
	
ENV CATALINA_OPTS="-Xms1024m -Xmx4096m -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m -Xss512k"

#Move over the War file from previous build step
WORKDIR /usr/local/tomcat/webapps/
COPY --from=maven /usr/src/app/target/msu.war /usr/local/tomcat/webapps/msu.war

#COPY ${TOMCAT_FILE_PATH}/* ${CATALINA_HOME}/conf/

WORKDIR $APP_DATA_FOLDER

EXPOSE 8080
ENTRYPOINT ["catalina.sh", "run"]