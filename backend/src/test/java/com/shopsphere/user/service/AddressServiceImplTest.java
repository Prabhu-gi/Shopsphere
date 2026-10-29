package com.shopsphere.user.service;

import com.shopsphere.common.exception.ResourceNotFoundException;
import com.shopsphere.user.dto.AddressRequest;
import com.shopsphere.user.dto.AddressResponse;
import com.shopsphere.user.entity.Address;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.mapper.AddressMapper;
import com.shopsphere.user.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserService userService;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User sampleUser;
    private Address sampleAddress;
    private AddressRequest addressRequest;
    private AddressResponse addressResponse;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).email("user@example.com").build();

        sampleAddress = Address.builder()
                .id(10L)
                .user(sampleUser)
                .fullName("John Doe")
                .phone("+1234567890")
                .streetLine1("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build();

        addressRequest = AddressRequest.builder()
                .fullName("John Doe")
                .phone("+1234567890")
                .streetLine1("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build();

        addressResponse = AddressResponse.builder()
                .id(10L)
                .fullName("John Doe")
                .phone("+1234567890")
                .streetLine1("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build();
    }

    @Test
    @DisplayName("givenFirstAddress_whenAddAddress_thenSetsDefaultTrueAndSaves")
    void givenFirstAddress_whenAddAddress_thenSetsDefaultTrueAndSaves() {
        // Given
        given(userService.getUserById(1L)).willReturn(sampleUser);
        given(addressMapper.toEntity(addressRequest)).willReturn(sampleAddress);
        given(addressRepository.findByUserId(1L)).willReturn(Collections.emptyList());
        given(addressRepository.save(any(Address.class))).willReturn(sampleAddress);
        given(addressMapper.toDto(sampleAddress)).willReturn(addressResponse);

        // When
        AddressResponse result = addressService.addAddress(1L, addressRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsDefault()).isTrue();
        verify(addressRepository).resetDefaultAddressForUser(1L);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("givenExistingAddress_whenDeleteAddress_thenDeletesFromRepo")
    void givenExistingAddress_whenDeleteAddress_thenDeletesFromRepo() {
        // Given
        given(addressRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(sampleAddress));

        // When
        addressService.deleteAddress(1L, 10L);

        // Then
        verify(addressRepository).delete(sampleAddress);
    }

    @Test
    @DisplayName("givenNonExistingAddress_whenDeleteAddress_thenThrowsNotFound")
    void givenNonExistingAddress_whenDeleteAddress_thenThrowsNotFound() {
        // Given
        given(addressRepository.findByIdAndUserId(99L, 1L)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> addressService.deleteAddress(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(addressRepository, never()).delete(any());
    }

    @Test
    @DisplayName("givenAddressId_whenSetDefaultAddress_thenResetsOthersAndSetsDefault")
    void givenAddressId_whenSetDefaultAddress_thenResetsOthersAndSetsDefault() {
        // Given
        given(addressRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(sampleAddress));
        given(addressRepository.save(sampleAddress)).willReturn(sampleAddress);
        given(addressMapper.toDto(sampleAddress)).willReturn(addressResponse);

        // When
        AddressResponse result = addressService.setDefaultAddress(1L, 10L);

        // Then
        assertThat(result).isNotNull();
        verify(addressRepository).resetDefaultAddressForUser(1L);
        verify(addressRepository).save(sampleAddress);
    }

    @Test
    @DisplayName("givenExistingAddress_whenUpdateAddress_thenUpdatesAndSaves")
    void givenExistingAddress_whenUpdateAddress_thenUpdatesAndSaves() {
        // Given
        given(addressRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(sampleAddress));
        given(addressRepository.save(sampleAddress)).willReturn(sampleAddress);
        given(addressMapper.toDto(sampleAddress)).willReturn(addressResponse);

        // When
        AddressResponse result = addressService.updateAddress(1L, 10L, addressRequest);

        // Then
        assertThat(result).isNotNull();
        verify(addressMapper).updateEntityFromDto(addressRequest, sampleAddress);
        verify(addressRepository).save(sampleAddress);
    }

    @Test
    @DisplayName("givenNonExistingAddress_whenUpdateAddress_thenThrowsNotFound")
    void givenNonExistingAddress_whenUpdateAddress_thenThrowsNotFound() {
        // Given
        given(addressRepository.findByIdAndUserId(99L, 1L)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> addressService.updateAddress(1L, 99L, addressRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("givenExistingAddresses_whenAddAddressWithDefaultFalse_thenNotDefault")
    void givenExistingAddresses_whenAddAddressWithDefaultFalse_thenNotDefault() {
        // Given
        AddressRequest nonDefaultRequest = AddressRequest.builder()
                .fullName("John Doe")
                .phone("+1234567890")
                .streetLine1("456 Other St")
                .city("Chicago")
                .state("IL")
                .postalCode("60601")
                .country("USA")
                .isDefault(false)
                .build();

        Address nonDefaultEntity = Address.builder().id(20L).user(sampleUser).isDefault(false).build();
        AddressResponse nonDefaultResponse = AddressResponse.builder().id(20L).isDefault(false).build();

        given(userService.getUserById(1L)).willReturn(sampleUser);
        given(addressMapper.toEntity(nonDefaultRequest)).willReturn(nonDefaultEntity);
        given(addressRepository.findByUserId(1L)).willReturn(List.of(sampleAddress));
        given(addressRepository.save(nonDefaultEntity)).willReturn(nonDefaultEntity);
        given(addressMapper.toDto(nonDefaultEntity)).willReturn(nonDefaultResponse);

        // When
        AddressResponse result = addressService.addAddress(1L, nonDefaultRequest);

        // Then
        assertThat(result.getIsDefault()).isFalse();
        verify(addressRepository, never()).resetDefaultAddressForUser(1L);
    }
}
