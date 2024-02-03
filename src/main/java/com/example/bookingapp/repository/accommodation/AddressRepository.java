package com.example.bookingapp.repository.accommodation;

import com.example.bookingapp.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long>,
        JpaSpecificationExecutor<Address> {
}
