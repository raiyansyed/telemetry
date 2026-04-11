package com.vehicle.telemetry.repository;

import com.vehicle.telemetry.entity.User;
import com.vehicle.telemetry.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findByRoleAndLocationIgnoreCase(Role role, String location);
}
