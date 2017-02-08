/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.SelectionGrid', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.view.grid.SelectionGrid',
        'Uni.util.FormErrorMessage'
    ],

    ignoreFields: ['id', 'lifeCycleId'],
    availableFields: [
        {id: 'deviceStates.lifeCycleName', value: Uni.I18n.translate('process.deviceCycleName', 'UNI', 'Device life cycle')},
        {id: 'deviceStates.name', value: Uni.I18n.translate('process.deviceState', 'UNI', 'Device state')},
        {id: 'issueReasons.name', value: Uni.I18n.translate('process.issueReasons', 'UNI', 'Issue reason')},
        {id: 'alarmReasons.name', value: Uni.I18n.translate('process.alarmReasons', 'UNI', 'Alarm reason')},
        {id: 'metrologyConfigurations.name', value: Uni.I18n.translate('process.metrologyConfiguration', 'UNI', 'Metrology configuration')},
        {id: 'connectionStates.name', value: Uni.I18n.translate('process.connectionState', 'UNI', 'Connection state')}
    ],

    getEditCmp: function () {
        var me = this;

        return  !me.getProperty().getPossibleValues() ?
        {
            xtype: 'displayfield',
            value: Uni.I18n.translate('Uni.property.selectionGrid.noItemsDefined', 'UNI', 'No items have been defined yet.'),
            itemId: 'no-item-defined',
            fieldStyle: {
                'color': '#FF0000'
            }
        } :
        {
            margin: '-7 0 0 0',
            xtype: 'fieldcontainer',
            itemId: 'selection-grid-fieldcontainer',
            name: me.getName(),
            readOnly: me.isReadOnly,
            layout : {
                type: 'vbox',
                align: 'stretch'
            },
            flex: 1,
            hidden: !me.getProperty().getPossibleValues(),
            items: [
                {
                    xtype: 'selection-grid',
                    itemId: 'selection-grid',
                    store: me.getGridStore(),
                    maxHeight: 368,
                    columns: me.getGridColumns(),
                    padding: '0 0 5 0'
                },
                {
                    html: '<div id="error-message" class="x-form-invalid-under" style="display:none"></div>'
                }
            ],
            // add validation support
            isFormField: true,

            markInvalid: function (errors) {
                var errorMessage = this.getEl().query('#error-message')[0];
                errorMessage.style.display ='block';
                errorMessage.innerHTML=errors;
            },
            clearInvalid: function () {
                this.getEl().query('#error-message')[0].style.display ='none';
            },
            isValid: function () {
                return true;
            },
            getModelData: function () {
                return null;
            }
        }
    },

    getGridStore: function(){
        var me = this,
            store = null,
            values = me.getProperty().getPossibleValues(),
            first, fields = [];

        if (values ) {
            if (values.length == 0) {
                return  store;
            } else {
                first = values[0];
                for (var dataIndex in first) {
                    if (Ext.Array.contains(me.ignoreFields, dataIndex))
                        continue;

                    var idx = Ext.String.format('{0}.{1}', me.getProperty().get('key'), dataIndex);
                    var obj = me.availableFields.filter(function ( index ) {
                        return index.id === idx;
                    })[0];

                    if (obj == undefined)
                        continue;

                    fields.push({
                        name: dataIndex,
                        type: 'string'
                    });
                }
            }
        }

        return Ext.create('Ext.data.Store',{fields: fields});
    },

    getGridColumns: function() {
        var me = this,
            store = null,
            values = me.getProperty().getPossibleValues(),
            first, columns = [];

        if (values ) {
            if (values.length == 0) {
                return store;
            } else {
                first = me.getProperty().getPossibleValues()[0];
                for (var dataIndex in first) {
                    if (Ext.Array.contains(me.ignoreFields, dataIndex))
                        continue;

                    var idx = Ext.String.format('{0}.{1}', me.getProperty().get('key'), dataIndex);
                    var obj = me.availableFields.filter(function ( index ) {
                        return index.id === idx;
                    })[0];

                    if (obj == undefined)
                        continue;

                    columns.push({
                        header: obj.value,
                        dataIndex: dataIndex,
                        flex: 1
                    });
                }
            }
        }

        return columns;
    },

    getValue: function () {
        var me = this,
            grid = me.down('grid'),
            result = [];

        if (grid) {
            result = me.recordsToIds(grid.getSelectionModel().getSelection());
        }

        return result.length ? result : null;
    },

    setValue: function (values) {
        var me = this,
            grid = me.down('#selection-grid');

        if (grid) {
            var store = grid.getStore(),
                possibleValues = me.getProperty().getPossibleValues();

            Ext.suspendLayouts();
            if (possibleValues) {
                store.loadData(possibleValues, true);
            }

            if (Ext.isArray(values)) {
                grid.getSelectionModel().suspendChanges();
                Ext.Array.forEach(values, function(value) {
                    grid.getSelectionModel().select(store.findExact('id', value), true);
                });
                grid.getSelectionModel().resumeChanges();
                grid.fireEvent('selectionchange');
            }
            if (store.getCount()) {
                grid.show();
            } else {
                grid.hide();
            }
            Ext.resumeLayouts(true);
        }
    },

    recordsToIds: function (data) {
        var modifiedData = [];

        Ext.Array.each(data, function (record) {
            modifiedData.push(record.getId());
        });
        return modifiedData;
    }
});