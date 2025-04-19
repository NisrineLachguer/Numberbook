package com.lachguer.mobile.repository;

import com.lachguer.mobile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
     User findByImei(String imei);

}
