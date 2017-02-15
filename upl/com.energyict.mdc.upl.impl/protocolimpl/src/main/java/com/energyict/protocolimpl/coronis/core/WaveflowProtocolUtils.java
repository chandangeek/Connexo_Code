package com.energyict.protocolimpl.coronis.core;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class WaveflowProtocolUtils {

	public static int toInt(final byte val) {
		return ((int)val & 0xff);
	}

	public static int toInt(final short val) {
		return ((int)val & 0xffff);
	}

	public static String toHexString(final byte val) {
		return toHexString(toInt(val));
	}

	public static String toHexString(final short val) {
		return "0x"+ Integer.toHexString(toInt(val));
	}

	public static String toHexString(final int val) {
		return "0x"+ Integer.toHexString(val);
	}

	public static String toHexString(final long val) {
		return "0x"+ Long.toHexString(val);
	}

    /**
     * returns a sub array from index to end
     * @param data source array
     * @param offset from index
     * @return subarray
     */
    public static byte[] getSubArray(final byte[] data, final int offset) {
        byte[] subArray = new byte[data.length-offset];
        System.arraycopy(data, offset, subArray, 0, subArray.length);
        return subArray;
    }

    /**
     * returns a sub array from index to end
     * @param data source array
     * @param offset from index
     * @param length
     * @return subarray
     */
    public static byte[] getSubArray(final byte[] data, final int offset, final int length) {
        byte[] subArray = new byte[length];
        System.arraycopy(data, offset, subArray, 0, subArray.length);
        return subArray;
    }

    public static int parseInt(String value) throws IOException {
		try {
			return Integer.parseInt(value, 10);
		}
		catch(NumberFormatException e) {
			try {
				if (value.toUpperCase().indexOf("0X") == 0) {
					value = value.substring(2);
				}
				return Integer.parseInt(value, 16);
			}
			catch(NumberFormatException ex) {
				throw new IOException("Number format error. Cannot parse ["+value+"] to int!");
			}
		}
    }

    public static byte[] getArrayFromStringHexNotation(String str) throws IOException {
		if ((str.length()%2) != 0) {
			throw new IOException("Invalid string to parse to byte array ["+str+"]");
		}

		byte[] array = new byte[str.length()/2];
		for (int i=0;i<array.length;i++) {
			array[i] = (byte)(Integer.parseInt(str.substring(i * 2, (i * 2) + 2), 16) & 0xFF);
		}
		return array;
	}

	public static PropertySpec propertySpec(String name, boolean required, String displayName, String description) {
        return new WaveflowPropertySpec(name, required, displayName, description);
    }

	// Hide utility class constructor
	private WaveflowProtocolUtils() {}

    private static class WaveflowPropertySpec implements PropertySpec {
        private final String name;
        private final boolean required;
        private final String displayName;
        private final String description;

        private WaveflowPropertySpec(String name, boolean required, String displayName, String description) {
            this.name = name;
            this.required = required;
            this.displayName = displayName;
            this.description = description;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getDisplayName() {
            return this.displayName;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public boolean isRequired() {
            return this.required;
        }

        @Override
        public boolean validateValue(Object value) throws PropertyValidationException {
            if (this.isRequired() && value == null) {
                throw MissingPropertyException.forName(this.getName());
            } else if (value instanceof String) {
                try {
                    getArrayFromStringHexNotation((String) value);
                    return true;
                } catch (IOException e) {
                    throw InvalidPropertyException.forNameAndValue(this.getName(), value, e);
                }
            } else {
                throw InvalidPropertyException.forNameAndValue(this.getName(), value);
            }
        }

        @Override
        public Optional<?> getDefaultValue() {
            return Optional.empty();
        }

        @Override
        public PropertySpecPossibleValues getPossibleValues() {
            return new NoPossibleValues();
        }

        @Override
        public boolean supportsMultiValues() {
            return false;
        }

        @Override
        public com.energyict.mdc.upl.properties.ValueFactory getValueFactory() {
            return new ValueFactory();
        }
    }

    private static class NoPossibleValues implements PropertySpecPossibleValues {
        @Override
        public PropertySelectionMode getSelectionMode() {
            return PropertySelectionMode.NONE;
        }

        @Override
        public List<?> getAllValues() {
            return Collections.emptyList();
        }

        @Override
        public boolean isExhaustive() {
            return true;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public Object getDefault() {
            return null;
        }
    }

    static class ValueFactory implements com.energyict.mdc.upl.properties.ValueFactory {
        @Override
        public Object fromStringValue(String stringValue) {
            return stringValue;
        }

        @Override
        public String toStringValue(Object object) {
            return String.valueOf(object);
        }

        @Override
        public String getValueTypeName() {
            return String.class.getName();
        }

        @Override
        public Object valueToDatabase(Object object) {
            return this.toStringValue(object);
        }

        @Override
        public Object valueFromDatabase(Object databaseValue) {
            return this.fromStringValue((String) databaseValue);
        }
    }

}