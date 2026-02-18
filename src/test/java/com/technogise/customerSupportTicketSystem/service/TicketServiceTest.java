package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.CreateTicketRequest;
import com.technogise.customerSupportTicketSystem.dto.CreateTicketResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
import com.technogise.customerSupportTicketSystem.exception.ResourceNotFoundException;
import com.technogise.customerSupportTicketSystem.model.Ticket;
import com.technogise.customerSupportTicketSystem.model.User;
import com.technogise.customerSupportTicketSystem.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {
    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;


    @InjectMocks
    private TicketService ticketService;

    private CreateTicketRequest mockTicketRequest;
    private User customer;
    private User supportAgent;
    private CreateTicketResponse mockTicketResponse;

    @BeforeEach
    void setup() {
        mockTicketRequest = new CreateTicketRequest();
        mockTicketRequest.setTitle("Sample Ticket");
        mockTicketRequest.setDescription("This is a sample ticket description.");

        customer = getMockCustomer();
        supportAgent = getMockSupportAgent();
        mockTicketResponse = getMockCreateTicketResponse();
    }

    @Test
    void shouldReturnTicket_WhenTicketCreatedSuccessfully() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());

        // When
        CreateTicketResponse result = ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), customer.getId());

        // Then
        assertEquals(mockTicketRequest.getTitle(), result.getTitle());
        assertEquals(mockTicketRequest.getDescription(), result.getDescription());
    }

    @Test
    void shouldSetDefaultStatusToOpen_WhenTicketCreatedSuccessfully() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // When
        ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), customer.getId());

        // Then
        verify(ticketRepository).save(ticketCaptor.capture());
        Ticket savedTicket = ticketCaptor.getValue();
        assertEquals(TicketStatus.OPEN, savedTicket.getStatus());
    }

    @Test
    void shouldSetDefaultPriorityToMedium_WhenTicketCreatedSuccessfully() {
        // When
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());
        ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), customer.getId());

        // Then
        verify(ticketRepository).save(argThat(ticket -> ticket.getPriority() == TicketPriority.MEDIUM));
    }

    @Test
    void shouldSetCreatedBy_WhenUserExistsAndRoleIsCustomer() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());
        when(userService.getUserByIdAndRole(customer.getId(), UserRole.CUSTOMER)).thenReturn(customer);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // When
        ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), customer.getId());

        // Then
        verify(ticketRepository).save(ticketCaptor.capture());
        assertEquals(customer.getId(), ticketCaptor.getValue().getCreatedBy().getId());
        assertEquals(UserRole.CUSTOMER, ticketCaptor.getValue().getCreatedBy().getRole());
    }

    @Test
    void shouldSetAssignedTo_WhenUserExistsAndRoleIsSupportAgent() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(getMockTicket());
        when(userService.getRandomUserByRole(UserRole.SUPPORT_AGENT)).thenReturn(supportAgent);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // When
        ticketService.createTicket(mockTicketRequest.getTitle(), mockTicketRequest.getDescription(), supportAgent.getId());

        // Then
        verify(ticketRepository).save(ticketCaptor.capture());
        assertEquals(UserRole.SUPPORT_AGENT, ticketCaptor.getValue().getAssignedTo().getRole());
    }

    @Test
    void shouldThrowResourceNotFoundException_WhenUserNotFoundOrRoleDoesNotMatch() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userService.getUserByIdAndRole(userId, UserRole.CUSTOMER))
                .thenThrow(new ResourceNotFoundException(
                        "INVALID_USER_ID",
                        "User not found with id: " + userId + " and role: " + UserRole.CUSTOMER
                        ));

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> ticketService.createTicket(
                        mockTicketRequest.getTitle(),
                        mockTicketRequest.getDescription(),
                        userId
                )
        );

        assertEquals(
                "INVALID_USER_ID",
                exception.getCode()
        );
        assertEquals(
                "User not found with id: " + userId + " and role: " + UserRole.CUSTOMER,
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowResourceNotFoundException_WhenNoUserWithRoleExists() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userService.getRandomUserByRole(UserRole.SUPPORT_AGENT))
                .thenThrow(new ResourceNotFoundException(
                        "NO_USER_FOUND",
                        "No user found with role: " + UserRole.SUPPORT_AGENT
                ));

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> ticketService.createTicket(
                        mockTicketRequest.getTitle(),
                        mockTicketRequest.getDescription(),
                        userId
                )
        );

        assertEquals(
                "NO_USER_FOUND",
                exception.getCode()
        );
        assertEquals(
                "No user found with role: " + UserRole.SUPPORT_AGENT,
                exception.getMessage()
        );
    }

    private User getMockCustomer() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Mock User");
        user.setRole(UserRole.CUSTOMER);

        return user;
    }

    private User getMockSupportAgent() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Jatin");
        user.setRole(UserRole.SUPPORT_AGENT);

        return user;
    }

    private CreateTicketResponse getMockCreateTicketResponse() {
        CreateTicketResponse response = new CreateTicketResponse();
        response.setId(UUID.randomUUID());
        response.setTitle(mockTicketRequest.getTitle());
        response.setDescription(mockTicketRequest.getDescription());
        response.setStatus(TicketStatus.OPEN);
        response.setAssignedToName("Jatin");
        response.setCreatedAt(LocalDateTime.now());

        return response;
    }

    private Ticket getMockTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTitle(mockTicketRequest.getTitle());
        ticket.setDescription(mockTicketRequest.getDescription());
        ticket.setAssignedTo(supportAgent);

        return ticket;
    }

    @Test
    void getTicketById_whenTicketExists() {

        UUID id = UUID.randomUUID();

        User agent = new User();
        agent.setName("Rakshit");

        Ticket ticket = new Ticket();
        ticket.setTitle("Login Issue");
        ticket.setDescription("Cannot login");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setAssignedTo(agent);

        when(ticketRepository.findById(id))
                .thenReturn(Optional.of(ticket));

        CustomerTicketResponse response = ticketService.getTicketForCustomerById(id);

        assertEquals("Login Issue", response.getTitle());
        assertEquals("Rakshit", response.getAgentName());
    }

    @Test
    void getTicketById_whenTicketNotFound() {

        UUID id = UUID.randomUUID();

        when(ticketRepository.findById(id))
                .thenReturn(Optional.empty());

                ResourceNotFoundException exception =
            assertThrows(ResourceNotFoundException.class,
                    () -> ticketService.getTicketForCustomerById(id));

                     assertEquals("TICKET_NOT_FOUND", exception.getCode());
    }

    @Test
    void getTicketById_whenAssignedToIsNull() {
        UUID id = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setTitle("Login Issue");
        ticket.setDescription("Cannot login");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setAssignedTo(null);

        when(ticketRepository.findById(id))
                .thenReturn(Optional.of(ticket));

        CustomerTicketResponse response = ticketService.getTicketForCustomerById(id);

        assertEquals("Login Issue", response.getTitle());
        assertNull(response.getAgentName());
    }

    @Test
    void getTicketById_whenAssignedToIsNotNull() {
        UUID id = UUID.randomUUID();
        User agent = new User();
        agent.setName("Rakshit");
        Ticket ticket = new Ticket();
        ticket.setTitle("Login Issue");
        ticket.setDescription("Cannot login");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setAssignedTo(agent);

        when(ticketRepository.findById(id))
                .thenReturn(Optional.of(ticket));
        CustomerTicketResponse response = ticketService.getTicketForCustomerById(id);
        assertEquals("Rakshit", response.getAgentName());
    }
    
}

