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
            title: Uni.I18n.translate('usagePoint.add.title', 'INS', 'Add usage point'),
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
                    xtype: 'textfield',
                    name: 'mRID',
                    fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.mRID', 'INS', 'mRID'),
                    required: true,
                    allowBlank: false,
                    maxLength: 75,
                    width: 600
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.name', 'INS', 'Name'),
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
                    width: 600,
                    listeners: {
                    	change: function(field, newValue, oldValue) {
	                		if (newValue == 'ELECTRICITY') {
								this.up().down('textfield[name=nominalVoltage.value]').show();
	                		} else {
	                			this.up().down('textfield[name=nominalVoltage.value]').hide();
	                		}
                    	}
                    }
                },
                {
                    xtype: 'textfield',
                    name: 'nominalVoltage.value',
                    fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.nominalVoltage', 'INS', 'Nominal Voltage'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600,
                    hidden: true
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
  //                          href: '#/administration/comservers/'
                            href: '#/insight'
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
