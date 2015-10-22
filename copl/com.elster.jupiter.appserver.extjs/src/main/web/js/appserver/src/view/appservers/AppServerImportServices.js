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
            title: Uni.I18n.translate('general.importServices', 'APR', 'Import Services'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'apr-import-services-grid',
                        itemId: 'apr-import-services-grid',
                        store: me.store,
                        dockedItems: [
                            {
                                xtype: 'pagingtoolbartop',
                                dock: 'top',
                                displayMsg: '',
                                displayMoreMsg: '',
                                emptyMsg: '',
                                exportButton: false,
                                items: [
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.saveSettings', 'APR', 'Save settings'),
                                        itemId: 'save-import-services-settings-button',
                                        privileges: Apr.privileges.AppServer.admin,
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
                        title: Uni.I18n.translate('validation.importServices.empty.title', 'APR', 'No import services found.'),
                        reasons: [
                            Uni.I18n.translate('validation.importServices.empty.list.item1', 'APR', 'No import services have been added to the application server.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addImportServices', 'APR', 'Add import services'),
                                itemId: 'add-import-services-button-from-details',
                                privileges: Apr.privileges.AppServer.admin
                            }
                        ]
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