Ext.define('Idv.view.MainOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-validation-main-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            title: Uni.I18n.translate('general.overview','IDV','overview'),
            xtype: 'menu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('issue.workspace.datavalidation', 'IDV', 'Data validation'),
            flex: 1
        }
    ]
});