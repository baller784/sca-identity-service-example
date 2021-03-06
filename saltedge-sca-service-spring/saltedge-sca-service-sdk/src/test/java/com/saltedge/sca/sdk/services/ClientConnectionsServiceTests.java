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
package com.saltedge.sca.sdk.services;

import com.saltedge.sca.sdk.MockServiceTestAbs;
import com.saltedge.sca.sdk.models.ClientConnection;
import com.saltedge.sca.sdk.models.UserIdentity;
import com.saltedge.sca.sdk.models.api.requests.ScaCreateConnectionRequest;
import com.saltedge.sca.sdk.models.api.responces.ScaCreateConnectionResponse;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionsRepository;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;

import static com.saltedge.sca.sdk.tools.UrlTools.DEFAULT_AUTHENTICATOR_RETURN_TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClientConnectionsServiceTests extends MockServiceTestAbs {
	@Autowired
	private ClientConnectionsService testService;
	@MockBean
	private ClientConnectionsRepository connectionsRepository;

	@Test
	public void givenInvalidParams_whenCreateConnection_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.createConnection(null, ""));
	}

	@Test
	public void givenUnknownAuthorizationSecret_whenCreateConnection_thenReturnRedirectToAuthPage() {
		//given
		ScaCreateConnectionRequest.Data requestData = new ScaCreateConnectionRequest.Data(publicKey, DEFAULT_AUTHENTICATOR_RETURN_TO, "android", "token", null);
		ClientConnectionEntity savedEntity = new ClientConnectionEntity();
		savedEntity.setId(1L);
		savedEntity.setPublicKey(requestData.getPublicKey());
		savedEntity.setPushToken(requestData.getPushToken());
		savedEntity.setPlatform(requestData.getPlatform());
		savedEntity.setReturnUrl(requestData.getReturnUrl());

		given(serviceProvider.getUserIdByAuthenticationSessionSecret("test")).willReturn(null);
		given(serviceProvider.getAuthorizationPageUrl(anyString())).willReturn("http://host.org/oauth");
		given(connectionsRepository.save(any(ClientConnectionEntity.class))).willReturn(savedEntity);

		//when
		ScaCreateConnectionResponse result = testService.createConnection(requestData, "test");

		//then
		ArgumentCaptor<ClientConnectionEntity> entityCaptor = ArgumentCaptor.forClass(ClientConnectionEntity.class);
		verify(connectionsRepository).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getPublicKeyString()).isEqualTo(publicKey);
		assertThat(entityCaptor.getValue().getReturnUrl()).isEqualTo(DEFAULT_AUTHENTICATOR_RETURN_TO);
		assertThat(entityCaptor.getValue().getPushToken()).isEqualTo("token");
		assertThat(entityCaptor.getValue().getPlatform()).isEqualTo("android");
		assertThat(entityCaptor.getValue().getAuthSessionSecret()).isNotEmpty();
		assertThat(entityCaptor.getValue().hasAuthSessionExpired()).isFalse();
		assertThat(entityCaptor.getValue().getAccessToken()).isEmpty();
		assertThat(entityCaptor.getValue().getUserId()).isNull();

		assertThat(result.data.connectionId).isEqualTo("1");
		assertThat(result.data.authorizeRedirectUrl).isEqualTo("http://host.org/oauth");
	}

	@Test
	public void givenUnknownAuthorizationSecret_whenCreateConnection_thenReturnRedirectToReturnPage() {
		//given
		UserIdentity identity = new UserIdentity("1");
		ScaCreateConnectionRequest.Data requestData = new ScaCreateConnectionRequest.Data(publicKey, DEFAULT_AUTHENTICATOR_RETURN_TO, "android", "token", null);
		ClientConnectionEntity savedEntity = new ClientConnectionEntity();
		savedEntity.setId(1L);
		savedEntity.setPublicKey(requestData.getPublicKey());
		savedEntity.setPushToken(requestData.getPushToken());
		savedEntity.setPlatform(requestData.getPlatform());
		savedEntity.setReturnUrl(requestData.getReturnUrl());
		savedEntity.setAccessToken("access_token");
		savedEntity.setUserId("1");

		given(serviceProvider.getUserIdByAuthenticationSessionSecret("test")).willReturn(identity);
		given(serviceProvider.getAuthorizationPageUrl(anyString())).willReturn("http://host.org/oauth");
		given(connectionsRepository.save(any(ClientConnectionEntity.class))).willReturn(savedEntity);

		//when
		ScaCreateConnectionResponse result = testService.createConnection(requestData, "test");

		//then
		ArgumentCaptor<ClientConnectionEntity> entityCaptor = ArgumentCaptor.forClass(ClientConnectionEntity.class);
		verify(connectionsRepository).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getPublicKeyString()).isEqualTo(publicKey);
		assertThat(entityCaptor.getValue().getReturnUrl()).isEqualTo(DEFAULT_AUTHENTICATOR_RETURN_TO);
		assertThat(entityCaptor.getValue().getPushToken()).isEqualTo("token");
		assertThat(entityCaptor.getValue().getPlatform()).isEqualTo("android");
		assertThat(entityCaptor.getValue().getAuthSessionSecret()).isEmpty();
		assertThat(entityCaptor.getValue().getAccessToken()).isNotEmpty();
		assertThat(entityCaptor.getValue().getUserId()).isEqualTo("1");
		assertThat(entityCaptor.getValue().isAuthenticated()).isTrue();

		assertThat(result.data.connectionId).isEqualTo("1");
		assertThat(result.data.authorizeRedirectUrl).isNull();
		assertThat(result.data.accessToken).isEqualTo("access_token");
	}

	@Test
	public void givenInvalidParams_whenGetClientConnections_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.getConnections(""));
	}

	@Test
	public void givenInvalidParams_whenGetClientConnections_thenReturnListOfConnections() {
		//given
		ClientConnectionEntity savedEntity = createAuthenticatedConnection();
		given(connectionsRepository.findByUserId("user_id")).willReturn(Lists.list(savedEntity));

		List<ClientConnection> result = testService.getConnections("user_id");

		assertThat(result).isEqualTo(Lists.list(savedEntity));
	}

	@Test
	public void givenInvalidParams_whenGetConnectionReturnUrl_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.getConnectionReturnUrl(null));
	}

	@Test
	public void givenInvalidParams_whenGetConnectionReturnUrl_thenReturnUrl() {
		//given
		given(connectionsRepository.findByAuthSessionSecret("secret")).willReturn(createAuthenticatedConnection());

		//when
		String result = testService.getConnectionReturnUrl("secret");

		//then
		assertThat(result).isEqualTo(DEFAULT_AUTHENTICATOR_RETURN_TO);
	}

	@Test
	public void givenInvalidParams_whenRevokeConnection_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.revokeConnection((ClientConnectionEntity) null));
	}

	@Test
	public void givenConnection_whenRevokeConnection_thenSaveRevokedEntity() {
		//given
		ClientConnectionEntity entity = new ClientConnectionEntity();
		entity.setId(1L);
		entity.setRevoked(false);

		//when
		testService.revokeConnection(entity);

		//then
		ArgumentCaptor<ClientConnectionEntity> entityCaptor = ArgumentCaptor.forClass(ClientConnectionEntity.class);
		verify(connectionsRepository).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getRevoked()).isTrue();
	}

	@Test
	public void givenInvalidParams_whenAuthenticateConnection_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.authenticateConnection(null, null));
		assertThrows(ConstraintViolationException.class, () -> testService.authenticateConnection(null, new UserIdentity("")));
		assertThrows(ConstraintViolationException.class, () -> testService.authenticateConnection("", null));
		assertThrows(ConstraintViolationException.class, () -> testService.authenticateConnection("", new UserIdentity("")));
	}

	@Test
	public void givenNullConnection_whenAuthenticateConnection_thenReturnNull() {
		//given
		given(connectionsRepository.findByAuthSessionSecret("secret")).willReturn(null);

		//when
		ClientConnectionEntity result = testService.authenticateConnection("secret", new UserIdentity("userId"));

		//then
		assertThat(result).isNull();
	}

	@Test
	public void givenConnectionWithExpiredAuthSession_whenAuthenticateConnection_thenReturnNotAuthenticatedConnection() {
		//given
		ClientConnectionEntity initialConnection = createNotAuthenticatedConnection();
		initialConnection.setAuthTokenExpiresAt(Instant.MIN);
		given(connectionsRepository.findByAuthSessionSecret("secret")).willReturn(initialConnection);

		//when
		ClientConnectionEntity result = testService.authenticateConnection("secret", new UserIdentity("userId"));

		//then
		assertThat(result.isAuthenticated()).isFalse();
	}

	@Test
	public void givenValidConnection_whenAuthenticateConnection_thenReturnAuthenticatedConnection() {
		//given
		ClientConnectionEntity initialConnection = createNotAuthenticatedConnection();
		initialConnection.setAuthTokenExpiresAt(Instant.MAX);
		given(connectionsRepository.findByAuthSessionSecret("secret")).willReturn(initialConnection);

		ClientConnectionEntity savedConnection = createNotAuthenticatedConnection();
		savedConnection.setUserId("2");
		given(connectionsRepository.save(any(ClientConnectionEntity.class))).willReturn(savedConnection);

		//when
		ClientConnectionEntity result = testService.authenticateConnection(
				"secret",
				new UserIdentity("2", "accessToken", null)
		);

		//then
		ArgumentCaptor<ClientConnectionEntity> entityCaptor = ArgumentCaptor.forClass(ClientConnectionEntity.class);
		verify(connectionsRepository).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getRevoked()).isFalse();
		assertThat(entityCaptor.getValue().getUserId()).isEqualTo("2");
		assertThat(entityCaptor.getValue().getAccessToken()).isEqualTo("accessToken");
		assertThat(entityCaptor.getValue().isAuthenticated()).isTrue();
		assertThat(result.isAuthenticated()).isTrue();
	}
}
