package com.technogise.customerSupportTicketSystem.repository;

import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findFirstByRole(UserRole role);
    List<User> findAllByRole(UserRole role);
    Optional<User> findByEmail(String email);
}
