Ext.define('Apr.view.appservers.AppServerMessageServices', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.appserver-message-services',
    requires: [
        'Apr.view.appservers.Menu'
    ],

    router: null,
    appServerName: null,

    initComponent: function () {
        var me = this;
        me.content = {
            //xtype: 'panel',
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
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-app-server',
                        title: Uni.I18n.translate('addMessageServices.empty.title', 'APR', 'No message services found'),
                        reasons: [
                            Uni.I18n.translate('addMessageServices.empty.list.item1', 'APR', 'There are no message services in the system')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addMessageServices', 'APR', 'Add message services'),
                                itemId: 'empty-grid-add-app-server',
                                privileges: Apr.privileges.AppServer.admin,
                                href: '#/administration/appservers/add'
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