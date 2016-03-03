Ext.define('Mdc.usagepointmanagement.view.AddUsagePoint', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-usage-point-setup',
    itemId: 'add-usage-point-setup',
    requires: [
        'Mdc.usagepointmanagement.view.AddUsagePointForm'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.content =[
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'add-usage-point-panel',
                title: Uni.I18n.translate('general.addUsagePoint', 'MDC', 'Add usage point'),
                //layout: {
                //    type: 'fit',
                //    align: 'stretch'
                //},
                items: [
                    {
                        xtype : 'add-usage-point-form'
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});