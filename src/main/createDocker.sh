mkdir -p deploy/target
cd deploy
cp ../target/myblogspot-0.0.1-SNAPSHOT.jar target
cp ../src/main/docker/Dockerfile .
docker build -t rtv2222/blogspot .
docker run -t -i -p 8999:8999 rtv2222/blogspot
