package com.alexivashchenko.auth.service.service.provisioning;

import java.util.UUID;

public interface UserProvisioningClient {

    void createProfile(UUID userId, String email);
}
