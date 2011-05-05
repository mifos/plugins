/*
 * Copyright (c) 2005-2011 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package ke.co.safaricom;

public interface MPesaConstants {

    String MPESA_TRANSACTION_ORDER = "MpesaTransactionOrder";
    String MAX_MPESA_DISBURSAL_LIMIT = "MaxMpesaDisbursalLimit";
    String NOT_DEFINED = "NotDefined";
    String ROW = "Row";
    String ERROR = "Error";
    String ERROR_LOWERCASE = "error";
    String IGNORED = "ignored";
    String CANNOT_READ_PHONE_NUMBER = "CannotReadPhoneNumber";
    String CLIENT_NOT_FOUND = "ClientNotFound";
    String TOO_MANY_MATCHES = "TooManyMatches";
    String EXIST_MORE_THAN_ONE_ACCOUNT = "existsMoreThanOneAccount";
    String FRACTION_DIGITS_IS_TOO_MUCH_WITHDRAWN = "FractionDigitsIsTooMuchWithdrawn";
    String FRACTION_DIGITS_IS_TOO_MUCH_PAID_IN = "FractionDigitsIsTooMuchPainIn";
    String MORE_THAN_1_LOAN_FOUND = "MoreThan1LoanFound";
    String NO_APPROVED_LOANS_FOUND = "NoApprovedLoansFound";
    String INVALID_TRANSACTION_DATE = "InvalidTransactionDate";
    String UNSUPPORTED_PAYMENT_TYPE = "UnsupportedPaymentType";
    String INVALID_LOAN_DISBURSAL_AMOUNT = "InvalidLoanDisbursalAmonut";
    String INVALID_LOAN_STATE = "InvalidAccountState";
    String OTHER_ACTIVE_LOANS_FOR_THE_SAME_PRODUCT = "OtherActiveLoansForTheSameProduct";
    String INVALID_DATA = "InvalidData";
    String INVALID_DATE = "InvalidDate";
    String UNKNOW_FILE_FORMAT = "UnknownFileFormat";
    String RECEIPT_ID_DUPLICATED = "ReceiptIdDuplicated";
    String INVALID_FORMAT_DATE = "InvalidFormatDate";
    String UNKNOWN_FORMAT_OF_CELL = "UnknownFormatOfCell";
    String SAVINGS_PRODUCT_SHORT_NAME = "SavingsProductShortName";
    String TOTAL_PAID_IN_AMOUNT_IS_GREATER_THAN_THE_TOTAL_DUE_AMOUNT = "PaidInAmountIsGreaterThanTheTotalDueAmount";
    String NO_VALID_ACCOUNTS_FOUND = "NoValidAccountsFound";
    String GOT_ERROR_BEFORE_READING_ROWS = "GotErrorBeforeReadingRows";
    String NO_ROWS_FOUND_WITH_IMPORT_DATA = "NoRowsFoundWithImportData";
    String NO_VALID_PRODUCT_NAME = "NoValidProductName";
    String PAYMENT_TYPE_NOT_FOUND = "PaymentTypeNotFound";
    String DISBURSMENT_TYPE_NOT_FOUND = "DisbursementTypeNotFound";
    String MISSING_REQUIRED_DATA = "MissingRequiredData";
    String MISSING_REQUIRED_DATA_RECEIPT = "MissingRequiredDataReceipt";
    String MISSING_REQUIRED_DATA_STATUS = "MissingRequiredDataStatus";
    String MISSING_REQUIRED_DATA_OTHER_PARTY_INFO = "MissingRequiredDataOtherPartyInfo";
    String MISSING_REQUIRED_DATA_WITHDRAWN = "MissingRequiredDataWithDrawn";
    String MISSING_REQUIRED_DATA_TRANSACTION_PARTY_DETAILS = "MissingRequiredDataTransactionPartyDetails";
    String MISSING_REQUIRED_DATA_PAID_IN = "MissingRequiredDataPaidIn";
    String AMOUNT_MUST_BE_GREATER_THAN_0 = "AmountMustBeGreaterThan0";
    String FIELD_IS_INAPPROPRIATE_DETAILS = "FieldIsInappropriateDetails";
    String INVALID_TRANSACTION_TYPE = "InvalidTransactionType";
    String DATE_FIELD_IS_EMPTY = "DateFieldIsEmpty";
    String STATUS_FIELD_IS_EMPTY = "StatusFieldIsEmpty";
    String INVALID_RECEIPT = "InvalidReceipt";
    String COULD_NOT_BE_EXTRACTED = "CouldNotBeExtracted";
    String INVALID_PAYMENT_AMOUNT = "InvalidPaymentAmount";
    String INVALID_PAYMENT = "InvalidPayment";
    String SAVINGS_NOT_FOUND = "SavingsNotFound";
    String LOAN_NOT_FOUND = "LoanNotFound";
    String INVALID_STATUS = "InvalidStatus";
    String INVALID_PAYMENT_REASON_UNKNOWN = "InvalidPaymentReasonUnknown";
}
