Ext.define('Imt.usagepointmanagement.view.calendars.VersionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.calendars-versions-grid',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.Category', 'IMT', 'Category'),
                dataIndex: 'category',
                flex: 1,
                renderer: function(a,b,record){
                    return record.getCalendar().get('category').displayName;
                }
            },
            {
                header: Uni.I18n.translate('general.Calendar', 'IMT', 'Calendar'),
                dataIndex: 'calendar',
                flex: 1,
                renderer: function(calendar){
                    return calendar.name;
                }
            },
            {
                header: Uni.I18n.translate('general.Period', 'IMT', 'Period'),
                dataIndex: 'fromTime',
                flex: 1,
                renderer: function(value, meta, record) {
                    var from = record.get('fromTime'),
                        to = record.get('toTime');

                    return to ? Uni.I18n.translate('general.period.fromUntil', 'IMT', 'From {0} until {1}', [
                        Uni.DateTime.formatDateTimeShort(from),
                        Uni.DateTime.formatDateTimeShort(to)
                    ])
                        : Uni.I18n.translate('general.period.from', 'IMT', 'From {0}', [
                        Uni.DateTime.formatDateTimeShort(from)
                    ]);
                }
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('Calendars.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} calendars'),
                displayMoreMsg: Uni.I18n.translate('Calendars.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} calendars'),
                emptyMsg: Uni.I18n.translate('Calendars.pagingtoolbartop.emptyMsg', 'IMT', 'There are no active calendars'),
                noBottomPaging: true,
                usesExactCount: true
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('Calendars.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Calendars per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});