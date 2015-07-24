Ext.define('Cfg.view.validationtask.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.validation-tasks-setup',    
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.view.validationtask.Menu',
        'Cfg.view.validationtask.Grid',
        'Cfg.view.validationtask.Preview',
        'Cfg.view.validationtask.ActionMenu'
    ],
	router: null,
	
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('validationTasks.general.validationTasks', 'CFG', 'Validation tasks'),
            items: [					
                {					
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'cfg-validation-tasks-grid',
                        itemId: 'grd-validation-tasks',
                        router: me.router
                    },
					
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-validation-task',
                        title: Uni.I18n.translate('validationTasks.empty.title', 'CFG', 'No validation tasks found'),
                        reasons: [
                            Uni.I18n.translate('validationTasks.empty.list.item1', 'CFG', 'No validation tasks have been defined yet.'),
                            Uni.I18n.translate('validationTasks.empty.list.item2', 'CFG', 'Validation tasks exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('validationTasks.general.addValidationTask', 'CFG', 'Add validation task'),
                                privileges : Cfg.privileges.Validation.admin,
                                href: '#/administration/validationtasks/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'cfg-tasks-preview',
                        itemId: 'pnl-validation-task-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});