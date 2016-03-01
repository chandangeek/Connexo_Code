Ext.define('Mdc.usagepointmanagement.view.UsagePointActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.usage-point-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [],


    setProcessMenu: function (usagepointID, router) {
        var me = this;
        if (Mdc.privileges.Device.canViewProcessMenu()) {

            me.add({
                itemId: 'action-menu-item-start-proc',
                privileges: Mdc.privileges.Device.deviceProcesses && Mdc.privileges.Device.deviceExecuteProcesses,
                text: Uni.I18n.translate('deviceconfiguration.process.startProcess', 'MDC', 'Start process'),
                href: '#/usagepoints/' + encodeURIComponent(usagepointID) + '/processes/start'
            })

            me.up('#usage-point-landing-actions-btn').show();
        }
    },

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
    }
});


