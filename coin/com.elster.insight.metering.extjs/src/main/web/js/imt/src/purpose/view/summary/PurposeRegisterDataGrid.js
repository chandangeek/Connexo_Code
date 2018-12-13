/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Imt.purpose.view.summary.PurposeRegisterDataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.purpose-register-data-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.purpose.util.GridRenderer'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('purpose.summary.register', 'IMT', 'Register'),
                dataIndex: 'output',
                flex: 1,
                renderer: function (value, metaData, record) {
                    var to = moment(record.get('timeStamp')).add(1, 'minutes').valueOf(),
                        url = '#/usagepoints/' + me.usagePointName + '/purpose/' + me.purposeId + '/output/' + value.id + '/readings?interval=-' + to;
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.measurementPeriod', 'IMT', 'Measurement period'),
                dataIndex: 'interval',
                flex: 2,
                renderer: Imt.purpose.util.GridRenderer.renderMeasurementPeriodColumn
            }, {
                header: Uni.I18n.translate('device.registerData.eventTime', 'IMT', 'Event time'),
                dataIndex: 'eventDate',
                flex: 1,
                renderer: Imt.purpose.util.GridRenderer.renderEventTimeColumn
            },
            {
                header: Uni.I18n.translate('purpose.summary.value', 'IMT', 'Value'),
                dataIndex: 'value',
                flex: 1,
                renderer: Imt.purpose.util.GridRenderer.renderValueAndUnit
            },
            {
                header: Uni.I18n.translate('device.registerData.deltaValue', 'IMT', 'Delta value'),
                dataIndex: 'deltaValue',
                flex: 1,
                renderer: Imt.purpose.util.GridRenderer.renderValueAndUnit
            },
            {
                header: Uni.I18n.translate('device.readingData.lastUpdate', 'IMT', 'Last update'),
                dataIndex: 'reportedDateTime',
                flex: 1,
                renderer: Imt.purpose.util.GridRenderer.renderLastUpdateColumn
            }
        ];


        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('purpose.summary.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} readings'),
                displayMoreMsg: Uni.I18n.translate('purpose.summary.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} readings'),
                emptyMsg: Uni.I18n.translate('purpose.summary.pagingtoolbartop.emptyMsg', 'IMT', 'There are no readings')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('purpose.summary.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Readings per page'),
                dock: 'bottom'
            }
        ];
        me.callParent(arguments);
    }
});