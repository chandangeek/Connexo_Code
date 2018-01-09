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
                var certificateCombo = me.down('#cbo-certificate-alias'),
                    trustStoreCombo = me.down('#cbo-trust-store');
                certificateCombo.setVisible(value);
                trustStoreCombo.setVisible(!value);

                certificateCombo.setValue('');
                trustStoreCombo.setValue(null);

                certificateCombo.allowBlank = !value;
                trustStoreCombo.allowBlank = value;


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
                                        var protocolSource = me.down('#security-protocol-source');
                                        protocolSource.setVisible(records[0].get('value') !== 'NONE');
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
                        listeners: {
                            show: function (c) {
                                c.down('#rdo-security-protocol-source').setValue({source: 'certificates'});
                            }
                        },
                        items: [
                            {
                                xtype: 'radiogroup',
                                itemId: 'rdo-security-protocol-source',
                                required: true,
                                fieldLabel: Uni.I18n.translate('userDirectories.protocol.source', 'USR', 'Source'),
                                columns: 1,
                                labelWidth: 250,
                                vertical: true,
                                defaults: {
                                    name: 'source'
                                },
                                items: [
                                    {
                                        boxLabel: Uni.I18n.translate('userDirectories.protocol.source.certificates', 'USR', 'Certificates'),
                                        inputValue: 'certificates',
                                        listeners: {
                                            change: function (rb, newValue, oldValue, eOpts) {
                                                switchCombos(newValue);
                                            }
                                        }
                                    },
                                    {
                                        boxLabel: Uni.I18n.translate('userDirectories.protocol.source.trustStores', 'USR', 'Trust stores'),
                                        inputValue: 'trustStores',
                                        listeners: {
                                            change: function (rb, newValue, oldValue, eOpts) {
                                                switchCombos(!newValue);
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'combobox',
                                itemId: 'cbo-certificate-alias',
                                name: 'certificateAlias',
                                hidden: true,
                                width: 600,
                                fieldLabel: Uni.I18n.translate('userDirectories.certificateAlias', 'USR', 'Certificate alias'),
                                labelWidth: 250,
                                required: false,
                                store: 'Usr.store.Certificates',
                                editable: false,
                                disabled: false,
                                // emptyText: Uni.I18n.translate('userDirectories.securityProtocolPrompt', 'USR', 'Select a security protocol...'),
                                allowBlank: true,
                                displayField: 'alias',
                                valueField: 'id'
                            },
                            {
                                xtype: 'combobox',
                                itemId: 'cbo-trust-store',
                                name: 'trustStore',
                                hidden: true,
                                width: 600,
                                fieldLabel: Uni.I18n.translate('userDirectories.trustStore', 'USR', 'Trust store'),
                                labelWidth: 250,
                                required: true,
                                store: 'Usr.store.TrustStores',
                                editable: false,
                                disabled: false,
                                // emptyText: Uni.I18n.translate('userDirectories.securityProtocolPrompt', 'USR', 'Select a security protocol...'),
                                allowBlank: true,
                                displayField: 'name',
                                valueField: 'id'
                            },
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
            protocolSourceRadio = me.down('#rdo-security-protocol-source'),
            addUserDirectoryForm = me.down('#frm-add-user-directory'),
            certificateCombo = me.down('#cbo-certificate-alias'),
            trustStoreCombo = me.down('#cbo-trust-store');

        addUserDirectoryForm.loadRecord(record);

        protocolSource.setVisible(record.get('securityProtocol') !== 'NONE');
        if (record.get('certificateAlias')) {
            protocolSourceRadio.setValue({source: 'certificates'});
            certificateCombo.setRawValue(record.get('certificateAlias'));
        } else if (record.get('trustStore')) {
            protocolSourceRadio.setValue({source: 'trustStores'});
            trustStoreCombo.getStore().load({
                callback: function () {
                    trustStoreCombo.setValue(record.get('trustStore').id);
                }
            })
        }
    }
});

