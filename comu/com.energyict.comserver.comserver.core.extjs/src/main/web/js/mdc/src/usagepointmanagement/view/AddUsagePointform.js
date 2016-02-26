Ext.define('Mdc.usagepointmanagement.view.AddUsagePointForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-usage-point-form',
    requires: [
        'Uni.form.field.Duration',
        'Mdc.usagepointmanagement.view.InstallationTimeField'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    width: 650,
    defaults: {
        labelWidth: 250
    },

    items: [
        {
            xtype: 'textfield',
            name: 'mRID',
            itemId: 'fld-up-mRID',
            required: true,
            fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.mrid', 'MDC', 'mRID')
        },
        {
            xtype: 'textfield',
            name: 'name',
            itemId: 'fld-up-name',
            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            renderer: function (value) {
                return value ? value : '-';
            }
        },
        {
            xtype: 'combobox',
            name: 'serviceCategory',
            itemId: 'fld-up-serviceCategory',
            required: true,
            fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.serviceCategory', 'MDC', 'Service category'),
        },
        {
            xtype: 'installationtimefield',
            dateFieldName: 'installationTime',
            itemId: 'up-createTime-installationtimefield',
            fieldLabel: Uni.I18n.translate('general.label.created', 'MDC', 'Created'),
            required: true,
            //width: 600
        },
        //{
        //    name: 'created',
        //    itemId: 'fld-up-created',
        //    fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.created', 'MDC', 'Created')
        //},
        {
            xtype: 'fieldcontainer',
            ui: 'actions',
            fieldLabel: '&nbsp',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                    xtype: 'button',
                    ui: 'action',
                    action: 'save',
                    itemId: 'deviceAddSaveButton'
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    xtype: 'button',
                    ui: 'link',
                    itemId: 'cancelLink',
                    href: '#/usagepoints/'
                }
            ]
        }
    ]
});