Ext.define('Imt.channeldata.view.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.channelDataGrid',
    itemId: 'channelDataGrid',
    store: 'Imt.channeldata.store.ChannelData',
    requires: [
        'Uni.grid.column.Action',
        'Imt.channeldata.view.DataActionMenu',
        'Uni.grid.column.IntervalFlags',
        'Uni.grid.column.Edited',
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.Action',
        'Imt.channeldata.view.DataBulkActionMenu'
    ],
    plugins: [
        {
            ptype: 'bufferedrenderer',
            trailingBufferZone: 12,
            leadingBufferZone: 24
        },
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
                header: Uni.I18n.translate('channels.endOfInterval', 'IMT', 'End of interval'),
                dataIndex: 'interval_end',
                renderer: function (value) {
                    return  value ? Uni.I18n.translate('general.dateattime', 'IMT', '{0} at {1}',[
                        Uni.DateTime.formatDateShort(value),
                        Uni.DateTime.formatTimeShort(value)
                    ]) : '';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('channels.value', 'IMT', 'Value') + ' (' + measurementType + ')',
                dataIndex: 'value',
                align: 'right',
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, record.get('mainValidationInfo'));
                },
                editor: {
                    xtype: 'textfield',
                    stripCharsRe: /[^0-9\.]/,
                    selectOnFocus: true,
                    validateOnChange: true,
                    fieldStyle: 'text-align: right'
                },
//                dynamicPrivilege: Imt.dynamicprivileges.UsagePointState.usagePointDataEditActions,
                width: 200
            },
            {
                header: Uni.I18n.translate('channels.value', 'IMT', 'Value') + ' (' + measurementType + ')',
                dataIndex: 'value',
                align: 'right',
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, record.get('mainValidationInfo'));
                },
                hidden: true, //Imt.dynamicprivileges.UsagePointState.canEditData(),
                width: 200
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'mainModificationState',
                width: 30,
                emptyText: ' '
            },
            {
                header: Uni.I18n.translate('channels.bulkValue', 'IMT', 'Bulk value') + ' (' + measurementType + ')',
                dataIndex: 'collectedValue',
                flex: 1,
                align: 'right',
                hidden: Ext.isEmpty(calculatedReadingType),
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, record.get('bulkValidationInfo'));
                }
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'bulkModificationState',
                width: 30,
                emptyText: ' '
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
                    xtype: 'channelDataActionMenu',
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
                        text: Uni.I18n.translate('general.saveChanges', 'IMT', 'Save changes'),
                        hidden: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'undo-button',
                        text: Uni.I18n.translate('general.undo', 'IMT', 'Undo'),
                        hidden: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'usagepoint-channel-data-bulk-action-button',
                        text: Uni.I18n.translate('general.bulkAction', 'IMT', 'Bulk action'),
                        privileges: Imt.privileges.UsagePoint.admin,
                        menu: {
                            xtype: 'channelDataBulkActionMenu',
                            itemId: 'channelDataBulkActionMenu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    formatColumn: function (v, metaData, record, validationInfo) {
        var me = this,
            status = validationInfo.validationResult ? validationInfo.validationResult.split('.')[1] : '',
            icon = '',
            value = Ext.isEmpty(v)
                ? '-'
                : Uni.Number.formatNumber(
                    v.toString(),
                    me.channelRecord && !Ext.isEmpty(me.channelRecord.get('nbrOfFractionDigits')) ? me.channelRecord.get('nbrOfFractionDigits') : -1
                );

        if (status === 'notValidated') {
            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>';
        } else if (validationInfo.confirmedNotSaved) {
            metaData.tdCls = 'x-grid-dirty-cell';
        } else if (status === 'suspect') {
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:red;"></span>';
        }

        if (validationInfo.estimatedByRule && !record.isModified('value')) {
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:#33CC33;"></span>';
        } else if (validationInfo.isConfirmed && !record.isModified('value')) {
            icon = '<span class="icon-checkmark3" style="margin-left:10px; position:absolute;"></span>';
        }
        return value + icon;
    }
});