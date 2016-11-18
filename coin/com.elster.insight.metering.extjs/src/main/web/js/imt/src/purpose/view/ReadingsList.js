Ext.define('Imt.purpose.view.ReadingsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.readings-list',
    itemId: 'readings-list',
    requires: [
        'Imt.purpose.store.Readings',
        'Uni.view.toolbar.PagingTop',
        'Imt.purpose.view.DataBulkActionMenu'
    ],
    selModel: {
        mode: 'MULTI'
    },
    viewConfig: {
        loadMask: false,
        enableTextSelection: true
    },
    // plugins: [
    //     {
    //         ptype: 'bufferedrenderer'
    //     }
    // ],
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
                pluginId: 'cellplugin',
                listeners: {
                    'beforeedit': function (e, f) {
                        return !f.record.get('slaveChannel');
                    }
                }
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
                flex: 2,
                renderer: function (v) {
                    return Ext.isEmpty(v) ? '-' : v;
                },
                align: 'right',
                dataIndex: 'value'
            },
            {
                flex: 1,
                header: ' ',
                renderer: function (v, metaData, record) {
                    var status = record.get('validationResult') ? record.get('validationResult').split('.')[1] : '',
                        icon = '';

                    if (status === 'notValidated') {
                        icon = '<span class="icon-flag6" style="margin-left:-15px;" data-qtip="'
                            + Uni.I18n.translate('reading.validationResult.notvalidated', 'IMT', 'Not validated') + '"></span>';
                    } else if (status === 'suspect') {
                        icon = '<span class="icon-flag5" style="margin-left:-15px; color:red;" data-qtip="'
                            + Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') + '"></span>';
                    }
                    return icon;
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
                        itemId: 'device-channel-data-bulk-action-button',
                        text: Uni.I18n.translate('general.bulkAction', 'IMT', 'Bulk action'),
                        menu: {
                            xtype: 'purpose-channel-data-bulk-action-menu',
                            itemId: 'purpose-channel-data-bulk-action-menu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});