Ext.define('Sct.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.sct-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'change-log-level-sct',
            text: Uni.I18n.translate('general.changeLogLevel', 'SCT', 'Change log level'),
            privileges: Sct.privileges.ServiceCallType.admin,
            action: 'changeLogLevel'
        }
    ]
});