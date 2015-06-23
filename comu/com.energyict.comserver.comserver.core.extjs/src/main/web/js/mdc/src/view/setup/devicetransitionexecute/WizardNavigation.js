Ext.define('Mdc.view.setup.devicetransitionexecute.WizardNavigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.deviceTransitionWizardNavigation',
    width: 256,
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    padding: '0 0 0 0',
    margin: '0 0 0 0',

    items: [
        {
            itemId: 'setProperties',
            text: Uni.I18n.translate('devicetransitionexecute.navigation.step1title', 'MDC', 'Set properties')
        },
        {
            itemId: 'executionStatus',
            text: Uni.I18n.translate('devicetransitionexecute.navigation.step1title', 'MDC', 'Status')
        }
    ]
});
