package uk.gov.justice.digital.hmpps.hmppstiersqstool.jpa.entity

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "calculation_request")
data class CalculationRequestEntity
(

  @Id
  @Column
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column
  val crn: String,

  @Column
  val created: LocalDateTime,

  @Column
  var processed: LocalDateTime? = null,

  @Column
  var messageId: UUID? = null,
)
