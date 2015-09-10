Ext.define('Usr.view.userDirectory.AddUserDirectory', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usr-add-user-directory',
    requires: [
        'Uni.util.FormErrorMessage',
        'Usr.store.SecurityProtocols'
    ],

    edit: false,
    importServiceRecord: null,
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
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'frm-add-user-directory',
                ui: 'large',
                width: '100%',
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
                        width: 400,
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'txt-name',
                        required: true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('general.name', 'USR', 'Name')
                    },
                    {
                        xtype: 'textfield',
                        name: 'prefix',
                        itemId: 'txt-prefix',
                        required: true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('userDirectories.prefix', 'USR', 'Prefix')
                    },
                    {
                        xtype: 'textfield',
                        name: 'url',
                        itemId: 'txt-url',
                        required: true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('userDirectories.url', 'USR', 'Url')
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
                                emptyText: Uni.I18n.translate('userDirectories.securityProtocolPrompt', 'USR', 'Select a security protocol..'),
                                allowBlank: false,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'value'
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
                        xtype: 'radiogroup',
                        required: true,
                        fieldLabel: Uni.I18n.translate('userDirectories.type', 'USR', 'Type'),
                        columns: 1,
                        vertical: true,
                        defaults: {
                            name: 'type'
                        },
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('userDirectories.type.activeDirectory', 'USR', 'Active directory'),
                                inputValue: 'ACD',
                                checked:true
                            },
                            {
                                boxLabel: Uni.I18n.translate('userDirectories.type.apacheDS', 'USR', 'Apache DS'),
                                inputValue: 'APD'
                            }
                        ]
                    },
                    {
                        xtype: 'textfield',
                        name: 'backupUrl',
                        itemId: 'txt-backupUrl',
                        fieldLabel: Uni.I18n.translate('userDirectories.backupURL', 'USR', 'Backup URL')
                    },
                    {
                        xtype: 'textfield',
                        name: 'baseUser',
                        itemId: 'txt-baseUser',
                        fieldLabel: Uni.I18n.translate('userDirectories.baseUser', 'USR', 'Base user')
                    },
                    {
                        xtype: 'textfield',
                        name: 'baseGroup',
                        itemId: 'txt-baseGroup',
                        fieldLabel: Uni.I18n.translate('userDirectories.baseGroup', 'USR', 'Base group')
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        margin: '20 0 0 0',
                        fieldLabel: '&nbsp',
                        labelAlign: 'right',
                        labelWidth: 260,
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
    }
});

