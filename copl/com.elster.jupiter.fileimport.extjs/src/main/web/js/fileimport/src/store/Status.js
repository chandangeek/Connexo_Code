Ext.define('Fim.store.Status', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            data: [
                {
                    value: 'fail',
                    display: Uni.I18n.translate('importService.history.failStatus', 'FIM', 'Fail'),
                },
                {
                    value: 'busy',
                    display: Uni.I18n.translate('importService.history.busyStatus', 'FIM', 'Busy'),
                },
                {
                    value: 'success',
                    display: Uni.I18n.translate('importService.history.successStatus', 'FIM', 'Success'),
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