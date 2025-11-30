# Uptime SSL Monitoring System Backend

## Backend Endpoint  
    - https://uptime-ssl-monitoring-system-backend.onrender.com/

## Description  
    The System monitors the health of domains and their SSL certificates.  
    This is useful to ensure services are available and secure.  

##  Deliverables  
    1. Architectural Diagram - system_diagrams/  
    2. Database ERD diagram - system_diagrams/  
    3. API's Postman collection - postman_collection/

## System Setup Guidelines  
    Clone the repository.  
### Prerequisites  
    1. You have a postgres database installed.  
    2. You have Java and JVM installed.  
    3. You have maven installed.  
### Configuration Files  
    Create a directory named sys.conf/ at the home of the project, at the same level as the src/ dir.  
#### Setting Up conf.xml  
    1. Create a conf.xml file in the sys.conf/ directory.  
    2. Copy the XML configurations below into the file.  
    3. Configure the items in the '{}' to your specifications.  

    ```xml
        <?xml version="1.0" encoding="UTF-8"?>
        <API>
            <UNDERTOW>
                <PORT REST="{rest_api_port}" />
                <HOST REST="{rest_api_host}" />
                <BASE_PATH PORTAL="" REST="/api" />
            </UNDERTOW>
            <DB>
                <HOST>{database_host}</HOST>
                <PORT>{database_port}</PORT>
                <DATABASE_NAME TYPE="PLAINTEXT">uptime_ssl_monitoring_system_db</DATABASE_NAME>
                <USERNAME TYPE="PLAINTEXT">{database_user}</USERNAME>
                <PASSWORD TYPE="PLAINTEXT">{database_password}</PASSWORD>
                <SHOW_SQL>false</SHOW_SQL>
            </DB>
            <SMTP>
                <APP_NAME TYPE="PLAINTEXT">{application_name}</APP_NAME>
                <DOMAIN TYPE="PLAINTEXT">{smptp_url}</DOMAIN>
                <PORT TYPE="PLAINTEXT">{smtp_server_port}</PORT>
                <EMAIL TYPE="PLAINTEXT">{youremail@example.com}</EMAIL>
                <APP_PASSWORD TYPE="PLAINTEXT">{google_app_password}</APP_PASSWORD>
            </SMTP>
            <FRONTEND>
                <DOMAIN TYPE="PLAINTEXT">{frontend_server_domain}</DOMAIN>
                <REGISTER_USER_ENDPOINT TYPE="PLAINTEXT">/auth/registration</REGISTER_USER_ENDPOINT>
            </FRONTEND>
            <DEFAULT>
                <ADMIN_EMAIL TYPE="PLAINTEXT">default.system@ssldom.com</ADMIN_EMAIL>
            </DEFAULT>
        </API>
    ```
#### Create db.properties  
    - Create a db.properties file in the sys.conf/ directory.  

### Database Setup  
    1. Find the ddl and insert SQL files in the sql_queries/ directory.  
    2. Run the scripts in your database, with the DDL run first, then the INSERT.  

### Run the Server  
    1. Open your terminal and cd to the home (the dir with the src/ dir) of the java project.  
    2. Run the following commands.  
        - mvn compile  
        - mvn exec:java -Dexec.mainClass="com.monitoringsystem.Main"  

    Your server should be running at this point.  
    Cheers.  

    Default Credentials:
        - default.system@ssldom.com:Password123