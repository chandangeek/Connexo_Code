Ext.define('Mdc.view.setup.comserver.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.comserversidemenu',
    title: Uni.I18n.translate('general.comServer', 'MDC', 'Communication server'),
    initComponent: function () {
        var me = this,
            serverId = me.serverId;
        me.menuItems = [
            {
                itemId: 'comserverLink',
                href: '#/administration/comservers/' + serverId + '/overview'
            },
            {
                text: Uni.I18n.translate('comserver.sidemenu.comports', 'MDC', 'Communication ports'),
                itemId: 'commportsLink',
                href: '#/administration/comservers/' + serverId + '/comports'
            }
        ];
        me.callParent(arguments);
    }
});