/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.devicegroupfromissues.Navigation', {

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
                text: Uni.I18n.translate('devicegroupfromissues.navigation.step.generalAttributes.title', 'ISU', 'General attributes')
            },
            {
                itemId: 'nav-select-issues-step',
                text: Uni.I18n.translate('devicegroupfromissues.navigation.step.selectIssues.title', 'ISU', 'Select issues')
            },
            {
                itemId: 'nav-select-devices-step',
                text: Uni.I18n.translate('devicegroupfromissues.navigation.step.selectDevices.title', 'ISU', 'Select devices')
            },
            {
                itemId: 'nav-confirmation-step',
                text: Uni.I18n.translate('devicegroupfromissues.navigation.step.confirmation.title', 'ISU', 'Confirmation')
            },
            {
                itemId: 'nav-status-step',
                text: Uni.I18n.translate('devicegroupfromissues.navigation.step.status.title', 'ISU', 'Status')
            }
        ];
        this.callParent(arguments);
    }
});
