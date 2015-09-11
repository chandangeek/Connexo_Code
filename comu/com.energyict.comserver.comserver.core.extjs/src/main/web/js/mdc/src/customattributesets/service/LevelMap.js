Ext.define('Mdc.customattributesets.service.LevelMap', {
    singleton: true,

    levelMap: {
        "LEVEL_1": Uni.I18n.translate('customattributesets.levelone', 'MDC', 'Level 1'),
        "LEVEL_2": Uni.I18n.translate('customattributesets.leveltwo', 'MDC', 'Level 2'),
        "LEVEL_3": Uni.I18n.translate('customattributesets.levelthree', 'MDC', 'Level 3'),
        "LEVEL_4": Uni.I18n.translate('customattributesets.levelfour', 'MDC', 'Level 4')
    },

    getTranslation: function (level) {
        return this.levelMap[level];
    },

    getPrivilegesString: function(privilegesArr) {
        var string = '';

        Ext.each(privilegesArr, function(privilege, index) {
            if (index != 0 ) string += ' - ';
            string += Mdc.customattributesets.service.LevelMap.getTranslation(privilege);
        });
        return string;
    }
});
