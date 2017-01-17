Ext.define('Imt.purpose.view.PurposeActionsMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.purpose-actions-menu',

    items: [
        {
            text: Uni.I18n.translate('general.validateNow', 'IMT', 'Validate now'),
            action: 'validateNow',
            itemId: 'validate-now',
            section: this.SECTION_ACTION
        },
        {
            text: Uni.I18n.translate('general.estimateNow', 'IMT', 'Estimate now'),
            action: 'estimateNow',
            itemId: 'estimate-now',
            section: this.SECTION_ACTION
        }
    ]
});

