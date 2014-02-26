Ext.define('Mdc.view.setup.Browse', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.setupBrowse',
    overflowY: 'auto',
    cls: 'content-container',
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
                            html: '<a href="#/setup/comservers">comservers</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/comportpools">communication port pools</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/devicecommunicationprotocols">device communication protocols</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/licensedprotocols">licensed protocols</a>'
                        }
                    ],
                    columnWidth: 0.5,
                    margin: '10 5 10 10'
                },
                {
                    xtype: 'fieldset',
                    collapsible: true,
                    title: 'Device management',
                    padding: 10,
                    layout: 'vbox',
                    items: [
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/devicetypes">devicetypes</a>'
                        }
                    ],
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