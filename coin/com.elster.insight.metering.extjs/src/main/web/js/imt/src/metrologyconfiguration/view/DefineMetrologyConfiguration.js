Ext.define('Imt.metrologyconfiguration.view.DefineMetrologyConfiguration', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'define-metrology-configuration',

    requires: [
        'Imt.metrologyconfiguration.view.Wizard'
    ],

    returnLink: null,
    isPossibleAdd: true,

    initComponent: function () {
        var me = this;

        me.side = {
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'navigation-menu',
                    jumpForward: false,
                    jumpBack: true,
                    width: 270,
                    ui: 'medium',
                    padding: '0 0 0 0',
                    itemId: 'define-metrology-configuration-navigation',
                    title: Uni.I18n.translate('defineMetrologyConfiguration.wizard.menu', 'IMT', 'Define metrology configuration'),
                    items: [
                        {
                            itemId: 'define-metrology-configuration-navigation-step-1',
                            text: Uni.I18n.translate('defineMetrologyConfiguration.navigation.step1', 'IMT', 'Select metrlogy configuration')
                        }
                    ]
                }
            ]
        };

        me.content = [
            {
                xtype: 'define-metrology-configuration-wizard',
                itemId: 'define-metrology-configuration-wizard',
                returnLink: me.returnLink,
                isPossibleAdd: me.isPossibleAdd
            }
        ];

        me.callParent(arguments);
    }
});