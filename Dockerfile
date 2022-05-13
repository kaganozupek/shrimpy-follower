FROM zenika/kotlin
WORKDIR /usr/src/app
COPY . .
RUN ./gradlew clean
RUN ./gradlew assemble
CMD ./gradlew run