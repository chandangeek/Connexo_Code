Ext.define('Imt.usagepointgroups.view.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usagepointgroups-menu',
    xtype: 'usagepointgroups-menu',
    usagePointGroupId: null,
    title: Uni.I18n.translate('general.usagePointGroups', 'IMT', 'Usage point groups'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
                itemId: 'usagepointgroups-view-link',
                href:  me.router.getRoute('usagepoints/usagepointgroups').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});


