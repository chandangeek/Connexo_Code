/*
 * AspectEditorManager.java
 *
 * Created on 5 februari 2003, 14:56
 */

package com.energyict.mdc.engine.offline.gui.editors;

import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.offline.core.LocalizableEnum;
import com.energyict.mdc.engine.offline.core.SerialCommunicationSettings;
import com.energyict.mdc.engine.offline.gui.actions.UserAction;
import com.energyict.mdc.engine.offline.gui.models.EncryptedStringAdapter;
import com.energyict.mdc.engine.offline.gui.table.renderer.LocalizedEnumListCellRenderer;
import com.energyict.mdc.engine.offline.model.Password;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Karel
 */
public class AspectEditorManager {

    static Map<Class, Class> editorMap = new HashMap<>();

    static {
        editorMap.put(String.class, StringAspectEditor.class);
        editorMap.put(Boolean.TYPE, BoolAspectEditor.class);
        editorMap.put(Date.class, DateAspectEditor.class);
        editorMap.put(Integer.TYPE, IntAspectEditor.class);
        editorMap.put(Integer.class, IntegerAspectEditor.class);
        editorMap.put(Double.TYPE, DoubleAspectEditor.class);
        editorMap.put(BigDecimal.class, BigDecimalAspectEditor.class);
        editorMap.put(Unit.class, UnitAspectEditor.class);
        editorMap.put(TimeDuration.class, TimeDurationAspectEditor.class);
        editorMap.put(SerialCommunicationSettings.class, SerialCommunicationSettingsAspectEditor.class);
        editorMap.put(Level.class, LevelAspectEditor.class);
        editorMap.put(ComServer.LogLevel.class, LogLevelListAspectEditor.class);
        editorMap.put(RelativePeriod.class, RelativePeriodAspectEditor.class);
        editorMap.put(TimeOfDay.class, TimeOfDayAspectEditor.class);
        editorMap.put(Quantity.class, QuantityAspectEditor.class);
        editorMap.put(Password.class, PasswordAspectEditor.class);
        editorMap.put(File.class, FileAspectEditor.class);
        editorMap.put(HexString.class, HexStringAspectEditor.class);
    }

    /**
     * Creates a new instance of AspectEditorManager
     */
    private AspectEditorManager() {
    }

    public static void put(Class targetClass, Class editorClass) {
        editorMap.put(targetClass, editorClass);
    }

    public static AspectEditor getEditor(Object model, PropertyDescriptor descriptor) {
        return getEditor(model, descriptor, false);
    }

    public static AspectEditor getEditor(Object model, PropertyDescriptor descriptor, boolean readOnly) {
        return getEditor(model, descriptor, readOnly, true);
    }

    public static AspectEditor getEditor(Object model, PropertyDescriptor descriptor, boolean readOnly, boolean translate) {
        Class aspectType = descriptor.getPropertyType();
        return getEditor(aspectType, model, descriptor, readOnly, translate, false);
    }

    public static AspectEditor getEditor(Object model, PropertyDescriptor descriptor, boolean readOnly, boolean translate, boolean customTranslate) {
        Class aspectType = descriptor.getPropertyType();
        return getEditor(aspectType, model, descriptor, readOnly, translate, customTranslate);
    }

    // Called via reflection from e.g. com.energyict.cpo.BasicPropertySpec
    public static AspectEditor getDynamicEditor(Object model, PropertyDescriptor descriptor) {
        return getEditor(model, descriptor, false, false, true);
    }

    // Called via reflection from e.g. com.energyict.cpo.BasicPropertySpec
    public static AspectEditor getLargeStringEditor (Object model, PropertyDescriptor descriptor) {
        AspectEditor editor = new LargeStringAspectEditor();
        init(editor, model, descriptor, false, false, true);
        return editor;
    }

    // Called via reflection from e.g. com.energyict.cpo.BasicPropertySpec
    public static AspectEditor getEncryptedStringEditor(EncryptedStringAdapter model, PropertyDescriptor descriptor) {
        AspectEditor editor = new PasswordAspectEditor(model.isAuthorized(UserAction.PASSWORD_MANAGEMENT));
        init(editor, model, descriptor, false, false, true);
        return editor;
    }

    public static AspectEditor getEditor(Object model, String aspect) {
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(aspect, model.getClass());
            return getEditor(model, descriptor);
        } catch (IntrospectionException ex) {
            throw new ApplicationException(ex);
        }
    }

    public static AspectEditor getEditor(Class aspectType, Object model, String aspect) {
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(aspect, model.getClass());
            return getEditor(aspectType, model, descriptor, false, true, false);
        } catch (IntrospectionException ex) {
            throw new ApplicationException(ex);
        }
    }

    public static AspectEditor getEditor(Class aspectType, Object model, PropertyDescriptor descriptor, boolean readOnly, boolean translate, boolean customTranslate) {
        AspectEditor editor;
        Class editorClass = editorMap.get(aspectType);
        if (editorClass == null) {
            editor = createEditor(aspectType);
            init(editor, model, descriptor, readOnly, translate, customTranslate);
            return editor;
        }
        try {
            editor = (AspectEditor) editorClass.newInstance();
            init(editor, model, descriptor, readOnly, translate, customTranslate);
            return editor;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new ApplicationException(ex);
        }
    }

    private static AspectEditor createEditor (Class aspectType) {
        AspectEditor editor;
        if (aspectType.isEnum()) {
            // The LocalizedEnumListCellRenderer works by showing the translation of the following key:
            //    simple ClassName of the Enum (with the first character in lower case)
            //    followed by a '.'
            //    followed by the toString() representation of the Enum value
            // eg. key 'connectionStrategy.AS_SOON_AS_POSSIBLE' has english translation "As soon as possible"
            // Remark: I don't want to apply this renderer for all Enum cases yet (the ideal solution)
            // because I'm not sure that all Enum values have that exact corresponding translation key
            // So, this condition can be extended whenever needed
            boolean enumRendererWanted = LocalizableEnum.class.isAssignableFrom(aspectType);
            if (enumRendererWanted) {
                LocalizedEnumListCellRenderer renderer = new LocalizedEnumListCellRenderer();
                renderer.setClassNameAsPrefix(true);
                editor = new EnumAspectEditor(aspectType, renderer);
            } else {
                editor = new EnumAspectEditor(aspectType);
            }
        } else {
            editor = new StringAspectEditor();
        }
        return editor;
    }

    public static void init (AspectEditor editor, Object model, PropertyDescriptor descriptor, boolean readOnly, boolean translate, boolean customTranslate) {
        editor.setCustomTranslate(customTranslate);
        editor.setTranslate(translate);
        if (readOnly) {
            editor.setForceReadOnly(readOnly);
        }
        editor.init(model, descriptor);
    }

}