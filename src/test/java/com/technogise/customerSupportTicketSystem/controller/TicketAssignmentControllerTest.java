package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentRequest;
import com.technogise.customerSupportTicketSystem.dto.TicketAssignmentResponse;
import com.technogise.customerSupportTicketSystem.model.TicketAssignment;
import com.technogise.customerSupportTicketSystem.service.TicketAssignmentService;
import jakarta.websocket.SendResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TicketAssignmentController.class)
class TicketAssignmentControllerTest {
    @MockitoBean
    private TicketAssignmentService ticketAssignmentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldReturnStatus201CreatedAndTicketAssignmentResponse_WhenTicketAssignedSuccessfully() throws Exception {
        //given
        UUID ticketId = UUID.randomUUID();
        UUID assignedToUserId = UUID.randomUUID();
        UUID assignedByUserId = UUID.randomUUID();

        TicketAssignmentRequest ticketAssignmentRequest = new TicketAssignmentRequest();
        ticketAssignmentRequest.setAssignedToUserId(assignedToUserId);
        ticketAssignmentRequest.setAssignedByUserId(assignedByUserId);

        TicketAssignmentResponse ticketAssignmentResponse = new TicketAssignmentResponse(
                UUID.randomUUID(),
                ticketId,
                assignedToUserId,
                assignedByUserId,
                "Ticket Assigned Successfully"
        );

        Mockito.when(ticketAssignmentService.assignTicket(
                Mockito.eq(ticketId),
                Mockito.any(UUID.class),
                Mockito.any(UUID.class)
        )).thenReturn(ticketAssignmentResponse);

        //when
        mockMvc.perform(
                post("/api/tickets/{id}/assign",ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketAssignmentRequest)))
                        .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$.assignedToUserId").value(assignedToUserId.toString()))
                .andExpect(jsonPath("$.assignedByUserId").value(assignedByUserId.toString()))
                .andExpect(jsonPath("$.message").value("Ticket Assigned Successfully"));

    }
}