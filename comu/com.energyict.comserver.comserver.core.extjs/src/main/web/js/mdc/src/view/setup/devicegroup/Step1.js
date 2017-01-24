Ext.define('Mdc.view.setup.devicegroup.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-group-wizard-step1',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage'
    ],

    isEdit: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'step1-adddevicegroup-errors',
                xtype: 'uni-form-error-message',
                width: 400,
                hidden: true
            },
            {
                xtype: 'textfield',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.name','MDC','Name'),
                itemId: 'deviceGroupNameTextField',
                required: true,
                allowBlank: false,
                maxLength: 80,
                enforceMaxLength: true,
                width: 400,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            }
        ];

        if (me.isEdit) {
            me.items.push({
                itemId: 'device-group-type-display-field',
                xtype: 'displayfield',
                name: 'dynamic',
                fieldLabel: Uni.I18n.translate('general.type','MDC','Type'),
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('devicegroup.wizard.dynamic', 'MDC', 'Dynamic device group (based on search criteria)')
                        : Uni.I18n.translate('devicegroup.wizard.static', 'MDC', 'Static device group (based on search results)');
                }
            });
        } else {
            me.items.push({
                itemId: 'staticDynamicRadioButton',
                xtype: 'radiogroup',
                columns: 1,
                fieldLabel: Uni.I18n.translate('general.type','MDC','Type'),
                required: true,
                vertical: true,

                items: [
                    {
                        itemId: 'dynamicDeviceGroup',
                        boxLabel: Uni.I18n.translate('devicegroup.wizard.dynamic', 'MDC', 'Dynamic device group (based on search criteria)'),
                        name: 'dynamic',
                        inputValue: true
                    },
                    {
                        itemId: 'staticDeviceGroup',
                        boxLabel: Uni.I18n.translate('devicegroup.wizard.static', 'MDC', 'Static device group (based on search results)'),
                        name: 'dynamic',
                        inputValue: false
                    }
                ]
            });
        }

        me.callParent(arguments);
    }
});