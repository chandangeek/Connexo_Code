Ext.define('Est.estimationrules.controller.Overview', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'/*,
        'Uni.controller.AppController'*/
    ],

    views: [
        'Est.estimationrules.view.Overview'
    ],

    stores: [
        'Est.estimationrules.store.Rules'
    ],

    models: [
        'Est.estimationrulesets.model.EstimationRuleSet',
        'Est.estimationrules.model.Rule'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'estimation-rules-overview'
        },
        {
            ref: 'grid',
            selector: 'estimation-rules-overview estimation-rules-grid'
        },
        {
            ref: 'preview',
            selector: 'estimation-rules-overview estimation-rules-detail-form'
        },
        {
            ref: 'sideMenu',
            selector: 'estimation-rules-overview estimation-rule-set-side-menu'
        }
    ],

    init: function () {
        this.control({
            'estimation-rules-overview estimation-rules-grid': {
                select: this.showPreview
            },
            '#estimation-rules-overview-action-menu': {
                click: this.chooseAction
            },
            'estimation-rules-overview [action=saveEstimationRulesOrder]': {
                click: this.saveEstimationRulesOrder
            }
        });
    },

    showOverview: function (ruleSetId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('estimation-rules-overview', {
                router: router,
                actionMenuItemId: 'estimation-rules-overview-action-menu',
                editOrder: router.queryParams.editOrder
            }),
            grid = widget.down('estimation-rules-grid'),
            pageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageView.setLoading(true);
        grid.getStore().loadData([], false);

        me.getModel('Est.estimationrules.model.Rule').getProxy().setUrl(ruleSetId);
        me.getModel('Est.estimationrulesets.model.EstimationRuleSet').load(ruleSetId, {
            success: function (record) {
                var rules = record.rules();

                me.getApplication().fireEvent('changecontentevent', widget);
                Ext.suspendLayouts();
                me.getSideMenu().down('#estimation-rule-set-link').setText(record.get('name'));
                rules.totalCount = rules.getCount();
                if (rules.totalCount) {
                    widget.down('preview-container').bindStore(rules);
                    grid.reconfigure(rules);
                    grid.down('pagingtoolbartop').bindStore(rules);
                }
                grid.getStore().fireEvent('load');
                widget.ruleSetRecord = record;
                me.getApplication().fireEvent('loadEstimationRuleSet', record);
                Ext.resumeLayouts(true);
            },
            failure: function () {
                grid.getStore().fireEvent('load');
            },
            callback: function () {
                pageView.setLoading(false);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview(),
            menu = preview.down('#estimation-rules-overview-action-menu');

        preview.updateForm(record);
        if (menu) {
            menu.record = record;
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        switch (item.action) {
            case 'remove':
                Ext.create('Uni.view.window.Confirmation').show({
                    title: Uni.I18n.translate('general.removex', 'EST', "Remove '{0}'?",[menu.record.get('name')]),
                    msg: Uni.I18n.translate('estimationrules.deleteConfirmation.msg', 'EST', 'This estimation rule will no longer be available.'),
                    fn: function (state) {
                        switch (state) {
                            case 'confirm':
                                me.removeRule(menu.record);
                                break;
                        }
                    }
                });
                break;
            case 'toggleActivation':
                me.toggleActivation(menu.record);
                break;
            case 'edit':
                router.arguments.ruleId = menu.record.getId();
                router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule/edit').forward(router.arguments);
                break;
        }
    },

    removeRule: function (record) {
        var me = this,
            page = me.getPage(),
            router = me.getController('Uni.controller.history.Router');

        page.setLoading(Uni.I18n.translate('general.removing', 'EST', 'Removing...'));
        record.destroy({
            success: function () {
                if (page.rendered) {
                    router.getRoute().forward();
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationrules.estimationRuleRemoved', 'EST', 'Estimation rule removed'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    toggleActivation: function (record) {
        var me = this,
            page = me.getPage(),
            isActive = record.get('active');

        record.set('active', !isActive);
        page.setLoading(true);
        record.save({
            callback: function (record, operation, success) {
                page.setLoading(false);
                if (success) {
                    record.commit();
                    me.getApplication().fireEvent('acknowledge', isActive
                        ? Uni.I18n.translate('estimationrules.deactivateRuleSuccess', 'EST', 'Estimation rule deactivated')
                        : Uni.I18n.translate('estimationrules.activateRuleSuccess', 'EST', 'Estimation rule activated'));
                    if (page.rendered) {
                        me.getPreview().updateForm(record);
                    }
                } else {
                    record.set('active', isActive);
                }
            }
        });
    },

    saveEstimationRulesOrder: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            page = me.getPage();

        page.setLoading(true);
        page.ruleSetRecord.save({
            callback: function (record, operation, success) {
                page.setLoading(false);
                if (success) {
                    router.getRoute(router.currentRoute).forward(router.arguments, null);
                }
            }
        });
    }
});