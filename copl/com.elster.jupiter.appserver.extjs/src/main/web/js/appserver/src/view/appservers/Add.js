Ext.define('Apr.view.appservers.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appservers-add',
    requires: [
        'Uni.util.FormErrorMessage',
        'Apr.view.appservers.MessageServicesGrid',
        'Apr.view.appservers.ImportServicesGrid',
        'Ext.toolbar.Spacer'
    ],
    edit: false,
    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#add-edit-button').setText(Uni.I18n.translate('general.save', 'APR', 'Save'));
            this.down('#add-edit-button').action = 'editAppServer';
        } else {
            this.edit = edit;
            this.down('#add-edit-button').setText(Uni.I18n.translate('general.add', 'APR', 'Add'));
            this.down('#add-edit-button').action = 'addAppServer';
        }
    },
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.addApplicationServer', 'APR', 'Add application server'),
                itemId: 'add-appserver-form',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 750
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'txt-appserver-name',
                        width: 750,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },
                    {
                        xtype: 'textfield',
                        name: 'exportPath',
                        itemId: 'appserver-export-path',
                        width: 750,
                        maskRe: /\S/,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.exportPath', 'APR', 'Export path'),
                        allowBlank: false
                    },
                    {
                        xtype: 'textfield',
                        name: 'importPath',
                        itemId: 'appserver-import-path',
                        width: 750,
                        maskRe: /\S/,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.importPath', 'APR', 'Import path'),
                        allowBlank: false
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                        layout: {
                            type:'vbox',
                            align:'right'
                        },
                        items: [
                            {
                                itemId: 'add-message-services-button',
                                xtype: 'button',
                                margin: '0 0 10 0',
                                text: Uni.I18n.translate('general.addMessageServices', 'APR', 'Add message services'),
                                menu: {
                                    itemId: 'add-message-services-menu',
                                    plain: true,
                                    border: false,
                                    shadow: false,
                                    items: []
                                }
                            },
                            {
                                xtype: 'message-services-grid',
                                itemId: 'message-services-grid',
                                store: me.store
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'empty-text-grid',
                                hidden: true,
                                value: Uni.I18n.translate('appServers.noMessageServices', 'APR', "This application server doesn't have any message service")
                            }

                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.importServices', 'APR', 'Import services'),
                        layout: {
                            type:'vbox',
                            align:'right'
                        },
                        items: [
                            {
                                itemId: 'add-import-services-button',
                                xtype: 'button',
                                margin: '0 0 10 0',
                                text: Uni.I18n.translate('general.addImportServices', 'APR', 'Add import services'),
                                menu: {
                                    itemId: 'add-import-services-menu',
                                    plain: true,
                                    border: false,
                                    shadow: false,
                                    items: []
                                }
                            },
                            {
                                xtype: 'apr-import-services-grid',
                                itemId: 'apr-import-services-grid',
                                store: me.importStore
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'import-empty-text-grid',
                                hidden: true,
                                value: Uni.I18n.translate('appServers.noImportServices', 'APR', "This application server doesn't have any import service")
                            }

                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'add-edit-button',
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('window.messabox.cancel', 'APR', 'Cancel'),
                                ui: 'link',
                                href: '#/administration/appservers/'
                            }
                        ]
                    },

                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit);
    },
    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    }
});
