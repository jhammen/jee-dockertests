
FROM ghcr.io/eclipse-ee4j/glassfish:7.0.18

COPY ${example.file} /opt/glassfish7/glassfish/domains/domain1/autodeploy

EXPOSE 4848 8080 8181
