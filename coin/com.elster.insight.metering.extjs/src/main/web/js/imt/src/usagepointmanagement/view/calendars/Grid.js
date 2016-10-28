Ext.define('Imt.usagepointmanagement.view.calendars.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.active-calendars-grid',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.Category', 'IMT', 'Category'),
              //  dataIndex: 'calendar',
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
                header: Uni.I18n.translate('general.ActiveSince', 'IMT', 'Active since'),
                dataIndex: 'fromTime',
                flex: 1,
                renderer: function(value){
                   return Uni.DateTime.formatDateTimeShort(value);
                }
            },
            {
                xtype: 'uni-actioncolumn',
              //  privileges: Mdc.privileges.DeviceType.admin,
                menu: {
                    xtype: 'calendarActionMenu'
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
                usesExactCount: true,
                items: [
                    {
                        text: Uni.I18n.translate('devicetype.createDeviceType', 'IMT', 'Add calendar'),
                      //  privileges: Mdc.privileges.DeviceType.admin,
                        itemId: 'addCalendar',
                        xtype: 'button',
                        href: me.router.getRoute('usagepoints/view/calendars/addcalendar').buildUrl({mRID: me.usagePoint.get('mRID')})
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});