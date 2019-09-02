FROM maven:3.6.1-slim as builder

RUN mkdir /workdir

WORKDIR /workdir

COPY . .

RUN cd src/main/resources/config/ && \
    cp course_config.properties.example course_config.properties && \
    cp db_config.properties.example db_config.properties && \
    cp gitlab_config.properties.example gitlab_config.properties && \
    cp jenkins_config.properties.example jenkins_config.properties && \
    cd /workdir && mvn -T 1C clean package -DskipTests

FROM tomcat

ARG DEBUG=false
ENV DEBUG=$DEBUG

RUN echo $DEBUG

RUN if [ ${DEBUG} = true ]; then \
    rm -rf /usr/local/tomcat/webapps/ROOT /usr/local/tomcat/webapps/ROOT.war \
    ;else \
    rm -rf /usr/local/tomcat/webapps/ && mkdir /usr/local/tomcat/webapps/ \
    ;fi

COPY --from=builder /workdir/target/ProgEdu.war /usr/local/tomcat/webapps/ROOT.war

