Ext.define('Mdc.metrologyconfiguration.view.AddForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configurations-add-form',
    requires: [
        'Uni.form.field.readingtypes.ReadingTypesField',
        'Uni.form.field.ComboReturnedRecordData',
        'Uni.util.FormErrorMessage'
    ],

    isEdit: false,
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
                xtype: 'combo-returned-record-data',
                name: 'serviceCategory',
                itemId: 'mc-serviceCategory-combobox',
                fieldLabel: Uni.I18n.translate('general.serviceCategory', 'MDC', 'Service category'),
                required: true,
                store: 'Mdc.metrologyconfiguration.store.ServiceCategories',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
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
                        text: me.isEdit
                            ? Uni.I18n.translate('general.save', 'MDC', 'Save')
                            : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                        ui: 'action',
                        action: me.isEdit
                            ? 'saveMetrologyConfiguration'
                            : 'addMetrologyConfiguration'
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
    },

    loadRecord: function (record) {
        var me = this,
            status = record.get('status'),
            editable = Ext.isObject(status) && status.id === 'active';

        Ext.suspendLayouts();
        me.down('#mc-serviceCategory-combobox').setDisabled(editable);
        me.down('#mc-readingTypes-reading-types-field').setDisabled(editable);
        me.callParent(arguments);
        Ext.resumeLayouts(true);
    }
});