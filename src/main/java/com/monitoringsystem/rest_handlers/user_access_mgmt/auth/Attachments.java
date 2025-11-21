package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import io.undertow.util.AttachmentKey;

public class Attachments
{
    public static final AttachmentKey<UserSession> USER_SESSION = AttachmentKey.create(UserSession.class);
}
