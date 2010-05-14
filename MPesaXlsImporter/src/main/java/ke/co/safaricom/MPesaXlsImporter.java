/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
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

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.joda.time.LocalDate;
import org.mifos.StandardImport;
import org.mifos.accounts.api.AccountPaymentParametersDto;
import org.mifos.accounts.api.AccountReferenceDto;
import org.mifos.accounts.api.InvalidPaymentReason;
import org.mifos.accounts.api.PaymentTypeDto;
import org.mifos.spi.ParseResultDto;

public class MPesaXlsImporter extends StandardImport {
    private static final String EXPECTED_STATUS = "Completed";
    protected static final String PAYMENT_TYPE = "MPESA/ZAP";
    protected static final int RECEIPT = 0;
    protected static final int TRANSACTION_DATE = 1;
    protected static final int DETAILS = 2;
    protected static final int STATUS = 3;
    protected static final int WITHDRAWN = 4;
    protected static final int PAID_IN = 5;
    protected static final int BALANCE = 6;
    protected static final int BALANCE_CONFIRMED = 7;
    protected static final int TRANSACTION_TYPE = 8;
    protected static final int OTHER_PARTY_INFO = 9;
    protected static final int TRANSACTION_PARTY_DETAILS = 10;
    protected static final int MAX_CELL_NUM = 11;

    private transient final List<String> errorsList = new ArrayList<String>();

    @Override
    public String getDisplayName() {
        return "M-PESA (Excel 2007)";
    }

    @Override
    public ParseResultDto parse(final InputStream input) {
        final List<AccountPaymentParametersDto> pmts = new ArrayList<AccountPaymentParametersDto>();
        final Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount = new HashMap<AccountReferenceDto, BigDecimal>();
        
        try {
            final Iterator<Row> rowIterator = new HSSFWorkbook(input).getSheetAt(0).iterator();
            int friendlyRowNum = 0;
            Row row;
            
            setPaymentType();

            skipToTransactionData(rowIterator);

            /* Parse transaction data */
            if (errorsList.isEmpty()) {
                while (rowIterator.hasNext()) {
                    try {
                        row = rowIterator.next();

                        friendlyRowNum = row.getRowNum() + 1;

                        if (!isRowValid(row, friendlyRowNum)) {
                            continue;
                        }

                        Date transDate;
                        try {
                            transDate = getDate(row.getCell(TRANSACTION_DATE));
                        } catch (Exception e) {
                            errorsList.add("Date in Row " + friendlyRowNum
                                    + "  does not begin with expected format (YYYY-MM-DD), it contains "
                                    + row.getCell(TRANSACTION_DATE).getStringCellValue());
                            continue;
                        }

                        final LocalDate paymentDate = LocalDate.fromDateFields(transDate);
                        String governmentId = "";
                        String advLoanProdSName = "";
                        String normLoanProdSName = "";
                        String savingsProdSName = "";

                        final String[] result = parseClientIdentifiers(row.getCell(TRANSACTION_PARTY_DETAILS)
                                .getStringCellValue());
                        governmentId = result[0];
                        advLoanProdSName = result[1];
                        normLoanProdSName = result[2];
                        savingsProdSName = result[3];

                        checkBlank(governmentId, "Government ID", friendlyRowNum);
                        checkBlank(advLoanProdSName, "Advance loan product short name", friendlyRowNum);
                        checkBlank(normLoanProdSName, "Normal loan product short", friendlyRowNum);
                        checkBlank(savingsProdSName, "Savings product short name", friendlyRowNum);

                        BigDecimal paymentAmount = BigDecimal.ZERO;

                        // FIXME: possible data loss converting double to BigDecimal?
                        paymentAmount = new BigDecimal(row.getCell(PAID_IN).getNumericCellValue());

                        AccountReferenceDto advanceLoanAccount;
                        AccountReferenceDto savingsAccount;
                        AccountReferenceDto normalLoanAccount;

                        BigDecimal advanceLoanAccountPaymentAmount;
                        BigDecimal normalLoanAccountPaymentAmount;
                        BigDecimal savingsAccountPaymentAmount;

                        BigDecimal advanceLoanAccountDue;
                        BigDecimal normalLoanAccountDue;

                        advanceLoanAccount = getLoanAccount(governmentId, advLoanProdSName, friendlyRowNum);
                        normalLoanAccount = getLoanAccount(governmentId, normLoanProdSName, friendlyRowNum);
                        savingsAccount = getSavingsAccount(governmentId, savingsProdSName, friendlyRowNum);

                        advanceLoanAccountDue = getTotalPaymentDueAmount(advanceLoanAccount, friendlyRowNum);
                        normalLoanAccountDue = getTotalPaymentDueAmount(normalLoanAccount, friendlyRowNum);

                        if (paymentAmount.compareTo(advanceLoanAccountDue) > 0) {
                            advanceLoanAccountPaymentAmount = advanceLoanAccountDue;
                            paymentAmount = paymentAmount.subtract(advanceLoanAccountDue);
                        } else {
                            advanceLoanAccountPaymentAmount = paymentAmount;
                            paymentAmount = BigDecimal.ZERO;
                        }

                        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                            if (paymentAmount.compareTo(normalLoanAccountDue) > 0) {
                                normalLoanAccountPaymentAmount = normalLoanAccountDue;
                                paymentAmount = paymentAmount.subtract(normalLoanAccountDue);
                            } else {
                                normalLoanAccountPaymentAmount = paymentAmount;
                                paymentAmount = BigDecimal.ZERO;
                            }
                        } else {
                            normalLoanAccountPaymentAmount = BigDecimal.ZERO;
                        }

                        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                            savingsAccountPaymentAmount = paymentAmount;
                            paymentAmount = BigDecimal.ZERO;
                        } else {
                            savingsAccountPaymentAmount = BigDecimal.ZERO;
                        }

                        final BigDecimal totalPaymentAmountForAdvanceLoanAccount = addToRunningTotalForAccount(
                                advanceLoanAccountPaymentAmount, cumulativeAmountByAccount, advanceLoanAccount);
                        final BigDecimal totalPaymentAmountForNormalLoanAccount = addToRunningTotalForAccount(
                                normalLoanAccountPaymentAmount, cumulativeAmountByAccount, normalLoanAccount);
                        final BigDecimal totalPaymentAmountForSavingsAccount = addToRunningTotalForAccount(
                                savingsAccountPaymentAmount, cumulativeAmountByAccount, savingsAccount);

                        final AccountPaymentParametersDto cumulativePaymentAdvanceLoan = new AccountPaymentParametersDto(
                                getUserReferenceDto(), advanceLoanAccount, totalPaymentAmountForAdvanceLoanAccount,
                                paymentDate, getPaymentTypeDto(), "");
                        final AccountPaymentParametersDto cumulativePaymentNormalLoan = new AccountPaymentParametersDto(
                                getUserReferenceDto(), advanceLoanAccount, totalPaymentAmountForNormalLoanAccount,
                                paymentDate, getPaymentTypeDto(), "");
                        final AccountPaymentParametersDto cumulativePaymentSavings = new AccountPaymentParametersDto(
                                getUserReferenceDto(), advanceLoanAccount, totalPaymentAmountForSavingsAccount,
                                paymentDate, getPaymentTypeDto(), "");

                        final AccountPaymentParametersDto advanceLoanPayment = new AccountPaymentParametersDto(
                                getUserReferenceDto(), advanceLoanAccount, advanceLoanAccountPaymentAmount,
                                paymentDate, getPaymentTypeDto(), "");
                        final AccountPaymentParametersDto normalLoanpayment = new AccountPaymentParametersDto(
                                getUserReferenceDto(), normalLoanAccount, normalLoanAccountPaymentAmount, paymentDate,
                                getPaymentTypeDto(), "");
                        final AccountPaymentParametersDto savingsPayment = new AccountPaymentParametersDto(
                                getUserReferenceDto(), savingsAccount, savingsAccountPaymentAmount, paymentDate,
                                getPaymentTypeDto(), "");

                        final List<InvalidPaymentReason> errors = getAccountService().validatePayment(
                                cumulativePaymentAdvanceLoan);
                        errors.addAll(getAccountService().validatePayment(cumulativePaymentNormalLoan));
                        errors.addAll(getAccountService().validatePayment(cumulativePaymentSavings));

                        checkPaymentErrors(errors, friendlyRowNum);

                        pmts.add(advanceLoanPayment);
                        pmts.add(normalLoanpayment);
                        pmts.add(savingsPayment);
                    } catch (Exception e) {
                        /* catch row specific exception and continue for other rows */
                        e.printStackTrace(System.err);
                        errorsList.add(e + ". Input line number: " + friendlyRowNum);
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            /* Catch any exception in the process */
            e.printStackTrace(System.err);
        }
        return new ParseResultDto(errorsList, pmts);
    }

    /**
     * @throws Exception
     */
    private void setPaymentType() throws Exception {
        final PaymentTypeDto paymentType = findPaymentType(PAYMENT_TYPE);

        if (paymentType == null) {
            throw new MPesaXlsImporterException("Payment type " + PAYMENT_TYPE + " not found. Have you configured"
                    + " this payment type?");
        }
        setPaymentTypeDto(paymentType);
    }

    private boolean isRowValid(final Row row, final int friendlyRowNum) {
        String missingDataMsg = "Row " + friendlyRowNum + " is missing data: ";
        // TODO Auto-generated method stub
        if (row.getLastCellNum() < MAX_CELL_NUM) {
            errorsList.add(missingDataMsg + "not enough fields.");
            return false;
        }
        if (null == row.getCell(TRANSACTION_DATE)) {
            errorsList.add(missingDataMsg + "Date field is empty.");
            return false;
        }
        if (null == row.getCell(TRANSACTION_PARTY_DETAILS)) {
            errorsList.add(missingDataMsg + "\"Transaction party details\" field is empty.");
            return false;
        }
        if (null == row.getCell(PAID_IN)) {
            errorsList.add(missingDataMsg + "\"Paid in\" field is empty.");
            return false;
        }
        if (row.getCell(STATUS) == null) {
            errorsList.add(missingDataMsg + "Status field is empty");
            return false;
        } else {
            if (!row.getCell(STATUS).getStringCellValue().trim().equals(EXPECTED_STATUS)) {
                errorsList.add("Status in row " + friendlyRowNum + " is " + row.getCell(STATUS) + " instead of "
                        + EXPECTED_STATUS);
                return false;
            }
        }
        return true;
    }

    private void checkBlank(final String value, final String name, final int friendlyRowNum) {
        if (StringUtils.isBlank(value)) {
            errorsList.add(name + " could not be extracted from row " + friendlyRowNum);
        }
    }

    private void checkPaymentErrors(final List<InvalidPaymentReason> errors, final int friendlyRowNum) {
        if (!errors.isEmpty()) {
            for (InvalidPaymentReason error : errors) {
                switch (error) {
                case INVALID_DATE:
                    errorsList.add("Invalid transaction date in row " + friendlyRowNum);
                    break;
                case UNSUPPORTED_PAYMENT_TYPE:
                    errorsList.add("Unsupported payment type in row " + friendlyRowNum);
                    break;
                case INVALID_PAYMENT_AMOUNT:
                    errorsList.add("Invalid payment amount in row " + friendlyRowNum);
                    break;
                default:
                    errorsList.add("Invalid payment in row " + friendlyRowNum + " (reason unknown).");
                    break;
                }
            }
        }

    }

    private BigDecimal getTotalPaymentDueAmount(final AccountReferenceDto advanceLoanAccount, final int friendlyRowNum) {
        BigDecimal totalPaymentDue = null;
        try {
            totalPaymentDue = getAccountService().getTotalPaymentDueAmount(advanceLoanAccount);
        } catch (Exception e) {
            errorsList.add("Error getting total payment due for row " + friendlyRowNum + ": " + e.getMessage());
        }
        if (totalPaymentDue == null) {
            totalPaymentDue = BigDecimal.ZERO;
        }
        return totalPaymentDue;
    }

    private AccountReferenceDto getSavingsAccount(final String governmentId, final String savingsProductShortName,
            final int friendlyRowNum) {
        AccountReferenceDto savingsAccount = null;
        try {
            savingsAccount = getAccountService()
                    .lookupSavingsAccountReferenceFromClientGovernmentIdAndSavingsProductShortName(governmentId,
                            savingsProductShortName);
        } catch (Exception e) {
            errorsList.add("Error looking up account from row " + friendlyRowNum + ": " + e.getMessage());
        }
        return savingsAccount;
    }

    private AccountReferenceDto getLoanAccount(final String governmentId, final String loanProductShortName,
            final int friendlyRowNum) {
        AccountReferenceDto loanAccount = null;
        try {
            loanAccount = getAccountService().lookupLoanAccountReferenceFromClientGovernmentIdAndLoanProductShortName(
                    governmentId, loanProductShortName);
        } catch (Exception e) {
            errorsList.add("Error looking up account from row " + friendlyRowNum + ": " + e.getMessage());
        }
        return loanAccount;
    }

    private void skipToTransactionData(final Iterator<Row> rowIterator) {
        boolean skippingRowsBeforeTransactionData = true;
        while (errorsList.isEmpty() && skippingRowsBeforeTransactionData) {
            if (!rowIterator.hasNext()) {
                errorsList.add("No rows found with import data.");
                break;
            }
            final Row row = rowIterator.next();
            if (row.getCell(0).getStringCellValue().trim().equals("Transactions")) {
                skippingRowsBeforeTransactionData = false;
                /* skip row with column descriptions */
                rowIterator.next();
            }
        }
    }

    protected static final String DATE_FORMATE = "yyyy-MM-dd HH:mm:ss";

    protected Date getDate(final Cell transDateCell) throws ParseException {
        Date date = null;
        if (transDateCell.getCellType() == Cell.CELL_TYPE_STRING) {
            final SimpleDateFormat dateAsText = new SimpleDateFormat(DATE_FORMATE, Locale.ENGLISH);
            dateAsText.setLenient(false);
            date = dateAsText.parse(transDateCell.getStringCellValue());
        } else if (transDateCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            date = transDateCell.getDateCellValue();
        }
        return date;
    }

    protected String[] parseClientIdentifiers(final String stringCellValue) {
        return stringCellValue.split(" ");
    }

    @Override
    public int getNumberOfTransactionsPerRow() {
        return 3;
    }

    class MPesaXlsImporterException extends RuntimeException {

        private static final long serialVersionUID = 731436914098659043L;

        MPesaXlsImporterException(final String msg) {
            super(msg);
        }

    }
}
