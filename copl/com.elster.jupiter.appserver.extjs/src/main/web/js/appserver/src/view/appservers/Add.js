/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appservers-add',
    requires: [
        'Uni.util.FormErrorMessage',
        'Apr.view.appservers.MessageServicesGrid',
        'Apr.view.appservers.ImportServicesGrid',
        'Apr.view.appservers.WebserviceEndpointsGrid',
        'Ext.toolbar.Spacer'
    ],
    edit: false,
    returnLink: null,
    webserviceStore: null,
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
        if (this.returnLink) {
            this.down('#cancel-link').href = this.returnLink;
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
                focusOnToFront: true,
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
                        maxLength: 80,
                        listeners: {
                            afterrender: function (field) {
                                field.focus(false, 200);
                            }
                        }
                    },
                    {
                        xtype: 'textfield',
                        name: 'exportDirectory',
                        itemId: 'appserver-export-path',
                        width: 750,
               //         maskRe: /\S/,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.exportPath', 'APR', 'Export path'),
                        allowBlank: false
                    },
                    {
                        xtype: 'textfield',
                        name: 'importDirectory',
                        itemId: 'appserver-import-path',
                        width: 750,
                //        maskRe: /\S/,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.importPath', 'APR', 'Import path'),
                        allowBlank: false
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                        itemId: 'messageServicesContainer',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'container',
                                width: '100%',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'component',
                                        html: Uni.I18n.translate('general.noMessageServices','APR','No message services have been added'),
                                        itemId: 'empty-text-grid',
                                        style: {
                                            'font': 'italic 13px/17px Lato',
                                            'color': '#686868',
                                            'margin-top': '6px',
                                            'margin-right': '10px'
                                        },
                                        hidden: true
                                    },
                                    {
                                        xtype: 'component',
                                        itemId: 'apr-add-msg-services-push-to-right-component',
                                        flex: 1
                                    },
                                    {
                                        itemId: 'add-message-services-button',
                                        xtype: 'button',
                                        margin: '0 0 10 0',
                                        text: Uni.I18n.translate('general.addMessageServices', 'APR', 'Add message services')
                                    }
                                ]
                            },
                            {
                                xtype: 'message-services-grid',
                                itemId: 'message-services-grid',
                                store: me.store
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.importServices', 'APR', 'Import services'),
                        itemId: 'importServicesContainer',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'container',
                                width: '100%',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'component',
                                        itemId: 'import-empty-text-grid',
                                        html: Uni.I18n.translate('appServers.noImportServices', 'APR', "No import services have been added"),
                                        style: {
                                            'font': 'italic 13px/17px Lato',
                                            'color': '#686868',
                                            'margin-top': '6px',
                                            'margin-right': '10px'
                                        },
                                        hidden: true
                                    },
                                    {
                                        xtype: 'component',
                                        itemId: 'apr-add-imp-services-push-to-right-component',
                                        flex: 1
                                    },
                                    {
                                        itemId: 'add-import-services-button',
                                        xtype: 'button',
                                        margin: '0 0 10 0',
                                        text: Uni.I18n.translate('general.addImportServices', 'APR', 'Add import services'),
                                    }
                                ]
                            },
                            {
                                xtype: 'apr-import-services-grid',
                                itemId: 'apr-import-services-grid',
                                store: me.importStore
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.webserviceEnpoints', 'APR', 'Web service endpoints'),
                        itemId: 'webserviceEndpointsContainer',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'container',
                                width: '100%',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'component',
                                        itemId: 'webservice-empty-text-grid',
                                        html: Uni.I18n.translate('appServers.noWebServiceEndpoints', 'APR', "No web service endpoints have been added"),
                                        style: {
                                            'font': 'italic 13px/17px Lato',
                                            'color': '#686868',
                                            'margin-top': '6px',
                                            'margin-right': '10px'
                                        },
                                        hidden: true
                                    },
                                    {
                                        xtype: 'component',
                                        itemId: 'apr-add-webservices-push-to-right-component',
                                        flex: 1
                                    },
                                    {
                                        itemId: 'add-webservices-button',
                                        xtype: 'button',
                                        margin: '0 0 10 0',
                                        text: Uni.I18n.translate('general.addWebserviceEndpoints', 'APR', 'Add web service endpoints'),
                                    }
                                ]
                            },
                            {
                                xtype: 'apr-web-service-endpoints-grid',
                                itemId: 'apr-webservice-endpoints-grid',
                                store: me.webserviceStore
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
                    }

                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit);
    }
});
