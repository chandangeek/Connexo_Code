Ext.define('Mdc.view.setup.devicegroup.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'devicegroup-wizard-step1',
    name: 'deviceGroupWizardStep1',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Ext.form.RadioGroup'

    ],

    title: Uni.I18n.translate('devicegroup.wizard.step1title', 'MDC', 'Step 1 of 2: Name and type'),


    items: [
        {
            xtype: 'form',
            border: false,
            margin: '20 0 0 0',

            items: [
                {
                    itemId: 'step1-adddevicegroup-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true,
                    text: Uni.I18n.translate('devicegroup.missingname', 'MDC', 'Please enter a name for the device group.')
                },
                {
                    itemId: 'step1-adddevicegroup-name-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true,
                    text: Uni.I18n.translate('devicegroup.duplicatename', 'MDC', 'A device group with this name already exists.')
                },
                {
                    xtype: 'textfield',
                    fieldLabel: 'Name',
                    itemId: 'deviceGroupNameTextField',
                    required: true,
                    allowBlank: false,
                    maxLength: 80,
                    enforceMaxLength: true,
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