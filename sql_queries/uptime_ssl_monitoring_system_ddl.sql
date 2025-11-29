
CREATE DATABASE uptime_ssl_monitoring_system_db;

\connect uptime_ssl_monitoring_system_db

CREATE OR REPLACE FUNCTION update_date_modified()
RETURNS TRIGGER AS $$
BEGIN
    IF ROW (NEW.*) IS DISTINCT FROM ROW (OLD.*) THEN
        NEW.date_modified = NOW();
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';


CREATE TABLE roles (
    role_id BIGSERIAL PRIMARY KEY,
    role_title VARCHAR(50) UNIQUE NOT NULL, -- e.g., ADMIN, OPERATOR, VIEWER
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_roles_date_modified BEFORE UPDATE ON roles
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE notification_channels (
    channel_id BIGSERIAL PRIMARY KEY,
    channel_title VARCHAR(50) UNIQUE NOT NULL, -- e.g., EMAIL, TELEGRAM, SMS
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_notification_channels_date_modified BEFORE UPDATE ON notification_channels
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE service_diagnosis_methods (
    svc_diag_id BIGSERIAL PRIMARY KEY,
    diagnosis_method VARCHAR(50) UNIQUE NOT NULL, -- e.g., PING, REQUEST, PORT_SCAN, TLS_HANDSHAKE
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_service_diagnosis_methods_date_modified BEFORE UPDATE ON service_diagnosis_methods
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE certificate_status (
    cert_status_id BIGSERIAL PRIMARY KEY,
    cert_health_status VARCHAR(20) UNIQUE NOT NULL, -- e.g., VALID, WATCH, EXPIRED, TERMINAL
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_certificate_status_date_modified BEFORE UPDATE ON certificate_status
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE service_status (
    svc_status_id BIGSERIAL PRIMARY KEY,
    svc_health_status VARCHAR(20) UNIQUE NOT NULL, -- e.g., UP, DOWN
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_service_status_date_modified BEFORE UPDATE ON service_status
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE notification_triggers (
    notification_trigger_id BIGSERIAL PRIMARY KEY,
    notification_trigger VARCHAR(50) UNIQUE NOT NULL, -- e.g., SVC_DOWN, SSL_CERT_EXPIRY
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_notification_triggers_date_modified BEFORE UPDATE ON notification_triggers
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE contact_groups (
    contact_group_id VARCHAR PRIMARY KEY NOT NULL,
    group_name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_contact_groups_date_modified BEFORE UPDATE ON contact_groups
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE service_info (
    service_id VARCHAR PRIMARY KEY NOT NULL,
    service_name VARCHAR(255) UNIQUE NOT NULL,
    service_url_domain VARCHAR(255) UNIQUE NOT NULL,
    service_port INTEGER NOT NULL,
    svc_registration_status VARCHAR(10) NOT NULL, -- e.g., ACTIVE, TERMINAL
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_service_info_date_modified BEFORE UPDATE ON service_info
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

-- =========================================================================
-- Dependent Entity Tables
-- =========================================================================

CREATE TABLE system_users (
    user_id VARCHAR PRIMARY KEY NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles(role_id) ON DELETE RESTRICT,
    channel_id BIGINT REFERENCES notification_channels(channel_id) ON DELETE SET NULL,
    email_address VARCHAR(255) UNIQUE NOT NULL,
    tel VARCHAR(20),
    telegram VARCHAR(50),
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    surname VARCHAR(100) NOT NULL,
    password TEXT NOT NULL,
    status VARCHAR(10) NOT NULL, -- e.g., ACTIVE, INACTIVE
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_system_users_date_modified BEFORE UPDATE ON system_users
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE ssl_certificate_info (
    cert_id VARCHAR PRIMARY KEY NOT NULL,
    service_id VARCHAR NOT NULL UNIQUE REFERENCES service_info(service_id) ON DELETE CASCADE,
    issuer VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    is_cert_active_status VARCHAR(10) NOT NULL, -- ACTIVE, TERMINAL
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_ssl_certificate_info_date_modified BEFORE UPDATE ON ssl_certificate_info
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE public.user_auth_tokens
(
    email_address character varying(255) NOT NULL,
    auth_token text NOT NULL,
    PRIMARY KEY (auth_token),
    CONSTRAINT email_address FOREIGN KEY (email_address)
        REFERENCES public.system_users (email_address) MATCH FULL
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
);

CREATE TABLE public.user_registration_tokens
(
    email_address character varying(255) NOT NULL,
    registration_token text NOT NULL,
    PRIMARY KEY (registration_token),
    CONSTRAINT email_address FOREIGN KEY (email_address)
        REFERENCES public.system_users (email_address) MATCH FULL
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
);

-- =========================================================================
-- Config Tables (Enforced 1:1 Relationship using FK as PK)
-- =========================================================================

CREATE TABLE service_configs (
    service_id VARCHAR PRIMARY KEY REFERENCES service_info(service_id) ON DELETE CASCADE,
    svc_diag_id BIGINT NOT NULL REFERENCES service_diagnosis_methods(svc_diag_id) ON DELETE RESTRICT,
    svc_diagnosis_interval INTEGER NOT NULL,
    num_of_retries INTEGER DEFAULT 3 NOT NULL,
    retry_interval_secs INTEGER DEFAULT 10 NOT NULL,
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_service_configs_date_modified BEFORE UPDATE ON service_configs
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE ssl_certificate_configs (
    cert_id VARCHAR PRIMARY KEY REFERENCES ssl_certificate_info(cert_id) ON DELETE CASCADE,
    svc_diag_id BIGINT NOT NULL REFERENCES service_diagnosis_methods(svc_diag_id) ON DELETE RESTRICT,
    cert_diagnosis_interval INTEGER NOT NULL,
    alert_threshold_days INTEGER NOT NULL,
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_ssl_certificate_configs_date_modified BEFORE UPDATE ON ssl_certificate_configs
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

-- =========================================================================
-- Junction Tables (Many-to-Many)
-- =========================================================================

CREATE TABLE contact_group_members (
    contact_group_id VARCHAR NOT NULL REFERENCES contact_groups(contact_group_id) ON DELETE CASCADE,
    user_id VARCHAR NOT NULL REFERENCES system_users(user_id) ON DELETE CASCADE,
    PRIMARY KEY (contact_group_id, user_id),
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_contact_group_members_date_modified BEFORE UPDATE ON contact_group_members
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE service_contact_groups (
    service_id VARCHAR NOT NULL REFERENCES service_info(service_id) ON DELETE CASCADE,
    contact_group_id VARCHAR NOT NULL REFERENCES contact_groups(contact_group_id) ON DELETE CASCADE,
    PRIMARY KEY (service_id, contact_group_id),
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_service_contact_groups_date_modified BEFORE UPDATE ON service_contact_groups
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

-- =========================================================================
-- Log and History Tables
-- =========================================================================

CREATE TABLE service_health_check_logs (
    shc_log_id BIGSERIAL PRIMARY KEY,
    service_id VARCHAR NOT NULL REFERENCES service_info(service_id) ON DELETE CASCADE,
    svc_status_id BIGINT NOT NULL REFERENCES service_status(svc_status_id) ON DELETE RESTRICT,
    response_time_ms INTEGER NOT NULL,
    http_response_code INTEGER,
    check_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    error_message TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_service_health_check_logs_date_modified BEFORE UPDATE ON service_health_check_logs
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE ssl_cert_health_check_logs (
    schl_log_id BIGSERIAL PRIMARY KEY,
    cert_id VARCHAR NOT NULL REFERENCES ssl_certificate_info(cert_id) ON DELETE CASCADE,
    cert_status_id BIGINT NOT NULL REFERENCES certificate_status(cert_status_id) ON DELETE RESTRICT,
    last_checked_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_ssl_cert_health_check_logs_date_modified BEFORE UPDATE ON ssl_cert_health_check_logs
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE user_actions_audit_logs (
    uaa_log_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR NOT NULL REFERENCES system_users(user_id) ON DELETE RESTRICT,
    service_id VARCHAR REFERENCES service_info(service_id) ON DELETE SET NULL,
    action VARCHAR(20) NOT NULL, -- e.g., CREATE, UPDATE, DELETE
    action_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_user_actions_audit_logs_date_modified BEFORE UPDATE ON user_actions_audit_logs
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();

CREATE TABLE notification_history_logs (
    nh_log_id BIGSERIAL PRIMARY KEY,
    notification_trigger_id BIGINT NOT NULL REFERENCES notification_triggers(notification_trigger_id) ON DELETE RESTRICT,
    user_id VARCHAR NOT NULL REFERENCES system_users(user_id) ON DELETE RESTRICT,
    channel_id BIGINT NOT NULL REFERENCES notification_channels(channel_id) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL,
    date_created TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER update_notification_history_logs_date_modified BEFORE UPDATE ON notification_history_logs
FOR EACH ROW EXECUTE PROCEDURE update_date_modified();