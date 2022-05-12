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

import java.net.URI;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.drivers.CertificateAuthorityDriver;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.dto.internal.PoaOnboardRequestDTO;
import eu.arrowhead.common.dto.internal.PoaOnboardingResponseDTO;
import eu.arrowhead.common.dto.shared.CertificateType;
import eu.arrowhead.common.dto.shared.ServiceEndpoint;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import io.jsonwebtoken.Claims;

@Service
public class PoaOnboardingService {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(PoaOnboardingService.class);

	@Autowired
	private DriverUtilities driverUtilities;

	@Autowired
	private SecurityUtilities securityUtilities;

	@Autowired
	private CertificateAuthorityDriver caDriver;

	@Autowired
	private PoaValidator poaValidator;

	//=================================================================================================
	// methods

	public PoaOnboardingResponseDTO onboardWithName(
		final PoaOnboardRequestDTO request,
		final String host,
		final String address
	) {

		// TODO: We don't actually need both the requester public key, since we have the keypair.
		final Claims claims = poaValidator.parsePoa(request.getPoa());
		final Map<String, String> metadata = claims.get("metadata", Map.class);
		final String name = metadata.get("agentName");

		// TODO: Check that the keys match?

		final KeyPair keyPair = securityUtilities.extractKeyPair(request.getKeyPair());
		final CertificateSigningResponseDTO csrResponse = sendCsrRequest(name, host, address, keyPair); // TODO: Error handling
	
		return new PoaOnboardingResponseDTO(
			findServiceEndpoint(CoreSystemService.DEVICEREGISTRY_ONBOARDING_WITH_CSR_SERVICE),
			findServiceEndpoint(CoreSystemService.SYSTEMREGISTRY_ONBOARDING_WITH_NAME_SERVICE),
			findServiceEndpoint(CoreSystemService.SERVICEREGISTRY_REGISTER_SERVICE),
			findServiceEndpoint(CoreSystemService.ORCHESTRATION_SERVICE),
			csrResponse.getCertificateChain()
		);
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private CertificateSigningResponseDTO sendCsrRequest(final String commonName, final String host, final String address, final KeyPair keyPair) {
		final String csr;
		try {
            csr = securityUtilities.createCertificateSigningRequest(commonName, keyPair, CertificateType.AH_ONBOARDING, host, address);
        } catch (final Exception e) {
            logger.error(e);
            throw new ArrowheadException("Unable to create certificate signing request");
        }

		final CertificateSigningRequestDTO csrDTO = new CertificateSigningRequestDTO(csr);
		return caDriver.signCertificate(csrDTO);
	}

	// -------------------------------------------------------------------------------------------------
	private ServiceEndpoint findServiceEndpoint(final CoreSystemService coreSystemService) {
		URI uri = null;
		try {
			final ServiceRegistryResponseDTO entry =
					driverUtilities.findByServiceRegistry(coreSystemService, false);
			uri = driverUtilities.createUri(entry).toUri();
		} catch (final Exception ex) {
			logger.debug("Could not find service", ex);
		}
		return new ServiceEndpoint(coreSystemService, uri);
	}

}
