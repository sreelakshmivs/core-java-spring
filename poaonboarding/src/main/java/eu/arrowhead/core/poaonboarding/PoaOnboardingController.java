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

import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.PoaOnboardRequestDTO;
import eu.arrowhead.common.dto.internal.PoaOnboardingResponseDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.core.poaonboarding.database.service.SubcontractorDBService;
import eu.arrowhead.core.poaonboarding.service.PoaGenerator;
import eu.arrowhead.core.poaonboarding.service.PoaOnboardingService;
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

	@Autowired
	private PoaGenerator poaGenerator;

	@Autowired
	private PoaOnboardingService onboardingService;

	@Autowired
	SubcontractorDBService subcontractorDBService;

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
	@ApiOperation(value = "Return a Power of Attorney allowing a subcontractor to onboard devices", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ONBOARDING_POA_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = POA_URI)
	public String issueOnboardingPoa(final HttpServletRequest request) {
		final X509Certificate requesterCert = getCertificate(request);
		logger.debug("PoA request received");
		return poaGenerator.generatePoa(requesterCert);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Onboards the device", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = ONBOARD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@Validated
	@PostMapping(path = CommonConstants.OP_POA_ONBOARDING_WITH_NAME, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PoaOnboardingResponseDTO onboardWithName(final HttpServletRequest request, @Valid @RequestBody final PoaOnboardRequestDTO body) {
		logger.debug("'Onboard with name' request received");
		final String host = request.getRemoteHost();
        final String address = request.getRemoteAddr();
		return onboardingService.onboardWithName(body, host, address);
	}

	@ApiOperation(value = "Onboards the device", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = ONBOARD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@Validated
	@PostMapping(path = CommonConstants.OP_POA_ONBOARDING_WITH_CSR, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PoaOnboardingResponseDTO onboardWithCsr(final HttpServletRequest request, @Valid @RequestBody final PoaOnboardRequestDTO body) {
		final X509Certificate requesterCert = getCertificate(request);
		logger.debug("'Onboard with CSR' request received");

		// TODO: Implement!
		// Compare the public key in the certificate with the public key of the PoA's agent.
		throw new UnsupportedOperationException("Not implemented");
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private X509Certificate getCertificate(final HttpServletRequest request) {
		final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(CommonConstants.ATTR_JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);
		if (certificates == null || certificates.length == 0) {
			throw new AuthException("Client certificate is needed!");
		}
		return certificates[0];
	}

}
