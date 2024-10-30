 
 FROM quay.io/wildfly/wildfly:33.0.0.Final-jdk17
 
 COPY ${example.file} /opt/jboss/wildfly/standalone/deployments/
