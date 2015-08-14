Ext.define('Mdc.view.setup.comportpool.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.comportpoolsidemenu',
    title: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
    initComponent: function () {
        var me = this,
            poolId = me.poolId;
        me.menuItems = [
            {
                itemId: 'comportpoolLink',
                href: '#/administration/comportpools/' + poolId
            },
            {
                text: Uni.I18n.translate('comserver.sidemenu.comports', 'MDC', 'Communication ports'),
                itemId: 'commportsLink',
                href: '#/administration/comportpools/' + poolId + '/comports'
            }
        ];
        me.callParent(arguments)
    }
});