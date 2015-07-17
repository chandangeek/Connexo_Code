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
        'Uni.grid.column.Action',
        'Mdc.view.setup.devicechannels.DataBulkActionMenu'
    ],
    plugins: [
        'bufferedrenderer',
        'showConditionalToolTip',
        {
            ptype: 'cellediting',
            clicksToEdit: 1,
            pluginId: 'cellplugin'
        }
    ],
    viewConfig: {
        loadMask: false,
        enableTextSelection: true
    },
    selModel: {
        mode: 'MULTI'
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
                    return value ? Uni.DateTime.formatDateShort(value) + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' ' + Uni.DateTime.formatTimeShort(value) : '';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value') + ' (' + measurementType + ')',
                dataIndex: 'value',
                align: 'right',
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, record.data.validationInfo.mainValidationInfo);
                },
                editor: {
                    xtype: 'textfield',
                    stripCharsRe: /[^0-9\.]/,
                    selectOnFocus: true,
                    validateOnChange: true,
                    fieldStyle: 'text-align: right'
                },
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions,
                width: 200
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value') + ' (' + measurementType + ')',
                dataIndex: 'value',
                align: 'right',
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, record.data.validationInfo.mainValidationInfo);
                },
                hidden: Mdc.dynamicprivileges.DeviceState.canEditData(),
                width: 200
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
                    return me.formatColumn(v, metaData, record, record.data.validationInfo.bulkValidationInfo);
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
            },
            {
                xtype: 'uni-actioncolumn',
                itemId: 'channel-data-grid-action-column',
                menu: {
                    xtype: 'deviceLoadProfileChannelDataActionMenu',
                    itemId: 'channel-data-grid-action-menu'
                }
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
                displayMsg: '{2} reading(s)',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'save-changes-button',
                        text: Uni.I18n.translate('general.saveChanges', 'MDC', 'Save changes'),
                        hidden: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'undo-button',
                        text: Uni.I18n.translate('general.undo', 'MDC', 'Undo'),
                        hidden: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'device-channel-data-bulk-action-button',
                        text: Uni.I18n.translate('general.bulkAction', 'MDC', 'Bulk Action'),
                        privileges: Mdc.privileges.Device.administrateDeviceData,
                        menu: {
                            xtype: 'channel-data-bulk-action-menu',
                            itemId: 'channel-data-bulk-action-menu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    formatColumn: function (v, metaData, record, validationInfo) {
        var cls = 'icon-validation-cell',
            status = validationInfo.validationResult ? validationInfo.validationResult.split('.')[1] : '';

        if (!record.data.validationInfo.dataValidated || status == 'notValidated') {
            cls += ' icon-validation-black';
        } else if (status == 'suspect') {
            cls += ' icon-validation-red';
        }
        metaData.tdCls = cls;
        if (!Ext.isEmpty(v)) {
            var value = Uni.Number.formatNumber(v, -1);
            if (validationInfo.estimatedByRule && !record.isModified('value')) {
                return !Ext.isEmpty(value) ? value + '<span style="margin: 0 0 0 10px; font-size: 16px; color: #33CC33; position: absolute" class="icon-play4"</span>' : '';
            } else {
                return !Ext.isEmpty(value) ? value : '';
            }
        }
    }
});