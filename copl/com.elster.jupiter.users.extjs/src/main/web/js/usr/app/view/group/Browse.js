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
                    // No need for Uni.view.container.EmptyGridContainer, at least one role (Administrators) is created at system install
                    xtype: 'groupList'
                },
                {
                    xtype: 'groupDetails'
                }
            ]
        }
    ]
});