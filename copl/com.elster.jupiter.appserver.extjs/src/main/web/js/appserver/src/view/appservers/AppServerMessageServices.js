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
                        minHeight: 250,
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
                                        text: Uni.I18n.translate('general.saveMessageServicesSettings', 'APR', 'Save settings'),
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
                                        itemId: 'add-message-services-button-from-details'
                                    }
                                ]
                            }
                        ],
                        store: this.store
                    },
                    hasNotEmptyComponent: true,
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