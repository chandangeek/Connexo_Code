Ext.define('Mdc.view.setup.deviceregisterdata.RegisterTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-registers-topfilter',
    hasDefaultFilters: true,
    filterDefault: {
        from: moment().startOf('day').subtract(1, 'years').toDate(),
        to: moment().endOf('day').toDate()
    },
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'interval',
                text: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement time'),
                dataIndex: 'interval',
                dataIndexFrom: 'intervalStart',
                dataIndexTo: 'intervalEnd',
                defaultFromDate: me.filterDefault.from,
                defaultToDate: me.filterDefault.to,
                itemId: 'deviceregister-topfilter-interval'
            },
            {
                type: 'checkbox',
                dataIndex: 'suspect',
                layout: 'hbox',
                defaults: {margin: '0 10 0 0'},
                emptyText: Uni.I18n.translate('communications.widget.topfilter.validationResult', 'MDC', 'Validation result'),
                options: [
                    {
                        display: Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect'),
                        value: 'suspect',
                        itemId: 'deviceregister-topfilter-suspect'
                    },
                    {
                        display: Uni.I18n.translate('validationStatus.ok', 'MDC', 'Not suspect'),
                        value: 'nonSuspect',
                        itemId: 'deviceregister-topfilter-notsuspect'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    enableClearAll: function (filters) {
        var me = this,
            enableClearAllBasedOnOtherThanFromTo = Ext.Array.filter(filters, function (filter) {
                    return filter.property !== 'intervalStart' && filter.property !== 'intervalEnd' && !Ext.Array.contains(me.noUiFilters, filter.property);
                }).length > 0,
            fromFilter = _.find(filters, function (item) {
                return item.property === 'intervalStart';
            }),
            toFilter = _.find(filters, function (item) {
                return item.property === 'intervalEnd';
            }),
            fromToFilterIsDefault = fromFilter && me.filterDefault.from && fromFilter.value === me.filterDefault.from.getTime() &&
                toFilter && me.filterDefault.to && toFilter.value === me.filterDefault.to.getTime();

        Ext.suspendLayouts();
        me.down('button[action=clearAll]').setDisabled(enableClearAllBasedOnOtherThanFromTo ? false : fromToFilterIsDefault);
        me.down('#deviceregister-topfilter-interval button[action=clear]').setDisabled(fromToFilterIsDefault);
        Ext.resumeLayouts(true);
    }

});