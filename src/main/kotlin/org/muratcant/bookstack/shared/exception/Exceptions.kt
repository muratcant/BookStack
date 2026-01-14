package org.muratcant.bookstack.shared.exception

open class BusinessRuleException(message: String) : RuntimeException(message)

open class ResourceNotFoundException(message: String) : RuntimeException(message)

class DuplicateResourceException(message: String) : BusinessRuleException(message)

class MemberNotActiveException(message: String) : BusinessRuleException(message)

class MemberNotCheckedInException(message: String) : BusinessRuleException(message)

class MemberAlreadyCheckedInException(message: String) : BusinessRuleException(message)

class CopyNotAvailableException(message: String) : BusinessRuleException(message)

class CopyNotBorrowableException(message: String) : BusinessRuleException(message)

class MaxLoansExceededException(message: String) : BusinessRuleException(message)

class UnpaidPenaltiesException(message: String) : BusinessRuleException(message)

class ReservationNotFoundException(message: String) : ResourceNotFoundException(message)

class ReservationAlreadyExistsException(message: String) : BusinessRuleException(message)

class InvalidStatusTransitionException(message: String) : BusinessRuleException(message)

