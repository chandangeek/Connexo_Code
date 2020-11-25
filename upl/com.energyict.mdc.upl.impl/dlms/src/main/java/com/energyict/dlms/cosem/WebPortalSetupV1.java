package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.attributes.DLMSClassAttributes;
import com.energyict.dlms.cosem.methods.DLMSClassMethods;
import com.energyict.dlms.cosem.methods.NetworkInterfaceType;
import com.energyict.dlms.cosem.methods.WebPortalMethods;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.DataParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Web portal configuration IC, V1.
 * 
 * Ref : https://confluence.eict.vpdc/display/G3IntBeacon3100/Configure+the+web+portal
 * 
 * @author alex
 */
public final class WebPortalSetupV1 extends AbstractCosemObject {
	
	/**
	 * Enumerate the attributes of this IC.
	 * 
	 * @author alex
	 */
	private enum WebPortalSetupV1Attribute implements DLMSClassAttributes {
		
		/** Attributes. */
		USER_ROLE_MAPPINGS(2),
		USER_PASSWORD_MAPPINGS(3),
		HTTP_PORT(4),
		HTTPS_PORT(5),
		GZIP_ENABLED(6),
		SSL_ENABLED(7),
		AUTHENTICATION_MECHANISM(8),
		MAX_LOGIN_ATTEMPTS(9),
		LOCKOUT_DURATION(10),
		ENABLED_INTERFACES(11),
		CLIENT_CERTIFICATES(12);
		
		/** The attribute ID. */
		private final int attributeId;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	attributeId		The attribute ID.
		 */
		private WebPortalSetupV1Attribute(final int attributeId) {
			this.attributeId = attributeId;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final DLMSClassId getDlmsClassId() {
			return DLMSClassId.WEB_PORTAL_CONFIGURATION;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final int getShortName() {
			return -1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final int getAttributeNumber() {
			return this.attributeId;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final DLMSAttribute getDLMSAttribute(final ObisCode obisCode) {
			return new DLMSAttribute(obisCode, this);
		}
	}
	
	/**
	 * Methods defined on the IC.
	 * 
	 * @author alex
	 */
	private enum WebportalSetupV1Method implements DLMSClassMethods {
		
		/** Methods. */
		CHANGE_USER_NAME(1),
		CHANGE_USER_PASSWORD(2),
		ENABLE_GZIP(3),
		ENABLE_SSL(4),
		IMPORT_CLIENT_CERTIFICATE(5),
		REMOVE_CLIENT_CERTIFICATE(6);
		
		/** The method ID. */
		private final int methodId;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	methodId	The ID of the method.
		 */
		private WebportalSetupV1Method(final int methodId) {
			this.methodId = methodId;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final DLMSClassId getDlmsClassId() {
			return DLMSClassId.WEB_PORTAL_CONFIGURATION;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final int getShortName() {
			return -1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final int getMethodNumber() {
			return this.methodId;
		}
	}
	
	/**
	 * Web portal roles.
	 * 
	 * @author alex
	 */
	public enum Role {
		ADMIN("admin"),
		READ_WRITE("readonly"),
		READ_ONLY("readwrite");
		
		/**
		 * Returns the role that matches the given role name.
		 * 
		 * @param 	name	The name of the role.
		 * 
		 * @return	The matching {@link Role}, <code>null</code> if none matches.
		 */
		public static final Role forName(final String name) {
			for (final Role role : Role.values()) {
				if (role.getName().equalsIgnoreCase(name)) {
					return role;
				}
			}
			
			return null;
		}
		
		/**
		 * Returns the different names of the roles.
		 * 
		 * @return	The names of the roles.
		 */
		public static final String[] names() {
			final List<String> names = new ArrayList<>();
			
			for (final Role role : Role.values()) {
				names.add(role.getName());
			}
			
			return names.toArray(new String[names.size()]);
		}
		
		/** Role name. */
		private final String name;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	name		The name of the role.
		 */
		private Role(final String name) {
			this.name = name;
		}
		
		/**
		 * Returns the role name.
		 * 
		 * @return	The role name.
		 */
		public final String getName() {
			return this.name;
		}
	}
	
	/**
	 * Enumerate the authentication mechanisms.
	 * 
	 * @author alex
	 */
	public enum WebPortalAuthenticationMechanism {
		NONE(0),
		PASSWORD(1),
		CERTIFICATE(2),
		CERTIFICATE_AND_PASSWORD(3);
		
		/**
		 * Returns the mechanism that corresponds to the given value.
		 * 
		 * @param 		value		The value.
		 * 
		 * @return		The corresponding {@link WebPortalAuthenticationMechanism}, <code>null</code> if none matches.
		 */
		public static final WebPortalAuthenticationMechanism forValue(final int value) {
			for (final WebPortalAuthenticationMechanism mechanism : WebPortalAuthenticationMechanism.values()) {
				if (mechanism.value == value) {
					return mechanism;
				}
			}
			
			return null;
		}
		
		/** The value. */
		private final int value;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	value	The value.
		 */
		private WebPortalAuthenticationMechanism(final int value) {
			this.value = value;
		}
	}
	
	/**
	 * Maps a {@link Role} to the configured user name.
	 * 
	 * @author alex
	 */
	public static final class UserRoleMapping {
		
		/**
		 * Converts the given {@link Structure} to a {@link UserRoleMapping}.
		 * 
		 * @param 	structure		The received {@link Structure}.
		 * 
		 * @return	The corresponding {@link UserRoleMapping}.
		 */
		private static final UserRoleMapping fromStructure(final Structure structure) throws DataParseException {
			if (structure.nrOfDataTypes() == 2) {
				final OctetString rolename = structure.getOctetString();
				final OctetString username = structure.getOctetString();
				
				if (username == null || rolename == null) {
					throw DataParseException.generalParseException(new RuntimeException("User name or role name is null !"));
				}
				
				return new UserRoleMapping(Role.forName(rolename.stringValue()), username.stringValue());
			} else {
				throw DataParseException.generalParseException(new RuntimeException("Expecting a structure of size 2, instead got a structure of size " + structure.nrOfDataTypes()));
			}
		}
		
		/** The role. */
		private final Role role;
		
		/** The user name that is mapped to it. */
		private final String username;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	role			The role.
		 * @param 	username		The user name.
		 */
		private UserRoleMapping(final Role role, final String username) {
			this.role = role;
			this.username = username;
		}

		/**
		 * Returns the role.
		 * 
		 * @return	The role.
		 */
		public final Role getRole() {
			return this.role;
		}

		/**
		 * Returns the user name.
		 * 
		 * @return	The user name.
		 */
		public final String getUsername() {
			return this.username;
		}
	}
	
	/**
	 * Maps a {@link Role} to the configured password.
	 * 
	 * @author alex
	 */
	public static final class UserPasswordMapping {
		
		/**
		 * Converts the given {@link Structure} to a {@link UserPasswordMapping}.
		 * 
		 * @param 	structure		The received {@link Structure}.
		 * 
		 * @return	The corresponding {@link UserRoleMapping}.
		 */
		private static final UserPasswordMapping fromStructure(final Structure structure) throws DataParseException {
			if (structure.nrOfDataTypes() == 2) {
				final OctetString rolename = structure.getOctetString();
				final OctetString password = structure.getOctetString();
				
				if (password == null || rolename == null) {
					throw DataParseException.generalParseException(new RuntimeException("User name or role name is null !"));
				}
				
				return new UserPasswordMapping(Role.forName(rolename.stringValue()), password.getOctetStr());
			} else {
				throw DataParseException.generalParseException(new RuntimeException("Expecting a structure of size 2, instead got a structure of size " + structure.nrOfDataTypes()));
			}
		}
		
		/** The role. */
		private final Role role;
		
		/** The password that is mapped to it. */
		private final byte[] password;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	role			The role.
		 * @param 	password		The password.
		 */
		private UserPasswordMapping(final Role role, final byte[] password) {
			this.role = role;
			this.password = password;
		}

		/**
		 * Returns the role.
		 * 
		 * @return	The role.
		 */
		public final Role getRole() {
			return this.role;
		}

		/**
		 * Returns the password.
		 * 
		 * @return	The password.
		 */
		public final byte[] getPassword() {
			return this.password;
		}
	}
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	protocolLink			The {@link ProtocolLink}.
	 * @param 	objectReference			The {@link ObjectReference}.
	 */
    public WebPortalSetupV1(final ProtocolLink protocolLink, final ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final int getClassId() {
		return DLMSClassId.WEB_PORTAL_CONFIGURATION.getClassId();
	}
	
	/**
	 * Returns the user names mapped to the defined roles.
	 * 
	 * @return	The user names mapped to the defined roles.
	 * 
	 * @throws	IOException		If an IO error occurs.
	 */
	public final UserRoleMapping[] getUserNames() throws IOException {
		final Array userRoleMappings = this.readDataType(WebPortalSetupV1Attribute.USER_ROLE_MAPPINGS, Array.class);
		final List<UserRoleMapping> mappings = new ArrayList<>();
		
		for (final AbstractDataType dataType : userRoleMappings) {
			if (dataType.isStructure()) {
				mappings.add(UserRoleMapping.fromStructure(dataType.getStructure()));
			}
		}
		
		return mappings.toArray(new UserRoleMapping[ mappings.size() ]);
	}
	
	/**
	 * Returns the user names mapped to the defined passwords.
	 * 
	 * @return	The user names mapped to the defined passwords.
	 * 
	 * @throws	IOException		If an IO error occurs.
	 */
	public final UserPasswordMapping[] getUserPasswords() throws IOException {
		final Array userRoleMappings = this.readDataType(WebPortalSetupV1Attribute.USER_PASSWORD_MAPPINGS, Array.class);
		final List<UserPasswordMapping> mappings = new ArrayList<>();
		
		for (final AbstractDataType dataType : userRoleMappings) {
			if (dataType.isStructure()) {
				mappings.add(UserPasswordMapping.fromStructure(dataType.getStructure()));
			}
		}
		
		return mappings.toArray(new UserPasswordMapping[ mappings.size() ]);
	}
	
	/**
	 * Returns the http port.
	 * 
	 * @return	The http port.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final int getHttpPort() throws IOException {
		return this.readDataType(WebPortalSetupV1Attribute.HTTP_PORT, Unsigned16.class)
				   .getValue();
	}
	
	/**
	 * Sets the HTTP port.
	 * 
	 * @param 	port	The new HTTP port.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final void setHttpPort(final int port) throws IOException {
		if (port > 0 && port <= 65535) {
			this.write(WebPortalSetupV1Attribute.HTTP_PORT, new Unsigned16(port));
		} else {
			throw new IllegalArgumentException("HTTP port should be between 1 and 65535, instead you specified " + port);
		}
	}
	
	/**
	 * Returns the https port.
	 * 
	 * @return	The https port.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final int getHttpsPort() throws IOException {
		return this.readDataType(WebPortalSetupV1Attribute.HTTPS_PORT, Unsigned16.class)
				   .getValue();
	}
	
	/**
	 * Sets the HTTPS port.
	 * 
	 * @param 	port	The new HTTPS port.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final void setHttpsPort(final int port) throws IOException {
		if (port > 0 && port <= 65535) {
			this.write(WebPortalSetupV1Attribute.HTTPS_PORT, new Unsigned16(port));
		} else {
			throw new IllegalArgumentException("HTTPS port should be between 1 and 65535, instead you specified " + port);
		}
	}
	
	/**
	 * Indicates whether or not GZIP compression is enabled or not.
	 * 
	 * @return	<code>true</code> if GZIP compression is enabled, <code>false</code> if not.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final boolean isGzipEnabled() throws IOException {
		return this.readDataType(WebPortalSetupV1Attribute.GZIP_ENABLED, BooleanObject.class)
				   .getState();			   
	}
	
	/**
	 * Enable / disable gzip compression.
	 * 	
	 * @param 	enabled			<code>true</code> for enabled, <code>false</code> for disabled.
	 * 
	 * @throws IOException		If an IO error occurs.
	 */
	public final void setGzipEnabled(final boolean enabled) throws IOException {
		this.methodInvoke(WebportalSetupV1Method.ENABLE_GZIP, enabled ? BooleanObject.TRUE : BooleanObject.FALSE);
	}
	
	/**
	 * Indicates whether or not SSL is enabled or not.
	 * 
	 * @return	<code>true</code> if SSL is enabled, <code>false</code> if not.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final boolean isSSLEnabled() throws IOException {
		return this.readDataType(WebPortalSetupV1Attribute.SSL_ENABLED, BooleanObject.class)
				   .getState();			   
	}
	
	/**
	 * Enable / disable SSL.
	 * 	
	 * @param 	enabled			<code>true</code> for enabled, <code>false</code> for disabled.
	 * 
	 * @throws IOException		If an IO error occurs.
	 */
	public final void setSSLEnabled(final boolean enabled) throws IOException {
		this.methodInvoke(WebportalSetupV1Method.ENABLE_SSL, enabled ? BooleanObject.TRUE : BooleanObject.FALSE);
	}
	
	/**
	 * Returns the configured authentication mechanism.
	 * 
	 * @return	The configured authentication mechanism.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final WebPortalAuthenticationMechanism getAuthenticationMechanism() throws IOException {
		return WebPortalAuthenticationMechanism.forValue(this.readDataType(WebPortalSetupV1Attribute.AUTHENTICATION_MECHANISM, TypeEnum.class).getValue());
	}
	
	/**
	 * Sets the web portal authentication mechanism.
	 * 
	 * @param 	mechanism		The mechanism.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final void setWebPortalAuthenticationMechanism(final WebPortalAuthenticationMechanism mechanism) throws IOException {
		this.write(WebPortalSetupV1Attribute.AUTHENTICATION_MECHANISM, new TypeEnum(mechanism.value));
	}
	
	/**
	 * Returns the maximum number of login attempts.
	 * 
	 * @return	The maximum number of login attempts.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final int getMaxLoginAttempts() throws IOException {
		return this.readDataType(WebPortalSetupV1Attribute.MAX_LOGIN_ATTEMPTS, Unsigned16.class)
				   .getValue();
	}
	
	/**
	 * Sets the max login attempts before lockout.
	 * 
	 * @param 	attempts		The maximum number of attempts before lockout.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final void setMaxLoginAttempts(final int attempts) throws IOException {
		if (attempts < 0 || attempts > 65535) {
			throw new IllegalArgumentException("Attempts should be between 0 and 65535, you specified " + attempts);
		}
		
		this.write(WebPortalSetupV1Attribute.MAX_LOGIN_ATTEMPTS, new Unsigned16(attempts));
	}
	
	/**
	 * Returns the lockout duration (milliseconds).
	 *  
	 * @return	The lockout duration in ms.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final long getLockoutDuration() throws IOException {
		return this.readDataType(WebPortalSetupV1Attribute.LOCKOUT_DURATION, Unsigned32.class)
				   .getValue();
	}
	
	/**
	 * Sets the lockout duration.
	 * 
	 * @param 		duration		The duration.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	public final void setLockoutDuration(final long duration) throws IOException {
		if (duration < 0 || duration > 0xFFFFFFFFL) {
			throw new IllegalArgumentException("Duration should be between 0 and 0xFFFFFFFF, you specified " + duration);
		}
		
		this.write(WebPortalSetupV1Attribute.LOCKOUT_DURATION, new Unsigned32(duration));
	}
	
	/**
	 * Returns the {@link Set} of interfaces on which the web interface is available.
	 * 
	 * @return	The {@link Set} of interfaces on which the web interface is available.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final Set<NetworkInterfaceType> getEnabledInterfaces() throws IOException {
		final Array enabledIfaceArray = this.readDataType(WebPortalSetupV1Attribute.ENABLED_INTERFACES, Array.class);
		final Set<NetworkInterfaceType> enabledIfaces = EnumSet.noneOf(NetworkInterfaceType.class);
		
		for (final AbstractDataType entry : enabledIfaceArray) {
			final NetworkInterfaceType correspondingInterface = NetworkInterfaceType.fromNetworkType(entry.getTypeEnum().getValue());
			enabledIfaces.add(correspondingInterface);
		}
		
		return enabledIfaces;
	}
	
	/**
	 * Sets the interfaces on which the web interface should be enabled.
	 * 
	 * @param 		interfaces		The set of interfaces.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	public final void setEnabledInterfaces(final Set<NetworkInterfaceType> interfaces) throws IOException {
		final Array interfaceArray = new Array();
		
		for (final NetworkInterfaceType iface : interfaces) {
			interfaceArray.addDataType(new TypeEnum(iface.getNetworkType()));
		}
		
		this.write(WebPortalSetupV1Attribute.ENABLED_INTERFACES, interfaceArray);
	}
	
	/**
	 * Returns the client certificates.
	 * 
	 * @return	The known client certificates.
	 * 
	 * @throws 	IOException					If an IO error occurs.
	 * @throws 	GeneralSecurityException	If a security error occurs.
	 */
	public final X509Certificate[] getClientCertificates() throws IOException, GeneralSecurityException {
		final Array certificateArray = this.readDataType(WebPortalSetupV1Attribute.CLIENT_CERTIFICATES, Array.class);
		final List<X509Certificate> clientCertificates = new ArrayList<>();
		
		for (final AbstractDataType entry : certificateArray) {
			final byte[] derEncodedCertificate = entry.getOctetString().getContentByteArray();
			final X509Certificate[] parsedCertificates = ParseUtils.parseCertificates(derEncodedCertificate);
			
			for (final X509Certificate cert : parsedCertificates) {
				clientCertificates.add(cert);
			}
		}
		
		return clientCertificates.toArray(new X509Certificate[ clientCertificates.size() ]);
	}
	
	/**
	 * Sets the user name mapped to a particular role.
	 * 
	 * @param 	role			The role.
	 * @param 	username		The new user name.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final void setUsername(final Role role, final String username) throws IOException {
		final Structure usernameData = new Structure();
		
		usernameData.addDataType(new OctetString(role.getName().getBytes(StandardCharsets.US_ASCII)));
		usernameData.addDataType(new OctetString(username.getBytes(StandardCharsets.US_ASCII)));
		
		this.methodInvoke(WebPortalMethods.CHANGE_USER_NAME, usernameData);
	}
	
	/**
	 * Sets the password for a particular role.
	 * 
	 * @param 	role			The role.
	 * @param 	password		The new password.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final void setPassword(final Role role, final byte[] password) throws IOException {
		final Structure passwordData = new Structure();
		
		passwordData.addDataType(new OctetString(role.getName().getBytes(StandardCharsets.US_ASCII)));
		passwordData.addDataType(new OctetString(password));
		
		final byte[] rawResponse = this.methodInvoke(WebPortalMethods.CHANGE_USER_PASSWORD, passwordData);
		
		if (rawResponse != null && rawResponse.length > 0) {
			AbstractDataType responseObj = AXDRDecoder.decode(rawResponse);
			if (responseObj.isBooleanObject()) {
				final BooleanObject response = responseObj.getBooleanObject();

				if (response.getState() == false) {
					throw new IOException("Password change was rejected by the device, please check if the password conforms to the instated policy.");
				}
			}
		}
	}
	
	/**
	 * Import a client certificate.
	 * 
	 * @param 		certificate		The new certificate.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	public final void importClientCertificate(final X509Certificate certificate) throws IOException, GeneralSecurityException {
		this.methodInvoke(WebPortalMethods.IMPORT_CLIENT_CERTIFICATE, new OctetString(ParseUtils.toDER(certificate)));
	}
	
	/**
	 * Removes a client certificate.
	 * 
	 * @param 		serialNumber	The serial number of the certificate.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	public final void removeClientCertificate(final byte[] serialNumber) throws IOException {
		this.methodInvoke(WebPortalMethods.REMOVE_CLIENT_CERTIFICATE, new OctetString(serialNumber));
	}
}
