Ext.define('Mdc.usagepointmanagement.view.AddUsagePoint', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-usage-point-setup',
    itemId: 'add-usage-point-setup',
    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.usagepointmanagement.view.AddUsagePointForm',
        'Mdc.usagepointmanagement.view.EditUsagePointForm'
    ],
    router: null,
    usagePointId: null,

    initComponent: function () {
        var me = this;

        me.content =[
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'add-usage-point-panel',
                title: me.edit ? Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", me.usagePointId) : Uni.I18n.translate('general.addUsagePoint', 'MDC', 'Add usage point'),
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'errors',
                        hidden: true,
                        width: 600,
                        margin: '0 0 10 0'
                    },
                    {
                        xtype : me.edit ? 'edit-usage-point-form' : 'add-usage-point-form' ,
                        itemId: 'add-edit-form',
                        router: me.router,
                        usagePointId: me.usagePointId
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});