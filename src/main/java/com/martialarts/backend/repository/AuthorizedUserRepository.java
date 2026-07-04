package com.martialarts.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.martialarts.backend.entity.AuthorizedUser;

public interface AuthorizedUserRepository extends JpaRepository<AuthorizedUser, Long> {
    Optional<AuthorizedUser> findByMobile(String mobile);
    boolean existsByMobile(String mobile);
}