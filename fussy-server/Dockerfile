
# Use the Azul Zulu JDK 22 base image
FROM azul/zulu-openjdk:22

# Copy the compiled Main.class file into the Docker image
COPY *.class /app/

# Set the working directory to /app
WORKDIR /app

# Specify the command to run the Java application
ENTRYPOINT ["java", "Main"]
