Ext.define('Mdc.view.setup.communicationschedule.CommunicationScheduleActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.communication-schedule-action-menu',
    itemId: 'communication-schedule-action-menu',
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'editCommunicationSchedule',
                action: 'editCommunicationSchedule',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'deleteCommunicationSchedule',
                action: 'deleteCommunicationSchedule',
                section: this.SECTION_REMOVE

            }
        ];
        this.callParent(arguments);
    }
});
