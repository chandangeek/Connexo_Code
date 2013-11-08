Ext.define('Mdc.view.setup.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.setupBrowse',
    overflowY: 'auto',
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
                    cls: 'content-container',
                    html: '<a href="#/setup/comservers">comservers</a>'
                },

                {
                    xtype: 'component',
                    cls: 'content-container',
                    html: '<a href="#/setup/devicecommunicationprotocols">device communication protocols</a>'
                }
            ]

        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});