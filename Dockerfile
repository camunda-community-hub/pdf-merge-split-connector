FROM camunda/connectors:8.9.1

# Connector JARs go in /opt/custom/ (loader.path for Spring Boot PropertiesLauncher).
# In 8.8.x and earlier, /opt/app/ worked; the 8.9.x base image changed this.
COPY target/pdf-merge-split-connector-1.4.0.jar /opt/custom/

# The base image will automatically pick up the connector
