Ext.define('Isu.view.workspace.datavalidation.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.datavalidation-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            title: 'overview',
            xtype: 'menu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('workspace.datavalidation.overview.title', 'ISE', 'Data validation'),
            flex: 1
        }
    ]
});