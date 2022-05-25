#!/bin/sh

sed -i 's/^#HTTPProxyServer .*$/HTTPProxyServer http:\/\/proxy.public.'"$HTTP_PROXY"'/g' /etc/clamav/freshclam.conf
sed -i 's/^#HTTPProxyPort .*$/HTTPProxyPort '"$HTTP_PROXY_PORT"'/g' /etc/clamav/freshclam.conf

freshclam -d &
clamd &
java -jar client.jar &

pids=$(jobs -p)

exitcode=0

terminate() {
    for pid in $pids; do
        if ! kill -0 "$pid" 2>/dev/null; then
            wait "$pid"
            exitcode=$?
        fi
    done
    kill "$pids" 2>/dev/null
}

trap terminate CHLD
wait

exit "$exitcode"
