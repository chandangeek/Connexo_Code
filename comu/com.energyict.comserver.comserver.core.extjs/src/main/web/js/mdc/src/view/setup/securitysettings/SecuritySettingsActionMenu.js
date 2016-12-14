Ext.define('Mdc.view.setup.securitysettings.SecuritySettingsActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.security-settings-action-menu',
    plain: true,
    border: false,
    itemId: 'security-settings-settings-menu',
    shadow: false,

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editsecuritysetting',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'deletesecuritysetting',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }

});
