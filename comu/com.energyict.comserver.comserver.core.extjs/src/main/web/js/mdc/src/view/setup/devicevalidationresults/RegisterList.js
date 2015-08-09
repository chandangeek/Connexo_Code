Ext.define('Mdc.view.setup.devicevalidationresults.RegisterList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.mdc-register-list',
    store: 'Mdc.store.ValidationResultsRegisters',
    requires: [
        'Mdc.store.ValidationResultsRegisters'
    ],

    columns: {
        items: [
            { 
				header: Uni.I18n.translate('device.dataValidation.register', 'MDC', 'Register'),
				dataIndex: 'name', 
				flex: 0.7, 
				sortable: false, 
				fixed: true
            },
            {
                header: Uni.I18n.translate('validationResults.period', 'MDC', 'Period'),
                flex: 0.7,
                sortable: false,
                fixed: true,
                renderer: function (value, meta, record) {
                    if (record.get('interval') == null) {
                           return Ext.String.format(Uni.I18n.translate('validationResults.last', 'MDC', 'Last {0} {1}'),
                               1,
                               Uni.I18n.translatePlural('general.'+record.get('intervalRecord').get('all').timeUnit,
                                   record.get('intervalRecord').get('all').count,
                                   'MDC',
                                   record.get('intervalRecord').get('all').timeUnit));
                    }
                    else if(record.get('intervalEnd') && record.get('intervalInMs')){

                        return Ext.String.format(Uni.I18n.translate('validationResults.starting', 'MDC', ' {0} {1} starting {2}'),
                            record.get('interval').count,
                            Uni.I18n.translatePlural('general.'+record.get('interval').timeUnit, record.get('interval').count, 'MDC', record.get('interval').timeUnit),
                            Uni.DateTime.formatDateTimeLong(new Date(record.get('intervalStart'))));
                    }
                    return '';
                }
            },
            { 
				header: Uni.I18n.translate('validationResults.result', 'MDC', 'Result'), 
				dataIndex: 'total', 
				sortable: false, 
				fixed: true,

                renderer: function (value, meta, record) {
                    var me = this,
                        href;

                    href = me.router.getRoute('devices/device/registers/registerdata').buildUrl(
                        {
                            mRID: record.get('mRID'),
                            registerId: record.get('id')
                        },
                        {
                            suspect: 'suspect',
                            interval: Ext.String.format('{0}-{1}{2}',
                                record.get('intervalStart'), 1, record.get('intervalRecord').get('all').timeUnit)
                        });
                    return '<a href="' + href + '">' + value + '</a>'
                }
            }
        ]
    }


});
