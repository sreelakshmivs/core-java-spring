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

package eu.arrowhead.core.poaonboarding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpStatus;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.poaonboarding.service.PoaGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@RestController
@RequestMapping(CommonConstants.POA_ONBOARDING_URI)
public class PoaOnboardingController {

	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(PoaOnboardingController.class);

	private static final String GET_ONBOARDING_POA_HTTP_200_MESSAGE = "PoA for device onboarding returned";
	private static final String ONBOARD_HTTP_200_MESSAGE = "Device onboarding successful"; // TODO: Change this formulation

	private static final String POA_URI = "/poa";
	private static final String ONBOARD_URI = "/onboard";

	@Autowired
	private PoaGenerator poaGenerator;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return a Power of Attorney for onboarding devices", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ONBOARDING_POA_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = POA_URI)
	public String onboardingPoa(final HttpServletRequest request) {
		final X509Certificate requesterCert = getCertificate(request);

		try {
			return poaGenerator.generatePoa(requesterCert);
		} catch (final JoseException ex) {
			logger.error("Failed to generate PoA", ex);
			throw new ArrowheadException("Failed to generate PoA");
		}
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Onboards the device", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = ONBOARD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = ONBOARD_URI)
	public String onboard(final HttpServletRequest request, @RequestBody Map<String, String> body) { // TODO: Change type of body, create DTO class
		final X509Certificate certificate = getCertificate(request);
		final PublicKey requesterPublicKey = certificate.getPublicKey();
		final String poa = body.get("poa");
		if (!allowedToOnboard(requesterPublicKey, poa)) {
			throw new BadPayloadException("Principal public key does not match PoA signature");
		}
		// TODO: Onboard
		return "OK";
	}

	//=================================================================================================
    // assistant methods

	//-------------------------------------------------------------------------------------------------
	private boolean allowedToOnboard(final PublicKey requesterPublicKey, final String poa) {
		final PublicKey subcontractorPublicKey = getPrincipalPublicKey(poa);

		// TODO: Ensure that this key belongs to an authorised subcontractor!

		final Claims claims = getValidatedClaims(poa, subcontractorPublicKey);
		System.out.println(claims.get("agentPublicKey", String.class));
		// TODO: Check agent public key
		final String agentPublicKeyString = claims.get("agentPublicKey", String.class);
		final PublicKey agentPublicKey = (PublicKey) getKey(agentPublicKeyString);
		return agentPublicKey.equals(requesterPublicKey);
	}

	//-------------------------------------------------------------------------------------------------
	private Claims getValidatedClaims(final String poa, final PublicKey signingKey) {
		try {
			return Jwts.parser()
			.setSigningKey(signingKey)
			.parseClaimsJws(poa)
			.getBody();
		} catch (final SignatureException e) {
			// TODO: Log error
			throw new BadPayloadException("Principal public key does not match PoA signature");
		}

	}

	//-------------------------------------------------------------------------------------------------
	private PublicKey getPrincipalPublicKey(final String poa) {
		final String[] splitToken = poa.split("\\.");
		final String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

		final Jwt<?, ?> jwt = Jwts.parser().parse(unsignedToken);
		final Claims claims = (Claims) jwt.getBody();
		final String keyString = claims.get("principalPublicKey", String.class);
		return (PublicKey) getKey(keyString);
	}

	//-------------------------------------------------------------------------------------------------
	private X509Certificate getCertificate(final HttpServletRequest request) {
		final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);
		if (certificates == null || certificates.length == 0) {
			throw new AuthException("Client certificate is needed!");
		}
		return certificates[0];
	}

	//-------------------------------------------------------------------------------------------------
	public static Key getKey(final String keyString) {
		try {
			final byte[] keyBytes = Base64.getDecoder().decode(keyString);
			final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			// TODO: Actual exception handling
			e.printStackTrace();
			return null;
		}
	}

}
