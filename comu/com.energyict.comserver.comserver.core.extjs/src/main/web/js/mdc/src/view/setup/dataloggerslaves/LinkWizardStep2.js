Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step2',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'mdc-dataloggerslave-link-wizard-step2-errors',
                xtype: 'uni-form-error-message',
                hidden: true
            },
            {
                xtype: 'container',
                itemId: 'mdc-dataloggerslave-link-wizard-step2-container',
                fieldLabel: '',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [

                ]
            }
        ];

        me.callParent(arguments);
    }
});