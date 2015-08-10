Ext.define('Mdc.view.setup.devicegroup.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.devicegroups-menu',

    deviceGroupId: null,

    title: Uni.I18n.translate('general.deviceGroups', 'MDC', 'Device groups'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                itemId: 'devicegroups-view-link',
                href:  '#/devices/devicegroups/' + me.deviceGroupId
            }
        ];

        me.callParent(arguments);
    }
});


