package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UserInfoFactory {
    UserInfo from(NlsService nlsService, User user);
}
