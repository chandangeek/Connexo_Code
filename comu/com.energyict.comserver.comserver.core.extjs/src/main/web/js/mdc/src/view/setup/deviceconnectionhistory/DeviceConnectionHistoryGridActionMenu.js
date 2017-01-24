Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryGridActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.mdc-device-connection-history-grid-action-menu',
    initComponent: function () {

        this.items = [
            {
                itemId: 'menu-perform-task',
                text: Uni.I18n.translate('deviceconnectionhistory.viewLog', 'MDC', 'View connection log'),
                action: 'viewLog',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});


