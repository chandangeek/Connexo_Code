Ext.define('Mdc.view.setup.comserver.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.comserver-actionmenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'edit',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.Communication.admin,
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'activate',
                text: Uni.I18n.translate('comserver.activate', 'MDC', 'Activate'),
                privileges: Mdc.privileges.Communication.admin,
                action: 'activate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'deactivate',
                text: Uni.I18n.translate('comserver.deactivate', 'MDC', 'Deactivate'),
                privileges: Mdc.privileges.Communication.admin,
                action: 'deactivate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'monitor',
                text: Uni.I18n.translate('comserver.monitor', 'MDC', 'Monitor'),
                privileges: Mdc.privileges.Monitor.monitor,
                action: 'monitor',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'remove',
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                privileges: Mdc.privileges.Communication.admin,
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
