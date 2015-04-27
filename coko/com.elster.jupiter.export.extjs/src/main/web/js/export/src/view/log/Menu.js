Ext.define('Dxp.view.log.Menu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.dxp-log-menu',
    toggle: null,
    router: null,

    initComponent: function () {
        var me = this;
        me.callParent(me);

        me.add(
            {
                text: Uni.I18n.translate('general.log', 'DES', 'Log'),
                itemId: 'tasks-log-link'
            }
        );

        me.toggleMenuItem(me.toggle);
    }
});


