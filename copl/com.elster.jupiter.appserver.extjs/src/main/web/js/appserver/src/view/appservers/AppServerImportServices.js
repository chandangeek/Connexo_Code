/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.AppServerImportServices', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appserver-import-services',
    requires: [
        'Apr.view.appservers.Menu'
    ],

    router: null,
    appServerName: null,



    initComponent: function () {
        var me = this;
        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.importServices', 'APR', 'Import services'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'apr-import-services-grid',
                        itemId: 'apr-import-services-grid',
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
                                        itemId: 'save-import-services-settings-button',
                                        privileges: Apr.privileges.AppServer.admin,
                                        disabled: true
                                    },
                                    {
                                        text: Uni.I18n.translate('general.undo', 'APR', 'Undo'),
                                        privileges: Apr.privileges.AppServer.admin,
                                        itemId: 'undo-import-services-button',
                                        disabled: true
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.addImportServices', 'APR', 'Add import services'),
                                        itemId: 'add-import-services-button-from-detail',
                                        privileges: Apr.privileges.AppServer.admin,
                                        disabled: true
                                    }
                                ]
                            }
                        ],
                        store: this.store
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('general.importServices.empty.title', 'APR', 'No import services found.'),
                        reasons: [
                            Uni.I18n.translate('general.importServices.empty.list.item1', 'APR', 'No import services have been added yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.saveChanges', 'APR', 'Save changes'),
                                privileges: Apr.privileges.AppServer.admin,
                                itemId: 'apr-no-imp-services-save-settings-btn',
                                disabled: true
                            },
                            {
                                text: Uni.I18n.translate('general.undo', 'APR', 'Undo'),
                                privileges: Apr.privileges.AppServer.admin,
                                itemId: 'apr-no-imp-services-undo-btn',
                                disabled: true
                            },
                            {
                                text: Uni.I18n.translate('general.addImportServices', 'APR', 'Add import services'),
                                itemId: 'add-import-services-button-from-detail-empty',
                                privileges: Apr.privileges.AppServer.admin
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'import-service-preview',
                        itemId: 'pnl-import-service-preview',
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