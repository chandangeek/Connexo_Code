Ext.define('Cfg.view.validationtask.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-validation-tasks-setup',

    router: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.view.validationtask.Menu',
        'Cfg.view.validationtask.Grid',
        'Cfg.view.validationtask.Preview',
        'Cfg.view.validationtask.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('dataValidationTasks.general.dataValidationTasks', 'CFG', 'Data validation tasks'),
            items: [					
                {
					
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'tasks-grid',
                        itemId: 'grd-data-validation-tasks',
                        router: me.router
                    },
					
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-validation-task',
                        title: Uni.I18n.translate('dataValidationTasks.empty.title', 'CFG', 'No data validation tasks found'),
                        reasons: [
                            Uni.I18n.translate('dataValidationTasks.empty.list.item1', 'CFG', 'No data validation tasks have been defined yet.'),
                            Uni.I18n.translate('dataValidationTasks.empty.list.item2', 'CFG', 'Data validation tasks exist, but you do not have permission to view them.'),
                            Uni.I18n.translate('dataValidationTasks.empty.list.item3', 'CFG', 'The filter is too narrow.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('dataValidationTasks.general.addDataValidationTask', 'CFG', 'Add data validation task'),
                                privileges:['privilege.administrate.validationConfiguration'],
                                ui: 'action',
                                href: '#/administration/datavalidationtasks/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'tasks-preview',
                        itemId: 'pnl-data-validation-task-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});