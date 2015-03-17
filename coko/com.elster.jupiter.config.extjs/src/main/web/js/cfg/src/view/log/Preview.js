Ext.define('Cfg.view.log.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.log-preview',
    router: null,
    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            itemId: 'log-preview-form',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200,
                labelAlign: 'right'
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('dataValidationTasks.general.name', 'CFG', 'Name'),
                    name: 'name',
                    renderer: function (value) {
                        var url = me.router.getRoute('administration/datavalidationtasks/datavalidationtask').buildUrl();
                        return '<a href="' + url + '">' + value + '</a>';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('dataValidationTasks.log.runStartedOn', 'CFG', 'Run started on'),
                    itemId: 'run-started-on'
                },
                {
                    fieldLabel: Uni.I18n.translate('dataValidationTasks.general.status', 'CFG', 'Status'),
                    name: 'status'
                }
            ]
        };
        me.callParent(arguments);
    }
});
