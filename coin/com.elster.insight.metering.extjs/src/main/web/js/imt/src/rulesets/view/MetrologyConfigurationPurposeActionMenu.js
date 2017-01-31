Ext.define('Imt.rulesets.view.MetrologyConfigurationPurposeActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.metrology-configuration-purpose-action-menu',

    items: [
        {
            itemId: 'remove',
            text: Uni.I18n.translate('general.remove', 'IMT', 'Remove'),
            action: 'remove',
            section: this.SECTION_REMOVE
        }
    ]
});