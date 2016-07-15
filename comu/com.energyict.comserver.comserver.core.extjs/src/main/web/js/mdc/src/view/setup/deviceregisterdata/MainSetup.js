Ext.define('Mdc.view.setup.deviceregisterdata.MainSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceRegisterDataPage',
    mRID: null,
    registerId: null,
    requires: [
        'Mdc.view.setup.deviceregisterdata.RegisterTopFilter'
    ],

    mentionDataLoggerSlave: false,
    router: null,

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    }
});