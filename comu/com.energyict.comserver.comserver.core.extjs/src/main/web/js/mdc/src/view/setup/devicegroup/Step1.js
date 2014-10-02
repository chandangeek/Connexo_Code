Ext.define('Mdc.view.setup.devicegroup.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'devicegroup-wizard-step1',
    name: 'deviceGroupWizardStep1',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Ext.form.RadioGroup'

    ],

    title: Uni.I18n.translate('devicegroup.wizard.step1title', 'MDC', 'Add a device group - Step 1 of 2: General'),


    items: [
        {
            xtype: 'panel',
            border: false,
            margin: '20 0 0 0',

            items: [
                {
                    xtype: 'textfield',
                    fieldLabel: 'Name',
                    itemId: 'deviceGroupNameTextField',
                    required: true,
                    width: 400
                },
                {
                    itemId: 'staticDynamicRadioButton',
                    xtype: 'radiogroup',
                    columns: 1,
                    fieldLabel: 'Type',
                    required: true,
                    vertical: true,
                    defaults: {
                        name: 'operation',
                        submitValue: false
                    },

                    items: [
                        { itemId: 'dynamicDeviceGroup', boxLabel: Uni.I18n.translate('devicegroup.wizard.dynamic', 'MDC', 'Dynamic device group (based on search criteria)'), name: 'operation', inputValue: 'dynamic', checked: true },
                        { itemId: 'staticDeviceGroup', boxLabel: Uni.I18n.translate('devicegroup.wizard.static', 'MDC', 'Static device group (based on search results)'), name: 'operation', inputValue: 'static'}
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});