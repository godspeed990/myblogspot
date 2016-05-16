# myblogspot
cmad blog spot 

To build a docker image

sudo docker run -t -i -p 8999:8999 rtv2222/blogspot

sudo docker build -f src/main/docker/Dockerfile -t  rtv2222/blogspot .

To run the image

sudo docker run -t -i -p 8999:8999 rtv2222/blogspot
