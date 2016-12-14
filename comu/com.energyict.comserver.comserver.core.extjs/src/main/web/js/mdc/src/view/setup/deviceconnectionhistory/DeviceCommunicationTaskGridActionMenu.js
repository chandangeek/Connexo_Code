Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskGridActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.mdc-device-communication-task-grid-action-menu',
    initComponent: function () {

        this.items = [
            {
                itemId: 'menu-perform-task',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.viewCommunicationLog', 'MDC', 'View communication log'),
                action: 'viewLog',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});


