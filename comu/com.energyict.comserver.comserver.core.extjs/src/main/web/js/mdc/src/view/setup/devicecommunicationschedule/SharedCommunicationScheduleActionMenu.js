Ext.define('Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.shared-communication-schedule-action-menu',
    plain: true,
    border: false,
    itemId: 'shared-communication-schedule-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('deviceCommunicationSchedules.removeCommunicationSchedule', 'MDC', 'Remove shared communication schedule'),
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            itemId: 'removeCommunicationSchedule',
            action: 'removeSharedCommunicationSchedule'

        }
    ]
});
