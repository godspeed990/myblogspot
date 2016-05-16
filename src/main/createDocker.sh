mkdir -p deploy/target
cd deploy
cp target/myblogspot-0.0.1-SNAPSHOT.jar target
docker build -t rtv2222/blogspot .
docker run -t -i -p 8999:8999 --link some-mongo:mongo rtv2222/blogspot
