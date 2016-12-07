Ext.define('Imt.usagepointmanagement.view.calendars.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.calendarActionMenu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'view-timeline',
            text: Uni.I18n.translate('general.menu.viewTimeline', 'IMT', 'View timeline'),
            action: 'viewTimeline'
        },
        {
            itemId: 'remove',
            text: Uni.I18n.translate('general.menu.viewPreview', 'IMT', 'View preview'),
//            privileges: Cfg.privileges.Validation.validateManual,
            action: 'viewPreview'
        }
    ]
});
