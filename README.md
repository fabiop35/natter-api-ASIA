
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

#Create user
curl -d '{"username":"demo","password":"password"}' -H 'Content-Type: application/json' http://localhost:4567/users

curl -d '{"username":"demo","password":"password"}' -H 'Content-Type: application/json' http://localhost:4567/users -v

#Create a Space
curl -u demo:password -d '{"name":"test space","owner":"demo"}' -H 'Content-Type: application/json' http://localhost:4567/spaces


#Request using HTTPS
#prerequisite: mkcert (mkcert -install, mkcert -pkcs12 localhost

curl --cacert "$(mkcert -CAROOT)/rootCA.pem" -d '{"username":"demo","password":"password"}' -H 'Content-Type: application/json' https://localhost:4567/users

#View audit logs
curl pem https://localhost:4567/logs | jq

#Check Access Control
1. Register a User
curl --cacert "$(mkcert -CAROOT)/rootCA.pem" -d '{"username":"demo","password":"password"}' -H 'Content-Type: application/json' https://localhost:4567/users -v

2. Create a Space
curl --cacert "$(mkcert -CAROOT)/rootCA.pem" -d '{"name":"demo2 space","owner":"demo"}' -H 'Content-Type: application/json' -u demo:password https://localhost:4567/spaces -v

3. #Post message
curl --cacert "$(mkcert -CAROOT)/rootCA.pem" -d '{"author":"demo","message":"My first message"}' -H 'Content-Type: application/json' -u demo:password https://localhost:4567/spaces/1/messages -v

#Read messages
curl -i -u demo:password https://localhost:4567/spaces/1/messages/1

curl --cacert "$(mkcert -CAROOT)/rootCA.pem" -u demo:password https://localhost:4567/spaces/1/messages/1

#Adding users to spaces
curl -u demo:password -H 'Content-Type: application/json' -d '{"username":"demo2","permissions":"r"}' https://localhost:4567/spaces/1/members

-check
curl -u demo2:password https://localhost:4567/spaces/1/messages/1


#Privilege scalation attacks
curl  -H 'Content-Type: application/json' -d '{"username":"evildemo2","password":"password"}' https://localhost:4567/users

curl -u demo2:password -H 'Content-Type: application/json' -d '{"username":"evildemo2","permissions":"rwd"}' https://localhost:4567/spaces/1/members

curl -i -X DELETE -u evildemo2:password https://localhost:4567/spaces/1/messages/1

#Get a new session cookie
curl -H 'Content-Type: application/json' -d '{"username":"test","password":"password"}' http://localhost:4567/users

curl -i -u test:password -H 'Content-Type: application/json' -X POST http://localhost:4567/sessions

#Validating session cookie
curl -H 'Content-Type: application/json' -d '{"username":"test","password":"password"}' http://localhost:4567/users

 curl -i -c ~/cookies -u test:password -H 'Content-Type: application/json' -X POST http://localhost:4567/sessions

curl -b ~/cookies -H 'Content-Type: application/json' -d '{"name":"test space","owner":"test"}' http://localhost:4567/spaces

#CSRF Protection Test
curl -H 'Content-Type: application/json' -d '{"username":"test","password":"password"}' http://localhost:4567/users

curl -i -c ~/cookies -u test:password -H 'Content-Type: application/json' -X POST http://localhost:4567/sessions

curl -i -b ~/cookies -H 'Content-Type: application/json' -H 'X-CSRF-Token: gB7CiKkxx0FFsR4lhV9hsvA1nyT7Nw5YkJw_ysMm6ic' -d '{"name":"test space","owner":"test"}' http://localhost:4567/spaces

###Replace Cookies With Tokens
# 1. Create a user
curl -H 'Content-Type: application/json' -d '{"username":"test","password":"password"}' http://localhost:4567/users

# 2. call the login endpoint to obtain a session token
curl -i -H 'Content-Type: application/json' -u test:password -X POST http://localhost:4567/sessions

# 3. Create a Space, pass the token
curl -i -H 'Content-Type: application/json' -H 'X-CSRF-Token: ' -d '{"name":"test","owner":"test"}' http://localhost:4567/spaces

# Test Bearer token
curl -i -H 'Content-Type: application/json' -H 'Authorization: Bearer ' -d '{"name":"test","owner":"test"}' http://localhost:4567/spaces

###JWT
#Generating a 256-bit HMAC KEY (Create a keystore)
keytool -genseckey -keyalg HmacSHA256 -keysize 256 -alias hmac-key -keystore keystore.p12 -storetype PKCS12 -storepass changeit

#Shows the secret key keeps by the  keystore(keystore.p12)
java -cp target/natter-api-ASIA-1.0.0-SNAPSHOT.jar com.asia.ShowKey

#System property with the keystore password
-Dkeystore.password=changeit

#Start MariaDB
cd '/data/data/com.termux/files/usr' ; /data/data/com.termux/files/usr/bin/mariadbd-safe --datadir='/data/data/com.termux/files/usr/var/lib/mysql'


#git ssh connection
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_rsa
ssh -T git@github.com




