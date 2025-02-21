FROM alpine:3.19

ENV CLAMAV_VER=1.2.2-r0 \
    JRE_VER=11.0.26_p4-r0 \
    RWX=750

# initially set the database mirror to pull from the web and then set the mirror to be internal as the default
RUN apk --no-cache add clamav=$CLAMAV_VER clamav-libunrar=$CLAMAV_VER openjdk11-jre=$JRE_VER \
    && mkdir /run/clamav \
    && chown clamav:clamav /run/clamav \
    && sed -i 's/^#Foreground .*$/Foreground true/g' /etc/clamav/clamd.conf \
    && sed -i 's/^#TCPSocket .*$/TCPSocket 3310/g' /etc/clamav/clamd.conf \
    && sed -i 's/^#Foreground .*$/Foreground true/g' /etc/clamav/freshclam.conf \
    && chown clamav:clamav /var/run/clamav  \
    && chmod $RWX /var/run/clamav \
    && freshclam --debug --verbose \
    && chown clamav:clamav /var/lib/clamav/*.cvd \
    && sed -i 's/^DatabaseMirror .*$/DatabaseMirror https:\/\/dwp.gitlab.io\/engineering\/clamav-mirror/g' /etc/clamav/freshclam.conf

COPY target/clamav-file-lander-*.jar /client.jar

COPY --from=pik94420.live.dynatrace.com/linux/oneagent-codemodules-musl:java / /
ENV LD_PRELOAD /opt/dynatrace/oneagent/agent/lib64/liboneagentproc.so

RUN chmod $RWX /client.jar
VOLUME ["/var/lib/clamav"]
EXPOSE 3310
EXPOSE 8080
COPY config/entrypoint.sh /
RUN chmod $RWX /entrypoint.sh
CMD ["/entrypoint.sh"]
