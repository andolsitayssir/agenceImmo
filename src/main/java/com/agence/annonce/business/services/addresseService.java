package com.agence.annonce.business.services;

import java.util.List;

import com.agence.annonce.dao.entities.Address;
import com.agence.annonce.dao.entities.Annonce;

import jakarta.transaction.Transactional;

public interface AddresseService {
    List<Address> getAllAddress();
    Address getAddressbyAnnonce(Annonce annonce);

    // Create
    Address addAddress(Address address);
    // Update 
    Address updateAddress(Address address);
  
    void deleteAddressByAnnonce(Annonce annonce);

}