Ext.define('Imt.usagepointmanagement.view.forms.LifeCycleTransition', {
    extend: 'Ext.form.Panel',
    alias: 'widget.life-cycle-transition-info-form',
    requires: [
        'Imt.usagepointmanagement.view.StepDescription'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'step-description',
                itemId: 'link-metrology-configuration-with-meters-step-description',
                text: Uni.I18n.translate('usagepoint.wizard.lifeCycleTransitionStep.description', 'IMT', 'The selected usage point life cycle will be triggered after the usage point creation.')
            }
        ];

        me.callParent(arguments);
    }
});