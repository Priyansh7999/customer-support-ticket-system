package com.technogise.customerSupportTicketSystem.controller;

import com.technogise.customerSupportTicketSystem.dto.CustomerTicketResponse;
import com.technogise.customerSupportTicketSystem.enums.TicketStatus;
import com.technogise.customerSupportTicketSystem.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;


    @Test
    void getTicketById_whenRoleIsCustomer_shouldReturnTicket() throws Exception {

        UUID id = UUID.randomUUID();

        CustomerTicketResponse response = new CustomerTicketResponse(
                "Login Issue",
                "Cannot login",
                TicketStatus.OPEN,
                LocalDateTime.now(),
                "Rakshit"
        );

        when(ticketService.getTicketForCustomerById(id))
                .thenReturn(response);

        mockMvc.perform(get("/api/tickets/{id}", id)
                        .param("role", "customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Login Issue"))
                .andExpect(jsonPath("$.data.agentName").value("Rakshit"));
    }

    @Test
    void getTicketById_whenRoleIsInvalid_shouldReturnBadRequest() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/tickets/{id}", id)
                        .param("role", "agent"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ROLE"))
                .andExpect(jsonPath("$.message").value("Invalid role provided"));
    }
   
}
