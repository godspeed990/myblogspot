# myblogspot
cmad blog spot 

To build a docker image

sudo docker build -f src/main/docker/Dockerfile -t  rtv2222/blogspot .

To run the image

sudo docker run -t -i -p 8999:8999 rtv2222/blogspot

sudo docker run -t -i -p 8999:8999 --link some-mongo:mongo rtv2222/blogspot 

with sudo docker run --name some-mongo -d mongo running
