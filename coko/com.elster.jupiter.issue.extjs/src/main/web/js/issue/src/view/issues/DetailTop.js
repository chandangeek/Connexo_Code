Ext.define('Isu.view.issues.DetailTop', {
    extend: 'Ext.container.Container',
    alias: 'widget.issue-detail-top',
    requires: [
        'Isu.view.issues.ActionMenu'
    ],
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    items: [
        {
            itemId: 'issue-detail-top-title',
            title: '&nbsp;',
            ui: 'large',
            flex: 1
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'issues-action-menu',
                itemId: 'issue-detail-action-menu',
                predefinedItems: null
            },
            listeners: {
                click: function () {
                    this.showMenu();
                }
            }
        }
    ]
});