Ext.define('Dxp.view.tasks.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-details',
    requires: [
        'Dxp.view.tasks.Menu',
        'Dxp.view.tasks.PreviewForm'
    ],

    side: [
        {
            xtype: 'panel',
            title: Uni.I18n.translate('general.dataExportTasks', 'DXP', 'Data export tasks'),
            ui: 'medium',
            items: [
                {
                    xtype: 'tasks-menu',
                    itemId: 'tasks-details-menu',
                    toggle: 0
                }
            ]
        }
    ],

    content: {
        ui: 'large',
        title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
        items: {
            xtype: 'tasks-preview-form',
            margin: '0 0 0 100'
        }
    }
});

