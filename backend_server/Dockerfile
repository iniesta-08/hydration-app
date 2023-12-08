# Use Ubuntu as the base image
FROM ubuntu:latest

# Update package lists and install necessary packages
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean

# Set the working directory in the container
WORKDIR /app

# Copy the Flask application files into the container
COPY . /app

# Install Flask and other dependencies
RUN pip3 install -r requirements.txt

EXPOSE 5002

CMD ["python3", "server.py"]
