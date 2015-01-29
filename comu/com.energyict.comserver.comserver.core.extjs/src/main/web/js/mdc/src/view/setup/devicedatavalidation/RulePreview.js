Ext.define('Mdc.view.setup.devicedatavalidation.RulePreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceDataValidationRulePreview',
    itemId: 'deviceDataValidationRulePreview',
    frame: true,
    requires: [
        'Cfg.model.ValidationRule'
    ],
    title: '',
    layout: {
        type: 'vbox'
    },
    defaults: {
        xtype: 'displayfield',
        labelWidth: 260
    },
    items: [
        {
            name: 'name',
            fieldLabel: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule')
        },
        {
            name: 'displayName',
            fieldLabel: Uni.I18n.translate('validation.validator', 'CFG', 'Validator')
        },
        {
            name: 'active',
            fieldLabel: Uni.I18n.translate('validation.status', 'CFG', 'Status'),
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('validation.active', 'CFG', 'Active')
                } else {
                    return Uni.I18n.translate('validation.inactive', 'CFG', 'Inactive')
                }
            }
        },
        {
            xtype: 'container',
            itemId: 'readingTypesArea',
            items: []
        },
        {
            xtype: 'property-form',
            padding: '5 10 0 10',
            width: '100%',
            isEdit: false
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});