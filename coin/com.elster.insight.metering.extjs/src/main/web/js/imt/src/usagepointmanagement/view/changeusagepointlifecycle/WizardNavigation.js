/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.changeusagepointlifecycle.WizardNavigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.usagepointChangeLifeCycleWizardNavigation',
    width: 256,
    jumpForward: false,
    jumpBack: false,
    ui: 'medium',
    title: Uni.I18n.translate('usagePointLifeCycle.change', 'IMT', 'Change usage point life cycle'),
    items: [
        {
            itemId: 'select-usage-point-life-cycle',
            action: 'selectUsagePointLifeCycle',
            text: Uni.I18n.translate('usagepointchangelifecycleexecute.navigation.step1title', 'IMT', 'Select usage point life cycle')
        },
        {
            itemId: 'status',
            action: 'status',
            text: Uni.I18n.translate('general.status', 'IMT', 'Status')
        }
    ]
});
