Ext.define('Mdc.view.setup.deviceregisterconfiguration.RegisterReadingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceRegisterReadingsGrid',
    store: undefined,
    router: undefined,
    mRID: undefined,
    showDataLoggerSlaveColumn: false,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterReadings',
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.register', 'MDC', 'Register'),
                dataIndex: 'register',
                renderer: function (value, metaData, record) {
                    var to = moment(record.get('timeStamp')).add(1, 'minutes').valueOf(),
                        url = '#/devices/' + me.mRID + '/registers/' + value.id + '/data?interval=-' + to;
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                },
                flex: 15
            },
            {
                header: Uni.I18n.translate('general.measurementPeriod', 'MDC', 'Measurement period'),
                dataIndex: 'interval',
                itemId: 'mdc-interval-column',
                renderer: function (value) {
                    var startDate,endDate;
                    if (!Ext.isEmpty(value) && !!value.start) {
                        startDate = new Date(value.start);
                        endDate = new Date(value.end);
                        return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate);
                    } else if (!Ext.isEmpty(value) && !!value.end){
                        endDate = new Date(value.end);
                        return Uni.DateTime.formatDateTimeShort(endDate)
                    }
                    return '-';
                },
                flex: 20
              //  hidden: true
            },
            {
                header: Uni.I18n.translate('general.eventDate', 'MDC', 'Event time'),
                flex: 10,
                dataIndex: 'eventDate',
                itemId: 'mdc-event-date-column',
                renderer: function (value) {
                    return Ext.isEmpty(value) || value === 0 ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                },
               // hidden: true
            },
            {
                header: Uni.I18n.translate('general.collected', 'MDC', 'Collected'),
                flex: 10,
                dataIndex: 'valueAndUnit',
                renderer: function (data, metaData, record) {
                    if (!Ext.isEmpty(data)) {
                        var status = record.data.validationResult ? record.data.validationResult.split('.')[1] : 'unknown',
                            icon = '';
                        if (record.get('isConfirmed')) {
                            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute;" data-qtip="'
                                + Uni.I18n.translate('reading.validationResult.confirmed', 'MDC', 'Confirmed') + '"></span>'
                        } else if (status === 'suspect') {
                            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:red;"></span>';
                        } else if (status === 'notValidated') {
                            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>';
                        }
                        return data + icon;
                    }
                },
            },
            {
                header: Uni.I18n.translate('general.calculatedValue', 'MDC', 'Calculated'),
                flex: 10,
                dataIndex: 'calculatedValue',
                renderer: function (data, metaData, record) {
                    if (!Ext.isEmpty(data)) {
                        return record.data.calculatedValue?record.data.calculatedValue + ' ' + record.data.calculatedUnit:'-';
                    } else {
                        return '-'
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.deltaValue', 'MDC', 'Delta value'),
                flex: 10,
                dataIndex: 'deltaValue'
            },
            {
                header: Uni.I18n.translate('device.registerData.reportedTime', 'MDC', 'Last updated'),
                dataIndex: 'reportedDateTime',
                flex: 15,
                renderer: function(value){
                    var date = new Date(value);
                    return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(date), Uni.DateTime.formatTimeShort(date)]);
                }
            }
        ];

        if (me.showDataLoggerSlaveColumn) {
            me.columns.push(
                {
                    dataIndex: 'dataloggerSlavemRID',
                    flex: 10,
                    header: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
                    renderer: function (value) {
                        if (Ext.isEmpty(value)) {
                            return '-';
                        }
                        var href = me.router.getRoute('devices/device/registers').buildUrl({mRID: encodeURIComponent(value)});
                        return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                    }
                }
            );
        }

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerreadings.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register readings'),
                displayMoreMsg: Uni.I18n.translate('registerreadings.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register readings'),
                emptyMsg: Uni.I18n.translate('registerreadings.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register readings')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('registerreadings.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register readings per page'),
                defaultPageSize: 100,
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    },

    renderMeasurementTime: function (value, metaData, record) {
        if (Ext.isEmpty(value)) {
            return '-';
        }
        var date = new Date(value),
            showDeviceQualityIcon = false,
            tooltipContent = '',
            icon = '';

        if (!Ext.isEmpty(record.get('readingQualities'))) {
            Ext.Array.forEach(record.get('readingQualities'), function (readingQualityObject) {
                if (readingQualityObject.cimCode.startsWith('1.')) {
                    showDeviceQualityIcon |= true;
                    tooltipContent += readingQualityObject.indexName + '<br>';
                }
            });
            if (tooltipContent.length > 0) {
                tooltipContent += '<br>';
                tooltipContent += Uni.I18n.translate('general.deviceQuality.tooltip.moreMessage', 'MDC', 'View reading quality details for more information.');
            }
            if (showDeviceQualityIcon) {
                icon = '<span class="icon-price-tags" style="margin-left:10px; position:absolute;" data-qtitle="'
                    + Uni.I18n.translate('general.deviceQuality', 'MDC', 'Device quality') + '" data-qtip="'
                    + tooltipContent + '"></span>';
            }
        }
        return Uni.DateTime.formatDateTimeShort(date) + icon;
    },

    customizeColumns: function (billing,cumulative,event,multiplier) {
        var fromColumn = this.down('#mdc-readings-grid-from-column'),
            toColumn = this.down('#mdc-readings-grid-to-column');

        if (fromColumn) {
            if (billing) {
                fromColumn.show();
            } else {
                fromColumn.hide();
            }
        }
        if (toColumn) {
            if (billing) {
                toColumn.show();
            } else {
                toColumn.hide();
            }
        }
    }
});