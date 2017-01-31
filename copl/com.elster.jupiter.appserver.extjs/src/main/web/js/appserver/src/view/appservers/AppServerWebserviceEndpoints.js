/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.AppServerWebserviceEndpoints', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appserver-webservices',
    requires: [
        'Apr.view.appservers.Menu',
        'Apr.view.appservers.AddWebserviceEndpointsGrid',
        'Apr.view.appservers.WebserviceEndpointPreview'
    ],

    router: null,
    appServerName: null,
    needLink: false,


    initComponent: function () {
        var me = this;
        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.webserviceEndpoits', 'APR', 'Web service endpoints'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'apr-web-service-endpoints-grid',
                        itemId: 'apr-web-service-endpoints-grid',
                        router: me.router,
                        needLink: me.needLink,
                        viewConfig: {
                            markDirty: true
                        },
                        store: me.store,
                        dockedItems: [
                            {
                                xtype: 'pagingtoolbartop',
                                dock: 'top',
                                displayMsg: '',
                                displayMoreMsg: '',
                                emptyMsg: '',
                                items: [
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.saveChanges', 'APR', 'Save changes'),
                                        itemId: 'save-webservices-settings-button',
                                        privileges: Apr.privileges.AppServer.admin,
                                        disabled: true
                                    },
                                    {
                                        text: Uni.I18n.translate('general.undo', 'APR', 'Undo'),
                                        privileges: Apr.privileges.AppServer.admin,
                                        itemId: 'undo-webservices-button',
                                        disabled: true
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.addWebserviceEndpoints', 'APR', 'Add web service endpoints'),
                                        itemId: 'add-webservices-button-from-detail',
                                        privileges: Apr.privileges.AppServer.admin,
                                        disabled: true
                                    }
                                ]
                            }
                        ],
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('general.webserviceEndpoints.empty.title', 'APR', 'No web service endpoints found.'),
                        reasons: [
                            Uni.I18n.translate('general.webserviceEndpoints.empty.list.item1', 'APR', 'No web service endpoints have been added yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.saveChanges', 'APR', 'Save changes'),
                                privileges: Apr.privileges.AppServer.admin,
                                itemId: 'apr-no-webservices-save-settings-btn',
                                disabled: true
                            },
                            {
                                text: Uni.I18n.translate('general.undo', 'APR', 'Undo'),
                                privileges: Apr.privileges.AppServer.admin,
                                itemId: 'apr-no-webservices-undo-btn',
                                disabled: true
                            },
                            {
                                text: Uni.I18n.translate('general.addWebserviceEndpoints', 'APR', 'Add web service endpoints'),
                                itemId: 'add-webservices-button-from-detail-empty',
                                privileges: Apr.privileges.AppServer.admin
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'webservice-preview',
                        itemId: 'pnl-web-service-preview',
                        router: me.router,
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