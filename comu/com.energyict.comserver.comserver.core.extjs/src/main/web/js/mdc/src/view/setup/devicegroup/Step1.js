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
            xtype: 'form',
            border: false,
            margin: '20 0 0 0',

            items: [
                {
                    itemId: 'step1-adddevicegroup-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true,
                    text: Uni.I18n.translate('devicegroup.misingname', 'MDC', 'Please enter a name for the device group.')
                },
                /*{
                    xtype: 'container',
                    itemId: 'stepSelectionError',
                    //margin: '-20 0 0 0',
                    //hidden: true,
                    html: '<span style="color: #eb5642">' + Uni.I18n.translate('devicegroup.misingname', 'MDC', 'Please enter a name for the device group.') + '</span>'
                },*/
                {
                    xtype: 'textfield',
                    fieldLabel: 'Name',
                    itemId: 'deviceGroupNameTextField',
                    required: true,
                    allowBlank : false,
                    width: 400
                },
                /*{
                    itemId: 'step1-adddevicegroup-errors',
                    xtype: 'uni-form-error-message',
                    //hidden: true,
                    text: Uni.I18n.translate('searchItems.bulk.devicesError', 'MDC', 'It is required to select one or more devices to go to the next step.')
                },*/
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