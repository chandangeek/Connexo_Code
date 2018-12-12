/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userDirectory.AddUserDirectory', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usr-add-user-directory',
    requires: [
        'Uni.util.FormErrorMessage',
        'Usr.store.SecurityProtocols',
        'Usr.store.Certificates',
        'Usr.store.TrustStores'
    ],

    edit: false,
    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#btn-add').setText(Uni.I18n.translate('general.save', 'USR', 'Save'));
            this.down('#btn-add').action = 'edit';
        } else {
            this.edit = edit;
            this.down('#btn-add').setText(Uni.I18n.translate('general.add', 'USR', 'Add'));
            this.down('#btn-add').action = 'add';
        }
        this.down('#btn-cancel-link').href = returnLink;
    },

    initComponent: function () {
        var me = this,
            switchCombos = function (value) {
                var certificateCombo = me.down('#cbo-certificate-alias');
                certificateCombo.setVisible(value);
                certificateCombo.setDisabled(!value);
                certificateCombo.allowBlank = !value;
            };

        me.content = [
            {
                xtype: 'form',
                itemId: 'frm-add-user-directory',
                ui: 'large',
                defaults: {
                    labelWidth: 250,
                    width: 600,
                    enforceMaxLength: true
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'txt-name',
                        required: true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('general.name', 'USR', 'Name'),
                        listeners: {
                            afterrender: function (field) {
                                if (!me.edit) {
                                    field.focus(false, 200);
                                }
                            }
                        }
                    },
                    {
                        xtype: 'textfield',
                        name: 'url',
                        itemId: 'txt-url',
                        required: true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('userDirectories.url', 'USR', 'URL'),
                        listeners: {
                            afterrender: function (field) {
                                if (me.edit) {
                                    field.focus(false, 200);
                                }
                            }
                        }
                    },
                    {
                        xtype: 'textfield',
                        name: 'backupUrl',
                        itemId: 'txt-backupUrl',
                        fieldLabel: Uni.I18n.translate('userDirectories.backupURL', 'USR', 'Backup URL')
                    },
                    {
                        xtype: 'container',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'cbo-security-protocol',
                                name: 'securityProtocol',
                                width: 600,
                                fieldLabel: Uni.I18n.translate('userDirectories.securityProtocol', 'USR', 'Security protocol'),
                                labelWidth: 250,
                                required: true,
                                store: 'Usr.store.SecurityProtocols',
                                editable: false,
                                disabled: false,
                                emptyText: Uni.I18n.translate('userDirectories.securityProtocolPrompt', 'USR', 'Select a security protocol...'),
                                allowBlank: false,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'value',
                                listeners: {
                                    select: function (combo, records, eOpts) {
                                        var protocolSource = me.down('#security-protocol-source'),
                                            value = records[0].get('value') !== 'NONE';
                                        protocolSource.setVisible(value);
                                        protocolSource.setDisabled(!value);
                                    }
                                }
                            },
                            {
                                xtype: 'container',
                                itemId: 'no-security-protocol',
                                hidden: true,
                                html: '<div style="color: #EB5642">' + Uni.I18n.translate('userDirectories.noSecurityProtocol', 'USR', 'No security protocol.') + '</div>',
                                margin: '0 0 0 265'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        itemId: 'security-protocol-source',
                        hidden: true,
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'cbo-trust-store',
                                name: 'trustStore.id',
                                width: 600,
                                fieldLabel: Uni.I18n.translate('userDirectories.trustStore', 'USR', 'Trust store'),
                                labelWidth: 250,
                                required: true,
                                store: 'Usr.store.TrustStores',
                                disabled: false,
                                emptyText: Uni.I18n.translate('userDirectories.trustStorePrompt', 'USR', 'Select a trust store...'),
                                queryDelay: 500,
                                queryCaching: false,
                                minChars: 0,
                                allowBlank: false,
                                loadStore: false,
                                forceSelection: false,
                                queryMode: 'remote',
                                queryParam: 'like',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'checkbox',
                                fieldLabel: Uni.I18n.translate('userDirectories.authentication', 'USR', 'Two-way authentication'),
                                itemId: 'authentication-checkbox',
                                name: 'twoWaySsl',
                                labelWidth: 250,
                                listeners: {
                                    change: function (rb, newValue, oldValue, eOpts) {
                                        switchCombos(newValue);
                                    }
                                }
                            },
                            {
                                xtype: 'combobox',
                                itemId: 'cbo-certificate-alias',
                                name: 'certificateAlias',
                                hidden: true,
                                required: true,
                                width: 600,
                                fieldLabel: Uni.I18n.translate('userDirectories.certificateAlias', 'USR', 'Certificate alias'),
                                labelWidth: 250,
                                store: 'Usr.store.Certificates',
                                disabled: false,
                                emptyText: Uni.I18n.translate('userDirectories.certificateAliasPrompt', 'USR', 'Start typing to select...'),
                                queryDelay: 500,
                                queryCaching: false,
                                minChars: 0,
                                allowBlank: true,
                                loadStore: false,
                                forceSelection: false,
                                displayField: 'alias',
                                queryMode: 'remote',
                                queryParam: 'like',
                                valueField: 'id'
                            }
                        ]
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'rdo-user-directory-type',
                        required: true,
                        fieldLabel: Uni.I18n.translate('userDirectories.type', 'USR', 'Type'),
                        columns: 1,
                        vertical: true,
                        defaults: {
                            name: 'type'
                        },
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('userDirectories.type.activeDirectory', 'USR', 'Active Directory'),
                                inputValue: 'ACD',
                                checked: true
                            },
                            {
                                boxLabel: Uni.I18n.translate('userDirectories.type.apacheDS', 'USR', 'LDAP'),
                                inputValue: 'APD'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        margin: '0 0 8 0',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        width: 700,
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'directoryUser',
                                itemId: 'txt-userName',
                                required: true,
                                allowBlank: false,
                                labelWidth: 250,
                                width: 600,
                                enforceMaxLength: true,
                                fieldLabel: Uni.I18n.translate('userDirectories.userName', 'USR', 'User')
                            },
                            {
                                xtype: 'button',
                                itemId: 'txt-user-name-info',
                                tooltip: Uni.I18n.translate('importService.filePattern.tooltip', 'USR', 'Click for more information'),
                                text: '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                                ui: 'blank',
                                shadow: false,
                                margin: '6 0 0 10',
                                width: 16,
                                tabIndex: -1,
                                listeners: {
                                    click: function () {
                                        var me = Ext.getCmp(this.id);
                                        me.up('contentcontainer').fireEvent('displayinfo', me);
                                    }
                                }
                            }

                        ]
                    },
                    {
                        xtype: 'textfield',
                        name: 'password',
                        itemId: 'txt-userPassword',
                        allowBlank: false,
                        required: true,
                        inputType: 'password',
                        fieldLabel: Uni.I18n.translate('userDirectories.userPassword', 'USR', 'Password')
                    },
                    {
                        xtype: 'textfield',
                        name: 'baseUser',
                        itemId: 'txt-baseUser',
                        fieldLabel: Uni.I18n.translate('userDirectories.baseUser', 'USR', 'User base DN')
                    },
                    {
                        xtype: 'textfield',
                        name: 'baseGroup',
                        itemId: 'txt-baseGroup',
                        fieldLabel: Uni.I18n.translate('userDirectories.baseGroup', 'USR', 'Group base DN'),
                        hidden: true
                    },
                    {
                        xtype: 'container',
                        margin: '0 0 0 265',
                        layout: 'hbox',
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'USR', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'btn-add'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.cancel', 'USR', 'Cancel'),
                                href: '#/administration/userdirectories',
                                itemId: 'btn-cancel-link',
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit, me.returnLink);
    },

    loadRecord: function (record) {
        var me = this,
            protocolSource = me.down('#security-protocol-source'),
            addUserDirectoryForm = me.down('#frm-add-user-directory'),
            certificateCombo = me.down('#cbo-certificate-alias'),
            certificateCheckbox = me.down('#authentication-checkbox'),
            trustStoreCombo = me.down('#cbo-trust-store');

        addUserDirectoryForm.loadRecord(record);

        protocolSource.setVisible(record.get('securityProtocol') !== 'NONE');
        protocolSource.setDisabled(record.get('securityProtocol') === 'NONE');

        certificateCheckbox.setValue(!!record.get('certificateAlias'));
        if (record.get('certificateAlias')) {
            certificateCombo.setValue(record.get('certificateAlias'));
        }

        if (record.get('trustStore')) {
            trustStoreCombo.getStore().load({
                callback: function () {
                    trustStoreCombo.setValue(record.get('trustStore').id);
                }
            })
        }
    }
});

