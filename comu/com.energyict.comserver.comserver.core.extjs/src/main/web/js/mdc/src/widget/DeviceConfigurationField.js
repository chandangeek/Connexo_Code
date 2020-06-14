/**
 * FieldContainer for editing a device configuration (on device f.i)
 **/
Ext.define('Mdc.widget.DeviceConfigurationField', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceConfigurationField',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    deviceTypeStore: undefined,
    queryMode: 'local',
    fieldLabel: Uni.I18n.translate('general.deviceTypeAndConfiguration', 'MDC', 'Device type and configuration'),
    //labelAlign: 'right',
    required: true,
    allowBlank: true,
    extraConfigListener: null,

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'combobox',
                msgTarget: 'under',
                name: 'deviceType',
                itemId: 'deviceType',
                allowBlank: me.allowBlank,
                queryMode: me.queryMode,
                typeAhead: true,
                autoSelect: true,
                emptyText: Uni.I18n.translate('deviceAdd.type.value', 'MDC', 'Select a device type...'),
                displayField: 'name',
                valueField: 'id',
                store: me.deviceTypeStore,
                validateOnBlur: false,
                validateOnChange: false,
                listConfig: {
                    loadMask: true,
                    maxHeight: 300
                },
                listeners: {
                    select: function (field, value) {
                        var configCombo = field.nextSibling(),
                            valueId;

                        if (Ext.isArray(value)) {
                            valueId = value[0].data.id;
                        } else {
                            valueId = value.data.id;
                        }
                        configCombo.getStore().getProxy().setExtraParam('deviceType', valueId);
                        configCombo.getStore().on('load', function(store) {
                            if (store.getTotalCount()===1) {
                                configCombo.select(store.getAt(0));
                                configCombo.fireEvent('select', configCombo, [store.getAt(0)]);
                            }
                        }, me, {single:true});
                        if (configCombo.isDisabled()) {
                            configCombo.getStore().reload();
                            configCombo.enable();
                        } else {
                            configCombo.reset();
                            configCombo.getStore().reload();
                        }

                        field.up('form').getRecord().set('deviceTypeId', valueId);
                    },
                    change: function (field, value) {
                        var configCombo = field.nextSibling();
                        if (value == '') {
                            configCombo.reset();
                            configCombo.disable();
                        }
                    },
                    blur: function (field) {
                        if (!field.getValue()) {
                            field.nextSibling().reset();
                            field.nextSibling().disable();
                        } else {
                            Ext.Array.each(field.getStore().getRange(), function (item) {
                                if (field.getValue() == item.get('name')) {
                                    field.fireEvent('select', field, item);
                                }
                            });
                        }
                    }
                }
            },
            {
                xtype: 'combobox',
                msgTarget: 'under',
                name: 'deviceConfiguration',
                itemId: 'deviceConfiguration',
                allowBlank: me.allowBlank,
                autoSelect: true,
                enabled: false,
                //fieldLabel: 'Device configuration25',
                //labelAlign: 'left',
                //labelAlign: 'right',
               // fieldLabel: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                emptyText: Uni.I18n.translate('deviceAdd.config.value', 'MDC', 'Select a device configuration...'),
                afterSubTpl: '<div style="color: #686868; margin-top: 6px"><i>'
                + Uni.I18n.translate('deviceAdd.firstSelectDeviceType', 'MDC', 'First select a device type.')
                + '</i></div>',
                displayField: 'name',
                valueField: 'id',
                disabled: true,
                store: 'AvailableDeviceConfigurations',
                validateOnBlur: false,
                validateOnChange: false,
                listConfig: {
                    loadMask: true,
                    maxHeight: 300
                },
                listeners: {
                    select: function (field, value) {
                        field.up('form').getRecord().set('deviceConfigurationId', value[0].data.id);
                    }
                }
            }
        ];

        me.callParent(arguments);
        if (me.extraConfigListener && Ext.isArray(me.extraConfigListener)){
             Ext.Array.each(me.extraConfigListener, function(listener) {
                 me.down('#deviceConfiguration').addListener("select", listener.listener, listener.scope);
             })
        }
    },

    getDeviceTypeStore: function(){
        return this.down('#deviceType').getStore();
    },

    getDeviceType: function(){
        var deviceTypeCombo =  this.down('#deviceType');
        return deviceTypeCombo.findRecordByValue(deviceTypeCombo.getValue());
    },

    getDeviceConfiguration: function(){
        var deviceConfigCombo =  this.down('#deviceConfiguration');
        return deviceConfigCombo.findRecordByValue(deviceConfigCombo.getValue());
    },

    validate: function(){
        Ext.Array.forEach(this.items.items, function(item){
            if (!item.validate()){
                return false;
            }
        });
        return true;
    },

    clearInvalid: function(){
        Ext.Array.forEach(this.items.items, function(item){
            item.clearInvalid();
        });
    }

});
