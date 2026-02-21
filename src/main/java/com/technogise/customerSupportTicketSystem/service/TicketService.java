package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.CreateCommentRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentResponse;
import com.technogise.customerSupportTicketSystem.dto.GetCommentResponse;
import com.technogise.customerSupportTicketSystem.exception.AccessDeniedException;
import com.technogise.customerSupportTicketSystem.exception.InvalidStateTransitionException;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.exception.*;
import com.technogise.customerSupportTicketSystem.model.Comment;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.model.Ticket;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.CommentRepository;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import com.technogise.customerSupportTicketSystem.dto.AgentTicketResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;
import com.technogise.customerSupportTicketSystem.dto.UpdateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.UpdateTicketResponse;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public TicketService(TicketRepository ticketRepository,CommentRepository commentRepository,UserRepository userRepository, UserService userService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public CreateTicketResponse createTicket(String title, String description, UUID userId) {
        User customer = userService.getUserByIdAndRole(userId, UserRole.CUSTOMER);
        User supportAgent = userService.getRandomUserByRole(UserRole.SUPPORT_AGENT);

        Ticket ticket = new Ticket();
        ticket.setTitle(title.trim());
        ticket.setDescription(description.trim());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setCreatedBy(customer);
        ticket.setAssignedTo(supportAgent);

        Ticket createdTicket = ticketRepository.save(ticket);

        CreateTicketResponse response = new CreateTicketResponse();
        response.setId(createdTicket.getId());
        response.setTitle(createdTicket.getTitle());
        response.setDescription(createdTicket.getDescription());
        response.setStatus(createdTicket.getStatus());
        response.setAssignedToName(createdTicket.getAssignedTo().getName());
        response.setCreatedAt(createdTicket.getCreatedAt());

        return response;
    }

    public User findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found with id: " + id));
    }

    public Ticket findTicketById(UUID id) {
        return ticketRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("TICKET_NOT_FOUND", "No ticket found for the provided ID."));

    }

    public boolean canCreateComment(UUID userId, UUID agentId, UUID creatorId){
        return userId.equals(agentId) || userId.equals(creatorId);
    }

    public CreateCommentResponse addComment(UUID ticketId, CreateCommentRequest request, UUID userId) {
        User user = findUserById(userId);
        Ticket ticket = findTicketById(ticketId);
        boolean isUserAuthorizedForTicket = canCreateComment(
                userId,
                ticket.getAssignedTo().getId(),
                ticket.getCreatedBy().getId());

        if(!isUserAuthorizedForTicket){
            throw new AccessDeniedException("ACCESS_DENIED", "Access to this ticket is not permitted");
        }

        if(ticket.getStatus().equals(TicketStatus.CLOSED)){
            throw new AccessDeniedException("ACCESS_DENIED", "Ticket is closed and cannot be modified");
        }

        Comment comment = new Comment();
        comment.setBody(request.getBody());
        comment.setCommenter(user);
        comment.setTicket(ticket);
        Comment savedComment = commentRepository.save(comment);
        CreateCommentResponse response = new CreateCommentResponse();
        response.setId(savedComment.getId());
        response.setBody(savedComment.getBody());
        response.setCreatedAt(savedComment.getCreatedAt());
        return response;
    }

    public CustomerTicketResponse getTicketForCustomerById(UUID id, UUID userId) {

        User customer = userService.getUserByIdAndRole(userId, UserRole.CUSTOMER);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("NOT_FOUND", "Ticket not found with id: " + id));

        if (!ticket.getCreatedBy().getId().equals(customer.getId())) {
            throw new AccessDeniedException("FORBIDDEN","You are not allowed to access this ticket");
        }
        return new CustomerTicketResponse(
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getAssignedTo().getName()

        );
    }

    public AgentTicketResponse getTicketByAgent(UUID ticketId, UUID userId) {
        userService.getUserByIdAndRole(userId, UserRole.SUPPORT_AGENT);

        Ticket foundTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("TICKET_NOT_FOUND","Ticket not found with id " + ticketId));

        return new AgentTicketResponse(
                foundTicket.getTitle(),
                foundTicket.getDescription(),
                foundTicket.getStatus(),
                foundTicket.getPriority(),
                foundTicket.getCreatedAt()
        );
    }

    public void validateTicketAccess(Ticket ticket, UUID userId) {
        boolean isCreator = ticket.getCreatedBy().getId().equals(userId);
        boolean isAssignee = ticket.getAssignedTo().getId().equals(userId);

        if (!isCreator && !isAssignee) {
            throw new AccessDeniedException(
                    "ACCESS_DENIED",
                    "Access to this ticket is not permitted"
            );
        }
    }

    public List<GetCommentResponse> getAllCommentsByTicketId(UUID ticketId, UUID userId) {
        Ticket ticket = findTicketById(ticketId);

        validateTicketAccess(ticket, userId);

        List<Comment> comments = commentRepository.findAllByTicketId(ticketId);
        return comments.stream()
                .map(comment -> new GetCommentResponse(
                        comment.getBody(),
                        comment.getCommenter().getName(),
                        comment.getCreatedAt()
                ))
                .toList();
    }
    public UpdateTicketResponse updateTicket(UUID id, UUID userId, UpdateTicketRequest request) {

        User user = userService.getUserById(userId);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "NOT_FOUND", "Ticket not found with id: " + id));

        if (user.getRole() == UserRole.CUSTOMER) {
            updateByCustomer(ticket, request);
        } else if (user.getRole() == UserRole.SUPPORT_AGENT) {
            updateBySupportAgent(ticket, user, request);
        }
         
         else {
            throw new InvalidUserRoleException("INVALID_ROLE", "Invalid role for updating ticket");
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

            return new UpdateTicketResponse(
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
}
        
        private void updateByCustomer(Ticket ticket, UpdateTicketRequest request) {

        if (request.getPriority() != null) {
            throw new InvalidUserRoleException(
                    "INVALID_PRIORITY_UPDATE",
                    "Cannot update priority");
        }
        if (request.getDescription() == null && request.getStatus() == null) {
            throw new BadRequestException(
                    "BAD_REQUEST",
                    "At least one of description or status must be provided");
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }

        TicketStatus requestedStatus = request.getStatus();

        if (requestedStatus == null) {
            return;
        }

        if (requestedStatus != TicketStatus.CLOSED) {
            throw new InvalidStateTransitionException(
                    "Can only update status to CLOSED");
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new ClosedTicketStatusException(
                    "INVALID_STATUS_UPDATE",
                    "Ticket is already CLOSED");
        }
        ticket.setStatus(TicketStatus.CLOSED);
    }

    private void updateBySupportAgent(Ticket ticket, User agent, UpdateTicketRequest request) {
        if (!ticket.getAssignedTo().getId().equals(agent.getId())) {
            throw new AccessDeniedException("FORBIDDEN", "You can only update tickets assigned to you");
        }

        if (request.getStatus() == null && request.getPriority() == null) {
            throw new BadRequestException("BAD_REQUEST", "At least one of status or priority must be provided");
        }

        if (request.getStatus() != null) {
            if (request.getStatus() != TicketStatus.CLOSED) {
                throw new InvalidStateTransitionException(
                        "Can only update status to CLOSED");
            }

            if (ticket.getStatus() == TicketStatus.CLOSED) {
                throw new ClosedTicketStatusException(
                        "INVALID_STATUS_UPDATE",
                        "Ticket is already CLOSED");
            }
            ticket.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
    }

}
