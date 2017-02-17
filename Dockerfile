FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/cledgers.luminus.jar /cledgers.luminus/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/cledgers.luminus/app.jar"]
