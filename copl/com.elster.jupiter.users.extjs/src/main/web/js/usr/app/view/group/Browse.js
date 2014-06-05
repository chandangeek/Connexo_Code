Ext.define('Usr.view.group.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.groupBrowse',
    itemId: 'groupBrowse',
    overflowY: 'auto',
    requires: [
        'Usr.view.group.List',
        'Usr.view.group.Details',
        'Ext.panel.Panel'
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
                         xtype: 'component',
                         html: 'There are no items'
                    },
                    previewComponent: {
                         xtype: 'groupDetails'
                    }
                }
            ]
        }
    ]
});