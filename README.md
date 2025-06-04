# Description


# Setup

```
git clone https://github.com/alexvasseur/redlettuce.git
cd redlettuce
```

Make sure you update the application.properties with the values you need.

You can also set values directly in the command line (shown below)

# Run the application
```
mvn clean package
java -jar target/bms-lettuce-1.0.0.jar
```

This will start the application with host,port,password from application.properties
By default it will start with 10 threads.

To run with specific number of threads e.g. 4
```
mvn clean package
java -jar target/bms-lettuce-1.0.0.jar 4
```

To run with host/port/password from command line
```
export R_HOST=<host>
export R_PORT=<port>
export R_PASSWORD=<password>
mvn clean package
java -jar target/bms-lettuce-1.0.0.jar --spring.data.redis.host=$R_HOST --spring.data.redis.port=$R_PORT --spring.data.redis.password=$R_PASSWORD
```
