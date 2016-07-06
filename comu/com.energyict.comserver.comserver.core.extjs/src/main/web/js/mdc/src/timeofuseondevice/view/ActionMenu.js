Ext.define('Mdc.timeofuseondevice.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tou-device-action-menu',
    plain: true,
    border: false,
    shadow: false,
    record: null,
    showPreview: true,
    items: [
        {
            itemId: 'activate-calendar-tou',
            text: Uni.I18n.translate('timeofuse.activatePassiveCalendar', 'MDC', 'Activate passive calendar'),
            privileges: Mdc.privileges.DeviceCommands.executeCommands,
            action: 'activatecalendar',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.activatePassive
        },
        {
            itemId: 'clear-tariff-tou',
            text: Uni.I18n.translate('timeofuse.clearPassiveCalendar', 'MDC', 'Clear passive calendar'),
            privileges: Mdc.privileges.DeviceCommands.executeCommands,
            action: 'cleartariff',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.clearAndDisable
        },
        {
            itemId: 'send-calendar-tou',
            text: Uni.I18n.translate('timeofuse.sendCalendar', 'MDC', 'Send calendar'),
            privileges: Mdc.privileges.DeviceCommands.executeCommands,
            action: 'sendcalendar',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.supportsSend
        },
        {
            itemId: 'verify-calendars-tou',
            text: Uni.I18n.translate('timeofuse.checkTimeOfUseCalendar', 'MDC', 'Check time of use calendar'),
            privileges: Mdc.privileges.DeviceCommands.executeCommands,
            action: 'verifycalendars',
        },
        {
            itemId: 'view-preview-tou',
            text: Uni.I18n.translate('timeofuse.viewPreview', 'MDC', 'View preview'),
            privileges: Mdc.privileges.Device.viewDevice,
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.timeOfUseAllowed,
            visible: function() {
                return this.showPreview;
            },
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