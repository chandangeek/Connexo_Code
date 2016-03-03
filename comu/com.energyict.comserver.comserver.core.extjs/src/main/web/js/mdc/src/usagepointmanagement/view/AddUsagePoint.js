Ext.define('Mdc.usagepointmanagement.view.AddUsagePoint', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-usage-point-setup',
    itemId: 'add-usage-point-setup',
    requires: [
        'Uni.util.FormErrorMessage',
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
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'errors',
                        hidden: true,
                        width: 600,
                        margin: '0 0 10 0'
                    },
                    {
                        xtype : 'add-usage-point-form'
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});