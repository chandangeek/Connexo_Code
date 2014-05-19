Ext.define('Mdc.view.setup.logbooktype.Item', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.logbook-item',
    frame: true,

    tools: [
        {
            xtype: 'button',
            text: 'Actions',
            iconCls: 'x-uni-action-iconA',
            menu: {
                xtype: 'logbook-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            name: 'logbookDetails',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Name',
                            name: 'name'
                        }
                    ]
                },
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'OBIS code',
                            name: 'obis'
                        }
                    ]
                }
            ]
        }
    ]
});
