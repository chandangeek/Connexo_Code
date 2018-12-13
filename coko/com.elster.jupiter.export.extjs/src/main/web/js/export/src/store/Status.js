Ext.define('Dxp.store.Status', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            data: [
                {
                    value: 'FAILED',
                    display: Uni.I18n.translate('exportTask.history.failStatus', 'DES', 'Failed')
                },
                {
                    value: 'BUSY',
                    display: Uni.I18n.translate('exportTask.history.busyStatus', 'DES', 'Ongoing')
                },
                {
                    value: 'SUCCESS',
                    display: Uni.I18n.translate('exportTask.history.successStatus', 'DES', 'Successful')
                }
            ],
            fields: [
                {
                    name: 'value'
                },
                {
                    name: 'display'
                }
            ]
        }, cfg)]);
    }
});