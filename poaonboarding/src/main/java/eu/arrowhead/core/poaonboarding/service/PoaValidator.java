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

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

@Service
public class PoaValidator {

	//=================================================================================================
	// members

	private static final Logger logger = LogManager.getLogger(PoaValidator.class);

	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String systemName;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	//=================================================================================================
	// methods

	public Claims parsePoa(final PublicKey requesterPublicKey, final String poa) {
		final PublicKey subcontractorPublicKey = getPrincipalPublicKey(poa);

		// TODO: Ensure that this key belongs to an authorised subcontractor!
		// TODO: Ensure that the subcontractor's onboarding powers have not
		// expired.

		final Claims claims = getValidatedClaims(poa, subcontractorPublicKey);
		final String agentPublicKeyString = claims.get("agentPublicKey", String.class);
		final PublicKey agentPublicKey = getPublicKey(agentPublicKeyString);
		if (!agentPublicKey.equals(requesterPublicKey)) {
			throw new ArrowheadException("Invalid PoA or public key");
		}
		return claims;
	}


	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Claims getValidatedClaims(final String poa, final PublicKey signingKey) {
		try {
			return Jwts.parser()
			.setSigningKey(signingKey)
			.parseClaimsJws(poa)
			.getBody();
		} catch (final SignatureException e) {
			logger.error(e);
			throw new BadPayloadException("PoA signature could not be verified");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private PublicKey getPrincipalPublicKey(final String poa) {
		final String[] splitToken = poa.split("\\.");
		final String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";
		final Jwt<?, ?> jwt = Jwts.parser().parse(unsignedToken);
		final Claims claims = (Claims) jwt.getBody();
		final String keyString = claims.get("principalPublicKey", String.class);
		return getPublicKey(keyString);
	}

	//-------------------------------------------------------------------------------------------------
	public static PublicKey getPublicKey(final String keyString) {
		try {
			final byte[] keyBytes = Base64.getDecoder().decode(keyString);
			final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			// TODO: Actual exception handling
			logger.error("Could not generate public key from string", e);
			return null;
		}
	}

}
