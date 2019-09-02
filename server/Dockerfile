FROM maven:3.6.1-slim as builder

RUN mkdir /workdir

WORKDIR /workdir

COPY . .

ARG WEB_API_BASEURL=
ENV WEB_API_BASEURL=$WEB_API_BASEURL

RUN apt-get update \
    && apt-get -y install gettext-base \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && envsubst < front/src/environments/environment.docker.ts > front/src/environments/environment.prod.ts

RUN mvn -T 1C clean package -DskipTests

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
