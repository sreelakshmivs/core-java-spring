/********************************************************************************
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.poaonboarding.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.Subcontractor;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.poaonboarding.database.service.SubcontractorDBService;

@Service
public class PoaGenerator {

	//=================================================================================================
	// members

	private static final Logger logger = LogManager.getLogger(PoaGenerator.class);

	private static final String POA_CONTENT_TYPE = "JWT";
	private static final String POA_ALG = "RS256";
	private static final String TRANSFERABLE = "transferable";
	private static final String PRINCIPAL_PUBLIC_KEY = "principalPublicKey";
	private static final String AGENT_PUBLIC_KEY = "agentPublicKey";
	private static final String METADATA = "metadata";
	private static final String DESTINATION_NETWORK_ID = "destinationNetworkId";
	private static final String PRINCIPAL_NAME = "principalName";
	private static final String AGENT_NAME = "agentName";
	private static final String CREDENTIALS = "credentials";
	private static final String IOT_DEVICE_SUBMISSION = "IoT device submission";

	private static final int TTL_MINUTES = 10;

	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String systemName;

	@Value(CoreCommonConstants.$POA_ONBOARDING_NETWORK_NAME)
	private String networkName;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	@Autowired
	SubcontractorDBService subcontractorDBService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String generatePoa(final X509Certificate agentCert) {
		final JsonWebSignature jws = new JsonWebSignature();
		final PrivateKey privateKey = (PrivateKey) arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY);

		final String agentCommonName = getCommonName(agentCert);
		final String agentName = agentCommonName.split("\\.", 2)[0];
		final PublicKey agentPublicKey = agentCert.getPublicKey();
		final String agentPublicKeyEncoded = Base64.getEncoder().encodeToString(agentPublicKey.getEncoded());

		final PublicKey principalPublicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		final String principalPublicKeyEncoded = Base64.getEncoder().encodeToString(principalPublicKey.getEncoded());

		validateSubcontractor(agentName, agentPublicKeyEncoded);

		final JwtClaims claims = generateClaims(principalPublicKeyEncoded, agentPublicKeyEncoded, agentName);

		jws.setAlgorithmHeaderValue(POA_ALG);
		jws.setContentTypeHeaderValue(POA_CONTENT_TYPE);
		jws.setPayload(claims.toJson());
		jws.setKey(privateKey);

		try {
			return jws.getCompactSerialization();
		} catch (final JoseException e) {
			final String errorMessage = "Failed to generate PoA";
			logger.error(errorMessage, e);
			throw new ArrowheadException(errorMessage);
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void validateSubcontractor(final String subcontractorName, final String subcontractorPublicKey) {
		final Subcontractor subcontractor = subcontractorDBService.getSubcontractorByName(subcontractorName);

		if (subcontractor.hasExpired()) {
			throw new AuthException("The subcontractor's onboarding rights have expired");
		}
		
		final String publicKeyFromDatabase = subcontractor.getPublicKey();
		final boolean hasCorrectPublicKey = publicKeyFromDatabase.equals(subcontractorPublicKey);

		if (!hasCorrectPublicKey) {
			throw new AuthException("Incorrect subcontractor publickey");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private JwtClaims generateClaims(
		final String principalPublicKey,
		final String agentPublicKey,
		final String agentName
	) {
		final JwtClaims claims = new JwtClaims();

		claims.setIssuedAtToNow();
		claims.setExpirationTimeMinutesInTheFuture(TTL_MINUTES);
		claims.setStringClaim(PRINCIPAL_PUBLIC_KEY, principalPublicKey);
		claims.setStringClaim(AGENT_PUBLIC_KEY, agentPublicKey);
		claims.setClaim(DESTINATION_NETWORK_ID, networkName);
		claims.setClaim(TRANSFERABLE, 0);
		claims.setClaim(METADATA, generateMetadata(agentName));

		return claims;
	}

	private Map<String, String> generateMetadata(final String agentName) {
		return Map.of(
				PRINCIPAL_NAME, systemName.toLowerCase(),
				AGENT_NAME, agentName,
				CREDENTIALS, IOT_DEVICE_SUBMISSION);
	}

	//-------------------------------------------------------------------------------------------------
	private static String getCommonName(final X509Certificate certificate) {
		try {
			final X500Name subject = new JcaX509CertificateHolder(certificate).getSubject();
			return getCommonName(subject);
		} catch (CertificateEncodingException e) {
			logger.error("Cannot get common name from cert, because: " + e.getMessage());
			throw new InvalidParameterException("Cannot get common name from cert.", e);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private static String getCommonName(final X500Name name) {
		final RDN[] rdns = name.getRDNs(BCStyle.CN);
		if (rdns.length == 0) {
			throw new InvalidParameterException("No common name");
		}
		final RDN cn = rdns[0];
		return IETFUtils.valueToString(cn.getFirst().getValue());
	}

}
