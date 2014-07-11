Ext.define('Usr.view.group.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.groupBrowse',
    itemId: 'groupBrowse',
    overflowY: 'auto',
    requires: [
        'Usr.view.group.List',
        'Usr.view.group.Details',
        'Ext.panel.Panel',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('group.title', 'USM', 'Roles'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'groupList'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('group.Browse.NoItemsFoundPanel.title', 'USR', 'No groups found')
                    },
                    previewComponent: {
                        xtype: 'groupDetails'
                    }
                }
            ]
        }
    ]
});