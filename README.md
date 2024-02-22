
#Create the project
mvn archetype:generate -DgroupId=com.asia -DartifactId=natter-api-ASIA -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false

#Post Request
curl -i -d '{"name": "test space", "owner": "demo"}' http://localhost:4567/spaces

#Post requests with SQL Injection
curl -i -d "{\"name\": \"test'space\",\"owner\":\"demo\" }" http://localhost:4567/spaces

curl -i -d "{\"name\": \"test\", \"owner\":\"'); DROP TABLE spaces; --\"}" http://localhost:4567/spaces 
