package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.api.UserFile;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the Info object for a {@link UserFile}
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:08
 */
@XmlRootElement
public class UserFileReferenceInfo {

    public int userFileReferenceId;
    public String name;

    public UserFileReferenceInfo() {
    }

    public UserFileReferenceInfo(Map<String, Object> map) {
        this.userFileReferenceId = (int) map.get("userFileId");
        this.name = (String) map.get("name");
    }

    public UserFileReferenceInfo(UserFile userFile) {
        userFileReferenceId = userFile.getId();
        name = userFile.getName();
    }

}
