package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.CreateCommentRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateCommentResponse;
import com.technogise.customerSupportTicketSystem.exception.AccessDeniedException;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.Comment;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.InvalidUserRoleException;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.Ticket;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.CommentRepository;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import com.technogise.customerSupportTicketSystem.repository.UserRepository;
import com.technogise.customerSupportTicketSystem.dto.AgentTicketResponse;
import org.springframework.stereotype.Service;
import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;
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
                    () -> new ResourceNotFoundException("USER_NOT_FOUND","User not found with id: " + id));
        }
        public Ticket findTicketById(UUID id) {
            return ticketRepository.findById(id).orElseThrow(
                    () -> new ResourceNotFoundException("TICKET_NOT_FOUND","Ticket not found with id: " + id));

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
            throw new AccessDeniedException("ACCESS_DENIED","This ticket does not belongs to you");
        }
        if(ticket.getStatus().equals(TicketStatus.CLOSED)){
            throw new AccessDeniedException("ACCESS_DENIED","This ticket is closed");
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
}
