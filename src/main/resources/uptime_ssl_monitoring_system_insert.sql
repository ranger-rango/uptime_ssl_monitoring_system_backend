-- =========================================================================
-- GUARANTEED INSERT SCRIPT (20 RECORDS PER CORE TABLE)
-- Uses only standard INSERT INTO ... VALUES and explicit SELECT subqueries.
-- =========================================================================

-- IMPORTANT: TRUNCATE is included to ensure a clean run. Use cautiously if you
-- have existing data not backed up.
-- =========================================================================
-- 1. LOOKUP TABLES (Static Data) - Satisfies minimum record count with all values
-- =========================================================================

-- Roles (3 records)
INSERT INTO roles (role_title) VALUES ('ADMIN'), ('OPERATOR'), ('VIEWER');
INSERT INTO notification_channels (channel_title) VALUES ('EMAIL'), ('TELEGRAM'), ('SMS');
INSERT INTO service_diagnosis_methods (diagnosis_method) VALUES ('PING'), ('REQUEST'), ('PORT_SCAN'), ('HEAD'), ('OPTIONS'),('TLS_HANDSHAKE');
INSERT INTO certificate_status (cert_health_status) VALUES ('VALID'), ('WATCH'), ('EXPIRED'), ('TERMINAL');
INSERT INTO service_status (svc_health_status) VALUES ('UP'), ('DOWN');
INSERT INTO notification_triggers (notification_trigger) VALUES ('SVC_DOWN'), ('SSL_CERT_EXPIRY'), ('CONFIG_CHANGE'), ('REG_NOTIFICATION');


-- =========================================================================
-- 2. CORE ENTITY TABLES (20 Records Each)
-- =========================================================================

-- Service Info (20 records)
INSERT INTO service_info (service_id, service_name, service_url_domain, service_port, svc_registration_status) VALUES
('svc_001', 'Primary API Prod', 'api.prod.com', 443, 'ACTIVE'),
('svc_002', 'Secondary Web Dev', 'dev.web.net', 80, 'ACTIVE'),
('svc_003', 'Client Portal Prod', 'portal.client.org', 443, 'ACTIVE'),
('svc_004', 'Internal Auth Dev', 'auth.dev.lan', 8443, 'ACTIVE'),
('svc_005', 'Payment Gateway Prod', 'pay.gateway.com', 443, 'ACTIVE'),
('svc_006', 'Logging Service Dev', 'log.dev.net', 9200, 'ACTIVE'),
('svc_007', 'Data Warehouse Prod', 'dw.corp.com', 5432, 'ACTIVE'),
('svc_008', 'CRM Backend Dev', 'crm.dev.lan', 8080, 'ACTIVE'),
('svc_009', 'Marketing Site Prod', 'marketing.com', 443, 'ACTIVE'),
('svc_010', 'Old Legacy Service', 'old.legacy.net', 80, 'TERMINAL'),
('svc_011', 'Microservice A Prod', 'msa.prod.com', 443, 'ACTIVE'),
('svc_012', 'Microservice B Dev', 'msb.dev.net', 8081, 'ACTIVE'),
('svc_013', 'E-commerce API', 'shop.api.com', 443, 'ACTIVE'),
('svc_014', 'Mobile App Backend', 'mobile.backend.net', 443, 'ACTIVE'),
('svc_015', 'CDN Edge Node 1', 'cdn1.example.com', 443, 'ACTIVE'),
('svc_016', 'Monitoring Dashboard', 'monitor.corp.com', 443, 'ACTIVE'),
('svc_017', 'EU Region API', 'eu.api.com', 443, 'ACTIVE'),
('svc_018', 'APAC Region Web', 'apac.web.net', 80, 'ACTIVE'),
('svc_019', 'Internal Proxy 1', 'proxy1.lan', 3128, 'ACTIVE'),
('svc_020', 'Database Service', 'db.internal.lan', 5432, 'ACTIVE');

-- Contact Groups (20 records)
INSERT INTO contact_groups (contact_group_id, group_name, description) VALUES
('grp_001', 'Production On-Call', '24/7 coverage for core services.'),
('grp_002', 'Development Team A', 'Contacts for dev environment alerts.'),
('grp_003', 'Security Operations', 'SSL/TLS configuration change alerts.'),
('grp_004', 'Management Escalation', 'Escalation point for critical outages.'),
('grp_005', 'Network Team', 'Alerts for network-layer issues (PING/Port Scan).'),
('grp_006', 'Database Admins', 'Alerts for internal DB check failures.'),
('grp_007', 'Product Owners', 'Notified of major customer-facing issues.'),
('grp_008', 'Frontend Developers', 'Alerts for client-side API failures.'),
('grp_009', 'APAC Regional Admins', 'Contacts for APAC specific services.'),
('grp_010', 'EU Regional Admins', 'Contacts for EU specific services.'),
('grp_011', 'Finance Team', 'Alerts for payment gateway status.'),
('grp_012', 'System Admins', 'General infrastructure alerts.'),
('grp_013', 'SRE Team', 'Site Reliability Engineering team.'),
('grp_014', 'Maintenance Team', 'Notified during scheduled maintenance windows.'),
('grp_015', 'Testing Team', 'Alerts for staging/testing environments.'),
('grp_016', 'External Vendors', 'Alerts related to external dependencies.'),
('grp_017', 'Microservices Team', 'Alerts for Microservice A & B.'),
('grp_018', 'CDN Team', 'Alerts for CDN performance/status.'),
('grp_019', 'Monitoring Team', 'Alerts related to the monitoring system itself.'),
('grp_020', 'Sales Team', 'Notified of marketing site downtime.');

-- System Users (20 records)
INSERT INTO system_users (user_id, role_id, channel_id, email_address, first_name, surname, password, status) VALUES
('usr_001', (SELECT role_id FROM roles WHERE role_title = 'ADMIN'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'EMAIL'), 'alice.adm@corp.com', 'Alice', 'Smith', 'pass1', 'ACTIVE'),
('usr_002', (SELECT role_id FROM roles WHERE role_title = 'OPERATOR'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'TELEGRAM'), 'bob.op@corp.com', 'Bob', 'Jones', 'pass2', 'ACTIVE'),
('usr_003', (SELECT role_id FROM roles WHERE role_title = 'VIEWER'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'SMS'), 'charlie.vw@corp.com', 'Charlie', 'Brown', 'pass3', 'ACTIVE'),
('usr_004', (SELECT role_id FROM roles WHERE role_title = 'OPERATOR'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'EMAIL'), 'david.op@corp.com', 'David', 'Clark', 'pass4', 'ACTIVE'),
('usr_005', (SELECT role_id FROM roles WHERE role_title = 'ADMIN'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'TELEGRAM'), 'emma.adm@corp.com', 'Emma', 'Harris', 'pass5', 'ACTIVE'),
('usr_006', (SELECT role_id FROM roles WHERE role_title = 'VIEWER'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'SMS'), 'frank.vw@corp.com', 'Frank', 'King', 'pass6', 'ACTIVE'),
('usr_007', (SELECT role_id FROM roles WHERE role_title = 'OPERATOR'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'EMAIL'), 'grace.op@corp.com', 'Grace', 'Lee', 'pass7', 'ACTIVE'),
('usr_008', (SELECT role_id FROM roles WHERE role_title = 'ADMIN'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'TELEGRAM'), 'henry.adm@corp.com', 'Henry', 'Miller', 'pass8', 'ACTIVE'),
('usr_009', (SELECT role_id FROM roles WHERE role_title = 'OPERATOR'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'SMS'), 'ivy.op@corp.com', 'Ivy', 'Nelson', 'pass9', 'ACTIVE'),
('usr_010', (SELECT role_id FROM roles WHERE role_title = 'VIEWER'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'EMAIL'), 'jack.vw@corp.com', 'Jack', 'Perez', 'pass10', 'INACTIVE'),
('usr_011', (SELECT role_id FROM roles WHERE role_title = 'ADMIN'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'TELEGRAM'), 'kate.adm@corp.com', 'Kate', 'Quinn', 'pass11', 'ACTIVE'),
('usr_012', (SELECT role_id FROM roles WHERE role_title = 'OPERATOR'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'SMS'), 'liam.op@corp.com', 'Liam', 'Roberts', 'pass12', 'ACTIVE'),
('usr_013', (SELECT role_id FROM roles WHERE role_title = 'VIEWER'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'EMAIL'), 'mia.vw@corp.com', 'Mia', 'Scott', 'pass13', 'ACTIVE'),
('usr_014', (SELECT role_id FROM roles WHERE role_title = 'OPERATOR'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'TELEGRAM'), 'noah.op@corp.com', 'Noah', 'Taylor', 'pass14', 'ACTIVE'),
('usr_015', (SELECT role_id FROM roles WHERE role_title = 'ADMIN'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'SMS'), 'olivia.adm@corp.com', 'Olivia', 'Underwood', 'pass15', 'ACTIVE'),
('usr_016', (SELECT role_id FROM roles WHERE role_title = 'VIEWER'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'EMAIL'), 'paul.vw@corp.com', 'Paul', 'Vance', 'pass16', 'ACTIVE'),
('usr_017', (SELECT role_id FROM roles WHERE role_title = 'OPERATOR'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'TELEGRAM'), 'quincy.op@corp.com', 'Quincy', 'White', 'pass17', 'ACTIVE'),
('usr_018', (SELECT role_id FROM roles WHERE role_title = 'ADMIN'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'SMS'), 'rachel.adm@corp.com', 'Rachel', 'Xavier', 'pass18', 'ACTIVE'),
('usr_019', (SELECT role_id FROM roles WHERE role_title = 'OPERATOR'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'EMAIL'), 'sam.op@corp.com', 'Sam', 'Young', 'pass19', 'ACTIVE'),
('usr_020', (SELECT role_id FROM roles WHERE role_title = 'VIEWER'), (SELECT channel_id FROM notification_channels WHERE channel_title = 'TELEGRAM'), 'tina.vw@corp.com', 'Tina', 'Zane', 'pass20', 'ACTIVE');

-- SSL Certificate Info (20 records - linked to svc_001 to svc_020)
INSERT INTO ssl_certificate_info (cert_id, service_id, issuer, expiry_date)
SELECT CONCAT('cert_', LPAD(i::text, 3, '0')), CONCAT('svc_', LPAD(i::text, 3, '0')),
    CASE WHEN i % 2 = 0 THEN 'Let''s Encrypt' ELSE 'Google Trust Services' END,
    NOW() + INTERVAL '1 month' * ((i % 12) + 1) AS expiry_date
FROM generate_series(1, 20) s(i);


-- =========================================================================
-- 3. CONFIGURATION TABLES (20 Records Each)
-- =========================================================================

-- Service Configs (20 records - linked to svc_001 to svc_020)
INSERT INTO service_configs (service_id, svc_diag_id, svc_diagnosis_interval, num_of_retries, retry_interval_secs)
SELECT CONCAT('svc_', LPAD(i::text, 3, '0')), (i % 4) + 1,
    CASE WHEN i % 5 = 0 THEN 300 ELSE 60 END, -- Interval
    3, 10
FROM generate_series(1, 20) s(i);

-- SSL Certificate Configs (20 records - linked to cert_001 to cert_020)
INSERT INTO ssl_certificate_configs (cert_id, svc_diag_id, cert_diagnosis_interval, alert_threshold_days)
SELECT CONCAT('cert_', LPAD(i::text, 3, '0')),
    (SELECT svc_diag_id FROM service_diagnosis_methods WHERE diagnosis_method = 'TLS_HANDSHAKE'),
    86400, -- Daily check
    CASE WHEN i % 5 = 0 THEN 15 ELSE 30 END -- Threshold
FROM generate_series(1, 20) s(i);


-- =========================================================================
-- 4. JUNCTION TABLES (20 Records Each)
-- =========================================================================

-- Contact Group Members (20 records - Simple 1:1 mapping for demonstration)
INSERT INTO contact_group_members (contact_group_id, user_id)
SELECT CONCAT('grp_', LPAD(i::text, 3, '0')), CONCAT('usr_', LPAD(i::text, 3, '0'))
FROM generate_series(1, 20) s(i);

-- Service Contact Groups (20 records - Simple 1:1 mapping for demonstration)
INSERT INTO service_contact_groups (service_id, contact_group_id)
SELECT CONCAT('svc_', LPAD(i::text, 3, '0')), CONCAT('grp_', LPAD(i::text, 3, '0'))
FROM generate_series(1, 20) s(i);


-- =========================================================================
-- 5. LOG AND HISTORY TABLES (20 Records Each)
-- =========================================================================

-- Service Health Check Logs (20 records)
INSERT INTO service_health_check_logs (service_id, svc_status_id, response_time_ms, http_response_code, check_at, error_message)
SELECT CONCAT('svc_', LPAD(i::text, 3, '0')), (i % 2) + 1, -- Cycles between UP/DOWN
    CASE WHEN i % 2 = 1 THEN (i * 10) ELSE 0 END, -- Response time
    CASE WHEN i % 2 = 1 THEN 200 ELSE 500 END, -- HTTP Code
    NOW() - INTERVAL '1 minute' * i,
    CASE WHEN i % 2 = 0 THEN 'Server failed to respond.' ELSE NULL END
FROM generate_series(1, 20) s(i);

-- SSL Cert Health Check Logs (20 records)
INSERT INTO ssl_cert_health_check_logs (cert_id, cert_status_id, last_checked_timestamp)
SELECT CONCAT('cert_', LPAD(i::text, 3, '0')), (i % 4) + 1, -- Cycles through 4 statuses
    NOW() - INTERVAL '6 hours' * i
FROM generate_series(1, 20) s(i);

-- User Actions Audit Logs (20 records)
INSERT INTO user_actions_audit_logs (user_id, service_id, action, action_timestamp)
SELECT CONCAT('usr_', LPAD(i::text, 3, '0')), CONCAT('svc_', LPAD(i::text, 3, '0')),
    CASE WHEN i % 3 = 0 THEN 'CREATE' WHEN i % 3 = 1 THEN 'UPDATE' ELSE 'DELETE' END,
    NOW() - INTERVAL '1 day' * (20 - i)
FROM generate_series(1, 20) s(i);

-- Notification History Logs (20 records)
INSERT INTO notification_history_logs (notification_trigger_id, user_id, channel_id, created_at)
SELECT (i % 3) + 1, CONCAT('usr_', LPAD(i::text, 3, '0')), (i % 3) + 1,
    NOW() - INTERVAL '30 seconds' * i
FROM generate_series(1, 20) s(i);