package com.technogise.customerSupportTicketSystem.repository;

import com.technogise.customerSupportTicketSystem.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
