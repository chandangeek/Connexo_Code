Ext.define('Dsh.view.Main', {
    alias: 'widget.app-main',
    extend: 'Ext.container.Container',
    requires: [
        'Ext.tab.Panel',
        'Ext.layout.container.Border'
    ],
    layout: {
        type: 'border'
    },
    items: [
        {
            region: 'west',
            xtype: 'panel',
            title: 'west',
            width: 150
        },
        {
            region: 'center',
            xtype: 'tabpanel',
            items: [
                {
                    title: 'Center Tab 1'
                }
            ]
        }
    ]
});