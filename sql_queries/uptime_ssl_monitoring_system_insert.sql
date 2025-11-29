
INSERT INTO roles (role_title) VALUES ('ADMIN'), ('OPERATOR'), ('VIEWER');
INSERT INTO notification_channels (channel_title) VALUES ('EMAIL'), ('TELEGRAM'), ('SMS');
INSERT INTO service_diagnosis_methods (diagnosis_method) VALUES ('PING'), ('REQUEST'), ('PORT_SCAN'), ('HEAD'), ('OPTIONS'),('TLS_HANDSHAKE');
INSERT INTO certificate_status (cert_health_status) VALUES ('VALID'), ('WATCH'), ('EXPIRED'), ('TERMINAL');
INSERT INTO service_status (svc_health_status) VALUES ('UP'), ('DOWN');
INSERT INTO notification_triggers (notification_trigger) VALUES ('SVC_DOWN'), ('SSL_CERT_EXPIRY'), ('CONFIG_CHANGE'), ('REG_NOTIFICATION');

INSERT INTO system_users (user_id, role_id, channel_id, email_address, tel, telegram, first_name, middle_name, surname, password, status)
VALUES ('Cj7KUtfyhWvrwi1cEfuU2DizWZ-dJ9-ATFaAJT2Yniw', 1, 1, 'default.system@ssldom.com', 'null', 'null', 'system', 'system', 'system', 
'804f50ddbaab7f28c933a95c162d019acbf96afde56dba10e4c7dfcfe453dec4bacf5e78b1ddbdc1695a793bcb5d7d409425db4cc3370e71c4965e4ef992e8c4', 'ACTIVE');

INSERT INTO user_auth_tokens (email_address, auth_token)
VALUES ('default.system@ssldom.com', 'local:qfpLcihCrB_C1zFtTjygWbt-QK0rDqYyX8V98PAHMPo')