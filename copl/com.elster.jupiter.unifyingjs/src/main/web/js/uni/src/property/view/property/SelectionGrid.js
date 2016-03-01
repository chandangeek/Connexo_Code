Ext.define('Uni.property.view.property.SelectionGrid', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.view.grid.SelectionGrid'
    ],

    ignoreFields: ['id', 'lifeCycleId'],
    availableFields: [
        {id: 'deviceStates.lifeCycleName', value: Uni.I18n.translate('process.deviceCycleName', 'UNI', 'Device cycle name')},
        {id: 'deviceStates.name', value: Uni.I18n.translate('process.deviceState', 'UNI', 'Device state')},
        {id: 'issueReasons.name', value: Uni.I18n.translate('process.issueReasons', 'UNI', 'Issue reason')}
    ],

    getEditCmp: function () {
        var me = this;

        return {
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
            items: [
                {
                    xtype: 'selection-grid',
                    itemId: 'selection-grid',
                    store: me.getGridStore(),
                    padding: 0,
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
            first, fields = [];

        if (me.getProperty().getPossibleValues().length == 0)
            return  store;

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

            fields.push({
                name: dataIndex,
                type: 'string'
            });
        }

        return Ext.create('Ext.data.Store',{fields: fields});
    },

    getGridColumns: function() {
        var me = this,
            store = null,
            first, columns = [];

        if (me.getProperty().getPossibleValues().length == 0)
            return store;

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

        return columns;

    },

    getValue: function () {
        var me = this,
            result = me.recordsToIds(me.down('grid').getSelectionModel().getSelection());
        return result.length ? result : null;
    },

    setValue: function (values) {
        var me = this,
            grid = me.down('#selection-grid'),
            store = grid.getStore();

        Ext.suspendLayouts();
        store.loadData(me.getProperty().getPossibleValues(), true);

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
    },

    recordsToIds: function (data) {
        var modifiedData = [];

        Ext.Array.each(data, function (record) {
            modifiedData.push(record.getId());
        });
        return modifiedData;
    }
});