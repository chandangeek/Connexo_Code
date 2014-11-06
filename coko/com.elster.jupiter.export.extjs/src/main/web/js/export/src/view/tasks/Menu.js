Ext.define('Dxp.view.tasks.Menu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.tasks-menu',
    toggle: null,

    initComponent: function () {
        var me = this;
        me.callParent(me);
        me.add(
            {
                text: Uni.I18n.translate('general.overview', 'DES', 'Overview'),
                itemId: 'tasks-view-link',
                href: '#/administration/dataexporttasks',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('general.history', 'DES', 'History'),
                itemId: 'tasks-history-link',
                hidden: true,
                href: '#/administration/dataexporttasks',
                hrefTarget: '_self'
            }
        );
        me.toggleMenuItem(me.toggle);
    }
});

