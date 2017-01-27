Ext.define('Imt.purpose.view.ReadingsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.readings-list',
    itemId: 'readings-list',
    requires: [
        'Imt.purpose.store.Readings',
        'Uni.view.toolbar.PagingTop',
        'Imt.purpose.view.SingleReadingActionMenu',
        'Imt.purpose.view.MultipleReadingsActionMenu',
        'Uni.grid.column.Edited'
    ],
    selModel: {
        mode: 'MULTI'
    },
    viewConfig: {
        loadMask: false,
        enableTextSelection: true,
        doFocus: Ext.emptyFn // workaround to avoid page jump during row selection
    },
    store: 'Imt.purpose.store.Readings',

    initComponent: function () {
        var me = this,
            readingType = me.output.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined;

        me.plugins = [
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
        ];

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'IMT', 'End of interval'),
                dataIndex: 'interval',
                renderer: function (interval) {
                    return  interval.end
                        ? Uni.I18n.translate(
                        'general.dateAtTime', 'IMT', '{0} at {1}',
                        [Uni.DateTime.formatDateShort(new Date(interval.end)), Uni.DateTime.formatTimeShort(new Date(interval.end))] )
                        : '';
                },
                flex: 1
            },
            {
                header: unit
                    ? Uni.I18n.translate('general.valueOf', 'IMT', 'Value ({0})', [unit])
                    : Uni.I18n.translate('general.value.empty', 'IMT', 'Value'),
                width: 200,
                renderer: me.formatColumn,
                editor: {
                    xtype: 'textfield',
                    stripCharsRe: /[^0-9\.]/,
                    selectOnFocus: true,
                    validateOnChange: true,
                    fieldStyle: 'text-align: right'
                },
                align: 'right',
                dataIndex: 'value'
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'modificationState',
                width: 30,
                emptyText: ' '
            },
            {
                header: Uni.I18n.translate('device.readingData.lastUpdate', 'IMT', 'Last update'),
                dataIndex: 'reportedDateTime',
                flex: 1,
                renderer: function(value){
                    var date = new Date(value);
                    return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(date), Uni.DateTime.formatTimeShort(date)])
                }
            },
            {
                xtype: 'uni-actioncolumn',
                itemId: 'channel-data-grid-action-column',
                privileges: Imt.privileges.UsagePoint.admin,
                menu: {
                    xtype: 'purpose-readings-data-action-menu',
                    itemId: 'purpose-readings-data-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                noBottomPaging: true,
                usesExactCount: true,
                isFullTotalCount: true,
                displayMsg: Uni.I18n.translate('reading.pagingtoolbartop.displayMsg', 'IMT', '{1} reading(s)'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'save-changes-button',
                        text: Uni.I18n.translate('general.saveChanges', 'IMT', 'Save changes'),
                        disabled: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'undo-button',
                        text: Uni.I18n.translate('general.undo', 'IMT', 'Undo'),
                        disabled: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'readings-bulk-action-button',
                        text: Uni.I18n.translate('general.bulkAction', 'IMT', 'Bulk action'),
                        menu: {
                            xtype: 'purpose-bulk-action-menu',
                            itemId: 'purpose-bulk-action-menu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    formatColumn: function (v, metaData, record) {
        var status = record.get('validationResult') ? record.get('validationResult').split('.')[1] : '',
            value = Ext.isEmpty(v) ? '-' : v,
            icon = '';

        if (status === 'notValidated') {
            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.notvalidated', 'IMT', 'Not validated') + '"></span>';
        } else if (record.get('confirmedNotSaved')) {
            metaData.tdCls = 'x-grid-dirty-cell';
        } else if (status === 'suspect') {
            icon = '<span class="icon-flag5" style="margin-left:10px; color:red; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') + '"></span>';
        }
        if (record.get('isConfirmed') && !record.isModified('value')) {
            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.confirmed', 'IMT', 'Confirmed') + '"></span>';
        }
        return value + icon;
    }
});