# ===== Stage 1: compile Java sources (thay cho NetBeans/Ant build) =====
FROM tomcat:9.0-jdk17-temurin AS build
WORKDIR /app

COPY src ./src
COPY web ./web

# Driver SQL Server moi (sqljdbc4.jar cu khong ho tro TLS 1.2 cua cloud DB)
ADD https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/12.8.1.jre11/mssql-jdbc-12.8.1.jre11.jar /tmp/mssql-jdbc.jar

RUN mkdir -p out \
 && cp -r web/. out/ \
 && rm -f out/WEB-INF/lib/sqljdbc4.jar out/META-INF/context.xml \
 && cp /tmp/mssql-jdbc.jar out/WEB-INF/lib/ \
 && mkdir -p out/WEB-INF/classes \
 && find src/java -name "*.java" > sources.txt \
 && javac -encoding UTF-8 -nowarn \
      -cp "$CATALINA_HOME/lib/*:out/WEB-INF/lib/*" \
      -d out/WEB-INF/classes @sources.txt \
 && cd src/java && find . -type f ! -name "*.java" -exec cp --parents {} /app/out/WEB-INF/classes/ \;

# ===== Stage 2: runtime Tomcat =====
FROM tomcat:9.0-jdk17-temurin
ENV TZ=Asia/Ho_Chi_Minh
RUN rm -rf $CATALINA_HOME/webapps/*
COPY --from=build /app/out $CATALINA_HOME/webapps/ROOT

EXPOSE 8080
# Render cap PORT qua bien moi truong -> doi cong Tomcat cho khop
CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT:-8080}\\\"/\" $CATALINA_HOME/conf/server.xml && catalina.sh run"]
