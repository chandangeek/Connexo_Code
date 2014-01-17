package com.energyict.dynamicattributes;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DataVault;
import com.energyict.mdc.common.DataVaultProvider;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

/**
 * Copyrights EnergyICT
 * Date: 13-sep-2010
 * Time: 15:52:11
 */
public class PasswordFactory extends AbstractValueFactory<Password> {

    private final DataVault dataVault;

    public PasswordFactory() {
        dataVault = DataVaultProvider.instance.get().getKeyVault();
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getPasswordEditor", model, attType, aspect);
    }

    public String getDbType() {
        return "varchar2(4000)";
    }

    public Password valueFromDb(Object object, ValueDomain domain) {
        return this.valueFromDb((String) object);
    }

    private Password valueFromDb(String encodedString) {
        return new Password(new String(dataVault.decrypt(encodedString)));
    }

    public Object valueToDb(Password password) {
        if (password == null) {
            return null;
        }
        else {
            return this.encrypt(password);
        }
    }

    private String encrypt (Password password) {
        String value = password.getValue();
        if (value != null) {
            return dataVault.encrypt(value.getBytes());
        }
        else {
            return null;
        }
    }

    public Password valueFromWS(Object object, ValueDomain domain) throws BusinessException {
        return (object != null) ? new Password((String) object) : null;
    }

    public Object valueToWS(Password object) {
        return (object != null) ? object.getValue() : null;
    }

    public Class<Password> getValueType() {
        return Password.class;
    }

    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    protected String doGetHtmlString(Password object) {
        String result = "<input type=\"password\" ";
        if (object != null) {
            result += "value=\"" + object.getValue() + "\" ";
        }
        result += "disabled=\"true\" >";
        return result;
    }

    public boolean isStringLike() {
        return true;
    }

    @Override
    public Password fromStringValue(String stringValue, ValueDomain domain) {
        return this.valueFromDb(stringValue);
    }

    @Override
    public String toStringValue(Password object) {
        if (object == null || !(object instanceof Password)) {
            return null;
        }
        else {
            return this.encrypt(object);
        }
    }

}