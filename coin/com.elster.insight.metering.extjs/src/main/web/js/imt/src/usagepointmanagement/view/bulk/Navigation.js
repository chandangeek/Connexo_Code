Ext.define('Imt.usagepointmanagement.view.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.usagepoints-bulk-navigation',
    width: 200,
    jumpForward: false,
    jumpBack: false,
    ui: 'medium',
    title: Uni.I18n.translate('general.bulkAction', 'IMT', 'Bulk action'),
    items: [
        {
            itemId: 'SelectDevices',
            text: Uni.I18n.translate('searchItems.bulk.selectDevices', 'IMT', 'Select devices')
        },
        {
            itemId: 'SelectAction',
            text: Uni.I18n.translate('searchItems.bulk.selectAction', 'IMT', 'Select action')
        },
        {
            itemId: 'actionDetails',
            text:  Uni.I18n.translate('searchItems.bulk.actionDetails', 'IMT', 'Action details')
        },
        {   itemId: 'Confirmation',
            text: Uni.I18n.translate('searchItems.bulk.confirmation', 'IMT', 'Confirmation')
        },
        {
            itemId: 'Status',
            text: Uni.I18n.translate('general.status', 'IMT', 'Status')
        }
    ]

});