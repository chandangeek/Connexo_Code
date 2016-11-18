Ext.define('Dsh.view.widget.ConnectionActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.connection-action-menu',
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.runNow', 'DSH', 'Run now'),
                privileges : Mdc.privileges.Device.operateDeviceCommunication,
                action: 'run',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.viewHistory', 'DSH', 'View history'),
                action: 'viewHistory',
                section: this.SECTION_VIEW
            },
            {
                text: Uni.I18n.translate('general.viewLog', 'DSH', 'View log'),
                action: 'viewLog',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function (menu) {
            if (menu && menu.record) {
                var viewLogMenuItem = menu.down('menuitem[action=viewLog]');
                if (menu.record.get('comSessionId') !== 0 && menu.down('menuitem[action=viewLog]') !== null) {
                    viewLogMenuItem.show();
                } else {
                    viewLogMenuItem.hide();
                }
            }
        }
    }
});

