/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-identity-service-example).
 * Copyright (c) 2020 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.sca.sdk.tools;

import org.junit.Test;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyToolsTests {
	@Test
	public void convertPemStringToPublicKeyTest() {
		String keyValue = "-----BEGIN PUBLIC KEY-----\n" +
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3Nsu1t3t/Kgd6Jeq6Yyo\n" +
				"tvbIuOgdL/5Ng/fny1fjxO4LKUlvaPDOxw5LXERfOJ5H0B7JX0Uu2ZFt4P0veOBv\n" +
				"ja0E2HS0VyUZGlb2EA1atRCueTgyzjw5PxpjIwr/HbZhqoSxzbba4N8OFpnX+sck\n" +
				"VCbF1cQ7qXLaFVAZrq8Hyklb05884ZDpLth1aTnnRStSKJkAi2+6V4xyRMLE5ylz\n" +
				"d6LF/S4Tvlw1/WpDmpPZSw+Cc4mzGSKi3PBGMVDpfLrJQxFwuh5TF/M1x+f0pQkB\n" +
				"yohWj7OqBxEkYbfCKkmjmPUKhoe9PQCKwJT6MSaVEbfbsSZlHWw1p7NzOmLVEbGO\n" +
				"1wIDAQAB\n" +
				"-----END PUBLIC KEY-----";

		PublicKey resultKey = KeyTools.convertPemStringToPublicKey(keyValue);

		assertThat(resultKey).isNotNull();
	}
}
