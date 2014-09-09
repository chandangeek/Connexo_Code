Ext.define('Mdc.controller.setup.LicensedProtocol', {
    extend: 'Ext.app.Controller',

    stores: [
        'LicensedProtocols'
    ],

    models: [
        'LicensedProtocol'
    ],

    views: [
        'setup.licensedprotocol.List'
    ],
    refs: [
        {
            ref: 'licensedProtocolGrid',
            selector: 'viewport #licensedprotocolgrid'
        }
    ],

    init: function () {
        this.control({
            'setupLicensedProtocols': {

            }
        });
    }

});
