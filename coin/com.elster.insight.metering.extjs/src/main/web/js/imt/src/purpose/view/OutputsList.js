/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.OutputsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.outputs-list',
    requires: [
        'Imt.purpose.store.Outputs',
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.ReadingType',
        'Uni.store.Periods'
    ],
    store: 'Imt.purpose.store.Outputs',
    overflowY: 'auto',
    itemId: 'outputs-list',

    initComponent: function () {
        var me = this,
            periods = Ext.create('Uni.store.Periods');

        me.columns = [
            {
                header: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                flex: 1,
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    var result = '<a href="' + me.router.getRoute('usagepoints/view/purpose/output').buildUrl({outputId: record.getId()}) + '">' + Ext.String.htmlEncode(value) + '</a>',
                        validationInfo = record.get('validationInfo');

                    if (validationInfo && validationInfo.hasSuspects) {
                        result += '<span class="icon-warning" style="color: #eb5642; margin-left: 10px" data-qtip="'
                            + Uni.I18n.translate('usagepoint.purpose.output.validation.hasSuspects.qtip', 'IMT', 'Output contains suspect values for specified period')
                            + '"></span>';
                    }

                    return result;
                }
            },
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('outputs.label.interval', 'IMT', 'Interval'),
                flex: 1,
                dataIndex: 'interval',
                renderer: function(value){
                    var period;
                    if(value){
                        period = periods.findRecord('value', value.timeUnit);
                        return period.get('translate').call(period, value.count);
                    } else {
                        return '-';
                    }

                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('outputs.pagingtoolbartop.displayMsg', 'IMT', '{2} outputs'),
                emptyMsg: Uni.I18n.translate('outputs.pagingtoolbartop.emptyMsg', 'IMT', 'There are no outputs to display')
            }

        ];
        me.callParent(arguments);
    }
});