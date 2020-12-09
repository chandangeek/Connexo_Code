package com.energyict.mdc.engine.users;

import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 16/10/2014 - 15:22
 */

@XmlRootElement
public class OfflineUserInfo {

    private long id;
    private String domain;
    private boolean defaultDomain;
    private String userName;
    private final int salt;
    private String hash;
    private Map<String, PrivilegesWrapper> applicationPrivileges;
    private boolean canUseComServerMobile;

    public OfflineUserInfo() {
        this.id = 0;
        this.userName = null;
        this.salt = 0;
        this.hash = "";
        this.applicationPrivileges = new HashMap<>();
    }

    public OfflineUserInfo(User user, boolean canUseComServerMobile, boolean defaultDomain) {
        this.id = user.getId();
        this.domain = user.getDomain();
        this.userName = user.getName();
        this.salt = user.getSalt();
        this.applicationPrivileges = new HashMap<>();
        for (Map.Entry<String, List<Privilege>> entry : user.getApplicationPrivileges().entrySet()) {
            this.applicationPrivileges.put(entry.getKey(), new PrivilegesWrapper(entry.getValue()));
        }
        this.canUseComServerMobile = canUseComServerMobile;
        this.defaultDomain = defaultDomain;
    }

    @XmlAttribute
    public long getId() {
        return id;
    }

    @XmlAttribute
    public boolean isCanUseComServerMobile() {
        return canUseComServerMobile;
    }

    @XmlAttribute
    public int getSalt() {
        return salt;
    }

    @XmlAttribute
    public String getDomain() {
        return domain;
    }

    @XmlAttribute
    public boolean isDefaultDomain() {
        return defaultDomain;
    }

    @XmlAttribute
    public String getUserName() {
        return userName;
    }

    @XmlAttribute
    public String getHash() {
        return hash;
    }

    @XmlElement
    public Map<String, PrivilegesWrapper> getApplicationPrivileges() {
        return applicationPrivileges;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}