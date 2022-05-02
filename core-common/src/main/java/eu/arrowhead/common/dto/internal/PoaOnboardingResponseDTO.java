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

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;
import eu.arrowhead.common.dto.shared.ServiceEndpoint;

public class PoaOnboardingResponseDTO implements Serializable {

	private List<String> certificateChain;

	private ServiceEndpoint deviceRegistry;
	private ServiceEndpoint systemRegistry;
	private ServiceEndpoint serviceRegistry;
	private ServiceEndpoint orchestrationService;

	public PoaOnboardingResponseDTO(
			final ServiceEndpoint deviceRegistry,
			final ServiceEndpoint systemRegistry,
			final ServiceEndpoint serviceRegistry,
			final ServiceEndpoint orchestrationService,
			final List<String> certificateChain) {
		this.deviceRegistry = deviceRegistry;
		this.systemRegistry = systemRegistry;
		this.serviceRegistry = serviceRegistry;
		this.orchestrationService = orchestrationService;
		this.certificateChain = certificateChain;
	}

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceEndpoint getDeviceRegistry() {
		return deviceRegistry;
	}

	//-------------------------------------------------------------------------------------------------
	public void setDeviceRegistry(final ServiceEndpoint deviceRegistry) {
		this.deviceRegistry = deviceRegistry;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceEndpoint getSystemRegistry() {
		return systemRegistry;
	}

	//-------------------------------------------------------------------------------------------------
	public void setSystemRegistry(final ServiceEndpoint systemRegistry) {
		this.systemRegistry = systemRegistry;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceEndpoint getServiceRegistry() {
		return serviceRegistry;
	}

	//-------------------------------------------------------------------------------------------------
	public void setServiceRegistry(final ServiceEndpoint serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceEndpoint getOrchestrationService() {
		return orchestrationService;
	}

	//-------------------------------------------------------------------------------------------------
	public void setOrchestrationService(ServiceEndpoint orchestrationService) {
		this.orchestrationService = orchestrationService;
	}

	public List<String> getCertificateChain() {
		return certificateChain;
	}

	public void setCertificateChain(List<String> certificateChain) {
		this.certificateChain = certificateChain;
	}

}
