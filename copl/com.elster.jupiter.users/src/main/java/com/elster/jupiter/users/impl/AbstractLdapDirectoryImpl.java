/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.UserService;

import javax.naming.Context;
import javax.validation.constraints.Size;
import java.util.Hashtable;

public abstract class AbstractLdapDirectoryImpl extends AbstractUserDirectoryImpl implements LdapUserDirectory{
    @Size(max = 128, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_128 + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String directoryUser;
    @Size(max = 128, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_128 + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String password;
    private String description;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String url;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String backupUrl;
    @Size(max = 4, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_4 + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String securityProtocol;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String baseUser;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String baseGroup;
    private boolean manageGroupsInternal;


    final Hashtable<String, Object> commonEnvLDAP = new Hashtable<String, Object>(){{
        put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        put(Context.SECURITY_AUTHENTICATION, "simple");
    }};

    public AbstractLdapDirectoryImpl(DataModel dataModel, UserService userService) {
        super(dataModel, userService);
    }

    @Override
    public boolean isManageGroupsInternal() {
        return manageGroupsInternal;
    }

    @Override
    public void setManageGroupsInternal(boolean manageGroupsInternal){
        this.manageGroupsInternal = manageGroupsInternal;
    }

    @Override
    public String getDirectoryUser() {
        return directoryUser;
    }

    @Override
    public void setDirectoryUser(String directoryUser) {
        this.directoryUser = directoryUser;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getSecurity(){
        return securityProtocol;
    }

    @Override
    public String getBackupUrl(){
        return backupUrl;
    }

    @Override
    public String getDescription(){
        return description;
    }

    @Override
    public void setSecurity(String security){
        this.securityProtocol = security;
    }

    @Override
    public void setDescription(String description){
        this.description = description;
    }

    @Override
    public void setBackupUrl(String backupUrl){
        this.backupUrl = backupUrl;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        if(!"".equals(password)) {
            this.password = userService.getDataVaultService().encrypt(password.getBytes());
        }
    }

    @Override
    public String getBaseUser(){
        return baseUser;
    }

    @Override
    public void setBaseUser(String baseUser) {
        this.baseUser = baseUser;
    }

    @Override
    public String getBaseGroup() {
        return baseGroup;
    }

    @Override
    public void setBaseGroup(String baseGroup) {
        this.baseGroup = baseGroup;
    }

    protected String getPasswordDecrypt(){
        return new String(userService.getDataVaultService().decrypt(getPassword()));
    }
}
