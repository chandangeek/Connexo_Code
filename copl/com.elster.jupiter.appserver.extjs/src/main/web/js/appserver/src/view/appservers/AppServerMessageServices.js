/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.AppServerMessageServices', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appserver-message-services',
    requires: [
        'Apr.view.appservers.Menu',
        'Apr.view.appservers.MessageServicePreview'
    ],

    router: null,
    appServerName: null,

    initComponent: function () {
        var me = this;
        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'message-services-grid',
                        itemId: 'message-services-grid',
                        dockedItems: [
                            {
                                xtype: 'pagingtoolbartop',
                                store: me.store,
                                dock: 'top',
                                displayMsg: '',
                                displayMoreMsg: '',
                                emptyMsg: '',
                                items: [
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.saveChanges', 'APR', 'Save changes'),
                                        privileges: Apr.privileges.AppServer.admin,
                                        itemId: 'save-message-services-settings',
                                        disabled: true
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.undo', 'APR', 'Undo'),
                                        privileges: Apr.privileges.AppServer.admin,
                                        itemId: 'undo-message-services-settings',
                                        disabled: true
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.addMessageServices', 'APR', 'Add message services'),
                                        privileges: Apr.privileges.AppServer.admin,
                                        itemId: 'add-message-services-button-from-details',
                                        disabled: true
                                    }
                                ]
                            }
                        ],
                        store: this.store
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-app-server',
                        title: Uni.I18n.translate('addMessageServices.empty.title', 'APR', 'No message services found'),
                        reasons: [
                            Uni.I18n.translate('addMessageServices.empty.list.item1', 'APR', 'No message services have been added yet')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.saveChanges', 'APR', 'Save changes'),
                                privileges: Apr.privileges.AppServer.admin,
                                itemId: 'apr-no-msg-services-save-settings-btn',
                                disabled: true
                            },
                            {
                                text: Uni.I18n.translate('general.undo', 'APR', 'Undo'),
                                privileges: Apr.privileges.AppServer.admin,
                                itemId: 'apr-no-msg-services-undo-btn',
                                disabled: true
                            },
                            {
                                text: Uni.I18n.translate('general.addMessageServices', 'APR', 'Add message services'),
                                privileges: Apr.privileges.AppServer.admin,
                                itemId: 'apr-no-msg-services-add-one-btn'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'msg-service-preview',
                        itemId: 'apr-msg-service-preview'
                    }
                }
            ]


        };
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'appservers-menu',
                        itemId: 'apr-menu',
                        router: me.router,
                        appServerName: me.appServerName
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});