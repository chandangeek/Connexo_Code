Ext.define('Imt.purpose.view.ReadingsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.readings-list',
    itemId: 'readings-list',
    requires: [
        'Imt.purpose.store.Readings',
        'Uni.view.toolbar.PagingTop'
    ],
    plugins: [
        {
            ptype: 'bufferedrenderer'
        }
    ],
    store: 'Imt.purpose.store.Readings',

    initComponent: function () {
        var me = this,
            readingType = me.output.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval',
                renderer: function (interval) {
                    return  interval.end
                        ? Uni.I18n.translate(
                        'general.dateAtTime', 'MDC', '{0} at {1}',
                        [Uni.DateTime.formatDateShort(new Date(interval.end)), Uni.DateTime.formatTimeShort(new Date(interval.end))] )
                        : '';
                },
                flex: 1
            },
            {
                header: unit
                    ? Uni.I18n.translate('general.value', 'MDM', 'Value ({0})', [unit])
                    : Uni.I18n.translate('general.value.empty', 'MDC', 'Value'),
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
                displayMsg: Uni.I18n.translate('reading.pagingtoolbartop.displayMsg', 'IMT', '{1} reading(s)')
            }
        ];

        me.callParent(arguments);
    }
});