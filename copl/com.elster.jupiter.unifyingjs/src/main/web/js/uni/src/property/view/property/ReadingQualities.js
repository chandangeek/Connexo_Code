Ext.define('Uni.property.view.property.ReadingQualities', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Ext.ux.form.MultiSelect',
        'Uni.property.view.property.AddReadingQualityWindow'
    ],
    readingQualityStore: undefined,
    systemStore: undefined,
    categoryStore: undefined,
    indexStore: undefined,
    indexModels: undefined,

    initComponent: function(){
        this.readingQualityStore = Ext.create('Ext.data.ArrayStore', {
            fields: [ 'cimCode', 'displayName', 'tooltip' ],
            sorters: [
                {
                    property: 'displayName',
                    direction: 'ASC'
                }
            ]
        });
        this.initializeStoreInformation();
        this.callParent(arguments);
    },

    initializeStoreInformation: function() {
        var me = this,
            systemCode,
            categoryCode,
            indexCode,
            systemCodesProcessed = new Set(),
            systemModels = [],
            categoryCodesProcessed = new Set(),
            categoryModels = [],
            indexCodesForCategoryProcessed = {};// key = categoryCode, value = a Set containing the processed index codes

        me.indexModels = {}; // key = categoryCode, value = array of index models (=index store items corresponding with the chosen category)
        if (Ext.isEmpty(me.systemStore)) {
            me.systemStore = Ext.create('Ext.data.ArrayStore', {
                fields: ['id', 'name']
            });
        }
        if (Ext.isEmpty(me.categoryStore)) {
            me.categoryStore = Ext.create('Ext.data.ArrayStore', {
                fields: [ 'id', 'name' ]
            });
        }

        Ext.Array.forEach(me.getProperty().getPredefinedPropertyValues().get('possibleValues'), function(possibleValueItem){
            var cimCodeParts = possibleValueItem.cimCode.split('.');
            systemCode = cimCodeParts[0];
            categoryCode = cimCodeParts[1];
            indexCode = cimCodeParts[2];
            if (!systemCodesProcessed.has(systemCode)) { // system code not processed yet
                systemCodesProcessed.add(systemCode);
                systemModels.push({id: systemCode, name: possibleValueItem.systemName});
            }
            if (!categoryCodesProcessed.has(categoryCode)) { // category code not processed yet
                categoryCodesProcessed.add(categoryCode);
                categoryModels.push({id: categoryCode, name: possibleValueItem.categoryName});
            }

            if (!Object.prototype.hasOwnProperty.call(me.indexModels, categoryCode)) { // no index models yet for this category code
                me.indexModels[categoryCode] = [];
                me.indexModels[categoryCode].push({id: indexCode, name: possibleValueItem.indexName});
                indexCodesForCategoryProcessed[categoryCode] = new Set();
                indexCodesForCategoryProcessed[categoryCode].add(indexCode);
            } else if (!indexCodesForCategoryProcessed[categoryCode].has(indexCode)) { // index code not processed yet
                indexCodesForCategoryProcessed[categoryCode].add(indexCode);
                me.indexModels[categoryCode].push({id: indexCode, name: possibleValueItem.indexName});
            }
        });

        // Sort the system models (The '*' first, the rest alphabetically)
        systemModels.sort(function(model1, model2){
            if (model1.id === '*') {
                return -1;
            }
            if (model2.id === '*') {
                return 1;
            }
            return model1.name.localeCompare(model2.name);
        });
        me.systemStore.add(systemModels);

        // Sort the category models alphabetically
        categoryModels.sort(function(model1, model2){
            return model1.name.localeCompare(model2.name);
        });
        me.categoryStore.add(categoryModels);

        // Sort (each of) the index models (The '*' first, the rest alphabetically)
        Object.keys(me.indexModels).forEach(function(key) {
            this[key].sort(function(model1, model2){
                if (model1.id === '*') {
                    return -1;
                }
                if (model2.id === '*') {
                    return 1;
                }
                return model1.name.localeCompare(model2.name);
            });
        }, me.indexModels);
    },

    getEditCmp: function () {
        var me = this;
        return [
            {
                layout: 'hbox',
                items: [
                    {
                        xtype: 'component',
                        html: Uni.I18n.translate('general.noReadingQualitiesAdded','UNI','No reading qualities have been added'),
                        itemId: 'uni-noReadingQualitiesLabel',
                        style: {
                            'font': 'italic 13px/17px Lato',
                            'color': '#686868',
                            'margin-top': '6px',
                            'margin-right': '10px'
                        }
                    },
                    {
                        xtype: 'gridpanel',
                        itemId: 'uni-reading-qualities-grid',
                        store: me.readingQualityStore,
                        hideHeaders: true,
                        padding: 0,
                        viewConfig: {
                            disableSelection: true
                        },
                        scroll: 'vertical',
                        columns: [
                            {
                                dataIndex: 'displayName',
                                flex: 1,
                                renderer: function(value, metaData, record) {
                                    return '<span style="display:inline-block; float: left; margin-right:7px;" >' + record.get('displayName') + '</span>'+
                                        '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="' + Ext.htmlEncode(record.get('tooltip')) + '"></span>';
                                }
                            },
                            {
                                xtype: 'actioncolumn',
                                align: 'right',
                                items: [
                                    {
                                        iconCls: 'uni-icon-delete',
                                        tooltip: Uni.I18n.translate('general.remove','UNI','Remove'),
                                        handler: function (grid, rowIndex) {
                                            grid.getStore().removeAt(rowIndex);
                                            if (grid.getStore().count() === 0) {
                                                me.updateGrid();
                                            }
                                        }
                                    }
                                ]
                            }
                        ],
                        height: 220,
                        width: 670
                    },
                    {
                        xtype: 'button',
                        itemId: 'addReadingQualitiesButton',
                        text: Uni.I18n.translate('general.addReadingQualities', 'UNI', 'Add reading qualities'),
                        action: 'addReadingQualities',
                        margin: '0 0 0 10'
                    }
                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;

        if (me.down('#addReadingQualitiesButton')) {
            me.down('#addReadingQualitiesButton').on('click', function () {
                Ext.create('Uni.property.view.property.AddReadingQualityWindow', {
                    title: Uni.I18n.translate('general.addReadingQuality', 'UNI', 'Add reading quality'),
                    readingQualities: me
                });
            });
        }
        me.callParent(arguments);
    },

    getDisplayCmp: function () {
        var me = this,
            store = me.getProperty().getPredefinedPropertyValues().possibleValues();

        return {
            xtype: 'displayfield',
            name: me.getName(),
            itemId: me.key + 'displayfield',
            renderer: function (data) {
                var result = '';
                Ext.isArray(data) && Ext.Array.each(data, function (item) {
                    result +=
                        '<span style="display:inline-block; float: left; margin-right:7px;" >' + me.getDisplayName(item) + '</span>'+
                        '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="' + Ext.htmlEncode(me.getTooltip(item)) + '"></span><br>';
                });
                return result.length>0 ? result : '-';
            }
        }
    },

    addReadingQuality: function(cimCode) {
        var me = this;
        me.readingQualityStore.add({cimCode: cimCode, displayName: me.getDisplayName(cimCode), tooltip: me.getTooltip(cimCode)});
        me.down('#uni-noReadingQualitiesLabel').hide();
        me.getField().show();
    },

    getDisplayName: function(cimCode) {
        var me = this,
            displayName = '',
            parts = cimCode.split('.'),
            categoryCode = parts[1],
            indexCode = parts[2],
            indexStore,
            storeRecord;

        indexStore = me.getIndexStore(categoryCode);
        storeRecord = Ext.isEmpty(indexStore) ? undefined : indexStore.findRecord('id', indexCode);
        displayName += storeRecord ? storeRecord.get('name') : Uni.I18n.translate('general.unknown', 'UNI', 'Unknown');
        displayName += ' (' + cimCode + ')';
        return displayName;
    },

    getTooltip: function(cimCode) {
        var me = this,
            parts = cimCode.split('.'),
            systemCode = parts[0],
            categoryCode = parts[1],
            indexCode = parts[2],
            indexStore,
            tooltip = '<table><tr><td>',
            storeRecord;

        tooltip += '<b>' + Uni.I18n.translate('general.readingQuality.field1.name', 'UNI', 'System') + ':</b></td>';
        storeRecord = me.systemStore.findRecord('id', systemCode);
        tooltip += '<td>' + (storeRecord ? storeRecord.get('name') : Uni.I18n.translate('general.unknown', 'UNI', 'Unknown')) + '</td></tr>';
        tooltip += '<tr><td><b>' + Uni.I18n.translate('general.readingQuality.field2.name', 'UNI', 'Category') + ':</b></td>';
        storeRecord = me.categoryStore.findRecord('id', categoryCode);
        tooltip += '<td>' + (storeRecord ? storeRecord.get('name') : Uni.I18n.translate('general.unknown', 'UNI', 'Unknown')) + '</td></tr>';
        tooltip += '<tr><td><b>' + Uni.I18n.translate('general.readingQuality.field3.name', 'UNI', 'Index') + ':</b></td>';
        indexStore = me.getIndexStore(categoryCode);
        storeRecord = Ext.isEmpty(indexStore) ? undefined : indexStore.findRecord('id', indexCode);
        tooltip += '<td>' + (storeRecord ? storeRecord.get('name') : Uni.I18n.translate('general.unknown', 'UNI', 'Unknown')) + '</td></tr></table>';
        return tooltip;
    },

    getSystemStore: function() {
        return this.systemStore;
    },

    getCategoryStore: function() {
        return this.categoryStore;
    },

    getIndexStore: function(categoryCode) {
        var me = this;

        if (!Object.prototype.hasOwnProperty.call(me.indexModels, categoryCode)) {
            return undefined;
        }
        if (Ext.isEmpty(me.indexStore)) {
            me.indexStore = Ext.create('Ext.data.ArrayStore', {
                fields: [ 'id', 'name' ]
            });
        } else {
            me.indexStore.removeAll();
        }
        me.indexStore.add(me.indexModels[categoryCode]);
        return me.indexStore;
    },

    updateGrid: function() {
        var me = this,
            grid = me.getField(),
            emptyLabel = me.down('#uni-noReadingQualitiesLabel');
        if (grid.getStore().count() === 0) {
            emptyLabel.show();
            grid.hide();
        } else {
            emptyLabel.hide();
            grid.show();
        }
    },

    getField: function () {
        return this.down('#uni-reading-qualities-grid');
    },

    getValue: function () {
        var me = this,
            grid = me.getField();

        if (grid.getStore().count() === 0) {
            return [];
        } else {
            var result = [];
            Ext.Array.forEach(grid.getStore().getRange(), function(storeRecord){
                result.push( storeRecord.get('cimCode') );
            });
            return result;
        }
    },

    setValue: function (value) {
        var me = this;
        if (me.isEdit) {
            if (Ext.isEmpty(value)) {
                me.down('#uni-noReadingQualitiesLabel').show();
                me.getField().hide();
            } else if (Ext.isArray(value)) {
                var modelItemsToAdd = [];
                Ext.Array.forEach(value, function(arrayItem){
                    modelItemsToAdd.push(
                        {
                            cimCode: arrayItem,
                            displayName: me.getDisplayName(arrayItem),
                            tooltip: me.getTooltip(arrayItem)
                        }
                    );
                });
                me.getField().getStore().add(modelItemsToAdd);
                me.down('#uni-noReadingQualitiesLabel').hide();
                me.getField().show();
            }
        } else {
            me.getDisplayField().setValue(Ext.isArray(value) ? value : []);
        }
    }
});