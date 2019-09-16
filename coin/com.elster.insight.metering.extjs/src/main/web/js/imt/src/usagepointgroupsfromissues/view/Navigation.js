/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroupsfromissues.view.Navigation', {

    extend: 'Uni.view.menu.NavigationMenu',

    alias: 'widget.add-group-navigation-panel',

    jumpForward: false,

    jumpBack: true,

    ui: 'medium',

    padding: '0 0 0 0',

    initComponent: function () {
        this.items = [
            {
                itemId: 'nav-general-attributes-step',
                text: Uni.I18n.translate('usagepointgroupfromissues.navigation.step.generalAttributes.title', 'IMT', 'General attributes')
            },
            {
                itemId: 'nav-select-issues-step',
                text: Uni.I18n.translate('usagepointgroupfromissues.navigation.step.selectIssues.title', 'IMT', 'Select issues')
            },
            {
                itemId: 'nav-select-usage-points-step',
                text: Uni.I18n.translate('usagepointgroupfromissues.navigation.step.selectUsagePoints.title', 'IMT', 'Select usage points')
            },
            {
                itemId: 'nav-confirmation-step',
                text: Uni.I18n.translate('usagepointgroupfromissues.navigation.step.confirmation.title', 'IMT', 'Confirmation')
            },
            {
                itemId: 'nav-status-step',
                text: Uni.I18n.translate('usagepointgroupfromissues.navigation.step.status.title', 'IMT', 'Status')
            }
        ];
        this.callParent(arguments);
    }
});
