/*
 * Copyright (c) 2005-2009 Grameen Foundation USA
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

package org.almajmoua;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.accounts.api.AccountReferenceDto;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AudiBankImporterTest {

    @Test
    public void descriptionFieldWithIgnoredTwoLetterCode() {
        assertThat(AudiBankImporter.getAccountId("PMTMAJ BO00001 Joey DeChantel"), is(""));
    }

    @Test
    public void canGetAccountIdFromLongForm() {
        assertThat(AudiBankImporter.getAccountId("PMTMAJ EA41560 83  James Stephens"), is("41560"));
    }

    @Test
    public void canParseFourDigitExternalId() {
        assertThat(AudiBankImporter.getAccountId("PMTMAJ EA01561183  James Stephens"), is("01561"));
    }

    @Test
    public void canParseGroupLoanExternalId() {
        assertThat(AudiBankImporter.getAccountId("PMTMAJ EZ01561183  James Stephens"), is("GL 01561"));
    }

    public void canParseLbpLoanExternalId() {
        assertThat(AudiBankImporter.getAccountId("PMTMAJ EC01561183  James Stephens"), is("LL 01561"));
    }

    @Test
    public void canParseMifosAccountId() {
        assertThat(AudiBankImporter.getAccountId("PMTMAJ 1234567  James Stephens"), is("1234567"));
    }

    @Test
    public void canParseMifosGlobalAccountNumber() {
        assertThat(AudiBankImporter.getAccountId("PMTMAJ 123456789012345  James Stephens"), is("123456789012345"));
    }

    @Test
    public void canTrackRunningPaymentTotalByAccount() {
        Map<AccountReferenceDto, BigDecimal> cumulativeAmountByAccount = new HashMap<AccountReferenceDto, BigDecimal>();
        AccountReferenceDto account = new AccountReferenceDto(21);
        BigDecimal totalSoFarForFirstAccount = AudiBankImporter.addToRunningTotalForAccount(new BigDecimal("10.25"),
                cumulativeAmountByAccount, account);
        assertThat(totalSoFarForFirstAccount, is(new BigDecimal("10.25")));
        totalSoFarForFirstAccount = AudiBankImporter.addToRunningTotalForAccount(new BigDecimal(".75"),
                cumulativeAmountByAccount, account);
        assertThat(totalSoFarForFirstAccount, is(new BigDecimal("11.00")));

        AccountReferenceDto otherAccount = new AccountReferenceDto(22);
        BigDecimal totalSoFarForSecondAccount = AudiBankImporter.addToRunningTotalForAccount(new BigDecimal(".85"),
                cumulativeAmountByAccount, otherAccount);
        assertThat(totalSoFarForSecondAccount, is(new BigDecimal(".85")));
    }
}
