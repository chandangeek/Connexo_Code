Ext.define('Mdc.view.setup.portal.RMR', {
    extend: 'Ext.container.Container',
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
    ]
});