FROM quay.io/debezium/connect-base:3.6

ARG DEBEZIUM_VERSION=3.6.2.Final
ARG POSTGRES_MD5=d25dd1020992ea679f2938fb6d7a9dd6

RUN docker-maven-download debezium postgres "$DEBEZIUM_VERSION" "$POSTGRES_MD5"
