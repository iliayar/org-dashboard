FROM gradle

COPY *.gradle gradle.* gradlew /var/www/app/
COPY gradle /var/www/app/gradle

WORKDIR /var/www/app

RUN gradle

COPY . .

RUN gradle --no-daemon build
