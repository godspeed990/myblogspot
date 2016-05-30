#!/bin/sh

mkdir -p deploy/target
cd deploy
cp ../target/myblogspot-0.0.1-SNAPSHOT.jar target
cp ../src/main/docker/Dockerfile .

docker build -t rtv2222/blogspot . 
if [ $? == 0 ]
then
   docker run -i -p 8999:8999 rtv2222/blogspot &  
else
   echo "Failed to deploy and run docker instance of myblogspot"
   exit 1
fi
