/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                        ? Uni.DateTime.formatDateTimeShort(value)
                        : '';
                },
                flex: 1
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

            // value column
            me.columns.push({
                header: channelHeader,
                tooltip: channelHeader,
                dataIndex: 'channelData',
                align: 'right',
                flex: 1,
                renderer: function (data) {
                    return !Ext.isEmpty(data[channel.id]) ? Uni.Number.formatNumber(data[channel.id], -1) : '-';
                }
            });

            // icons column
            me.columns.push({
                header: '',
                dataIndex: 'channelData',
                maxWidth: 60,
                align: 'left',
                style: {
                    padding: '8px 0px 8px 0px' // Doesn't seem to work
                },
                renderer: function (data, metaData, record) {
                    var icon = '<span class="icon-flag5" style="margin-left:10px; display:inline-block; color:rgba(0,0,0,0.0);"></span>', // invisible,
                        validationData = record.get('channelValidationData'),
                        readingQualities = record.get('readingQualities');

                    if (validationData && validationData[channel.id]) {
                        var status = validationData[channel.id].mainValidationInfo && validationData[channel.id].mainValidationInfo.validationResult
                            ? validationData[channel.id].mainValidationInfo.validationResult.split('.')[1]
                            : 'unknown';
                        if (status === 'suspect') {
                            icon = '<span class="icon-flag5" style="display:inline-block; color:red;"></span>';
                        }
                        if (status === 'notValidated') {
                            icon = '<span class="icon-flag6" style="display:inline-block;"></span>';
                        }
                        if (validationData[channel.id].mainValidationInfo.estimatedByRule) {
                            icon = '<span class="icon-flag5" style="display:inline-block; color:#33CC33;"></span>';
                        }
                    }
                    return !Ext.isEmpty(data[channel.id]) ? icon : '';
                }
            });
        });

        me.callParent(arguments);
    }
});