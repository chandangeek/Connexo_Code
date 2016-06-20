package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.users.Group;

import java.util.Optional;

/**
 * Created by bvn on 5/4/16.
 */
public interface InboundEndPointConfiguration extends EndPointConfiguration {
    EndPointAuthentication getAuthenticationMethod();

    void setAuthenticationMethod(EndPointAuthentication authenticated);

    Optional<Group> getGroup();

    void setGroup(Group group);
}
