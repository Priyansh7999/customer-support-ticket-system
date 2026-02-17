package com.technogise.customerSupportTicketSystem.repository;


import com.technogise.customerSupportTicketSystem.model.TicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, Long> {
}
