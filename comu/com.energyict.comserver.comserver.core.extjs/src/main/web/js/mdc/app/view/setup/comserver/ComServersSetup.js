Ext.define('Mdc.view.setup.comserver.ComServersSetup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comServersSetup',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: 0,

    requires: [
        'Mdc.view.setup.comserver.ComServersGrid',
        'Mdc.view.setup.comserver.ComServerPreview'
    ],

    initComponent: function () {
        this.items = [
            {
                xtype: 'comServersGrid',
                padding: 10,
                border: 0
             },
            {
                xtype: 'comServerPreview',
                padding: 10,
                border: 0
            }
        ];
        this.callParent(arguments);
    }
});

