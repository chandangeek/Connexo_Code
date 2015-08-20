Ext.define('Est.estimationtasks.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Est.estimationtasks.view.Grid',
        'Est.estimationtasks.view.Preview'
    ],

    alias: 'widget.estimationtasks-overview',
    router: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('estimationtasks.title', 'EST', 'Estimation tasks'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'estimationtasks-grid',
                        itemId: 'estimationtasks-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-items-found-panel',
                        title: Uni.I18n.translate('estimationtasks.empty.title', 'EST', 'No estimation tasks found'),
                        reasons: [
                            Uni.I18n.translate('estimationtasks.empty.list.item1', 'EST', 'No estimation tasks have been defined yet.'),
                            Uni.I18n.translate('estimationtasks.empty.list.item2', 'EST', 'Estimation tasks exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('estimationtasks.general.addEstimationTask', 'EST', 'Add estimation task'),
                                privileges: Est.privileges.EstimationConfiguration.administrateTask,
                                href: '#/administration/estimationtasks/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'estimationtasks-preview',
                        itemId: 'estimationtasks-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
