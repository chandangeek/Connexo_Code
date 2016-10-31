Ext.define('Mdc.view.setup.communicationschedule.CommunicationScheduleActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.communication-schedule-action-menu',
    plain: true,
    border: false,
    itemId: 'communication-schedule-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'editCommunicationSchedule',
            action: 'editCommunicationSchedule'

        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'deleteCommunicationSchedule',
            action: 'deleteCommunicationSchedule'

        },
        {
            text: Uni.I18n.translate('general.clone', 'MDC', 'Clone'),
            itemId: 'cloneCommunicationSchedule',
            action: 'cloneCommunicationSchedule'
        }
    ]
});
