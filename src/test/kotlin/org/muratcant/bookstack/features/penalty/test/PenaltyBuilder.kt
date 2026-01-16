package org.muratcant.bookstack.features.penalty.test

import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.test.LoanBuilder
import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.test.MemberBuilder
import org.muratcant.bookstack.features.penalty.domain.Penalty
import org.muratcant.bookstack.features.penalty.domain.PenaltyStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object PenaltyBuilder {
    fun aPenalty(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        loan: Loan = LoanBuilder.aReturnedLoan(member = member),
        amount: BigDecimal = BigDecimal("5.00"),
        daysOverdue: Int = 5,
        status: PenaltyStatus = PenaltyStatus.UNPAID,
        paidAt: LocalDateTime? = null,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Penalty = Penalty(
        id = id,
        member = member,
        loan = loan,
        amount = amount,
        daysOverdue = daysOverdue,
        status = status,
        paidAt = paidAt,
        createdAt = createdAt
    )

    fun anUnpaidPenalty(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        loan: Loan = LoanBuilder.aReturnedLoan(member = member),
        amount: BigDecimal = BigDecimal("5.00"),
        daysOverdue: Int = 5
    ) = aPenalty(
        id = id,
        member = member,
        loan = loan,
        amount = amount,
        daysOverdue = daysOverdue,
        status = PenaltyStatus.UNPAID
    )

    fun aPaidPenalty(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        loan: Loan = LoanBuilder.aReturnedLoan(member = member),
        amount: BigDecimal = BigDecimal("5.00"),
        daysOverdue: Int = 5
    ) = aPenalty(
        id = id,
        member = member,
        loan = loan,
        amount = amount,
        daysOverdue = daysOverdue,
        status = PenaltyStatus.PAID,
        paidAt = LocalDateTime.now()
    )
}
