# Use an official Java runtime as a parent image
FROM openjdk:17

# Set the working directory in the container
WORKDIR /usr/src/myapp

# Copy the .jar file into the container at /usr/src/myapp
COPY demo-jvm-new-method.jar /usr/src/myapp/demo-jvm-all.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run your-application.jar when the container launches
CMD ["java", "-jar", "demo-jvm-all.jar", "-Dorg.slf4j.simpleLogger.defaultLogLevel=error"]

