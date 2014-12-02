Ext.define('Mdc.view.setup.deviceregisterconfiguration.GeneralDetail', {
    extend: 'Uni.view.container.ContentContainer',
    itemId: 'device-register-configuration-general-detail',

    mRID: null,
    registerId: null,

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.Menu'
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    }
});