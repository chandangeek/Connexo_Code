/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.about.Static', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.about-static-info',
    html:
      '<br>'
      + '<p>' + Uni.I18n.translate('about.static.copyright', 'SAM', 'Copyright &copy; 2016 Honeywell International Inc. All rights reserved.', false) + '</p>'
      + '<p>' + Uni.I18n.translate('about.static.part.1', 'SAM', 'The use of this product is subject to the terms of the relevant Honeywell Software License Agreement, unless otherwise agreed or specified.', false) + '</p>'
      + '<p>' + Uni.I18n.translate('about.static.part.2', 'SAM', 'This product contains components (including open source software) from third parties which are subject to separate licensing and warranty conditions.', false) + '</p>'
      + '<p>' + Uni.I18n.translate('about.static.part.3', 'SAM', 'More detailed information regarding such third party components, including applicable copyright, legal and licensing notices, can be found in the "licenses" subdirectory of the Connexo installation directory.', false) + '</p>'
      + '<p>' + Uni.I18n.translate('about.static.part.4', 'SAM', 'In the event of a conflict between the terms of the relevant Honeywell Software License Agreement and the licensing and warranty information contained in any third party licence,', false)
              + Uni.I18n.translate('about.static.part.5', 'SAM', 'the provisions of such third party license will take precedence with respect to the relevant third party component.', false) + '</p>'
});