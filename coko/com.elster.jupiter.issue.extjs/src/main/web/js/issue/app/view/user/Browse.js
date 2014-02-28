Ext.define('Mtr.view.user.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.userBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Mtr.view.user.List'
    ],

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Users</h1>'
                },
                {
                    xtype: 'userList'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});