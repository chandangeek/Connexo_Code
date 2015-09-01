Ext.define('Idc.view.MainOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-collection-main-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            title: Uni.I18n.translate('general.overview','IDC','Overview'),
            xtype: 'menu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('issue.workspace.datacollection', 'IDC', 'Data collection'),
            flex: 1
        }
    ]
});