package org.muratcant.bookstack.features.loan.test

import org.muratcant.bookstack.features.bookcopy.domain.BookCopy
import org.muratcant.bookstack.features.bookcopy.test.BookCopyBuilder
import org.muratcant.bookstack.features.loan.domain.Loan
import org.muratcant.bookstack.features.loan.domain.LoanStatus
import org.muratcant.bookstack.features.member.domain.Member
import org.muratcant.bookstack.features.member.test.MemberBuilder
import java.time.LocalDateTime
import java.util.UUID

object LoanBuilder {
    fun aLoan(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        copy: BookCopy = BookCopyBuilder.anAvailableCopy(),
        borrowedAt: LocalDateTime = LocalDateTime.now(),
        dueDate: LocalDateTime = LocalDateTime.now().plusDays(14),
        returnedAt: LocalDateTime? = null,
        status: LoanStatus = LoanStatus.ACTIVE,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Loan = Loan(
        id = id,
        member = member,
        copy = copy,
        borrowedAt = borrowedAt,
        dueDate = dueDate,
        returnedAt = returnedAt,
        status = status,
        createdAt = createdAt
    )

    fun anActiveLoan(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        copy: BookCopy = BookCopyBuilder.anAvailableCopy(),
        dueDate: LocalDateTime = LocalDateTime.now().plusDays(14)
    ) = aLoan(
        id = id,
        member = member,
        copy = copy,
        dueDate = dueDate,
        status = LoanStatus.ACTIVE
    )

    fun anOverdueLoan(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        copy: BookCopy = BookCopyBuilder.anAvailableCopy(),
        dueDate: LocalDateTime = LocalDateTime.now().minusDays(5)
    ) = aLoan(
        id = id,
        member = member,
        copy = copy,
        dueDate = dueDate,
        status = LoanStatus.ACTIVE
    )

    fun aReturnedLoan(
        id: UUID = UUID.randomUUID(),
        member: Member = MemberBuilder.anActiveMember(),
        copy: BookCopy = BookCopyBuilder.anAvailableCopy(),
        borrowedAt: LocalDateTime = LocalDateTime.now().minusDays(10),
        dueDate: LocalDateTime = LocalDateTime.now().minusDays(3),
        returnedAt: LocalDateTime = LocalDateTime.now().minusDays(2)
    ) = aLoan(
        id = id,
        member = member,
        copy = copy,
        borrowedAt = borrowedAt,
        dueDate = dueDate,
        returnedAt = returnedAt,
        status = LoanStatus.RETURNED
    )
}
