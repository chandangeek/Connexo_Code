Ext.define('Mdc.Application', {
    name: 'Mdc',

    extend: 'Ext.app.Application',

    requires: [
        'Mdc.PolyReader',
        'Mdc.PolyAssociation',
        'Mdc.Association',
        'Mdc.lib.form.field.Vtypes'
    ],

    views: [
        // TODO: add views here
    ],

    controllers: [
        'Main',
        'setup.SetupOverview',
        'setup.ComServers',
        'setup.ComPortPools',
        'history.Setup',
        'setup.DeviceCommunicationProtocol',
        'setup.LicensedProtocol'
    ],

    stores: [
    ],

    launch: function () {
        Ext.create('Mdc.lib.form.field.Vtypes').init();

    }
});
