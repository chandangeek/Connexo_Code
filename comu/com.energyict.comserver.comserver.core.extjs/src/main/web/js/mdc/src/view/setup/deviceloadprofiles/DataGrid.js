Ext.define('Mdc.view.setup.deviceloadprofiles.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfilesDataGrid',
    itemId: 'deviceLoadProfilesDataGrid',
    store: 'Mdc.store.LoadProfilesOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.deviceloadprofiles.DataActionMenu'
    ],
    plugins: [
        {
            ptype: 'bufferedrenderer',
            trailingBufferZone: 12,
            leadingBufferZone: 24
        }
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
                        ? Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}',[Uni.DateTime.formatDateShort(value),Uni.DateTime.formatTimeShort(value)])
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
            var channelHeader = !Ext.isEmpty(channel.calculatedReadingType)
                ? channel.calculatedReadingType.fullAliasName
                : channel.readingType.fullAliasName;

            me.columns.push({
                header: channelHeader,
                dataIndex: 'channelData',
                align: 'right',
                minWidth: 150,
                flex: 1,
                renderer: function (data, metaData, record) {
                    var validationData = record.get('channelValidationData'),
                        icon = '';

                    if (validationData && validationData[channel.id]) {
                        var status = validationData[channel.id].mainValidationInfo && validationData[channel.id].mainValidationInfo.validationResult
                                ? validationData[channel.id].mainValidationInfo.validationResult.split('.')[1]
                                : 'unknown';
                        if (status === 'suspect') {
                            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:red;"></span>';
                        }
                        if (status === 'notValidated') {
                            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>';
                        }
                        if (validationData[channel.id].mainValidationInfo.estimatedByRule) {
                            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:#33CC33;"></span>';
                        }
                    }

                    return !Ext.isEmpty(data[channel.id])
                        ? Uni.Number.formatNumber(data[channel.id], -1) + icon
                        : '';
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