/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.configuration.view.RuleWithAttributesActionsMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.rule-with-attributes-actions-menu',
    noSort: true,
    items: [],
    records: null,
    router: null,
    application: null,
    type: null,
    kindOfReadingType: '',
    isRegister: false,

    initComponent: function () {
        var me = this,
            records = me.records,
            editItems = [],
            restoreItems = [],
            duplicatedNames,
            properties,
            hasOverriddenProperty,
            hasCanBeOverriddenProperty,
            recordId;

        if (me.type === 'validation') {
            duplicatedNames = me.findDuplicates(getNames(records));
        }
        me.items = [];
        records.forEach(function (record) {
            properties = record.properties().getRange();
            if (properties.length) {
                recordId = record.getId();
                hasOverriddenProperty = _.find(properties, function (property) {
                    return property.get('overridden');
                });
                hasCanBeOverriddenProperty = _.find(properties, function (property) {
                    return property.get('canBeOverridden');
                });

                if (me.type === 'validation' && duplicatedNames.length && (duplicatedNames.indexOf(record.get('name')) !== -1)) {
                    record.set('duplicated', true);
                }

                if (hasCanBeOverriddenProperty) {
                    editItems.push({
                        record: record,
                        text: Ext.String.format("{0} '{1}'", Uni.I18n.translate('general.edit', 'CFG', 'Edit'), record.get('name')) +
                            (!!record.get('duplicated') ? ' (' + record.get('dataQualityLevel').charAt(0).toLowerCase() + record.get('dataQualityLevel').substring(1) + ')' : ''),
                        action: 'edit' + recordId,
                        itemId: 'edit-rule' + recordId + me.kindOfReadingType
                    });
                }
                if (hasOverriddenProperty) {
                    restoreItems.push({
                        record: record,
                        text: Ext.String.format("{0} '{1}'", Uni.I18n.translate('general.restore', 'CFG', 'Restore'), record.get('name')) +
                            (!!record.get('duplicated') ? ' (' + record.get('dataQualityLevel').charAt(0).toLowerCase() + record.get('dataQualityLevel').substring(1) + ')' : ''),
                        action: 'restore' + recordId,
                        itemId: 'restore-rule' + recordId + me.kindOfReadingType
                    });
                }
            }
        });

        Ext.Array.push(me.items, editItems, restoreItems);
        me.callParent();
        me.mon(me, 'click', me.chooseRuleWithAttributesAction, me);
        function getNames(arr) {
            return arr.map(function (item) {
                return item.get('name');
            });
        }
    },

    findDuplicates: function (names) {
        var result = [];

        names.forEach(function (element, index) {

            // Find if there is a duplicate or not
            if (names.indexOf(element, index + 1) > -1) {

                // Find if the element is already in the result array or not
                if (result.indexOf(element) === -1) {
                    result.push(element);
                }
            }
        });

        return result;
    },

    chooseRuleWithAttributesAction: function (menu, item) {
        var me = this,
            router = me.router,
            record = item.record,
            id = record.getId(),
            queryParams = me.kindOfReadingType ? {readingType: record.get('readingType').mRID} : {},
            route;

        if (me.kindOfReadingType && !me.isRegister) {
            route = me.type === 'validation' ? router.getRoute('devices/device/channels/validation/editrule') : router.getRoute('devices/device/channels/estimation/editrule');
        } else if (me.kindOfReadingType && me.isRegister) {
            route = router.getRoute('devices/device/registers/validation/editrule');
        } else {
            route = me.type === 'validation' ? router.getRoute('usagepoints/view/purpose/output/editvalidationrule') : router.getRoute('usagepoints/view/purpose/output/editestimationrule');
        }

        switch (item.action) {
            case 'edit' + id:
                route.forward({ruleId: id}, queryParams);
                break;
            case 'restore' + id:
                me.onRestoreRuleWithAttributes(record);
                break;
        }
    },

    onRestoreRuleWithAttributes: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.restore', 'CFG', 'Restore')
            });

        confirmationWindow.show({
            title: Uni.I18n.translate('general.restoreAttributes.question', 'CFG', "Restore attributes?"),
            msg: Uni.I18n.translate('general.restoreAttributes.msg', 'CFG', 'All edited attributes will be removed'),
            fn: confirm
        });

        function confirm(state) {
            if (state === 'confirm') {
                me.restoreRuleWithAttributes(record);
            }
        }
    },

    restoreRuleWithAttributes: function (record) {
        var me = this,
            router = me.router,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        if (me.kindOfReadingType && !me.isRegister) {
            record.getProxy().extraParams = {deviceId: Uni.util.Common.encodeURIComponent(router.arguments.deviceId), channelId: router.arguments.channelId};
        } else if (me.kindOfReadingType && me.isRegister) {
            record.getProxy().extraParams = {deviceId: Uni.util.Common.encodeURIComponent(router.arguments.deviceId), registerId: router.arguments.registerId};
        } else {
            record.getProxy().extraParams = {usagePointId: router.arguments.usagePointId, purposeId: router.arguments.purposeId, outputId: router.arguments.outputId};
        }
        record.destroy({
            isNotEdit: true,
            success: function () {
                me.application.fireEvent('acknowledge', Uni.I18n.translate('general.restoreAttributes.success', 'CFG', 'Edited attributes restored'));
                router.getRoute().forward();
            }
        });
    }
});
