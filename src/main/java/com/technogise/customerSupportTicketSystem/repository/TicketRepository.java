package com.technogise.customerSupportTicketSystem.repository;

import com.technogise.customerSupportTicketSystem.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findAllByCreatedById(UUID userId);
    List<Ticket> findAllByAssignedToId(UUID userId);
}
