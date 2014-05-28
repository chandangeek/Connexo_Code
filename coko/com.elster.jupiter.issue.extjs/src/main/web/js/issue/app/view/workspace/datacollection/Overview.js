Ext.define('Isu.view.workspace.datacollection.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.datacollection-overview',

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
            title: 'Data collection',
            flex: 1
        }
    ]
});