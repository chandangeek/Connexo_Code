Ext.define('Imt.usagepointmanagement.view.transitionexecute.WizardNavigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.usagepointTransitionWizardNavigation',
    jumpBack: false,
    ui: 'medium',
    items: [
        {
            itemId: 'setProperties',
            text: Uni.I18n.translate('usagepointtransitionexecute.navigation.step1title', 'IMT', 'Set properties')
        },
        {
            itemId: 'executionStatus',
            text: Uni.I18n.translate('general.status', 'IMT', 'Status')
        }
    ]
});
