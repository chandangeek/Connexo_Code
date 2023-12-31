/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userDirectory.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usr-user-directory-preview-form',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                itemId: 'usr-user-directory-preview-form',
                defaults: {
                    xtype: 'container',
                    layout: 'form'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 150
                                },
                                items: [

                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('general.name', 'USR', 'Name'),
                                        name: 'name',
                                        itemId: 'usr-user-directory-name'
                                    },
                                    {
                                        xtype: 'container',
                                        itemId: 'ctn-user-directory-properties1',
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        defaults: {
                                            labelWidth: 150
                                        },
                                        items:
                                            [
                                                {
                                                    xtype: 'displayfield',
                                                    name: 'baseUser',
                                                    itemId: 'usr-user-directory-user-base-dn',
                                                    fieldLabel: Uni.I18n.translate('userDirectories.baseUser', 'USR', 'User base DN')
                                                },
												{
                                                    xtype: 'displayfield',
                                                    name: 'groupName',
                                                    itemId: 'usr-user-directory-user-group-dn',
                                                    fieldLabel: Uni.I18n.translate('userDirectories.groupDN', 'USR', 'Group DN')
                                                },
                                                {
                                                    xtype: 'displayfield',
                                                    name: 'baseGroup',
                                                    itemId: 'usr-user-directory-group-base-dn',
                                                    fieldLabel: Uni.I18n.translate('userDirectories.baseGroup', 'USR', 'Group base DN'),
                                                },
                                                {
                                                    xtype: 'displayfield',
                                                    fieldLabel: Uni.I18n.translate('userDirectories.url', 'USR', 'URL'),
                                                    name: 'url',
                                                    itemId: 'usr-user-directory-url'
                                                },
                                                {
                                                    xtype: 'displayfield',
                                                    fieldLabel: Uni.I18n.translate('userDirectories.backupURL', 'USR', 'Backup URL'),
                                                    name: 'backupUrl',
                                                    itemId: 'usr-user-directory-backup-url'
                                                }
                                        ]
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                itemId: 'ctn-user-directory-properties2',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 150
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('userDirectories.type', 'USR', 'Type'),
                                        name: 'typeDisplay',
                                        itemId: 'usr-user-directory-type'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('userDirectories.securityProtocol', 'USR', 'Security protocol'),
                                        name: 'securityProtocolDisplay',
                                        itemId: 'usr-user-directory-security-protocol'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('userDirectories.protocol.source', 'USR', 'Source'),
                                        name: 'securityProtocolSource',
                                        itemId: 'usr-user-directory-security-protocol-source'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('userDirectories.certificateAlias', 'USR', 'Certificate alias'),
                                        name: 'certificateAlias',
                                        itemId: 'protocol-source-certificates',
                                        hidden: true
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('userDirectories.trustStore', 'USR', 'Trust store'),
                                        name: 'trustStore',
                                        itemId: 'protocol-source-trustStores',
                                        hidden: true,
                                        renderer: function(value){
                                            return value.name;
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent();
    }
});
