Ext.define('Imt.usagepointmanagement.view.calendars.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-calendar-configuration-details',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.usagepointmanagement.view.calendars.Grid',
        'Imt.usagepointmanagement.view.calendars.Preview',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    router: null,
    usagePoint: null,
    initComponent: function () {
        var me = this;
        me.calendarStore = Ext.getStore('Imt.usagepointmanagement.store.ActiveCalendars') || Ext.create('Imt.usagepointmanagement.store.ActiveCalendars');
        me.content = [
            {
                title: Uni.I18n.translate('general.label.calendars', 'IMT', 'Calendars'),
                ui: 'large',
                flex: 1,
                itemId: 'calendar-details-main-panel',
                items: {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'active-calendars-grid',
                        itemId: 'active-calendars-grid-grid',
                        store: me.calendarStore,
                        router: me.router,
                        usagePoint: me.usagePoint,
                        listeners: {
                            select: {
                                fn: Ext.bind(me.select, me)
                            }
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-calendars-found-panel',
                        title: Uni.I18n.translate('general.noCalendars', 'IMT', 'No active time of use calendar found'),
                        reasons: [
                            Uni.I18n.translate('usagePoint.calendars.empty.reason', 'IMT', 'There is no active calendar')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('usagePoint.calendars.define', 'IMT', 'Add calendar'),
                                // privileges: Imt.privileges.UsagePoint.canAdministrate,
                                href: me.router.getRoute('usagepoints/view/calendars/addcalendar').buildUrl({mRID: me.usagePoint.get('mRID')}),
                                action: 'define',
                                itemId: 'define-calendar-configuration'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'activeCalendarPreview',
                                itemId: 'activeCalendarPreview',
                                frame: true,
                                title: ' '
                            }
                        ]
                    }
                }
            }
        ];
        me.side = [
            {
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint
                    }
                ]
            }
        ];
        me.callParent(arguments);
    //    purposesStore.fireEvent('load');
    //    if (!Ext.isEmpty(me.meterRolesAvailable)) {
    //        me.down('#metrology-configuration-details-form').insert(2, {
    //            xtype: 'meter-roles-grid',
    //            style: 'padding-left: 0; padding-right: 0',
    //            store: meterRolesStore,
    //            router: me.router,
    //            itemId: 'metrology-configuration-meter-roles-grid'
    //        });
    //    }
    },

    select: function(slectionMode,selection){
        this.down('activeCalendarPreview').loadRecord(selection);
    }
});
