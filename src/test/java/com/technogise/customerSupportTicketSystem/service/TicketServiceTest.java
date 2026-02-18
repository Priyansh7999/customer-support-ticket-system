package com.technogise.customerSupportTicketSystem.service;

import com.technogise.customerSupportTicketSystem.dto.CreateTicketRequest;
import com.technogise.customerSupportTicketSystem.enums.TicketPriority;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.enums.UserRole;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private CreateTicketRequest request;
    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        request = new CreateTicketRequest();
        request.setTitle("Sample Ticket");
        request.setDescription("This is a sample ticket description.");
    }

    @Test
    void shouldReturnTicket_WhenTicketCreatedSuccessfully() {
        // Given
        Ticket createdTicket = new Ticket();
        createdTicket.setTitle(request.getTitle());
        createdTicket.setDescription(request.getDescription());

        when(ticketRepository.save(any(Ticket.class))).thenReturn(createdTicket);

        // When
        Ticket result = ticketService.createTicket(request.getTitle(), request.getDescription(), testUser.getId());

        // Then
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(request.getDescription(), result.getDescription());
    }

    @Test
    void shouldSetDefaultStatusToOpen_WhenTicketCreatedSuccessfully() {
        // Given
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // When
        ticketService.createTicket(request.getTitle(), request.getDescription(), testUser.getId());

        // Then
        verify(ticketRepository).save(ticketCaptor.capture());
        Ticket savedTicket = ticketCaptor.getValue();
        assertEquals(TicketStatus.OPEN, savedTicket.getStatus());
    }

    @Test
    void shouldSetDefaultPriorityToMedium_WhenTicketCreatedSuccessfully() {
        // When
        ticketService.createTicket(request.getTitle(), request.getDescription(), testUser.getId());

        // Then
        verify(ticketRepository).save(argThat(ticket -> ticket.getPriority() == TicketPriority.MEDIUM));
    }

    @Test
    void shouldSetCreatedBy_WhenUserExistsAndRoleIsCustomer() {
        // Given
        testUser.setRole(UserRole.CUSTOMER);

        when(userService.getUserByIdAndRole(testUser.getId(), UserRole.CUSTOMER)).thenReturn(testUser);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // When
        ticketService.createTicket(request.getTitle(), request.getDescription(), testUser.getId());

        // Then
        verify(ticketRepository).save(ticketCaptor.capture());
        assertEquals(testUser.getId(), ticketCaptor.getValue().getCreatedBy().getId());
        assertEquals(UserRole.CUSTOMER, ticketCaptor.getValue().getCreatedBy().getRole());
    }

    @Test
    void shouldSetAssignedTo_WhenUserExistsAndRoleIsSupportAgent() {
        // Given
        testUser.setRole(UserRole.SUPPORT_AGENT);

        when(userService.getRandomUserByRole(UserRole.SUPPORT_AGENT)).thenReturn(testUser);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        // When
        ticketService.createTicket(request.getTitle(), request.getDescription(), testUser.getId());

        // Then
        verify(ticketRepository).save(ticketCaptor.capture());
        assertEquals(UserRole.SUPPORT_AGENT, ticketCaptor.getValue().getAssignedTo().getRole());
    }

}
