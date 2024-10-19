FROM maven:3.9.6-amazoncorretto-17 AS maven
LABEL COMPANY="Flint Innovative Solutions"
LABEL MAINTAINER="dkurin@swiftlet.technology"
LABEL APPLICATION="Flint Eats Server App"

WORKDIR /usr/src/app
ADD pom.xml /usr/src/app/
RUN mvn dependency:resolve
#RUN mvn dependency:resolve-plugins
ADD . /usr/src/app
RUN mvn package -Dmaven.test.skip=true -DwarName=eats-1

FROM tomcat:9.0-jdk17-corretto-al2 AS tomcat
ARG TOMCAT_FILE_PATH=/docker 

#Data & Config - Persistent Mount Point
ENV APP_DATA_FOLDER=/var/lib/eats-1
#ENV SAMPLE_APP_CONFIG=${APP_DATA_FOLDER}/config/
	
ENV CATALINA_OPTS="-Xms1024m -Xmx4096m -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m -Xss512k"

#RUN yum install -y dos2unix

#Move over the War file from previous build step
WORKDIR /usr/local/tomcat/webapps/
COPY --from=maven /usr/src/app/target/eats-1.war /usr/local/tomcat/webapps/eats-1.war

#COPY ${TOMCAT_FILE_PATH}/* ${CATALINA_HOME}/conf/

WORKDIR $APP_DATA_FOLDER

ADD ./loadSecrets.sh /usr/local/tomcat/bin
#RUN dos2unix /usr/local/tomcat/bin/loadSecrets.sh
RUN ["chmod", "+x", "/usr/local/tomcat/bin/loadSecrets.sh"]

EXPOSE 8080
ENTRYPOINT ["/usr/local/tomcat/bin/loadSecrets.sh"]