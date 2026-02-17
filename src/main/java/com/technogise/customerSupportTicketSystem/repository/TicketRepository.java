package com.technogise.customerSupportTicketSystem.repository;

import com.technogise.customerSupportTicketSystem.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
