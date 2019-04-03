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
        labelWidth: 150,
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

    initComponent: function () {

        var me = this;

        me.securitySetsStore = Ext.create('Ext.data.Store', {
                    model: 'Mdc.model.DeviceSecuritySetting',
                    proxy: {
                        type: 'rest',
                        urlTpl: '/api/ddr/devices/{deviceNameToSet}/securityproperties/hsm',
                        reader: {
                            type: 'json',
                            root: 'securityPropertySets'
                        },
            setUrl: function (deviceNameToSet) {
                this.url = this.urlTpl.replace('{deviceNameToSet}', deviceNameToSet);
                        }
                    }
                });

                me.securityAccessorsStore = Ext.create('Ext.data.Store', {
            model: 'Mdc.securityaccessors.model.DeviceSecurityKey',
            proxy: {
                type: 'rest',
                urlTpl: '/api/ddr/devices/{deviceNameToSet}/securityaccessors/keys',
                reader: {
                    type: 'json',
                    root: 'keys'
                },
                setUrl: function (deviceNameToSet) {
                    this.url = this.urlTpl.replace('{deviceNameToSet}', deviceNameToSet);
                }
            }
        });

        me.resetButtonHidden = true;
        me.name =  me.getName();


        /* initComponent method in base class will call getEditCmp method so we should define
        needed stores before callParent call */
        me.callParent();

        var deviceNameToSet = me.parentForm.context.deviceName;

        me.securitySetsStore.getProxy().setUrl(deviceNameToSet);

        me.securitySetsStore.load(function () {
            if (me.securitySetsStore.getCount() == 0){
                me.down('#sets-container').hide();
                me.down('#accessors-container').hide();
                me.down('#no-security-sets').show();
            }else{
                me.down('#sets-container').show();
                me.down('#accessors-container').show();
                me.down('#no-security-sets').hide();

                me.securityAccessorsStore.getProxy().setUrl(deviceNameToSet);
                me.securityAccessorsStore.load(function () {
                        me.getGrid().getSelectionModel().selectAll();
                    })
            }
        });

    },

    getEditCmp: function () {
        var me = this;

        me.layout = 'vbox';

        return [
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'sets-container',
                        width: '100%',
                        hidden: true,
                        labelWidth: 150,
                        fieldLabel: Uni.I18n.translate('securityacessrors.securitySets', 'MDC', "Security sets"),
                        required: true,
                        layout: 'column',
                        items: [
                            {
                                xtype: 'grid',
                                itemId: 'metrology-configuration-outputs-grid',
                                hideHeaders: true,
                                store: me.securitySetsStore,
                                width: 400,
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
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('securityacessrors.securityAccessors', 'MDC', "Security accessors"),
                        itemId: 'accessors-container',
                        width: '100%',
                        layout: 'column',
                        requred: true,
                        hidden: true,
                        labelWidth: 150,
                        items: [
                            {
                                xtype: 'property-form',
                                itemId: 'acessors-property-form',
                                margin: '10 0 0 0',
                                width: 600
                            }
                        ]

                    },
                    {
                        xtype: 'component',
                        html: Uni.I18n.translate('securityacessrors.noSecuritySets', 'MDC', 'There are no security sets on the device'),
                        itemId: 'no-security-sets',
                        hidden: true,
                        style: {
                            'color': '#FF0000',
                            'margin': '6px 10px 6px 0px'
                        }
                    }
        ];
    },

    renderAccessors: function(selectedAccessors){

        var me = this;
        var registry = Uni.property.controller.Registry;
        var formForAccessors = me.getPropForm();

        Ext.suspendLayouts();

        var arrayToRender = [];
        renderedKeys = [];

        arrayToRender = selectedAccessors;

        var propertiesToRender = [];

        /* Save filed values for acesssors before we redraw them */
        var fieldValues = formForAccessors.getFieldValues();
        formForAccessors.removeAll();

        me.securityAccessorsStore.each(function (accessor) {
            var nameOfAccessor = accessor.get('name');
            var properties = accessor.currentProperties();
            var accessorId = accessor.get('id');
            var defaultServiceKeyValue = accessor.get('defaultServiceKey');


            properties.each(function (property) {
                property.set('name', nameOfAccessor);
                var propertyKey = property.get('key');

                if (propertyKey !== "label"){
                    var keyToset = nameOfAccessor;
                    var savedValue = fieldValues.properties[nameOfAccessor]
                    property.set('key', keyToset);

                    /* If it firs load of this form fill all accessors fields with predefined default values.
                    It is done according to design requirement. If we changed selected sets fields will be filled with
                    saved values */

                    if (savedValue == undefined)
                    {
                        property.data.value = defaultServiceKeyValue;
                    } else {
                        property.data.value = savedValue;
                    }
                    /*Set value to which key will be set when restore to default button is pressed */
                    property.data['default'] = defaultServiceKeyValue;




                    var type = property.getType();
                    fieldType = registry.getProperty(type);

                    tmpKey = property.get('key');

                    if (arrayToRender.indexOf(accessorId) >=0 )
                    {
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
                        formForAccessors.add(field);
                        renderedKeys.push(keyToset);
                    } else {
                    /* Accessor is not in array so delete in from the form */
                    }
                }
            });
        })
        Ext.resumeLayouts(true);
    },

    selectionChanged: function() {
        var me = this;
        var selections = me.getGrid().getSelectionModel().getSelection();
        var accessorIdArray = [];

        if (selections.length == 0)
        {
            me.down('#accessors-container').hide();

        }else{
            me.down('#accessors-container').show();
            selections.forEach(function (selection) {
                    console.log("SLECTION!!!=",selection.properties);
                    selection.properties().each(function (property) {
                        console.log("PROPERTY=",property);
                        var idToAdd = property.raw.propertyValueInfo.value.id;
                        if(!(accessorIdArray.indexOf(idToAdd)>=0)){
                        /* Id of secutrityAccessor is not in array. Add it to array */
                            accessorIdArray.push(idToAdd);
                        }
                    })
                })

            me.renderAccessors(accessorIdArray);
        }
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
                    var tmpSetName = record.get('name');
                    tmpSetName = tmpSetName.replace(",","[,]");
                    selectedSets.push(tmpSetName);
                    return record;
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
            var field = me.getPropForm().getComponent(renderedKeys[index]);
            if (field !== undefined) {
                var tmpvalue = field.getValue(raw);
                if (tmpvalue !== ""){
                    var tmpKey = renderedKeys[index];
                    tmpKey = tmpKey.replace(",","[,]");
                    tmpKey = tmpKey.replace(";","[;]");
                    tmpvalue = tmpvalue.replace(",","[,]");
                    tmpvalue = tmpvalue.replace(";","[;]");
                    tmpvalue = tmpvalue.replace(":","[:]");

                    resultAccessors = resultAccessors + tmpKey + "::" + tmpvalue;
                    if (renderedKeys.length - index > 1 ){
                        resultAccessors = resultAccessors + ",,";
                    }
                }
            }
        }
        /* Format of value is <list of sets> ;; <list of accessors> */
        result = resultSets + ";;" + resultAccessors;

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