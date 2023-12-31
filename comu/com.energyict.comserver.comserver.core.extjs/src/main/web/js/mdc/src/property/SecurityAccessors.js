/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.property.SecurityAccessors', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.grid.column.ReadingType',
         'Mdc.model.DeviceSecuritySetting'
    ],

    defaults: {
        labelWidth: 250,
        resetButtonHidden: true
    },

    labelWidth: 25,
    hideLabel: true,

    mixins: {
        field: 'Ext.form.field.Field'
    },

    msgTarget: 'under',
    width: 600,

    tmpKey: '',

    isEditParameters: true,
    renderedFieldKeys: [],


    initComponent: function () {

        var me = this;
        me.securitySetsStore = Ext.create('Ext.data.Store', {
            model: 'Mdc.model.DeviceSecuritySetting',
            proxy: {
                type: 'rest',
                reader: {
                    type: 'json',
                    root: 'securityPropertySets'
                }
            }
        });

        me.securityAccessorsStore = Ext.create('Ext.data.Store', {
                    model: 'Mdc.securityaccessors.model.DeviceSecurityKey',
                    proxy: {
                        type: 'rest',
                        reader: {
                            type: 'json',
                            root: 'keys'
                        }
                    }
                });



        me.name =  me.getName();

        me.layout = 'vbox';
        me.resetButtonHidden = true;

        /* getEditCmp or getDisplayCmp will be called here in parent initComponent */
        me.callParent();

        var tmpArray = [];
        var preparedKeys = [];

        preparedKeys = me.property.data.value.split(",,");

        for (var index = 0; index < preparedKeys.length; index++){
            var nameAndValue = [];
            nameAndValue = preparedKeys[index].split(":");
            var itemIdForPreparedKey = nameAndValue[0] + 'prep';
            var itemIdForGeneratedKey = nameAndValue[0] + 'gen';

            if (me.getPropForm()){
                if(me.isEditParameters){
                    me.getPropForm().add({
                            xtype: 'textareafield',
                            fieldLabel: nameAndValue[0] + " (prepared key)",
                            labelWidth: 250,
                            readOnly: true,
                            width: 1000,
                            height: 100,
                            value: nameAndValue[1],
                            itemId: itemIdForPreparedKey
                        },
                        {
                            xtype: 'textareafield',
                            fieldLabel: nameAndValue[0] + " (generated signature)",
                            width: 1000,
                            height: 100,
                            margin: '0 0 20 0',
                            value: nameAndValue[2],
                            required: true,
                            allowBlank: false,
                            itemId: itemIdForGeneratedKey
                        }
                    );
                }else{
                    me.getPropForm().add(
                        {
                            xtype: 'displayfield',
                            fieldLabel: nameAndValue[0] + " (prepared key)",
                            readOnly: true,
                            value: nameAndValue[1],
                            itemId: itemIdForPreparedKey,
                            renderer: function(value){
                                var numberOfInterations = Math.floor(value.length/120);
                                var initialLength = value.length
                                var resultValue = value;
                                for (var i = 0; i < numberOfInterations; i++)
                                {
                                    resultValue = resultValue.substr(0, 120*(i+1)+i*8) + '<br>' + resultValue.substr(120*(i+1)+i*8);
                                }
                                return Ext.isEmpty(resultValue) ? '-' : resultValue;
                            }
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: nameAndValue[0] + " (generated signature)",
                            readOnly: true,
                            value: nameAndValue[2],
                            itemId: itemIdForGeneratedKey,
                            renderer: function(value){
                                var numberOfInterations = Math.floor(value.length/100);
                                var initialLength = value.length
                                var resultValue = value;
                                for (var i = 0; i < numberOfInterations; i++)
                                {
                                    resultValue = resultValue.substr(0, 120*(i+1)+i*8) + '<br>' + resultValue.substr(120*(i+1)+i*8);
                                }
                                return Ext.isEmpty(resultValue) ? '-' : resultValue;
                            }
                        }
                    );
                }

            }

            tmpArray.push(nameAndValue[0]);
        }

        renderedFieldKeys = tmpArray;
    },

    getEditCmp: function () {
        return [
                    {
                        xtype: 'form',
                        itemId: 'accessors-property-form',
                        ui: 'large',
                        width: '100%',
                        defaults: {
                            labelWidth: 250
                        }
                    }

        ];
    },

    getDisplayCmp: function(){
        this.isEditParameters = false;
        return this.getEditCmp();
    },

    getGrid: function () {
        return this.down('grid');
    },

    getPropForm: function() {
        return this.down('form');
    },

    setValue: function (value) {
    /* It should override methid from base class in this case.
       Do nothing. */
    },

    getValue: function () {
        var me = this;

        var result = "";

        if (me.getPropForm()){
            var raw = me.getPropForm().getValues();
            for (var index = 0; index < renderedFieldKeys.length; index++){

                var preparedKey = renderedFieldKeys[index] + 'prep';
                var preparedResult = me.prepareValue(renderedFieldKeys[index], preparedKey, raw);
                if(preparedResult != null){
                    result = result + preparedResult
                }

                var generatedKey = renderedFieldKeys[index] + 'gen';
                var generatedResult = me.prepareValue("", generatedKey, raw);
                if(generatedResult != null){
                    result = result + generatedResult
                }

                if (renderedFieldKeys.length - index > 1 ){
                    result = result + ",,";
                }

            }
        }
        return result;
    },

    prepareValue: function(fieldNameToAdd, fieldKey, raw){
        var me = this;
        var field = me.getPropForm().getComponent(fieldKey);
        if (field !== undefined) {
            var tmpValue = field.getValue(raw);
            if (tmpValue !== ""){

                tmpValue = tmpValue.replace(",","[,]");
                tmpValue = tmpValue.replace(";","[;]");

                //result = result + renderedFieldKeys[index] + ":" + me.prepareValue(tmpPreparedValue);
                return fieldNameToAdd + ":" + tmpValue;
            }
        }
        return null;
    },

    getRawValue: function () {
        var me = this;

        return me.getValue().toString();
    },

    markInvalid: function (error) {
        var me = this;

        me.toggleInvalid(error);
    },

    clearInvalid: function () {
        var me = this;

        me.toggleInvalid();
    },

    toggleInvalid: function (error) {
        var me = this,
            oldError = me.getActiveError(),
            grid = me.getGrid();

        Ext.suspendLayouts();
        if (error) {
            me.setActiveErrors(error);
        } else {
            me.unsetActiveError();
        }
        if (oldError !== me.getActiveError()) {
            me.doComponentLayout();
        }
        Ext.resumeLayouts(true);
    },

    getValueAsDisplayString: function (value) {
        var me = this;

        return '-';
    },

    setLocalizedName: function(name) {
             fieldLabel = "";
    }

});