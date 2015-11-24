//TODO: Pull ENUM values through REST?  Add I18N translations?
var serviceTypes = Ext.create('Ext.data.Store', {
	fields: ['value', 'localizedValue'],
	data : [
		    {'value':'ELECTRICITY', 'localizedValue':'ELECTRICITY'},
		    {'value':'WATER', 'localizedValue':'WATER'},
		    {'value':'GAS', 'localizedValue':'GAS'}
	]
});

var connectionStates = Ext.create('Ext.data.Store', {
	fields: ['value', 'localizedValue'],
	data : [
	    	{'value':'UNKNOWN', 'localizedValue':'UNKNOWN'},
	    	{'value':'CONNECTED', 'localizedValue':'CONNECTED'},
	    	{'value':'PHYSICALLYDISCONNECTED', 'localizedValue':'PHYSICALLY DISCONNECTED'},
	    	{'value':'LOGICALLYDISCONNECTED', 'localizedValue':'LOGICALLY DISCONNECTED'}
	]
});

var loadUnits = Ext.create('Ext.data.Store', {
	fields: ['value', 'localizedValue'],
	data : [
	    	{'value':'W', 'localizedValue':'kW'},
	    	{'value':'VA', 'localizedValue':'kVA'}
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

var amiBillingReadyKinds = Ext.create('Ext.data.Store', {
	fields: ['value', 'localizedValue'],
	data : [
	    	{'value':'UNKNOWN', 'localizedValue':'UNKNOWN'},
	    	{'value':'ENABLED', 'localizedValue':'ENABLED'},
	    	{'value':'OPERABLE', 'localizedValue':'OPERABLE'},
	    	{'value':'BILLINGAPPROVED', 'localizedValue':'BILLING APPROVED'},
	    	{'value':'NONAMI', 'localizedValue':'NONAMI'},
	    	{'value':'AMIDISABLED', 'localizedValue':'AMI DISABLED'},
	    	{'value':'AMICAPABLE', 'localizedValue':'AMI CAPABLE'},
	    	{'value':'NONMETERED', 'localizedValue':'NONMETERED'}
	]
});

Ext.define('Imt.usagepointmanagement.view.UsagePointEdit', {
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
            title: Uni.I18n.translate('usagepoint.label.add', 'IMT', 'Add usage point'),
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
                    xtype: 'textfield',
                    name: 'mRID',
                    fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'mRID'),
                    required: true,
                    allowBlank: false,
                    maxLength: 75,
                    width: 600
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },               
                {
                    xtype: 'textfield',
                    name: 'aliasName',
                    fieldLabel: Uni.I18n.translate('general.label.aliasName', 'IMT', 'Alias name'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },
                {
                    xtype: 'textareafield',
                    name: 'description',
                    fieldLabel: Uni.I18n.translate('general.label.description', 'IMT', 'Description'),
                    allowBlank: true,
                    grow: true,
                    required: false,
                    width: 600
                },
                {
                    xtype: 'checkbox',
                    name: 'isSdp',
                    value: true,
                    fieldLabel: Uni.I18n.translate('general.label.sdp', 'IMT', 'SDP'),
                    width: 600
                },
                {
                    xtype: 'checkbox',
                    name: 'isVirtual',
                    fieldLabel: Uni.I18n.translate('general.label.virtual', 'IMT', 'Virtual'),
                    width: 600
                },
                {
                    xtype: 'checkbox',
                    name: 'checkBilling',
                    fieldLabel: Uni.I18n.translate('general.label.checkBilling', 'IMT', 'Check billing'),
                    width: 600
                },        
                {
                	xtype: 'combobox',
                    name: 'amiBillingReady',
                    fieldLabel: Uni.I18n.translate('general.label.amiBillingReady', 'IMT', 'AMI billing ready'),
                    store: amiBillingReadyKinds,
                    value: 'UNKNOWN',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'localizedValue',
                    valueField: 'value',
                    allowBlank: true,
                    required: false,
                    labelWidth: 250,
                    width: 600
                },
                {
                    xtype: 'textfield',
                    name: 'outageRegion',
                    fieldLabel: Uni.I18n.translate('general.label.outageRegion', 'IMT', 'Outage region'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },
                {
                    xtype: 'textfield',
                    name: 'readCycle',
                    fieldLabel: Uni.I18n.translate('general.label.readCycle', 'IMT', 'Read cycle'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },
                {
                    xtype: 'textfield',
                    name: 'readRoute',
                    fieldLabel: Uni.I18n.translate('general.label.readRoute', 'IMT', 'Read route'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },
                {
                    xtype: 'textfield',
                    name: 'servicePriority',
                    fieldLabel: Uni.I18n.translate('general.label.servicePriority', 'IMT', 'Service priority'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },               
                {
                    xtype: 'textfield',
                    name: 'serviceDeliveryRemark',
                    fieldLabel: Uni.I18n.translate('general.label.serviceDeliveryRemark', 'IMT', 'Service delivery remark'),
                    allowBlank: true,
                    maxLength: 75,
                    required: false,
                    width: 600
                },
                {
                	xtype: 'combobox',
                    name: 'connectionState',
                    fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state'),
                    store: connectionStates,
                    value: 'UNKNOWN',
                    queryMode: 'local',
                    editable: false,
                    displayField: 'localizedValue',
                    valueField: 'value',
                    allowBlank: true,
                    required: false,
                    labelWidth: 250,
                    width: 600
                },
                {
                    xtype: 'combobox',
                    name: 'metrologyConfiguration',
                    fieldLabel: Uni.I18n.translate('usagepoint.label.metrologyconfiguration', 'IMT', 'Metrology configuration'),
                    store: 'Imt.metrologyconfiguration.store.MetrologyConfigurationSelect',
                    displayField: 'name',
                    valueField: 'id',
                    value: 'NONE',
                    queryMode: 'local',
                    editable: false,
                    allowBlank: true,
                    required: false,
                    labelWidth: 250,
                    width: 600
                },
                {
                	xtype: 'combobox',
                    name: 'serviceCategory',
                    fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category'),
                    store: serviceTypes,
                    queryMode: 'local',
                    editable: false,
                    displayField: 'localizedValue',
                    valueField: 'value',
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
                	title: Uni.I18n.translate('usagepoint.technical.information', 'IMT', 'Technical information'),
                	layout: 'form',
                	ui: 'large',
                	itemId: 'form-technical-information',
                	hidden: true,
                	width: 600,
                	items: [
						{
						    xtype: 'checkbox',
						    name: 'grounded',
						    labelWidth: 250,
						    value: true,
						    fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')
						},
                        {
                            xtype: 'numberfield',
                            name: 'nominalServiceVoltageValue',
                            allowNegative: false,
                            minValue: 0,
                            labelWidth: 250,
                            fieldLabel: Uni.I18n.translate('general.label.nominalVoltage', 'IMT', 'Nominal voltage (V)'),
                            allowBlank: true,
                            required: false
                        },
                        {
	                    	xtype: 'combobox',
	                        name: 'phaseCode',
	                        fieldLabel: Uni.I18n.translate('general.label.phaseCode', 'IMT', 'Phase code'),
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
                            xtype: 'numberfield',
                            name: 'ratedCurrentValue',
                            labelWidth: 250,
                            allowNegative: false,
                            minValue: 0,
                            fieldLabel: Uni.I18n.translate('general.label.ratedCurrent', 'IMT', 'Rated current (A)'),
                            allowBlank: true,
                            required: false
                        },
                        {
                            xtype: 'numberfield',
                            name: 'ratedPowerValue',
                            labelWidth: 250,
                            allowNegative: false,
                            minValue: 0,
                            fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Rated power (kW)'),
                            allowBlank: true,
                            required: false
                        },
                        {
                        	xtype: 'panel',
                        	layout: 'hbox',
                        	items: [
                        	    {
                        	    	xtype: 'numberfield',
                        	    	name: 'estimatedLoadValue',
                        	    	labelWidth: 250,
                                    minValue: 0,
                        	    	fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load'),
                        	    	allowBlank: true,
                        	    	required: false,
                        	    	width: 500
                        	    },
                                {
        	                    	xtype: 'combobox',
        	                        name: 'estimatedLoadUnit',
        	                        store: loadUnits,
        	                        value: 'W',
        	                        queryMode: 'local',
        	                        editable: false,
        	                        displayField: 'localizedValue',
        	                        valueField: 'value',
        	                        width: 100
                                }
                        	]
                        }
                	]                	
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.button.add', 'IMT', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'saveModel',
                            itemId: 'createEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.button.cancel', 'IMT', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            href: '#'
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
            this.down('form').setTitle(Uni.I18n.translate('usagepoint.label.edit', 'IMT', 'Edit usage point'));
            this.down('#createEditButton').setText(Uni.I18n.translate('general.button.save', 'IMT', 'Save'));
            this.down('textfield[name="mRID"]').setDisabled(true);
            this.down('combobox[name="serviceCategory"]').setDisabled(true);
            this.down('checkbox[name="isSdp"]').setDisabled(true);
            this.down('checkbox[name="isVirtual"]').setDisabled(true);
        } else {
            this.edit = edit;
            this.down('form').setTitle(Uni.I18n.translate('usagepoint.label.add', 'IMT', 'Add usage point'));
            this.down('#createEditButton').setText(Uni.I18n.translate('general.button.add', 'IMT', 'Add'));
            this.down('textfield[name="mRID"]').setDisabled(false);
            this.down('combobox[name="serviceCategory"]').setDisabled(false);
            this.down('checkbox[name="isSdp"]').setDisabled(false);
            this.down('checkbox[name="isVirtual"]').setDisabled(false);
        }
        this.down('#cancelLink').href = returnLink;
    }
});
