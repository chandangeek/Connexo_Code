Ext.define('Mdc.usagepointmanagement.view.MetrologyConfiguration', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configuration',
    itemId: 'metrology-configuration',
    title: Uni.I18n.translate('usagePointManagement.metrologyConfiguration', 'MDC', 'Metrology configuration'),
    router: null,
    ui: 'tile',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                labelAlign: 'right',
                xtype: 'fieldcontainer',
                labelWidth: 125,
                fieldLabel: Uni.I18n.translate('usagePointManagement.linkedDevices', 'MDC', 'Linked device'),
                layout: {
                    type: 'vbox'
                },
                itemId: 'metrologyLinkedDevice',
                items: []
            },
            {
                xtype: 'menuseparator',
                itemId: 'metrologySeparator',
                margin: '0 0 20px 0',
                hidden: true
            },
            {
                labelAlign: 'right',
                xtype: 'fieldcontainer',
                labelWidth: 125,
                fieldLabel: Uni.I18n.translate('general.history', 'MDC', 'History'),
                layout: {
                    type: 'vbox'
                },
                itemId: 'metrologyHistory',
                hidden: true,
                items: []
            }

        ];
        me.callParent(arguments);
    }
});