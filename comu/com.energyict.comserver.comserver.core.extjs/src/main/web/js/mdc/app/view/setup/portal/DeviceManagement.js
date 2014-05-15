Ext.define('Mdc.view.setup.portal.DeviceManagement', {
    extend: 'Ext.container.Container',
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
            html: '<a href="#/setup/searchitems">Search items</a>'
        },
        {
            xtype: 'component',
            html: '<a href="#/setup/logbooktypes">Logbook types</a>'
        }
    ]
});