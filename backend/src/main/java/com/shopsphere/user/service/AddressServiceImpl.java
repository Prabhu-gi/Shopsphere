package com.shopsphere.user.service;

import com.shopsphere.common.exception.ResourceNotFoundException;
import com.shopsphere.user.dto.AddressRequest;
import com.shopsphere.user.dto.AddressResponse;
import com.shopsphere.user.entity.Address;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.mapper.AddressMapper;
import com.shopsphere.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addressMapper.toDtoList(addresses);
    }

    @Override
    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        User user = userService.getUserById(userId);
        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        // If this is the user's first address or requested default, set as default
        List<Address> existing = addressRepository.findByUserId(userId);
        if (existing.isEmpty() || Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.resetDefaultAddressForUser(userId);
            address.setIsDefault(true);
        }

        Address saved = addressRepository.save(address);
        log.info("Saved new address ID: {} for user ID: {}", saved.getId(), userId);
        return addressMapper.toDto(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        addressMapper.updateEntityFromDto(request, address);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.resetDefaultAddressForUser(userId);
            address.setIsDefault(true);
        }

        Address updated = addressRepository.save(address);
        log.info("Updated address ID: {} for user ID: {}", updated.getId(), userId);
        return addressMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        addressRepository.delete(address);
        log.info("Deleted address ID: {} for user ID: {}", addressId, userId);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        addressRepository.resetDefaultAddressForUser(userId);
        address.setIsDefault(true);
        Address updated = addressRepository.save(address);
        log.info("Set address ID: {} as default for user ID: {}", addressId, userId);
        return addressMapper.toDto(updated);
    }
}
