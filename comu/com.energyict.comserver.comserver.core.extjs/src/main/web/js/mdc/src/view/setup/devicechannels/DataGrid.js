Ext.define('Mdc.view.setup.devicechannels.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelDataGrid',
    itemId: 'deviceLoadProfileChannelDataGrid',
    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Mdc.view.setup.devicechannels.DataActionMenu',
        'Uni.grid.column.IntervalFlags',
        'Uni.grid.column.Edited',
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.Action'
    ],
    plugins: [
        'bufferedrenderer'
    ],
    viewConfig: {
        loadMask: false,
        enableTextSelection: true
    },

    channelRecord: null,
    router: null,

    initComponent: function () {
        var me = this,
            calculatedReadingType = me.channelRecord.get('calculatedReadingType'),
            measurementType = me.channelRecord.get('unitOfMeasure');

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                renderer: function (value) {
                    return value
                        ? Uni.DateTime.formatDateShort(value)
                        + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                        + Uni.DateTime.formatTimeShort(value)
                        : '';
                },
                width: 200
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value') + ' (' + measurementType + ')',
                dataIndex: 'value',
                flex: 1,
                align: 'right',
                renderer: function (v, metaData, record) {
                    var validationInfo = record.get('readingProperties'),
                        cls = 'icon-validation-cell';
                    if (!record.getValidationInfo().get('dataValidated')) {
                        cls += ' icon-validation-black';
                    } else if (validationInfo.delta) {
                        if (validationInfo.delta.suspect) {
                            cls += ' icon-validation-red'
                        }
                    }
                    metaData.tdCls = cls;
                    if (!Ext.isEmpty(v)) {
                        var value = Uni.Number.formatNumber(v, -1);
                        return !Ext.isEmpty(value) ? value : '';
                    }
                },
                editor: {
                    xtype: 'textfield',
                    stripCharsRe: /[^0-9\.]/,
                    selectOnFocus: true,
                    validateOnChange: true,
                    fieldStyle: 'text-align: right'
                }
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'mainModificationState',
                width: 30
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.channels.bulkValue', 'MDC', 'Bulk value') + ' (' + measurementType + ')',
                dataIndex: 'collectedValue',
                flex: 1,
                align: 'right',
                hidden: Ext.isEmpty(calculatedReadingType),
                renderer: function (v, metaData, record) {
                    var validationInfo = record.get('readingProperties'),
                        cls = 'icon-validation-cell';

                    if (!record.getValidationInfo().get('dataValidated')) {
                        cls += ' icon-validation-black';
                    } else if (validationInfo.bulk) {
                        if (validationInfo.bulk.suspect) {
                            cls += ' icon-validation-red'
                        }
                    }

                    metaData.tdCls = cls;
                    if (!Ext.isEmpty(v)) {
                        var value = Uni.Number.formatNumber(v, -1);
                        return !Ext.isEmpty(value) ? value : '';
                    }
                }
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'bulkModificationState',
                width: 30
            },
            {
                xtype: 'interval-flags-column',
                dataIndex: 'intervalFlags',
                align: 'right',
                width: 150
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                isFullTotalCount: true,
                displayMsg: '{2} reading(s)'
            }
        ];

        me.callParent(arguments);
    }

});