Ext.define('Mdc.usagepointmanagement.view.UsagePointActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.usage-point-action-menu',
    plain: true,
    border: false,
    shadow: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.addUsagePoint.editUsagePoint', 'MDC', 'Edit'),
                href: me.router.getRoute('usagepoints/usagepoint/edit').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});


