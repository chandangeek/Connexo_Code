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
    channels: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                width: 200
            }
        ];
        Ext.Array.each(me.channels, function (channel) {
            me.columns.push({
                header: channel.name,
                dataIndex: 'channelData',
                align: 'right',
                minWidth : 150,
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
                                validationFlag = '&nbsp;&nbsp;&nbsp;&nbsp;';
                                break;
                        }
                    }
                    return !Ext.isEmpty(data[channel.id])
                        ? '<span class="validation-column-align">' + data[channel.id] + ' ' + channel.unitOfMeasure.unit + ' ' + validationFlag + '</span>'
                        : '<span class="icon-validation icon-validation-black"></span>';
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