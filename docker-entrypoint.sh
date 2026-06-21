#!/usr/bin/env sh
set -eu

APP_PORT="${PORT:-8080}"
sed -i "s/port=\"8080\" protocol=\"HTTP\/1.1\"/port=\"${APP_PORT}\" protocol=\"HTTP\/1.1\"/" /usr/local/tomcat/conf/server.xml
exec catalina.sh run
