FROM alpine:3.15.4

ENV CLAMAV_VER=0.104.3-r0 \
    JRE_VER=11.0.15_p10-r0 \
    RWX=750

RUN sed -i "s|http://dl-cdn.alpinelinux.org|https://nexus.nonprod.dwpcloud.uk/repository|" /etc/apk/repositories \
    && apk --no-cache add clamav=$CLAMAV_VER clamav-libunrar=$CLAMAV_VER openjdk11-jre=$JRE_VER \
    && mkdir /run/clamav \
    && chown clamav:clamav /run/clamav \
    && sed -i 's/^#Foreground .*$/Foreground true/g' /etc/clamav/clamd.conf \
    && sed -i 's/^#TCPSocket .*$/TCPSocket 3310/g' /etc/clamav/clamd.conf \
    && sed -i 's/^#Foreground .*$/Foreground true/g' /etc/clamav/freshclam.conf \
    && sed -i 's/^DatabaseMirror .*$/DatabaseMirror https:\/\/dwp.gitlab.io\/engineering\/clamav-mirror/g' /etc/clamav/freshclam.conf \
    && chown clamav:clamav /var/run/clamav  \
    && chmod $RWX /var/run/clamav \
    && freshclam --show-progress \
    && chown clamav:clamav /var/lib/clamav/*.cvd

COPY target/clamav-file-lander-*.jar /client.jar

RUN chmod $RWX /client.jar
VOLUME ["/var/lib/clamav"]
EXPOSE 3310
EXPOSE 8080
COPY config/entrypoint.sh /
RUN chmod $RWX /entrypoint.sh
CMD ["/entrypoint.sh"]
