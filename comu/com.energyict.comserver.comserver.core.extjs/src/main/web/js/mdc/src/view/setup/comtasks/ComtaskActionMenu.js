Ext.define('Mdc.view.setup.comtasks.ComtaskActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.comtaskActionMenu',
    communicationTask: undefined,

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editComTask',
                privileges: Mdc.privileges.Communication.admin,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'deleteComTask',
                privileges: Mdc.privileges.Communication.admin,
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});