// TODO:  Pull through REST from back-end???
var serviceTypes = Ext.create('Ext.data.Store', {
	fields: ['name', 'displayName'],
	data : [
		    {'name':'ELECTRICITY', 'displayName':'Electric'},
		    {'name':'WATER', 'displayName':'Water'},
		    {'name':'GAS', 'displayName':'Gas'}
	]
});

Ext.define('InsightApp.view.UsagePointEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagePointEdit',
    itemId: 'usagePointEdit',

    edit: false,

    content: [
        {
            xtype: 'form',
            itemId: 'usagePointEditForm',
            ui: 'large',
            width: '100%',
            defaults: {
                labelWidth: 250,
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'hiddenfield',
                    name: 'id'
                },
                {
                    xtype: 'hiddenfield',
                    name: 'amiBillingReady',
                    value: 'UNKNOWN'
                },
                {
                    xtype: 'hiddenfield',
                    name: 'connectionState',
                    value: 'UNKNOWN'
                },
                {
                    xtype: 'textfield',
                    name: 'mRID',
                    fieldLabel: Uni.I18n.translate('general.formFieldLabel.mRID', 'INS', 'mRID'),
                    maxLength: 75,
                    width: 600
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('general.formFieldLabel.name', 'INS', 'Name'),
                    allowBlank: false,
                    maxLength: 75,
                    required: true,
                    width: 600
                },
                {
                	xtype: 'combobox',
                    name: 'serviceCategory',
                    fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.serviceType', 'INS', 'Service Type'),
                    store: serviceTypes,
                    queryMode: 'local',
                    editable: false,
                    displayField: 'displayName',
                    valueField: 'name',
                    allowBlank: false,
                    required: true,
                    width: 600
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'INS', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'saveModel',
                            itemId: 'createEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'INS', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            href: '#/administration/comservers/'
                        }
                    ]
                }
            ]
        }
    ],

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'INS', 'Save'));
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'INS', 'Add'));
        }
        this.down('#cancelLink').href = returnLink;
    }
});
