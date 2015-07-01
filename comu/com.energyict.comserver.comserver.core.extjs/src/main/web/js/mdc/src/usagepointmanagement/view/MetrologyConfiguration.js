Ext.define('Mdc.usagepointmanagement.view.MetrologyConfiguration', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configuration',
    itemId: 'metrology-configuration',
    title: Uni.I18n.translate('usagePointManagement.metrologyConfiguration', 'MDC', 'Metrology configuration'),
    router: null,
    ui: 'tile',
    layout: {
        type: 'hbox'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'panel',
                itemId: 'activationsArea',
                items: [
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.linkedDevices', 'MDC', 'Linked device'),
                        labelWidth: 100,
                        value: '-'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});