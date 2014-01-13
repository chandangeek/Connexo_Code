Ext.define('Mdc.view.setup.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.setupBrowse',
    overflowY: 'auto',
    padding: 10,
    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'column'
            },
            items: [
                {
                    xtype: 'fieldset',
                    collapsible: true,
                    title: 'RMR',
                    padding: 10,
                    layout: 'vbox',
                    items: [
                        {
                            xtype: 'component',
                            cls: 'content-container',
                            html: '<a href="#/setup/comservers">comservers</a>'
                        },
                        {
                            xtype: 'component',
                            cls: 'content-container',
                            html: '<a href="#/setup/comportpools">communication port pools</a>'
                        },
                        {
                            xtype: 'component',
                            cls: 'content-container',
                            html: '<a href="#/setup/devicecommunicationprotocols">device communication protocols</a>'
                        },
                        {
                            xtype: 'component',
                            cls: 'content-container',
                            html: '<a href="#/setup/licensedprotocols">licensed protocols</a>'
                        }
                    ],
                    columnWidth: 0.5,
                    margin: '10 5 10 10'
                },
                {
                    border: 0,
                    columnWidth: 0.5,
                    margin: '10 5 10 10'
                }

            ]

        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});