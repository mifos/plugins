import org.joda.time.LocalDate
import org.mifos.accounts.api.AccountPaymentParametersDto
import org.mifos.framework.util.UnicodeUtil
import org.mifos.spi.ParseResultDto

def paymentType = null
parent.accountService.loanPaymentTypes.each { ptype ->
    if (ptype.name.contains("Groovy payment type")) { paymentType = ptype }
}

def input = UnicodeUtil.getUnicodeAwareBufferedReader(rawInput)
input.readLine() // first line is just column headers: skip
def lineNum = 2 // since we skipped the first line
def errorsList = []
def payments = []
input.each { line ->
    def (dateString, accountId, amount, serial) = line.split(",")
    def paymentDate = LocalDate.fromDateFields(Date.parse("yyyy-MM-dd", dateString))
    def account = parent.accountService.lookupLoanAccountReferenceFromGlobalAccountNumber(accountId)
    def paymentAmount = new BigDecimal(amount)
    def payment = new AccountPaymentParametersDto(parent.userReferenceDto,
        account, paymentAmount, paymentDate, paymentType, "serial=" + serial)
    def errors = parent.accountService.validatePayment(payment)
    if (!errors.isEmpty()) {
        errors.each { error -> errorsList.add("error on line " + lineNum + ": " + error) }
    }
    payments.add(payment)
    lineNum += 1
}

parseResultDto = new ParseResultDto(errorsList, payments)
