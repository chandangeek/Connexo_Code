Ext.define('Mdc.view.setup.logbooktype.Item', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.logbook-item',
    frame: true,

    tools: [
        {
            itemId: 'actions',
            xtype: 'button',
            text: 'Actions',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'logbook-action-menu'
            }
        }
    ],

    items: [
        {
            itemId: 'loogbookDetails',
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
                            itemId: 'name',
                            xtype: 'displayfield',
                            fieldLabel: 'Name',
                            name: 'name'
                        }
                    ]
                },
                {
                    items: [
                        {
                            itemId: 'obis',
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
