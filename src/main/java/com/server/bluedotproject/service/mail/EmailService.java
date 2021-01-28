package com.server.bluedotproject.service.mail;

import com.server.bluedotproject.dto.request.EmailMessageApiRequest;

public interface EmailService {
    void sendEmail(EmailMessageApiRequest emailMessage);
}