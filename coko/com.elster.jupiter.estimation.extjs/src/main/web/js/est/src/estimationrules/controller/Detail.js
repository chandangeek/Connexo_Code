Ext.define('Est.estimationrules.controller.Detail', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Est.estimationrules.view.Detail'
    ],

    models: [
        'Est.estimationrulesets.model.EstimationRuleSet',
        'Est.estimationrules.model.Rule'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'estimation-rule-detail'
        },
        {
            ref: 'sideMenu',
            selector: 'estimation-rule-detail estimation-rule-side-menu'
        }
    ],

    init: function () {
        this.control({
            '#estimation-rule-detail-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showOverview: function (ruleSetId, ruleId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            ruleModel = me.getModel('Est.estimationrules.model.Rule'),
            widget = Ext.widget('estimation-rule-detail', {
                router: router,
                actionMenuItemId: 'estimation-rule-detail-action-menu'
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        ruleModel.getProxy().setUrl(ruleSetId);
        ruleModel.load(ruleId, {
            callback: function (record, operation, success) {
                widget.setLoading(false);
                if (success && widget.rendered) {
                    me.getSideMenu().down('#estimation-rule-link').setText(record.get('name'));
                    me.getApplication().fireEvent('loadEstimationRule', record);
                    widget.down('estimation-rules-detail-form').updateForm(record);
                    widget.down('#estimation-rule-detail-action-menu').record = record;
                }
            }
        });
        me.getModel('Est.estimationrulesets.model.EstimationRuleSet').load(ruleSetId, {
            success: function (record) {
                me.getApplication().fireEvent('loadEstimationRuleSet', record);
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        switch (item.action) {
            case 'remove':
                Ext.create('Uni.view.window.Confirmation').show({
                    title: Uni.I18n.translate('general.remove', 'EST', 'Remove') + ' \'' + menu.record.get('name') + '\'?',
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
                router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule/edit').forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});
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
                router.getRoute('administration/estimationrulesets/estimationruleset/rules').forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationrules.estimationRuleRemoved', 'EST', 'Estimation rule removed'));
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
                        page.down('estimation-rules-detail-form').updateForm(record);
                    }
                } else {
                    record.set('active', isActive);
                }
            }
        });
    }
});