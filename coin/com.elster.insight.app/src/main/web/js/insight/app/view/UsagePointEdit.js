// TODO:  Pull through REST from back-end???
var serviceTypes = Ext.create('Ext.data.Store', {
	fields: ['name', 'displayName'],
	data : [
		    {'name':'ELECTRICITY', 'displayName':'ELECTRICITY'},
		    {'name':'WATER', 'displayName':'WATER'},
		    {'name':'GAS', 'displayName':'GAS'}
	]
});

var phaseCodes = Ext.create('Ext.data.Store', {
	fields: ['value'],
	data: [
	       {'value': 'UNKNOWN'},
	       {'value': 'ABCN'}, 
	       {'value': 'ABC'}, 
	       {'value': 'ABN'}, 
	       {'value': 'ACN'}, 
	       {'value': 'BCN'}, 
	       {'value': 'AB'}, 
	       {'value': 'AC'}, 
	       {'value': 'BC'}, 
	       {'value': 'AN'}, 
	       {'value': 'BN'}, 
	       {'value': 'CN'}, 
	       {'value': 'A'}, 
	       {'value': 'B'}, 
	       {'value': 'C'}, 
	       {'value': 'N'}, 
	       {'value': 'S1N'}, 
	       {'value': 'S2N'}, 
	       {'value': 'S12N'}, 
	       {'value': 'S1'}, 
	       {'value': 'S2'}, 
	       {'value': 'S12'}]
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
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },
                {
                    xtype: 'textfield',
                    name: 'description',
                    fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.description', 'INS', 'Description'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },
                {
                    xtype: 'checkbox',
                    name: 'isSdp',
                    fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.isSdp', 'INS', 'SDP'),
                    width: 600
                },
                {
                    xtype: 'checkbox',
                    name: 'isVirtual',
                    fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.isVirtual', 'INS', 'Virtual'),
                    width: 600
                },
                {
                	xtype: 'combobox',
                    name: 'serviceCategory',
                    fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.serviceType', 'INS', 'Service category'),
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
								this.up().down('panel[itemId=form-technical-information]').show();
	                		} else {
	                			this.up().down('panel[itemId=form-technical-information]').hide();
	                		}
                    	}
                    }
                },
                {
                	xtype: 'panel',
                	title: Uni.I18n.translate('usagePoint.formFieldLabel.technicalInformation', 'INS', 'Technical information'),
                	layout: 'form',
                	ui: 'large',
                	itemId: 'form-technical-information',
                	hidden: true,
                	width: 600,
                	items: [
                        {
                            xtype: 'numberfield',
                            name: 'nominalVoltageValue',
                            labelWidth: 250,
                            fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.nominalVoltage', 'INS', 'Nominal Voltage (V)'),
                            allowBlank: true,
                            required: false
                        },
                        {
                            xtype: 'numberfield',
                            name: 'ratedCurrentValue',
                            labelWidth: 250,
                            fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.ratedCurrent', 'INS', 'Rated Current (A)'),
                            allowBlank: true,
                            required: false
                        },
                        {
                            xtype: 'numberfield',
                            name: 'ratedPowerValue',
                            labelWidth: 250,
                            fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.ratedPower', 'INS', 'Rated Power (kW)'),
                            allowBlank: true,
                            required: false
                        },
                        {
	                    	xtype: 'combobox',
	                        name: 'phaseCode',
	                        fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.phaseCode', 'INS', 'Phase Code'),
	                        store: phaseCodes,
	                        value: 'UNKNOWN',
	                        queryMode: 'local',
	                        editable: false,
	                        displayField: 'value',
	                        valueField: 'value',
	                        allowBlank: true,
	                        required: false,
                            labelWidth: 250
                        },
                        {
                            xtype: 'checkbox',
                            name: 'grounded',
                            labelWidth: 250,
                            fieldLabel: Uni.I18n.translate('usagePoint.formFieldLabel.grounded', 'INS', 'Grounded')
                        }
                	]                	
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
