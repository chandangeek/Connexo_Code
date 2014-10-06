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
//                width: 200,
                flex: 1
            }
        ];

        //Getting 4th magic number of a reading type to understand if it holds cumulative values or not
        if (readingType) {
            accumulationBehavior = readingType.split('.')[3];
        }
        me.columns.push(
            {
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                dataIndex: 'value',
                align: 'right',
                minWidth: 150,
                flex: 1,
                renderer: function (data, metaData, record) {
                    var validationFlag = '';
                    switch (record.get('validationResult')) {
                        case 'validationStatus.notValidated':
                            validationFlag = '<span class="icon-validation icon-validation-black"></span>';
                            break;
                        case 'validationStatus.ok':
                            validationFlag = '&nbsp;&nbsp;&nbsp;&nbsp;';
                            break;
                        case 'validationStatus.suspect':
                            validationFlag = '<span class="icon-validation icon-validation-red"></span>';
                            break;
                        default:
                            validationFlag = '&nbsp;&nbsp;&nbsp;&nbsp;';
                            break;
                    }
                    return !Ext.isEmpty(data)
                        ? '<span class="validation-column-align">' + data + ' ' + measurementType + ' ' + validationFlag + '</span>'
                        : '<span class="icon-validation icon-validation-black"></span>';
                }
            }
 /*           {
                header: Uni.I18n.translate('deviceloadprofiles.channels.cumulativeValue', 'MDC', 'Cumulative value'),
                dataIndex: 'delta',
                align: 'right',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return !Ext.isEmpty(value) ? value + ' ' + measurementType : '';
                }
            }*/
        );

        me.columns.push({
                xtype: 'interval-flags-column',
                dataIndex: 'intervalFlags',
                flex: 1
            }
            /* Commented because of JP-5861
             ,
             {
             xtype: 'uni-actioncolumn',
             menu: {
             xtype: 'deviceLoadProfileChannelDataActionMenu'
             }
             }*/);

        me.callParent(arguments);
    }

});