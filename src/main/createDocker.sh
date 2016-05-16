mkdir -p deploy/target
cd deploy
sudo cp target/myblogspot-0.0.1-SNAPSHOT.jar target
sudo docker build -t rtv2222/blogspot .
sudo docker run -t -i -p 8999:8999 --link some-mongo:mongo rtv2222/blogspot
