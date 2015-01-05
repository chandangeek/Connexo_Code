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
    height: 395,
    plugins: [
        'bufferedrenderer',
        'showConditionalToolTip'
    ],
    viewConfig: {
        loadMask: false
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
                        ? Uni.DateTime.formatDateShort(value)
                        + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                        + Uni.DateTime.formatTimeShort(value)
                        : '';
                },
                width: 200
            }
        ];
        Ext.Array.each(me.channels, function (channel) {
            me.columns.push({
                header: channel.name,
                dataIndex: 'channelData',
                align: 'right',
                minWidth: 150,
                flex: 1,
                renderer: function (data, metaData, record) {
                    var validationFlag = '';
                    if (record.data.channelValidationData && record.data.channelValidationData[channel.id]) {
                        switch (record.data.channelValidationData[channel.id].validationResult) {
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
                                validationFlag = '';
                                break;
                        }
                    }

                    if (Ext.isEmpty(data[channel.id]) && !Ext.isEmpty(validationFlag)) {
                        return validationFlag;
                    } else if (!Ext.isEmpty(data[channel.id])) {
                        return '<span class="validation-column-align">' + data[channel.id] + ' ' + validationFlag + '</span>';
                    } else {
                        return '<span class="icon-validation icon-validation-black"></span>';
                    }
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