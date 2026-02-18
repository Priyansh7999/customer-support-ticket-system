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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
