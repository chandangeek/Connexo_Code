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
        }
    ],

    init: function () {
        this.control({
            'add-reading-types [action=addReadingTypes]': {
                click: this.addSelectedReadingTypes
            }
        });
    },

    showOverview: function (ruleSetId, ruleId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            readingTypesStore = me.getStore('Est.main.store.ReadingTypes'),
            clipboard = me.getStore('Est.main.store.Clipboard'),
            rule = clipboard.get('estimationRule'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            setStoreFilter = function () {
                var selectedReadings = [];

                clipboard.set('estimationRule', rule);
                Ext.util.History.on('change', me.checkRoute, me);
                rule.readingTypes().each(function (item) {
                    selectedReadings.push(item.get('mRID'));
                });
                viewport.down('est-main-view-readingtypetopfilter').setSelectedReadings(selectedReadings);

                readingTypesStore.data.clear();
                readingTypesStore.load();
            },
            widget = Ext.widget('add-reading-types', {
                returnLink: router.getRoute(router.currentRoute.replace('/addreadingtypes', '')).buildUrl(router.arguments, router.queryParams)
            });

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
            // When starting from a url for adding reading types to an estimation rule:
            me.navigateToPreviousPage();
            return;
        }

        me.getModel('Est.estimationrulesets.model.EstimationRuleSet').load(ruleSetId, {
            success: function (record) {
                me.getApplication().fireEvent('loadEstimationRuleSet', record);
            }
        });
    },

    navigateToPreviousPage: function () {
        var router = this.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();
        router.getRoute(splittedPath.join('/')).forward();
    },

    checkRoute: function (token) {
        var me = this,
            currentRoute = me.getController('Uni.controller.history.Router').currentRoute,
            allowableRoutes = [
                'administration/estimationrulesets/estimationruleset/rules/rule/add',
                'administration/estimationrulesets/estimationruleset/rules/rule/edit'
            ];

        Ext.util.History.un('change', me.checkRoute, me);

        if (Ext.Array.findBy(allowableRoutes, function (item) {return item === currentRoute}) !== null) {
            me.getStore('Est.main.store.Clipboard').clear('estimationRule');
        }
    },

    addSelectedReadingTypes: function () {
        var me = this;

        me.getStore('Est.main.store.Clipboard').get('estimationRule').readingTypes().add(me.getGrid().getSelectionModel().getSelection());
        window.location.href = me.getPage().returnLink;
    }
});