Ext.define('Mdc.timeofuseondevice.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tou-device-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'activate-calendar-tou',
            text: Uni.I18n.translate('timeofuse.activatePassiveCalendar', 'MDC', 'Activate passive calendar'),
            //privileges: Scs.privileges.ServiceCall.admin,
            action: 'activatecalendar'
            //visible: function() {
            //    return this.record.get('canCancel');
            //}
        },
        {
            itemId: 'clear-tariff-tou',
            text: Uni.I18n.translate('timeofuse.clearPassiveTariff', 'MDC', 'Clear passive tariff'),
            action: 'cleartariff'
        },
        {
            itemId: 'send-calendar-tou',
            text: Uni.I18n.translate('timeofuse.sendCalendar', 'MDC', 'Send calendar'),
            action: 'sendcalendar'
        },
        {
            itemId: 'verify-calendars-tou',
            text: Uni.I18n.translate('timeofuse.verifyCalendars', 'MDC', 'Verify calendars'),
            action: 'verifycalendars'
        },
        {
            itemId: 'view-preview-tou',
            text: Uni.I18n.translate('timeofuse.viewPreview', 'MDC', 'View preview'),
            action: 'viewpreview'
        }

    ],
    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) ? item.show() : item.hide(); //hier nog privileges in de check?
                }
            })
        }
    }
});