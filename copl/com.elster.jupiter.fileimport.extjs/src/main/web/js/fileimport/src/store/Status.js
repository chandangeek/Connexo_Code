Ext.define('Fim.store.Status', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            data: [
                {
                    value: 'FAILURE',
                    display: Uni.I18n.translate('importService.history.failStatus', 'FIM', 'Fail'),
                },
                {
                    value: 'PROCESSING',
                    display: Uni.I18n.translate('importService.history.busyStatus', 'FIM', 'Busy'),
                },
                {
                    value: 'SUCCESS',
                    display: Uni.I18n.translate('importService.history.successStatus', 'FIM', 'Success'),
                },
                {
                    value: 'SUCCESS_WITH_FAILURES',
                    display: Uni.I18n.translate('importService.history.successWithFailuresStatus', 'FIM', 'Success with failures'),
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