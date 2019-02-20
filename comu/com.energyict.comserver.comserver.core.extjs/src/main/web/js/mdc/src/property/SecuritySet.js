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


                    console.log("GET FIRST RECORD!!!!!!!!!!!!!!!!!");
                    //var record = me.securitySetsStore.first();

                    me.securityAccessorsStore.getProxy().url = '/api/ddr/devices/' + deviceNameToSet + '/securityaccessors/keys';
                    me.securityAccessorsStore.load(function () {
                        me.getGrid().getSelectionModel().selectAll();
                    /* RENDER ACCESSORS HER!!!!!!!!!*/
                    })
/*                     console.log("RECORD = ", record);

                     console.log("RECORD name = ", record.get('name'));
                     console.log("RECORD parent = ", record.get('parent'));

                     console.log("RECORD properties= ", record.properties());

                     var properties = record.properties();
                     var xPROPR;
                     properties.each(function (property) {
                        console.log("PROPERTY = ", property);

                     var type = property.getType();
                     fieldType = registry.getProperty(type);
                        console.log("fieldType=",fieldType);

                        var field = Ext.create(fieldType, Ext.apply(me.defaults, {
                                                                property: property,
                                                                isEdit: true,
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
                                                                propertyParams : null
                                                            }));
                                        //me.add(field);
                                        me.up('property-form').add(field)

                     });*/

                });

                /*var record = me.deliverablesStore.get(0);

                console.log("RECORD = ", record);*/
                /*var field = Ext.create(fieldType, Ext.apply(me.defaults, {
                                        property: property,
                                        isEdit: me.isEdit,
                                        isReadOnly: me.isReadOnly,
                                        inputType: me.inputType,
                                        passwordAsTextComponent: me.passwordAsTextComponent,
                                        userHasEditPrivilege: me.userHasEditPrivilege,
                                        userHasViewPrivilege: me.userHasViewPrivilege,
                                        showEditButton: me.isMultiEdit,
                                        resetButtonHidden: me.defaults.resetButtonHidden || me.isMultiEdit,
                                        editButtonTooltip: me.editButtonTooltip,
                                        removeButtonTooltip: me.removeButtonTooltip,
                                        blankText: me.blankText,
                                        propertyParams : property.getPropertyParams()
                                    }));*/
                //me.add(field);
                //me.up('property-form').add(field)


            }
        }
    },

    getEditCmp: function () {
        var me = this;

        console.log("getEditCmp!!!!!!!!!!");


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
                        /*refreshRadarChart(selected);
                        refreshEmployeeDetails(selected);*/
                            },
                         columns: [
                            {
                                //header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                                dataIndex: 'name',
                                flex: 1
                            }/*,
                            {
                                xtype: 'reading-type-column',
                                header: Uni.I18n.translate('general.readingType', 'IMT', 'Reading type'),
                                dataIndex: 'readingType',
                                flex: 1
                            }*/
                         ]
                        },
                        {
                            xtype: 'label',
                            margin: '20 0 0 0',
                            text: "SECURITY ACCESSORS",
                            itemId: 'secutiry-accessors-label-id'
                        },
                        {
                            xtype: 'property-form',
                            itemId: 'my-form-xromvyu',
                            ui: 'large',
                            width: '100%',
                            defaults: {
                                labelWidth: 250
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
            console.log("ACCESSOR ID =", accessorId);

            properties.each(function (property) {
                property.set('name', nameOfAccessor);
                var propertyKey = property.get('key');
                console.log("PROPERTY KEY!!!=",propertyKey);
                if (propertyKey !== "label"){

                    console.log("NOT LABEL!!!!");
                    var keyToset = nameOfAccessor;//nameOfAccessor + propertyKey; TO DO move it to place where load if finished !!!!!!!!!
                    property.set('key', keyToset);//to get unigue key compose it from nameOfAccessor and property Key

                    var type = property.getType();
                    fieldType = registry.getProperty(type);


                    console.log("PROPERTY =",property);
                    console.log("KEY TO FINE =",property.get('key'));
                    console.log("TRY TO FIND FIELD = ",me.up('property-form').down(property.get('key')));

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
                                            propertyParams : null,
                                            listeners: {
                                                change: {
                                                    fn: function (newValue) {
                                                        console.log("CHANGE FOR FIELD !!! mewValue=",mewValue);
                                                    }
                                                }
                                            }
                                        }));
                        //me.up('property-form').add(field);
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




                         //console.log("REMOVE FROM FORM AuthenticationKeykey!!!!!!!!!!!!");
                         //me.up('property-form').remove('AuthenticationKeykey');
                         //me.up('property-form').remove('AuthenticationKeykey');

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
        console.log("accessorIdArray = ",accessorIdArray);
        console.log("RENDER ACCESSORS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
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
        //console.log("GET VALUE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! for key =",tmpKey);
        //console.log("GET VALUE for tmpKey=",tmpKey);
        //var field = me.up('property-form');//.down(tmpKey);
        //console.log("field = ",field);
        //var value = this.down('txt-name').getValue();
        //console.log('Obtained value = ',value);
       //console.log("field value = ",field.getValue());
       //var raw = me.up('property-form').getFieldValues();
       var selectedSets = [];
        _.map(me.getGrid().getSelectionModel().getSelection(), function (record) {
                    console.log("SELECTED RECORDS = ",record.get('name'));
                    var tmpSetName = record.get('name');
                    tmpSetName = tmpSetName.replace(",","[,]");
                    tmpSetName = tmpSetName.replace(";","[;]");
                    selectedSets.push(tmpSetName);
                    return record;//.get('readingType').mRID;
                })

        for(var setId =0; setId < selectedSets.length; setId++){
            resultSets = resultSets + selectedSets[setId]; + ",,"
            if (selectedSets.length - setId > 1 ){
                resultSets = resultSets + ",,";
            }
        }


        var raw = me.getPropForm().getFieldValues();
        console.log("RAW VALUES = ",raw);

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

        result = resultSets + ";;" + resultAccessors;

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
    }
});