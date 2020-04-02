package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.GroupSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;

/**
 * Interface for SCIMService that is responsible implementing a bridge
 * between SCIM API and Connexo API.
 *
 * @author edragutan
 */
public interface SCIMService {

    UserSchema createUser(UserSchema userSchema);

    UserSchema getUser(String id);

    UserSchema updateUser(UserSchema userSchema);

    void deleteUser(String id);

    GroupSchema createGroup(GroupSchema groupSchema);

    GroupSchema getGroup(String id);

    GroupSchema updateGroup(GroupSchema groupSchema);

    void deleteGroup(String id);

}
