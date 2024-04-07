
#Create the project
mvn archetype:generate -DgroupId=com.asia -DartifactId=natter-api-ASIA -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false

#run app
mvn clean compile exec:java

#Post Request
curl -i -d '{"name": "test space", "owner": "demo"}' http://localhost:4567/spaces

#Post requests with SQL Injection
curl -i -d "{\"name\": \"test'space\",\"owner\":\"demo\" }" http://localhost:4567/spaces

curl -i -d "{\"name\": \"test\", \"owner\":\"'); DROP TABLE spaces; --\"}" http://localhost:4567/spaces 

#Test rate-limiting
for i in {1..5}
> do
curl -i -d "{\"owner\":\"test\",\"name\":\"space$i\"}" -H 'Content-Type: application/json' http://localhost:4567/spaces
> done

#Authentication
curl -d '{"name":"test space","owner":"demo"}' -H 'Content-Type: application/json' http://localhost:4567/spaces

curl -d '{"username":"demo","password":"password"}' -H 'Content-Type: application/json' http://localhost:4567/users

curl -u demo:password -d '{"name":"test space","owner":"demo"}' -H 'Content-Type: application/json' http://localhost:4567/spaces

