Ext.define('Mdc.view.setup.deviceregisterdata.MainSetup', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceRegisterDataPage',
    deviceId: null,
    registerId: null,
    requires: [
        'Mdc.view.setup.deviceregisterdata.RegisterTopFilter'
    ],

    mentionDataLoggerSlave: false,
    router: null
});