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

package eu.arrowhead.core.poaonboarding.database.service;

import java.time.ZonedDateTime;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Subcontractor;
import eu.arrowhead.common.database.repository.SubcontractorRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.SubcontractorListResponseDTO;
import eu.arrowhead.common.dto.internal.SubcontractorResponseDTO;
import eu.arrowhead.common.dto.shared.SubcontractorRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class SubcontractorDBService {
	
	//=================================================================================================
	// members
	
	private static final String EMPTY_NAME_ERROR_MESSAGE= "Name is empty.";
	
	private static final Logger logger = LogManager.getLogger(SubcontractorDBService.class);
	
	@Autowired
	private SubcontractorRepository subcontractorRepository;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SubcontractorResponseDTO getSubcontractorByNameResponse(final String name) {		
		logger.debug("getSubcontractorByIdResponse started...");
		
		final Subcontractor subcontractor = getSubcontractorByName(name);
		return DTOConverter.convertSubcontractorToSubcontractorResponseDTO(subcontractor);
	}

	//-------------------------------------------------------------------------------------------------
	public SubcontractorListResponseDTO getSubcontractorsResponse(final int page, final int size, final Direction direction, final String sortField) {		
		logger.debug("getSubcontractorsResponse started...");
		
		final Page<Subcontractor> subcontractors = getSubcontractors(page, size, direction, sortField);
		return DTOConverter.convertSubcontractorListToSubcontractorListResponseDTO(subcontractors);
	}

	//-------------------------------------------------------------------------------------------------
	public SubcontractorResponseDTO createSubcontractorResponse(final SubcontractorRequestDTO request) {		
		logger.debug("createSubcontractorResponse started...");
		
		final Subcontractor subcontractor = createSubcontractorEntity(request);
		return DTOConverter.convertSubcontractorToSubcontractorResponseDTO(subcontractor);
	}

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeSubcontractorByName(final String name) {
		logger.debug("removeSubcontractorByName started...");
		try {
			if (!subcontractorRepository.existsByName(name)) {
				throw new InvalidParameterException("Subcontractor with name '" + name + "' not found");
			}
			subcontractorRepository.deleteByName(name);
			subcontractorRepository.flush();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}

	//-------------------------------------------------------------------------------------------------
	public Subcontractor getSubcontractorByName(final String name) {		
		logger.debug("getSubcontractorByName started...");
		
		if (name.isEmpty()) {
			throw new InvalidParameterException(EMPTY_NAME_ERROR_MESSAGE);
		}
		
		try {
			final Optional<Subcontractor> subcontractorOption = subcontractorRepository.findByName(name);
			if (subcontractorOption.isEmpty()) {
				throw new InvalidParameterException("Subcontractor with name '" + name + "' not found.");		
			}
	
			return subcontractorOption.get();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//=================================================================================================
	// assistant methods	

	//-------------------------------------------------------------------------------------------------
	private Page<Subcontractor> getSubcontractors(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getSubcontractors started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!Subcontractor.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return subcontractorRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------	
	@Transactional(rollbackFor = ArrowheadException.class)
	private Subcontractor createSubcontractorEntity(final SubcontractorRequestDTO request) {
		logger.debug("createSubcontractorEntity started...");

		final ZonedDateTime validBefore = ZonedDateTime.parse(request.getValidBefore());
		Subcontractor subcontractor = new Subcontractor(request.getName(), request.getPublicKey(), validBefore);

		try {
			return subcontractorRepository.saveAndFlush(subcontractor);	
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

}