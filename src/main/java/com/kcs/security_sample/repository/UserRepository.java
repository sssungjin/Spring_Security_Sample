package com.kcs.security_sample.repository;

import com.kcs.security_sample.domain.User;
import com.kcs.security_sample.dto.type.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByAccountId(String accountId);
    Optional<User> findByRole(ERole role);
}
