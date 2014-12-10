Ext.define('Mdc.view.setup.devicegroup.Menu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.devicegroups-menu',
    toggle: null,
    router: null,

    deviceGroupId: null,

    initComponent: function () {
        var me = this;
        me.callParent(me);

        me.add(
            {
                text: Uni.I18n.translate('general.overview', 'DES', 'Overview'),
                itemId: 'devicegroups-view-link',
                href:  '#/devices/devicegroups/' + this.deviceGroupId,
                hrefTarget: '_self'
            }
        );

        me.toggleMenuItem(me.toggle);
    }
});


