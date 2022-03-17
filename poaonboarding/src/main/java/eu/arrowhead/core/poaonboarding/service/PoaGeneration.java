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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class PoaGeneration {
	
	//=================================================================================================
	// members

	private static final Logger logger = LogManager.getLogger(PoaGeneration.class);

	private static final String POA_CONTENT_TYPE = "JWT";
	private static final String POA_ALG = "RS256";
	private static final String TRANSFERABLE = "Transferable";
	private static final String PRINCIPAL_PUBLIC_KEY = "Principal public key";
	private static final String AGENT_PUBLIC_KEY = "Agent public key";
	private static final String METADATA = "metadata";
	private static final int TTL_MINUTES = 10;

	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String systemName;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String generatePoa(final X509Certificate agentCert) throws JoseException {
		final JsonWebSignature jws = new JsonWebSignature(); 
		final PublicKey principalPublicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		final PrivateKey privateKey = (PrivateKey) arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY);

		final String agentName = getCommonName(agentCert);
		final PublicKey agentPublicKey = agentCert.getPublicKey();
		final JwtClaims claims = generateClaims(principalPublicKey, agentPublicKey, agentName);
		jws.setAlgorithmHeaderValue(POA_ALG);
		jws.setContentTypeHeaderValue(POA_CONTENT_TYPE);
		jws.setPayload(claims.toJson());
		jws.setKey(privateKey);
		
		return jws.getCompactSerialization();
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private JwtClaims generateClaims(
		final PublicKey principalPublicKey,
		final PublicKey agentPublicKey,
		final String agentName
	) {
		final JwtClaims claims = new JwtClaims();

		claims.setIssuedAtToNow();
		claims.setExpirationTimeMinutesInTheFuture(TTL_MINUTES);
		claims.setStringClaim(PRINCIPAL_PUBLIC_KEY, principalPublicKey.toString());
		claims.setStringClaim(AGENT_PUBLIC_KEY, agentPublicKey.toString());
		claims.setClaim(TRANSFERABLE, 0);
		claims.setClaim(METADATA, generateMetadata(agentName));
		
		return claims;
	}

	private Map<String, String> generateMetadata(final String agentName) {
		return Map.of(
				"Principal name", systemName,
				"Agent name", agentName,
				"Credentials", "IoT device submission");
	}

	// -------------------------------------------------------------------------------------------------
	private static String getCommonName(final X509Certificate certificate) {
		try {
			final X500Name subject = new JcaX509CertificateHolder(certificate).getSubject();
			return getCommonName(subject);
		} catch (CertificateEncodingException e) {
			logger.error("Cannot get common name from cert, because: " + e.getMessage());
			throw new InvalidParameterException("Cannot get common name from cert.", e);
		}
	}

	// -------------------------------------------------------------------------------------------------
	private static String getCommonName(final X500Name name) {
		final RDN[] rdns = name.getRDNs(BCStyle.CN);
		if (rdns.length == 0) {
			throw new InvalidParameterException("No common name");
		}
		final RDN cn = rdns[0];
		return IETFUtils.valueToString(cn.getFirst().getValue());
	}

}
