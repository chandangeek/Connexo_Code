Ext.define('Mdc.usagepointmanagement.view.ChannelDataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.channel-data-grid',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],
    plugins: [
        {
            ptype: 'bufferedrenderer'
        }
    ],
    channel: null,

    initComponent: function () {
        var me = this,
            readingType = me.channel.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval',
                renderer: function (interval) {
                    return interval.end
                        ? Uni.I18n.translate(
                        'general.dateAtTime', 'MDC', '{0} at {1}',
                        [Uni.DateTime.formatDateShort(new Date(interval.end)), Uni.DateTime.formatTimeShort(new Date(interval.end))])
                        : '';
                },
                flex: 1
            },
            {
                header: unit
                    ? Uni.I18n.translate('general.value.with.param', 'MDC', 'Value ({0})', [unit])
                    : Uni.I18n.translate('general.value.empty', 'MDC', 'Value'),
                flex: 2,
                dataIndex: 'value',
                align: 'right'
            },
            {
                dataIndex: 'validation',
                flex: 1,
                renderer: function (value, metaData, record) {
                    var validationMap = {
                        NOT_VALIDATED: '<span class="icon-flag6" style="margin-left: -15px; line-height: 12px" data-qtip="' + Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated') + '"></span>',
                        SUSPECT: '<span class="icon-flag5" style="margin-left: -15px; color:red; line-height: 12px" data-qtip="' + Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect') + '"></span>',
                        INFORMATIVE: '<span class="icon-flag5" style="margin-left: -15px; color:yellow; line-height: 12px" data-qtip="' + Uni.I18n.translate('validationStatus.informative', 'MDC', 'Informative') + '"></span>',
                        NO_LINKED_DEVICES: '<span class="icon-flag5" style="margin-left: -15px; color:#686868; line-height: 12px" data-qtip="' + Uni.I18n.translate('validationStatus.noLinkedDevices', 'MDC', 'No linked devices') + '"></span>'
                    };

                    return validationMap[value];
                }
            },
            {
                header: Uni.I18n.translate('general.readingData.lastUpdate', 'MDC', 'Last update'),
                dataIndex: 'readingTime',
                flex: 1,
                renderer: function(value){
                    var date = new Date(value);
                    return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(date), Uni.DateTime.formatTimeShort(date)])
                }
            },
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                isFullTotalCount: true,
                noBottomPaging: false,
                usesExactCount: true,
                displayMsg: Uni.I18n.translate('usagePointChannelData.pagingtoolbartop.displayMsg', 'MDC', '{2} readings'),
                emptyMsg: Uni.I18n.translate('usagePointChannelData.pagingtoolbartop.emptyMsg', 'MDC', 'There are no readings to display')
            }
        ];

        me.callParent(arguments);
    }
});