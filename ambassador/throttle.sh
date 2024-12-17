for i in {1..1000}
do
    # Use curl to get the headers and response code
    response=$(curl -s -o /dev/null -w "%{http_code}" -D - http://localhost:8086/service)

    # Print the iteration and HTTP status code
    echo "Response $i: $response"

    # Add a separator for readability
    echo "-----------------------------------------"
done
