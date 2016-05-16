Ext.define('Mdc.metrologyconfiguration.view.AddForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configurations-add-form',
    requires: [
        'Uni.form.field.readingtypes.ReadingTypesField'
    ],

    returnLink: null,

    initComponent: function () {
        var me = this;

        me.defaults = {
            labelWidth: 260,
            width: 600
        };

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'mc-add-warning',
                hidden: true
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'mc-name-textfield',
                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                required: true
            },
            {
                xtype: 'textareafield',
                name: 'description',
                itemId: 'mc-description-textareafield',
                fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                height: 160
            },
            {
                xtype: 'combobox',
                name: 'serviceCategory',
                itemId: 'mc-serviceCategory-combobox',
                fieldLabel: Uni.I18n.translate('general.serviceCategory', 'MDC', 'Service category'),
                required: true,
                store: 'Mdc.usagepointmanagement.store.ServiceCategories',
                queryMode: 'local',
                displayField: 'displayName',
                valueField: 'name',
                forceSelection: true,
                valueIsRecordData: true
            },
            {
                xtype: 'reading-types-field',
                name: 'readingTypes',
                itemId: 'mc-readingTypes-reading-types-field',
                fieldLabel: Uni.I18n.translate('general.readingTypes', 'MDC', 'Reading types'),
                required: true
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'mc-form-buttons',
                fieldLabel: ' ',
                layout: 'hbox',
                margin: '20 0 0 0',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'mc-add-button',
                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                        ui: 'action',
                        action: 'addMetrologyConfiguration'
                    },
                    {
                        xtype: 'button',
                        itemId: 'mc-cancel-add-button',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        ui: 'link',
                        action: 'cancelAddMetrologyConfiguration',
                        href: me.returnLink
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});