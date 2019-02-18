/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.devicelifecycletransitions.DeviceLifecycleTransitions', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.property.view.property.devicelifecycletransitions.AddDeviceLifecycleTransitionView',
        'Uni.property.model.PropertyDeviceLifecycleTransition',
        'Uni.model.BreadcrumbItem',
        'Uni.grid.column.RemoveAction'
    ],

    possibleValues : [],
    currentPageView: null,
    addDeviceConfigurationsView: null,

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'fieldcontainer',
            itemId: 'add-device-lifecycle-transition-fieldcontainer',
            name: me.getName(),
            width: 800,
            readOnly: me.isReadOnly,
            layout: 'hbox',
            items: [
                {
                    xtype: 'displayfield',
                    itemId: 'add-device-lifecycle-transition-empty-text',
                    value: Uni.I18n.translate('devicelifecycletransitions.noAdded', 'UNI', 'There are no device lifecycle transition added yet to this')
                },
                {
                    xtype: 'grid',
                    itemId: 'add-device-lifecycle-transition-grid',
                    store: Ext.create('Ext.data.Store', {
                        model: 'Uni.property.model.PropertyDeviceLifecycleTransition'
                    }),
                    width: 500,
                    padding: 0,
                    maxHeight: 323,
                    columns: [
                        {
                            header: Uni.I18n.translate('devicelifecycletransitions.deviceType', 'UNI', 'Device Type'),
                            dataIndex: 'deviceTypeName',
                            flex: 1,
                            renderer: function (value) {
                                return value ? Ext.htmlEncode(value) : '';
                            }
                        },
                        {
                            header: Uni.I18n.translate('devicelifecycletransitions.deviceLifecycle', 'UNI', 'Device Lifecycle'),
                            dataIndex: 'deviceLifecycleName',
                            flex: 1
                        },
                        {
                            header: Uni.I18n.translate('devicelifecycletransitions.transition', 'UNI', 'Transition'),
                            dataIndex: 'stateTransitionName',
                            flex: 1
                        },
                        {
                            header: Uni.I18n.translate('devicelifecycletransitions.from', 'UNI', 'From'),
                            dataIndex: 'fromStateName',
                            flex: 1
                        },
                        {
                            header: Uni.I18n.translate('devicelifecycletransitions.to', 'UNI', 'To'),
                            dataIndex: 'toStateName',
                            flex: 1
                        },
                        {
                            xtype: 'uni-actioncolumn-remove',
                            handler: function (grid, rowIndex) {
                                grid.getStore().removeAt(rowIndex);
                                me.setValue();
                            }
                        }
                    ]
                },
                {
                    xtype: 'button',
                    itemId: 'add-device-lifecycle-transition-add-button',
                    text: Uni.I18n.translate('devicelifecycletransitions.addTransition', 'UNI', 'Add transition'),
                    action: 'addDeviceConfigurations',
                    margin: '0 0 0 10',
                    handler: Ext.bind(me.showAddView, me)
                }
            ],
            // add validation support
            isFormField: true,
            markInvalid: function (errors) {
                this.down('displayfield').markInvalid(errors);
            },
            clearInvalid: function () {
                this.down('displayfield').clearInvalid();
            },
            isValid: function () {
                return true;
            },
            getModelData: function () {
                return null;
            }
        }
    },

    getValue: function () {
        var me = this,
            result = me.recordsToIds(me.down('grid').getStore().getRange());

        return result.length ? result : null;
    },

    setValue: function (value) {
        var me = this,
            grid = me.down('#add-device-lifecycle-transition-grid'),
            emptyMessage = me.down('#add-device-lifecycle-transition-empty-text'),
            store = grid.getStore();

        Ext.suspendLayouts();
        if (Ext.isArray(value)) {
            store.loadData(me.idsToRecords(value), true);
        }

        if (store.getCount()) {
            grid.show();
            emptyMessage.hide();
        } else {
            grid.hide();
            emptyMessage.show();
        }
        Ext.resumeLayouts(true);
    },



    showAddView: function () {
        var me = this,
            store,
            selectionForm;

        me.decodePossibleValues();

        me.currentPageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0].down();
        me.addDeviceConfigurationsView = Ext.widget('uni-add-device-lifecycle-transition-view', {
            itemId: me.key + 'uni-add-device-lifecycle-transition-view',
        });

        me.loadDeviceTypes();
        me.addDeviceConfigurationsView.down('#addAction').on('click', me.hideAddView, me, {single: true});
        me.addDeviceConfigurationsView.down('#cancel').on('click', me.cancelAction, me, {single:true});

        //selectionGrid = me.addDeviceConfigurationsView.down('uni-add-device-configurations-grid');
        // store = selectionGrid.getStore();

        // selectionGrid.on('allitemsadd', me.hideAddView, me, {single: true});
        // selectionGrid.on('selecteditemsadd', me.hideAddView, me, {single: true});
        // selectionGrid.getCancelButton().on('click', me.hideAddView, me, {single: true});

        Ext.suspendLayouts();
        me.currentPageView.hide();
        Ext.ComponentQuery.query('viewport > #contentPanel')[0].add(me.addDeviceConfigurationsView);
        //store.loadData(me.getProperty().getPossibleValues() || []);
        // store.filterBy(function (record, id) {
        //     return !Ext.Array.contains(addedConfigurations, id);
        // });
        me.setPseudoNavigation(true);
        Ext.resumeLayouts(true);


        // store.fireEvent('load');
    },

    decodePossibleValues: function(){
        var me = this,
            propertyPossibleValues = me.getProperty().getPossibleValues();
        me.possibleValues = [];

        Ext.Array.each(propertyPossibleValues, function (propertyPossibleValue) {
            var item,
            jsonPossibleValue = Ext.decode(propertyPossibleValue.name);
            //jsonPossibleValue.stateTransitionName = 'STATE'; // remove it

            item = me.findInByProperty(me.possibleValues, jsonPossibleValue.deviceTypeName);
            item = me.findInByProperty(item.values, jsonPossibleValue.deviceLifeCycleName);
            item = me.findInByProperty(item.values, jsonPossibleValue.stateTransitionName);
            item = me.findInByProperty(item.values, jsonPossibleValue.fromStateName);
            item = me.findInByProperty(item.values, jsonPossibleValue.toStateName);
            item.values = propertyPossibleValue.id;
        });
    },

    findInByProperty: function(findIn, value){
        //var index =  findIn.findIndex(idx => idx.name == value);
          var index = -1;
          findIn.some(function(el, i) {
            if (el.name == value) {
                index = i;
               return true;
            }
        });
        if (index <=-1){
            var item = {
                name: value,
                values: []
            };
            findIn.push(item);
            return item;
        }
        return findIn[index];
    },


    loadDeviceTypes: function(){
        var me = this,
            deviceTypeCombo = me.addDeviceConfigurationsView.down('#deviceType');

        deviceTypeCombo.getStore().loadData(me.possibleValues);
    },

    cancelAction: function() {
        var me = this;
        Ext.suspendLayouts();
        me.setPseudoNavigation();
        me.addDeviceConfigurationsView.destroy();
        me.currentPageView.show();
        Ext.resumeLayouts(true);
    },


    hideAddView: function () {
        var me = this,
            view = me.addDeviceConfigurationsView,
            grid = me.down('#add-device-lifecycle-transition-grid'),
            store = grid.getStore(),
            emptyMessage = me.down('#add-device-lifecycle-transition-empty-text'),
            id = view.down('#toTransition').getStore().findRecord(view.down('#toTransition').valueField || view.down('#toTransition').displayField, view.down('#toTransition').getValue()).get('values');

        var valueToAdd = Ext.Array.findBy(me.getProperty().getPossibleValues(), function (value) {
            return value.id === id
        });

       // me.idsToRecords(id);

        var modelToAdd = Ext.create('Uni.property.model.PropertyDeviceLifecycleTransition');
        modelToAdd.set('deviceTypeName', view.down('#deviceType').getValue());
        modelToAdd.set('deviceLifecycleName', view.down('#deviceLifecycle').getValue());
        modelToAdd.set('stateTransitionName', view.down('#transition').getValue());
        modelToAdd.set('fromStateName', view.down('#fromTransition').getValue());
        modelToAdd.set('toStateName', view.down('#toTransition').getValue());
        modelToAdd.set('id', id);
        store.add(modelToAdd);

        Ext.suspendLayouts();
        me.setPseudoNavigation();
        me.addDeviceConfigurationsView.destroy();
        me.currentPageView.show();
        if (store.getCount()) {
            grid.show();
            emptyMessage.hide();
        } else {
            grid.hide();
            emptyMessage.show();
        }

        Ext.resumeLayouts(true);
        // return;
        //
        //
        // if (arguments[0] && Ext.isArray(arguments[0])) {
        //     me.setValue(me.recordsToIds(arguments[0]));
        // } else if (arguments[0] && arguments[0].single) {
        //     me.setValue(me.recordsToIds(me.addDeviceConfigurationsView.down('uni-add-device-lifecycle-transition-form').getStore().getRange()));
        // }
        // Ext.suspendLayouts();
        // me.setPseudoNavigation();
        // me.addDeviceConfigurationsView.destroy();
        // me.currentPageView.show();
        // Ext.resumeLayouts(true);
    },

    setPseudoNavigation: function (toAddDeviceConfigurations) {
        var me = this,
            breadcrumbTrail = Ext.ComponentQuery.query('breadcrumbTrail')[0],
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];

        if (toAddDeviceConfigurations) {
            lastBreadcrumbLink.renderData.href = window.location.href;
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
            breadcrumbTrail.addBreadcrumbItem(Ext.create('Uni.model.BreadcrumbItem', {
                text: Uni.I18n.translate('devicelifecycletransitions.addTransitions', 'UNI', 'Add transition'),
                relative: false
            }));
            lastBreadcrumbLink.getEl().on('click', me.hideAddView, me, {single: true});
        } else {
            lastBreadcrumbLink.destroy();
            breadcrumbTrail.query('breadcrumbSeparator:last')[0].destroy();
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];
            lastBreadcrumbLink.renderData.href = '';
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
        }
    },

    recordsToIds: function (data) {
        var modifiedData = [];

        Ext.Array.each(data, function (record) {
           // modifiedData.push(record.getId());
            modifiedData.push(record.getData().id)
        });

        return modifiedData;
    },

    idsToRecords: function (data) {
        var me = this,
            modifiedData = [],
            store = Ext.getStore('Uni.property.store.PropertyDeviceLifecycleTransition');

            store.loadData(me.getProperty().getPossibleValues());


           Ext.Array.each(data, function (id) {
            var record = store.getById(id);
            if(record) {
                var decodedValue = Ext.decode(record.get('name'));
                var model = Ext.create('Uni.property.model.PropertyDeviceLifecycleTransition', decodedValue);

                model.set('id', id);
                model.set('deviceLifecycleName', decodedValue.deviceLifeCycleName);
                model.set('deviceTypeName', decodedValue.deviceTypeName);
                model.set('fromStateName', decodedValue.fromStateName);
                model.set('stateTransitionName', decodedValue.stateTransitionName);
                model.set('toStateName', decodedValue.toStateName);
                modifiedData.push(model);
            }
        });
        return modifiedData;
    }
});