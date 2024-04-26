import yaml
import sys

service_name = "app"
image_name = "demo-jvm-all"
start_port = 8081

if len(sys.argv) != 2:
    print("usage: python3 generate_yaml.py <number_of_services>");
else:
    instances = int(sys.argv[1])  
    docker_compose_dict = {
        "version": "3.7",
        "services": {}
    }

    for i in range(instances):
        service_key = f"{service_name}_instance{i+1}"
        port_mapping = f"{start_port+i}:8080"
        docker_compose_dict["services"][service_key] = {
            "image": image_name,
            "ports": [port_mapping],
            "extra_hosts": ["host.docker.internal:host-gateway"]
        }

    # Now let's write this dictionary to a docker-compose.yml file
    yaml_file_path = "./docker-compose.yml"



    
    with open(yaml_file_path, "w") as file:
        yaml.dump(docker_compose_dict, file, default_flow_style=False)

    yaml_file_path
