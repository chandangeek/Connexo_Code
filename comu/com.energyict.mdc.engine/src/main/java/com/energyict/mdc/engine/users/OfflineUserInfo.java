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
    private String userName;
    private final int salt;
    private final String hash;
    private Map<String, PrivilegesWrapper> applicationPrivileges;
    private boolean canUseComServerMobile;

    public OfflineUserInfo() {
        this.id = 0;
        this.userName = null;
        this.salt = 0;
        this.hash = "";
        this.applicationPrivileges = new HashMap<String, PrivilegesWrapper>();
    }

    public OfflineUserInfo(User user) {
        this.id =user.getId();
        this.userName = user.getName();
        this.salt = user.getSalt();
        this.hash = user.getDigestHa1();
        this.applicationPrivileges = new HashMap<String, PrivilegesWrapper>();
        for(Map.Entry<String,List<Privilege>> entry: user.getApplicationPrivileges().entrySet())
            this.applicationPrivileges.put(entry.getKey(), new PrivilegesWrapper(entry.getValue()));
        this.canUseComServerMobile = false;
    }

    public OfflineUserInfo(User user, boolean canUseComServerMobile) {
        this(user);
        this.canUseComServerMobile = canUseComServerMobile;
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
}