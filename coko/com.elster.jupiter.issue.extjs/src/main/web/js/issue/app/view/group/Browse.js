Ext.define('Mtr.view.group.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.groupBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Mtr.view.group.List'
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
                    html: '<h1>Groups</h1>'
                },
                {
                    xtype: 'groupList'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});