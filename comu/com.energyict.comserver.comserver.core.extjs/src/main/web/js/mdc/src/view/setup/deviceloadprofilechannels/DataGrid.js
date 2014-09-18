Ext.define('Mdc.view.setup.deviceloadprofilechannels.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelDataGrid',
    itemId: 'deviceLoadProfileChannelDataGrid',
    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Mdc.view.setup.deviceloadprofilechannels.DataActionMenu',
        'Uni.grid.column.IntervalFlags'
    ],
    height: 395,
    plugins: {
        ptype: 'bufferedrenderer'
    },

    //test

    channelRecord: null,

    initComponent: function () {
        var me = this,
            readingType = me.channelRecord.get('cimReadingType'),
            measurementType = me.channelRecord.get('unitOfMeasure_formatted'),
            accumulationBehavior;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                width: 200
            }
        ];

        //Getting 4th magic number of a reading type to understand if it holds cumulative values or not
        if (readingType) {
            accumulationBehavior = readingType.split('.')[3];
        }

        // 1 means cumulative
        if (accumulationBehavior && accumulationBehavior == 1) {
            me.columns.push({
                header: Uni.I18n.translate('deviceloadprofiles.channels.delta', 'MDC', 'Delta'),
                dataIndex: 'value',

                renderer: function (value, metaData, record) {
                    var toDisplay = value ? Uni.I18n.formatNumber(value, 'MDC', 3) + ' ' + measurementType : '';
                    switch (record.get('validationResult')) {
                        case 'validationStatus.notValidated':
                            return '<span class="validation-column-align"><span class="icon-validation icon-validation-black"></span>' + ' '
                                + toDisplay + '</span>';
                            break;
                        case 'validationStatus.ok':
                            return toDisplay;
                            break;
                        case 'validationStatus.suspect':
                            return '<span class="validation-column-align"><span class="icon-validation icon-validation-red"></span>' + '  '
                                + toDisplay + '</span>';
                            break;
                        default:
                            return toDisplay;
                            break;
                    }
                },
                flex: 1
            }, {
                header: Uni.I18n.translate('deviceloadprofiles.channels.cumulativeValue', 'MDC', 'Cumulative value'),
                dataIndex: 'delta',
                align: 'right',
                renderer: function (value, metaData, record) {
                    return value ? Uni.I18n.formatNumber(value, 'MDC', 3) + ' ' + measurementType : '';
                },
                flex: 1
            });
        } else {
            me.columns.push({
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                dataIndex: 'value',
                align: 'right',
                //todo: refactor component ValidationFlag so we can use it here for rendering the flag
                renderer: function (value, metaData, record) {
                    var toDisplay = value ? Uni.I18n.formatNumber(value, 'MDC', 3) + ' ' + measurementType : '';
                    switch (record.get('validationResult')) {
                        case 'validationStatus.notValidated':
                            return '<span class="validation-column-align"><span class="icon-validation icon-validation-black"></span>' + ' '
                                + toDisplay + '</span>';
                            break;
                        case 'validationStatus.ok':
                            return toDisplay;
                            break;
                        case 'validationStatus.suspect':
                            return '<span class="validation-column-align"><span class="icon-validation icon-validation-red"></span>' + '  '
                                + toDisplay + '</span>';
                            break;
                        default:
                            return toDisplay;
                            break;
                    }
                },
                flex: 1
            });
        }

        me.columns.push({
                xtype: 'interval-flags-column',
                dataIndex: 'intervalFlags',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'deviceLoadProfileChannelDataActionMenu'
                }
            });

        me.callParent(arguments);
    }

});