Ext.define('Fim.view.log.Menu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.fim-log-menu',
    toggle: null,
    router: null,
    importServiceId: null,
    initComponent: function () {
        var me = this;
        me.callParent(me);
        me.add(
            {
                text: Uni.I18n.translate('importService.log', 'FIM', 'Log'),
                itemId: 'history-log-link'
            }
        );

        me.toggleMenuItem(me.toggle);
    }
});


