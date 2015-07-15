Ext.define('Est.estimationrules.controller.AddReadingTypes', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Uni.util.Common',
        'Uni.util.History'
    ],

    views: [
        'Est.main.view.AddReadingTypes'
    ],

    models: [
        'Est.estimationrulesets.model.EstimationRuleSet',
        'Est.estimationrules.model.Rule'
    ],

    stores: [
        'Est.main.store.ReadingTypes',
        'Est.main.store.UnitsOfMeasure',
        'Est.main.store.TimeOfUse',
        'Est.main.store.Intervals'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'add-reading-types'
        },
        {
            ref: 'grid',
            selector: 'add-reading-types reading-types-grid'
        },
        {
            ref: 'filterForm',
            selector: 'add-reading-types reading-types-side-filter'
        },
        {
            ref: 'filterToolbar',
            selector: 'add-reading-types filter-top-panel'
        }
    ],

    init: function () {
        this.control({
            'add-reading-types [action=addReadingTypes]': {
                click: this.addReadingTypes
            },
            'add-reading-types [action=applyFilter]': {
                click: this.applyFilter
            },
            'add-reading-types [action=clearFilter]': {
                click: this.clearFilter
            },
            'add-reading-types filter-top-panel': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            }
        });
    },

    showOverview: function (ruleSetId, ruleId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            readingTypesStore = me.getStore('Est.main.store.ReadingTypes'),
            clipboard = me.getStore('Est.main.store.Clipboard'),
            rule = clipboard.get('estimationRule'),
            setStoreFilter = function () {
                var selectedReadings = [];

                clipboard.set('estimationRule', rule);
                Ext.util.History.on('change', me.checkRoute, me);
                rule.readingTypes().each(function (item) {
                    selectedReadings.push(item.get('mRID'));
                });
                readingTypesStore.getProxy().setExtraParam('filter', Ext.encode([
                    {
                        property: 'selectedReadings',
                        value: selectedReadings
                    },
                    {
                        property: 'equidistant',
                        value: true
                    }
                ]));
                readingTypesStore.data.clear();
                readingTypesStore.loadPage(1);
            },
            widget = Ext.widget('add-reading-types', {
                returnLink: router.getRoute(router.currentRoute.replace('/addreadingtypes', '')).buildUrl(router.arguments, router.queryParams)
            }),
            ruleModel;

        if (router.queryParams.previousRoute) {
            Uni.util.History.suspendEventsForNextCall();
            router.getRoute().forward(router.arguments, null);
        }

        me.getApplication().fireEvent('changecontentevent', widget);

        if (rule) {
            setStoreFilter();
        } else if (!ruleId) {
            rule = Ext.create('Est.estimationrules.model.Rule');
            setStoreFilter();
        } else {
            ruleModel = me.getModel('Est.estimationrules.model.Rule');
            ruleModel.getProxy().setUrl(ruleSetId);
            ruleModel.load(ruleId, {
                success: function (record) {
                    me.getApplication().fireEvent('loadEstimationRule', record);
                    rule = record;
                    setStoreFilter();
                }
            });
        }

        me.getModel('Est.estimationrulesets.model.EstimationRuleSet').load(ruleSetId, {
            success: function (record) {
                me.getApplication().fireEvent('loadEstimationRuleSet', record);
            }
        });
    },

    checkRoute: function (token) {
        var me = this,
            currentRoute = me.getController('Uni.controller.history.Router').currentRoute,
            allowableRoutes = [
                'administration/estimationrulesets/estimationruleset/rules/rule/add',
                'administration/estimationrulesets/estimationruleset/rules/rule/edit'
            ];

        Ext.util.History.un('change', me.checkRoute, me);

        if (!Ext.Array.findBy(allowableRoutes, function (item) {return item === currentRoute})) {
            me.getStore('Est.main.store.Clipboard').clear('estimationRule');
        }
    },

    addReadingTypes: function () {
        var me = this;

        me.getStore('Est.main.store.Clipboard').get('estimationRule').readingTypes().add(me.getGrid().getSelectionModel().getSelection());
        window.location.href = me.getPage().returnLink;
    },

    applyFilter: function () {
        var me = this,
            readingTypesStore = me.getStore('Est.main.store.ReadingTypes'),
            readingTypesStoreProxy = readingTypesStore.getProxy(),
            selectedReadings = Ext.Array.findBy(Ext.decode(readingTypesStoreProxy.extraParams.filter), function (item) {
                return item.property === 'selectedReadings';
            }),
            filterToolbar = me.getFilterToolbar(),
            newFilter = [];

        Ext.suspendLayouts();
        filterToolbar.getContainer().removeAll();
        Ext.Array.each(me.getFilterForm().query('[isFormField=true]'), function (field) {
            var value = field.getValue(),
                record;

            if (value !== '' && value !== undefined) {
                switch (field.name) {
                    case 'name':
                        newFilter.push({
                            property: 'name',
                            value: value
                        });
                        filterToolbar.setFilter('name', field.getFieldLabel(), value, false);
                        break;
                    case 'unitOfMeasure':
                        record = field.findRecordByValue(value);
                        if (record) {
                            newFilter.push({
                                property: 'unitOfMeasure',
                                value: record.get('unit')
                            });
                            newFilter.push({
                                property: 'multiplier',
                                value: record.get('multiplier')
                            });
                            filterToolbar.setFilter('unitOfMeasure', field.getFieldLabel(), record.get('name'), false);
                        }
                        break;
                    case 'tou':
                        if (value !== null) {
                            newFilter.push({
                                property: 'tou',
                                value: value
                            });
                            filterToolbar.setFilter('tou', field.getFieldLabel(), value, false);
                        }
                        break;
                    case 'time':
                        record = field.findRecordByDisplay(field.getRawValue());
                        if (record) {
                            if (value !== null) {
                                newFilter.push({
                                    property: 'time',
                                    value: value
                                });
                            }
                            if (record.get('macro') !== null) {
                                newFilter.push({
                                    property: 'macro',
                                    value: record.get('macro')
                                });
                            }
                            filterToolbar.setFilter('time', field.getFieldLabel(), record.get('name'), false);
                        }
                        break;
                }
            }
        });
        Ext.resumeLayouts(true);
        newFilter.push(selectedReadings);
        newFilter.push({
            property: 'equidistant',
            value: true
        });
        readingTypesStoreProxy.setExtraParam('filter', Ext.encode(newFilter));
        readingTypesStore.data.clear();
        readingTypesStore.loadPage(1);
    },

    clearFilter: function () {
        var me = this;

        Ext.suspendLayouts();
        Ext.Array.each(me.getFilterForm().query('[isFormField=true]'), function (field) {
            field.reset();
        });
        Ext.resumeLayouts(true);
        me.applyFilter();
    },

    removeFilterItem: function (key) {
        var me = this,
            formField = me.getFilterForm().down('[name=' + key + ']');

        if (formField) {
            formField.reset();
        }

        me.applyFilter();
    }
});