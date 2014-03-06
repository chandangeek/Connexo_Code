Ext.define('Mdc.view.setup.comportpool.ComPortPoolsSetup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comPortPoolsSetup',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: 0,

    requires: [
        'Mdc.view.setup.comportpool.ComPortPoolsGrid',
        'Mdc.view.setup.comportpool.ComPortPoolPreview'
    ],

    initComponent: function () {
        this.items = [
            {
                xtype: 'comPortPoolsGrid',
                padding: 10,
                border: 0
            },
            {
                xtype: 'comPortPoolPreview',
                padding: 10,
                border: 0
            }
        ];
        this.callParent(arguments);
    }
});
