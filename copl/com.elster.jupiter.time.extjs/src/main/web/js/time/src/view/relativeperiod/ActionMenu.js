Ext.define('Tme.view.relativeperiod.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.relative-periods-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'remove-period',
            text: Uni.I18n.translate('general.remove', 'TME', 'Remove'),
            privileges : Tme.privileges.Period.admin,
            action: 'removePeriod'
        },
        {
            itemId: 'view-details',
            text: Uni.I18n.translate('general.viewDetails', 'TME', 'View details'),
            action: 'viewDetails'
        }
    ]
});

