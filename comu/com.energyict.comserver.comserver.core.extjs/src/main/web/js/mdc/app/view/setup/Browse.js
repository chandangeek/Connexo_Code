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
                            html: '<a href="#/setup/comservers">Comservers</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/comportpools">Communication port pools</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/devicecommunicationprotocols">Device communication protocols</a>'
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
                            html: '<a href="#/setup/devicetypes">Device types</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/registertypes">Register types</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/registergroups">Register groups</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/communicationschedules">Communication schedules</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/searchitems">Search items</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/setup/logbooktypes">Logbook types</a>'
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