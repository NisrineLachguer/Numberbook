package com.lachguer.mobile.repository;


import com.lachguer.mobile.model.Contact;
import com.lachguer.mobile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByUserId(Long userId);
    Contact findByUserIdAndNumber(Long userId, String number);
}