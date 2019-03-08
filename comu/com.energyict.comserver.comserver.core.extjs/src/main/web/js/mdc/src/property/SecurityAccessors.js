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

    //margin: '0 0 0 0',

    msgTarget: 'under',
    width: 600,

    tmpKey: '',

    PREP: 'prep',
    GEN: 'gen',

    renderedFieldKeys: [],

    listeners: {
        afterrender: {
            fn: function () {
                var me = this;

                var propertyTypeInfo1 = Ext.create('Uni.property.model.PropertyType',{
                raw: []
                });
                var tmpArray = [];
                var preparedKeys = [];

                preparedKeys = me.property.data.value.split(",,");

                console.log("PREPARED KEYS = ",preparedKeys);

                for (var index = 0; index < preparedKeys.length; index++){
                    var nameAndValue = [];
                    nameAndValue = preparedKeys[index].split(":");
                    var itemIdForPreparedKey = nameAndValue[0] + 'prep';
                    var itemIdForGeneratedKey = nameAndValue[0] + 'gen';

                    if (me.getPropForm()){
                        me.getPropForm().add({
                                            xtype: 'textareafield',
                                            fieldLabel: nameAndValue[0] + "(prepared key)",
                                            labelWidth: 250,
                                            readOnly: true,
                                            width: 1000,
                                            height: 100,
                                            value: nameAndValue[1],
                                            //allowBlank: false,
                                            itemId: itemIdForPreparedKey
                                        },
                                        {
                                            xtype: 'textareafield',
                                            fieldLabel: nameAndValue[0] + "(generated signature)",
                                            width: 1000,
                                            height: 100,
                                            value: nameAndValue[2],
                                            required: true,
                                            allowBlank: false,
                                            itemId: itemIdForGeneratedKey
                                        }
                                      );
                    }

                    tmpArray.push(nameAndValue[0]);
                }

                renderedFieldKeys = tmpArray;
            }
        }
    },

    getEditCmp: function () {
        var me = this;

        console.log("getEditCmp!!!!!!!!!!");

        console.log("PROPERTY =",me.property.data.value);

        me.securitySetsStore = Ext.create('Ext.data.Store', {
            //fields: ['name'/*, 'readingType'*/],

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
                    //fields: ['name'/*, 'readingType'*/],

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

        return [
                    {
                        //xtype: 'property-form',
                        xtype: 'form',
                        itemId: 'my-form-xromvyu',
                        ui: 'large',
                        width: '100%',
                        defaults: {
                            labelWidth: 250
                        }
                    }

        ];
    },

    setReadOnly: function(readOnly){
        console.log("TRY TO SET READ ONLY");
    },

    getGrid: function () {
        return this.down('grid');
    },

    getPropForm: function() {
        //return this.down('property-form');
        return this.down('form');
    },

    setValue: function (value) {
    },

    getValue: function () {
        var me = this;

        var result = "";

        //var raw = me.getPropForm().getFieldValues();
        //onsole.log("IS FORM VALID = ",me.getPropForm().isValid());
        //var raw = me.up('property-form').getValues();
        console.log("renderedFieldKeys=",renderedFieldKeys );
        if (me.getPropForm()){
            var raw = me.getPropForm().getValues();
            for (var index = 0; index < renderedFieldKeys.length; index++){

                var preparedKey = renderedFieldKeys[index] + 'prep';
                var preparedField = me.getPropForm().getComponent(preparedKey);
                if (preparedField !== undefined) {
                    var tmpPreparedValue = preparedField.getValue(raw);
                    console.log("Obtained prepared value=", tmpPreparedValue);
                    if (tmpPreparedValue  !== ""){ //TO DO add check for multiple spaces

                        tmpPreparedValue = tmpPreparedValue.replace(",","[,]");
                        tmpPreparedValue = tmpPreparedValue.replace(";","[;]");

                        result = result + renderedFieldKeys[index] + ":" + tmpPreparedValue;

                    }
                }
                console.log("result = ",result);

                var generatedKey = renderedFieldKeys[index] + 'gen';
                var generatedField = me.getPropForm().getComponent(generatedKey);
                    if (generatedField !== undefined) {
                        var tmpGeneratedValue = generatedField.getValue(raw);
                        console.log("Obtained generated value=", tmpGeneratedValue);
                        if (tmpGeneratedValue !== ""){ //TO DO add check for multiple spaces
                            tmpGeneratedValue = tmpGeneratedValue.replace(",","[,]");
                            tmpGeneratedValuee = tmpGeneratedValue.replace(";","[;]");

                            result = result +  ":" + tmpGeneratedValue;
                    }
                }


                if (renderedFieldKeys.length - index > 1 ){
                    result = result + ",,";
                }

            }
        }

        console.log("RAW VALUES = ",raw);



        console.log("RESULT ======",result);
        return result;
        /*return _.map(me.getGrid().getSelectionModel().getSelection(), function (record) {
            return record.get('readingType').mRID;
        }).join(';');*/
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