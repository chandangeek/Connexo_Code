/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.property.SecuritySet', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.grid.column.ReadingType',
         'Mdc.model.DeviceSecuritySetting'
    ],

    defaults: {
        labelWidth: 250,
        resetButtonHidden: true
    },

    mixins: {
        field: 'Ext.form.field.Field'
    },

    msgTarget: 'under',
    width: 600,
    hideLabel: true,


    tmpKey: '',

    renderedKeys: [],

    listeners: {
        afterrender: {
            fn: function () {
                var me = this;

                console.log("LOAD DELIVERABLES!!!!");
                console.log("deviceName from context = ",me.up('property-form').context.deviceName);
                var deviceNameToSet = me.up('property-form').context.deviceName;
                me.securitySetsStore.getProxy().url = '/api/ddr/devices/' + deviceNameToSet + '/securityproperties/hsm';

                me.securitySetsStore.load(function () {

                    me.securityAccessorsStore.getProxy().url = '/api/ddr/devices/' + deviceNameToSet + '/securityaccessors/keys';
                    me.securityAccessorsStore.load(function () {
                        me.getGrid().getSelectionModel().selectAll();
                    })
                });
            }
        }
    },

    getEditCmp: function () {
        var me = this;

        console.log("getEditCmp!!!!!!!!!!");

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

        return [

                    {
                        xtype: 'form',
                        fieldLable: "LABLE FOR CONTAINER!!!",
                        itemId: 'sct-bottom-container',
                        width: '100%',
                        layout: 'column',
                        items: [
                            {
                                xtype: 'label',
                                //required: true,
                                align: 'right',
                                width: '160',
                                minWidth: '160',
                                //margin: '0 0 0 0',
                                text: "Securiy sets",
                                itemId: 'secutiry-sets-label-id'
                            },
                            {
                                xtype: 'grid',
                                itemId: 'metrology-configuration-outputs-grid',
                                hideHeaders: true,
                                store: me.securitySetsStore,
                                width: 600,
                                margin: 0,
                                padding: 0,
                                scroll: false,
                                align: 'stretch',
                                resetButtonHidden: true,
                                selModel: Ext.create('Ext.selection.CheckboxModel', {
                                    mode: 'MULTI',
                                    checkOnly: true,
                                    showHeaderCheckbox: false,
                                    pruneRemoved: false,
                                    updateHeaderState: Ext.emptyFn
                                }),
                                listeners:{
                                    selectionchange:function(selModel, selected) {
                                        console.log("SELECTION IS CHANGED !!!!!!!!!!!!!");
                                        me.selectionChanged();
                                    }
                                },
                                columns: [
                                {

                                    dataIndex: 'name',
                                    flex: 1
                                }
                                ],

                                dockedItems: [
                                    {
                                        xtype: 'label',
                                        dock: 'bottom',
                                        itemId: 'version-date-field-error-container',
                                        padding: '0 0 20 0',
                                        hidden: true,
                                        cls: 'x-form-invalid-under'
                                    }
                                ],
                            },

                        ]
                    },
                        {
                            xtype: 'label',
                            required: true,
                            margin: '20 0 0 0',
                            text: "Security accessors",
                            itemId: 'secutiry-accessors-label-id'
                        },
                        {
                            xtype: 'property-form',
                            itemId: 'my-form-xromvyu',
                            //floating: true,
                            //ui: 'large',
                            width: '100%',
                            defaults: {
                                labelWidth: 50
                            },
                            listeners:{
                                'afterrender': function(panel){
                                    console.log("SET POSITION!!!!!!!!!!!!!!!!",panel);
                                    panel.setPosition(-100,true);
                                    console.log("AFTER SET POSITION!!!!!!!!!!!!!!!!",panel);
                                }
                            }
                        }

        ];
    },

    renderAccessors: function(array){

        var me = this;
        var registry = Uni.property.controller.Registry;

        Ext.suspendLayouts();

        var arrayToRender = [];
        renderedKeys = [];

        arrayToRender = array;

        var propertiesToRender = [];
        console.log("CLEAR FORM");
        me.getPropForm().removeAll();

       me.securityAccessorsStore.each(function (accessor) {
            var nameOfAccessor = accessor.get('name');
            var properties = accessor.currentProperties();
            var accessorId = accessor.get('id');
            var defaultServiceKeyValue = accessor.get('defaultServiceKey');
            console.log("ACCESSOR=",accessor );
            console.log("ACCESSOR ID =", accessorId);
            console.log("defaultServiceKeyValue  =", defaultServiceKeyValue );

            properties.each(function (property) {
                property.set('name', nameOfAccessor);
                var propertyKey = property.get('key');
                console.log("PROPERTY KEY!!!=",propertyKey);
                if (propertyKey !== "label"){

                    console.log("NOT LABEL!!!!");
                    var keyToset = nameOfAccessor;//nameOfAccessor + propertyKey; TO DO move it to place where load if finished !!!!!!!!!
                    property.set('key', keyToset);//to get unigue key compose it from nameOfAccessor and property Key
                    console.log("SET DEFAULT VALUIE = ",defaultServiceKeyValue);
                    property.data.value = defaultServiceKeyValue;
                    property.data.default = defaultServiceKeyValue;

                    var type = property.getType();
                    fieldType = registry.getProperty(type);


                    console.log("PROPERTY =",property);
                    /*console.log("PROPERTY PARAMS = ",property.getPropertyParams());
                    console.log("KEY TO FINE =",property.get('key'));
                    console.log("TRY TO FIND FIELD = ",me.up('property-form').down(property.get('key')));*/

                    tmpKey = property.get('key');

                    if (arrayToRender.indexOf(accessorId) >=0 )
                    {
                        console.log("FIELD TYPE =",fieldType);
                        /* If accessor in array to render then add it to form */
                        var field = Ext.create(fieldType, Ext.apply(me.defaults, {
                                            property: property,
                                            isEdit: true,
                                            //itemId: property.get('key'),
                                            isReadOnly: false,
                                            inputType: "text",
                                            passwordAsTextComponent: false,
                                            userHasEditPrivilege: true,
                                            userHasViewPrivilege: true,
                                            showEditButton: false,
                                            resetButtonHidden: false,
                                            editButtonTooltip: "Edit",
                                            removeButtonTooltip: "Remove",
                                            blankText: "This field is required",
                                            propertyParams : property.getPropertyParams()
                                        }));
                        me.getPropForm().add(field);
                        renderedKeys.push(keyToset);
                    } else {
                    /* Accessor is not in array so delete in from the form */
                        console.log("REMOVE PROPERTY = ",property.get('key'));
                        console.log("PRINT THE FORM = ", me.up('property-form'))
                        //me.up('property-form').remove(property.get('key'));
                    }
                }
            });
        })
        Ext.resumeLayouts(true);
    },

    selectionChanged: function() {
        var me = this;
        var selections = me.getGrid().getSelectionModel().getSelection();
        console.log("selections =",selections);
        var accessorIdArray = [];
        selections.forEach(function (selection) {

            selection.properties().each(function (property) {
                console.log("PROPERTY  = ", property);
                console.log("PRINT ID OF THE PROPERTY = ",property.raw.propertyValueInfo.value.id);
                var idToAdd = property.raw.propertyValueInfo.value.id;
                console.log("idToAdd = ",idToAdd);
                console.log("accessorIdArray.indexOf(idToAdd)=",accessorIdArray.indexOf(idToAdd));
                if(!(accessorIdArray.indexOf(idToAdd)>=0)){
                /* Id of secutrityAccessor is not in array. Add it to array */
                    accessorIdArray.push(idToAdd);
                }
            })
        })

        me.renderAccessors(accessorIdArray);


    },

    getGrid: function () {
        return this.down('grid');
    },

    getPropForm: function() {
        return this.down('property-form');
    },

    setValue: function (value) {
    },

    getValue: function () {
        var me = this;

        var result = "";
        var resultSets = "";
        var resultAccessors = "";

       var selectedSets = [];
        _.map(me.getGrid().getSelectionModel().getSelection(), function (record) {
                    console.log("SELECTED RECORDS = ",record.get('name'));
                    var tmpSetName = record.get('name');
                    tmpSetName = tmpSetName.replace(",","[,]");
                    tmpSetName = tmpSetName.replace(";","[;]");
                    selectedSets.push(tmpSetName);
                    return record;//.get('readingType').mRID;
                })

        if (selectedSets.length == 0){
            /* requred field is not selected. just send empty result */
            return ;
        }

        for(var setId =0; setId < selectedSets.length; setId++){
            resultSets = resultSets + selectedSets[setId]; + ",,"
            if (selectedSets.length - setId > 1 ){
                resultSets = resultSets + ",,";
            }
        }


        var raw = me.getPropForm().getFieldValues();

        for( var index = 0 ; index < renderedKeys.length ; index++){
            console.log("index =",index);
            console.log("keys[i] =",renderedKeys[index]);
            var field = me.getPropForm().getComponent(renderedKeys[index]);
            if (field !== undefined) {
                var tmpvalue = field.getValue(raw);
                console.log("Obtained value=", tmpvalue);
                if (tmpvalue !== ""){ //TO DO add check for multiple spaces
                    var tmpKey = renderedKeys[index];
                    tmpKey = tmpKey.replace(",","[,]");
                    tmpKey = tmpKey.replace(";","[;]");
                    tmpvalue = tmpvalue.replace(",","[,]");
                    tmpvalue = tmpvalue.replace(";","[;]");

                    resultAccessors = resultAccessors + tmpKey + ":" + tmpvalue;
                    if (renderedKeys.length - index > 1 ){
                        resultAccessors = resultAccessors + ",,";
                    }
                }

            }

        }

        /* Format of value is <list of sets> ;; <list of accessors> */
        result = resultSets + ";;" + resultAccessors;

        console.log("RESULT =",result);
        return result;
    },

    getRawValue: function () {
        var me = this;

        return me.getValue().toString();
    },

    getValueAsDisplayString: function (value) {
        var me = this;

        return '-';
    },

    setLocalizedName: function(name) {
            fieldLabel = "";
    },

    isValid: function(){
        return false;
    },

    /* For validation */
    getErrorContainer: function () {
        return this.down('#version-date-field-error-container')
    },

    markInvalid: function (msg) {
        Ext.suspendLayouts();
        this.getErrorContainer().setText(msg);
        this.getErrorContainer().show();
        Ext.resumeLayouts(true);
    },

    clearInvalid: function () {
        this.getErrorContainer().hide();
    }
});