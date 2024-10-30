
xdg-open test-${server.name}.html

docker run -p ${server.port}:${server.port} dockertest/${example.name}:${server.name} 
