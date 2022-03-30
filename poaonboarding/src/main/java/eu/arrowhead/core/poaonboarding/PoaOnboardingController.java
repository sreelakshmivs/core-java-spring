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
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;
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
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.poaonboarding.service.PoaGeneration;
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
	private PoaGeneration poaGeneration;

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
			final String poa = poaGeneration.generatePoa(requesterCert);
			prettyPrintToken(poa);
			return poa;
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
		public String onboard(final HttpServletRequest request, @RequestBody Object body) { // TODO: Change type of body
			// TODO: Implement
			return "OK";
		}

	// -------------------------------------------------------------------------------------------------
	private void prettyPrintToken(String jwtToken) { // TODO: Remove this method.
		System.out.println("------------ Decode JWT ------------");
		String[] split_string = jwtToken.split("\\.");
		String base64EncodedHeader = split_string[0];
		String base64EncodedBody = split_string[1];
		String base64EncodedSignature = split_string[2];

		System.out.println("~~~~~~~~~ JWT Header ~~~~~~~~");
		Base64 base64Url = new Base64(true);
		String header = new String(base64Url.decode(base64EncodedHeader));
		System.out.println("JWT Header : " + header);


		System.out.println("~~~~~~~~~ JWT Body ~~~~~~~~~~");
		String body = new String(base64Url.decode(base64EncodedBody));
		System.out.println("JWT Body : " + body);

		System.out.println("~~~~~~~~~ JWT Signature ~~~~~");
		String signature = new String(base64Url.decode(base64EncodedSignature));
		System.out.println("JWT Signature : " + signature);
	}

	// -------------------------------------------------------------------------------------------------
	private X509Certificate getCertificate(final HttpServletRequest request) {
		final X509Certificate[] clientCerts =
				(X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
		if (clientCerts == null || clientCerts.length < 1) {
			throw new InvalidParameterException("Invalid client certificate");
		}
		return clientCerts[0];
	}

}
