FROM gradle

RUN apt-get update \
	&& apt-get -y install netcat

COPY *.gradle gradle.* gradlew /usr/app/
COPY gradle /usr/app/gradle

WORKDIR /usr/app

RUN gradle --no-daemon --version

COPY . .

RUN gradle --no-daemon build

ENTRYPOINT ["/usr/app/entrypoint.sh"]
