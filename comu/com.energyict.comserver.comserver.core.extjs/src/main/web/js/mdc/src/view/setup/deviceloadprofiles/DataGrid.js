Ext.define('Mdc.view.setup.deviceloadprofiles.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfilesDataGrid',
    itemId: 'deviceLoadProfilesDataGrid',
    store: 'Mdc.store.LoadProfilesOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.deviceloadprofiles.DataActionMenu',
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    plugins: [
        'bufferedrenderer',
        'showConditionalToolTip'
    ],
    viewConfig: {
        enableTextSelection: true
    },
    channels: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.dateattime', 'MDC', '{0} At {1}',[Uni.DateTime.formatDateShort(value),Uni.DateTime.formatTimeShort(value)]).toLowerCase()
                        : '';
                },
                width: 200
            }
        ];
        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                isFullTotalCount: true,
                noBottomPaging: true,
                usesExactCount: true,
                displayMsg: '{2} reading(s)',
                items: [
                ]
            }
        ];
        Ext.Array.each(me.channels, function (channel) {
            var channelHeader = !Ext.isEmpty(channel.calculatedReadingType) ? channel.calculatedReadingType.measuringPeriod + ' ' + channel.calculatedReadingType.aliasName +
                ' (' + channel.calculatedReadingType.unit + ')' : channel.readingType.measuringPeriod + ' ' + channel.readingType.aliasName + ' (' + channel.readingType.unit + ')';

            me.columns.push({
                header: channelHeader,
                dataIndex: 'channelData',
                align: 'right',
                minWidth: 150,
                flex: 1,
                renderer: function (data, metaData, record) {
                    if (record.data.channelValidationData && record.data.channelValidationData[channel.id]) {
                        var result = record.data.channelValidationData[channel.id].mainValidationInfo.validationResult,
                            status = result.split('.')[1],
                            cls = 'icon-validation-cell';
                        if (status === 'suspect') {
                            cls +=  ' icon-validation-red'
                        }
                        if (status === 'notValidated') {
                            cls +=  ' icon-validation-black'
                        }
                        metaData.tdCls = cls;
                        if (record.data.channelValidationData[channel.id].mainValidationInfo.estimatedByRule) {
                            return !Ext.isEmpty(data[channel.id]) ? data[channel.id] + ' ' +
                                '<span style="margin: 0 0 0 10px; font-size: 16px; color: #33CC33; position: absolute" class="icon-play4"</span>' : '';
                        }
                    }
                    return !Ext.isEmpty(data[channel.id]) ? data[channel.id] : '';
                }
            });
        });
        /* Commented for now because of JP-5561
         me.columns.push({
         xtype: 'uni-actioncolumn',
         menu: {
         xtype: 'deviceLoadProfilesDataActionMenu'
         }
         });
         */

        me.callParent(arguments);
    }
});