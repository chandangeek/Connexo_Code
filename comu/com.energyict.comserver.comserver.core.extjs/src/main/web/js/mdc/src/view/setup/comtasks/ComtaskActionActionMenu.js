Ext.define('Mdc.view.setup.comtasks.ComtaskActionActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.comtaskActionActionMenu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editComTaskAction',
                privileges: Mdc.privileges.Communication.admin,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'deleteComTaskAction',
                privileges: Mdc.privileges.Communication.admin,
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});