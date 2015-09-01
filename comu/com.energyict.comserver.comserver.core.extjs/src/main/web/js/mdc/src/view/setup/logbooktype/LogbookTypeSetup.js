Ext.define('Mdc.view.setup.logbooktype.LogbookTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.logbookTypeSetup',
    itemId: 'logbookTypeSetup',

    requires: [
        'Mdc.view.setup.logbooktype.LogbookTypeGrid',
        'Mdc.view.setup.logbooktype.LogbookTypePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'logbookTypeGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'empty-grid-logbooktypes',
                        title: Uni.I18n.translate('logbooktype.empty.title', 'MDC', 'No logbook types found'),
                        reasons: [
                            Uni.I18n.translate('logbooktype.empty.list.item1', 'MDC', 'No logbook types have been defined yet.')
                        ],
                        stepItems: [
                            {
                                //itemId: 'logbookTypeCreateActionButton',
                                text: Uni.I18n.translate('logbooktype.add', 'MDC', 'Add logbook type'),
                                privileges: Mdc.privileges.MasterData.admin,
                                action: 'createLogbookType'
                                //href: '#/administration/logbooktypes/create'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'logbookTypePreview'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});


