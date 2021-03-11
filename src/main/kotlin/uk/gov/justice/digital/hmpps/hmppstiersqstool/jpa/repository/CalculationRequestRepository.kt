package uk.gov.justice.digital.hmpps.hmppstiersqstool.jpa.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppstiersqstool.jpa.entity.CalculationRequestEntity

@Repository
interface CalculationRequestRepository : PagingAndSortingRepository<CalculationRequestEntity, Long> {

  fun findFirstByCrn(crn: String): CalculationRequestEntity?

  fun findAllByProcessedIsNull(pageable: Pageable): Page<CalculationRequestEntity>
}
