Ext.define('Imt.usagepointmanagement.view.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.add-usage-point-navigation',
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    padding: '0 0 0 0',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'navigation-general-info',
                text: Uni.I18n.translate('usagepoint.navigation.step1', 'IMT', 'General information')
            },
            {
                itemId: 'navigation-technical-info',
                text: Uni.I18n.translate('usagepoint.navigation.step2', 'IMT', 'Technical information')
            },
            {
                itemId: 'navigation-metrology-configuration-with-meters-info',
                text: Uni.I18n.translate('general.linkMetrologyConfiguration', 'IMT', 'Link metrology configuration')
            },
            {
                itemId: 'navigation-life-cycle-transition-info',
                text: Uni.I18n.translate('general.lifeCycleTransition', 'IMT', 'Life cycle transition')
            }
        ];

        me.callParent(arguments);
    }
});