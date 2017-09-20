# vegas-speed-warning-nifi
NiFi Processor to calculate/issue speed warning to move vehicles based on GPS/speed data

1. git clone https://github.com/pinkdevelops/vegas-speed-warning-nifi
2. cd vegas-speed-warning-nifi
3. mvn clean install -DskipTests
4. cp vegas-speed-warning-nifi/target/nifi-speed-warning-nar-1.0-SNAPSHOT.nar $NIFI_HOME/lib
5. Restart NiFi
