Ext.define('Mdc.view.setup.devicevalidationresults.LoadProfileList', {
    extend: 'Ext.grid.Panel',
    border: true,	
	alias: 'widget.mdc-load-profile-list',    
    store: 'Mdc.store.ValidationResultsLoadProfiles',
    requires: [
        'Mdc.store.ValidationResultsLoadProfiles'
    ],
    router: null,
    mRID: null,
    columns: {
        items: [
            { 
				header: Uni.I18n.translate('device.dataValidation.loadProfile', 'MDC', 'Load profile'),
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
                            record.get('intervalRecord').get('all').count,
                                    Uni.I18n.translatePlural('general.'+record.get('intervalRecord').get('all').timeUnit, record.get('intervalRecord').get('all').count, 'MDC', record.get('intervalRecord').get('all').timeUnit));
                    }
                    else if(record.get('intervalEnd') && record.get('intervalInMs')){

                        return Ext.String.format(Uni.I18n.translate('validationResults.starting', 'MDC', ' {0} {1} starting {2}'),
                                    record.get('intervalRecord').get('all').count,
                                    Uni.I18n.translatePlural('general.'+record.get('intervalRecord').get('all').timeUnit, record.get('intervalRecord').get('all').count, 'MDC', record.get('intervalRecord').get('all').timeUnit),
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
                        filter = me.router.filter.getWriteData(true, true),
                        href;
                        filter['onlySuspect'] = true;
                        filter['duration'] = record.get('intervalRecord').get('all').count + record.get('intervalRecord').get('all').timeUnit;
                        filter['intervalStart'] = Ext.util.Format.date(
                                                    new Date(record.get('intervalStart')),'Y-m-dTH:i:s');
                    href = me.router.getRoute('devices/device/loadprofiles/loadprofiletableData').buildUrl(
                            {   mRID: record.get('mRID'),
                                loadProfileId: record.get('id')

                            }, {filter: filter});
                    return '<a href="' + href + '">' + value + '</a>'
                }

            }
        ]
    }


});
