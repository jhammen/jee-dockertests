
FROM icr.io/appcafe/open-liberty:23.0.0.3-kernel-slim-java17-openj9-ubi

COPY --chown=1001:0  ol23-server.xml /config/server.xml

RUN features.sh

COPY --chown=1001:0 ${warfile} /config/dropins/

RUN configure.sh