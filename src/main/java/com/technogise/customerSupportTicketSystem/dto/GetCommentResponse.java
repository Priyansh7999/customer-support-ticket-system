package com.technogise.customerSupportTicketSystem.dto;

import com.technogise.customerSupportTicketSystem.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetCommentResponse {
    private String comment;
    private String commenter;
    private LocalDateTime createdAt;
}
