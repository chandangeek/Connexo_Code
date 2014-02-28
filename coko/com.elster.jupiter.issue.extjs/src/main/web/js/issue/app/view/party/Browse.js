Ext.define('Mtr.view.party.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.partyBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Mtr.view.party.List'
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
                    html: '<h1>Parties</h1>'
                },
                {
                    xtype: 'partyList'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});